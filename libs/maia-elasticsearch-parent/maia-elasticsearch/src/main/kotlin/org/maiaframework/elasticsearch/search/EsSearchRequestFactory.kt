package org.maiaframework.elasticsearch.search

import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder
import co.elastic.clients.elasticsearch.core.CountRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.elasticsearch.index.model.IndexResolvedName
import org.maiaframework.json.JsonNodeExtensions.getArrayNode
import org.maiaframework.json.JsonNodeExtensions.getDouble
import org.maiaframework.json.JsonNodeExtensions.getString
import org.maiaframework.json.JsonNodeExtensions.getLocalDate
import org.maiaframework.json.JsonNodeExtensions.getStringOrNull
import tools.jackson.databind.JsonNode
import java.time.LocalDate

class EsSearchRequestFactory {


    fun buildSearchRequest(
        searchModel: AgGridSearchModel,
        fieldNameMapper: (String) -> String,
        indexName: IndexResolvedName
    ): SearchRequest {

        val from = searchModel.startRow
        val size = searchModel.endRow?.minus(searchModel.startRow) ?: 10

        return SearchRequest.of { r ->
            r.index(indexName.value)
                .query { q ->
                    q.bool { b ->
                        buildQuery(searchModel, fieldNameMapper, b)
                    }
                }.from(from)
                .size(size)

                if (searchModel.sortModel.isNotEmpty()) {
                    r.sort { s ->
                        createSortBuilders(searchModel, fieldNameMapper, s)
                    }
                }

                r

        }

    }


    fun buildCountRequest(
        searchModel: AgGridSearchModel,
        fieldNameMapper: (String) -> String,
        indexName: IndexResolvedName
    ): CountRequest {

        return CountRequest.of { r ->
            r.index(indexName.value)
                .query { q ->
                    q.bool { b ->
                        buildQuery(searchModel, fieldNameMapper, b)
                    }
                }
        }

    }


    private fun buildQuery(
        searchModel: AgGridSearchModel,
        fieldNameMapper: (String) -> String,
        boolQueryBuilder: BoolQuery.Builder
    ): BoolQuery.Builder {

        searchModel.filterModel.properties().forEach { filterModelEntry ->
            val fieldPath = fieldNameMapper.invoke(filterModelEntry.key)
            val filterModelItem = filterModelEntry.value
            if (filterModelItem.has("operator")) {
                addCompoundFilter(boolQueryBuilder, fieldPath, filterModelItem)
            } else {
                addConditionFilter(boolQueryBuilder, fieldPath, filterModelItem)
            }
        }

        return boolQueryBuilder

    }


    /**
     * `{operator, conditions}` becomes one nested bool added as a single `must` of the parent: AND is a `must` of
     * the conditions, OR is a `should` of them with at least one required. Matches the JDBC converter.
     */
    private fun addCompoundFilter(
        boolQueryBuilder: BoolQuery.Builder,
        fieldPath: String,
        filterModelItem: JsonNode
    ) {

        val operator = filterModelItem.getString("operator").lowercase()

        val conditions = filterModelItem.getArrayNode("conditions")

        if (conditions.isEmpty) {
            throw IllegalArgumentException("A compound filter on [$fieldPath] needs at least one condition")
        }

        val conditionQueries = conditions.values().map { condition ->
            Query.of { q -> q.bool { b -> addConditionFilter(b, fieldPath, condition); b } }
        }

        when (operator) {
            "and" -> boolQueryBuilder.must { m -> m.bool { b -> b.must(conditionQueries) } }
            "or" -> boolQueryBuilder.must { m -> m.bool { b -> b.should(conditionQueries).minimumShouldMatch("1") } }
            else -> throw IllegalArgumentException("Unknown conditional operator '$operator'")
        }

    }


    /**
     * Adds one single-condition filter of a field to [boolQueryBuilder]. Dispatch is strict: by `filterType`
     * (id, text, number, date, set) and then by `type`. Anything else throws, as the JDBC converter does.
     */
    private fun addConditionFilter(
        boolQueryBuilder: BoolQuery.Builder,
        fieldPath: String,
        filterModelItem: JsonNode
    ) {

        when (val filterType = filterModelItem.getString("filterType")) {
            "id" -> addIdFilter(boolQueryBuilder, fieldPath, filterModelItem)
            "text" -> addTextFilter(boolQueryBuilder, fieldPath, filterModelItem)
            "number" -> addNumberFilter(boolQueryBuilder, fieldPath, filterModelItem)
            "date" -> addDateFilter(boolQueryBuilder, fieldPath, filterModelItem)
            "set" -> boolQueryBuilder.must { m -> buildSetQuery(fieldPath, filterModelItem.get("values"), m) }
            else -> throw unknownFilterType("filter field type", filterType)
        }

    }


    /** An id filter needs no type, but if one is sent it must be "equals". */
    private fun addIdFilter(
        boolQueryBuilder: BoolQuery.Builder,
        fieldPath: String,
        filterModelItem: JsonNode
    ) {

        val type = filterModelItem.getStringOrNull("type")
        if (type != null && type != "equals") {
            throw unknownFilterType("filter type", type, " for an id filter")
        }

        boolQueryBuilder.must { m -> m.term { t -> t.field(fieldPath).value(filterModelItem.getString("filter")) } }

    }


    private fun addTextFilter(
        boolQueryBuilder: BoolQuery.Builder,
        fieldPath: String,
        filterModelItem: JsonNode
    ) {

        val type = filterModelItem.getString("type")

        if (type == "blank" || type == "notBlank") {
            addBlankFilter(boolQueryBuilder, fieldPath, blank = type == "blank")
            return
        }

        if (type == "true" || type == "false") {
            boolQueryBuilder.must { m -> m.term { t -> t.field(fieldPath).value(type.toBoolean()) } }
            return
        }

        val filter = filterModelItem.getString("filter")

        when (type) {
            "equals" -> boolQueryBuilder.must(termQuery(fieldPath, filter))
            "contains" -> boolQueryBuilder.must(wildcardQuery(fieldPath, "*${escapeWildcards(filter)}*"))
            "notEqual" -> {
                mustExistIfScalarText(boolQueryBuilder, fieldPath)
                boolQueryBuilder.mustNot(termQuery(fieldPath, filter))
            }
            "notContains" -> {
                mustExistIfScalarText(boolQueryBuilder, fieldPath)
                boolQueryBuilder.mustNot(wildcardQuery(fieldPath, "*${escapeWildcards(filter)}*"))
            }
            "startsWith" -> boolQueryBuilder.must(wildcardQuery(fieldPath, "${escapeWildcards(filter)}*"))
            "endsWith" -> boolQueryBuilder.must(wildcardQuery(fieldPath, "*${escapeWildcards(filter)}"))
            else -> throw unknownFilterType("filter type", type)
        }

    }


    /**
     * A scalar text field is indexed with a `.raw` keyword sub-field, which is how it is told apart from array
     * and non-text fields here (only the mapped path is available). This assumes the field-name mapper points
     * text-and-keyword columns at `<field>.raw`; a text column mapped any other way is not recognised.
     */
    private fun isScalarText(fieldPath: String): Boolean = fieldPath.endsWith(".raw")


    /**
     * Negative text filters exclude null/missing rows, as SQL does (a bare must_not would match them). Not applied
     * to array fields: a missing field there is an empty array, which SQL's `not any(...)` still includes.
     */
    private fun mustExistIfScalarText(boolQueryBuilder: BoolQuery.Builder, fieldPath: String) {

        if (isScalarText(fieldPath)) {
            boolQueryBuilder.must(existsQuery(fieldPath))
        }

    }


    /**
     * Blank is null/missing, plus the empty string for scalar text; ES does not index an empty array, so an
     * empty array is missing and therefore Blank too. notBlank is the exact complement.
     */
    private fun addBlankFilter(boolQueryBuilder: BoolQuery.Builder, fieldPath: String, blank: Boolean) {

        val exists = existsQuery(fieldPath)
        val emptyString = termQuery(fieldPath, "")

        when {
            blank && isScalarText(fieldPath) -> boolQueryBuilder.must { m ->
                m.bool { b -> b.should { s -> s.bool { nb -> nb.mustNot(exists) } }.should(emptyString).minimumShouldMatch("1") }
            }
            blank -> boolQueryBuilder.mustNot(exists)
            isScalarText(fieldPath) -> boolQueryBuilder.must(exists).mustNot(emptyString)
            else -> boolQueryBuilder.must(exists)
        }

    }


    /** Follows the JDBC converter: negative filters exclude nulls, inRange is lower-inclusive and upper-exclusive. */
    private fun addNumberFilter(
        boolQueryBuilder: BoolQuery.Builder,
        fieldPath: String,
        filterModelItem: JsonNode
    ) {

        val type = filterModelItem.getString("type")

        if (type == "blank" || type == "notBlank") {
            addBlankFilter(boolQueryBuilder, fieldPath, blank = type == "blank")
            return
        }

        val filter = filterModelItem.getDouble("filter")

        when (type) {
            "equals" -> boolQueryBuilder.must(termQuery(fieldPath, filter))
            "notEqual" -> boolQueryBuilder
                .must(existsQuery(fieldPath))
                .mustNot(termQuery(fieldPath, filter))
            "greaterThan" -> boolQueryBuilder.must { m -> m.range { r -> r.number { n -> n.field(fieldPath).gt(filter) } } }
            "greaterThanOrEqual" -> boolQueryBuilder.must { m -> m.range { r -> r.number { n -> n.field(fieldPath).gte(filter) } } }
            "lessThan" -> boolQueryBuilder.must { m -> m.range { r -> r.number { n -> n.field(fieldPath).lt(filter) } } }
            "lessThanOrEqual" -> boolQueryBuilder.must { m -> m.range { r -> r.number { n -> n.field(fieldPath).lte(filter) } } }
            "inRange" -> {
                val filterTo = filterModelItem.getDouble("filterTo")
                boolQueryBuilder.must { m -> m.range { r -> r.number { n -> n.field(fieldPath).gte(filter).lt(filterTo) } } }
            }
            else -> throw unknownFilterType("filter type", type)
        }

    }


    /**
     * Date filters use UTC day boundaries, matching the Postgres converter: equals is [d, d+1), notEqual its
     * negation (rows with no value are excluded, so it is two ranges rather than a must_not), greaterThan is
     * >= d+1, lessThan is < d and inRange is [from, to+1).
     */
    private fun addDateFilter(boolQueryBuilder: BoolQuery.Builder, fieldPath: String, filterModelItem: JsonNode) {

        val type = filterModelItem.getString("type")

        if (type == "blank" || type == "notBlank") {
            addBlankFilter(boolQueryBuilder, fieldPath, blank = type == "blank")
            return
        }

        val dateFrom = filterModelItem.getLocalDate("dateFrom")

        fun range(gte: LocalDate?, lt: LocalDate?): Query = Query.of { q ->
            q.range { r ->
                r.date { d ->
                    d.field(fieldPath).timeZone("UTC")
                    gte?.let { d.gte(it.toString()) }
                    lt?.let { d.lt(it.toString()) }
                    d
                }
            }
        }

        when (type) {
            "equals" -> boolQueryBuilder.must(range(dateFrom, dateFrom.plusDays(1)))
            "notEqual" -> boolQueryBuilder.must { m ->
                m.bool { b -> b.should(range(null, dateFrom)).should(range(dateFrom.plusDays(1), null)).minimumShouldMatch("1") }
            }
            "greaterThan" -> boolQueryBuilder.must(range(dateFrom.plusDays(1), null))
            "lessThan" -> boolQueryBuilder.must(range(null, dateFrom))
            "inRange" -> boolQueryBuilder.must(range(dateFrom, filterModelItem.getLocalDate("dateTo").plusDays(1)))
            else -> throw unknownFilterType("filter type", type)
        }

    }


    /**
     * Set filter: matches any listed value. A null entry is "(Blanks)" and also matches rows where the field
     * is missing. The empty string matches only if listed. An empty list matches nothing.
     */
    private fun buildSetQuery(
        fieldPath: String,
        valuesNode: JsonNode?,
        queryBuilder: Query.Builder
    ): ObjectBuilder<Query> {

        val entries = valuesNode?.toList() ?: emptyList()
        val values = entries.filterNot { it.isNull }.map { FieldValue.of(it.asString()) }
        val includesBlanks = entries.any { it.isNull }

        return queryBuilder.bool { b ->

            if (values.isNotEmpty()) {
                b.should { s -> s.terms { t -> t.field(fieldPath).terms { tf -> tf.value(values) } } }
            }

            if (includesBlanks) {
                b.should { s -> s.bool { nb -> nb.mustNot(existsQuery(fieldPath)) } }
            }

            if (values.isEmpty() && !includesBlanks) {
                b.must { m -> m.matchNone { it } }
            } else {
                b.minimumShouldMatch("1")
            }

            b

        }

    }


    private fun existsQuery(fieldPath: String): Query = Query.of { q -> q.exists { e -> e.field(fieldPath) } }


    private fun termQuery(fieldPath: String, value: Any): Query = Query.of { q ->
        q.term { t ->
            when (value) {
                is String -> t.field(fieldPath).value(value)
                is Double -> t.field(fieldPath).value(value)
                else -> throw IllegalArgumentException("Unsupported term value [$value]")
            }
        }
    }


    private fun wildcardQuery(fieldPath: String, pattern: String): Query = Query.of { q ->
        q.wildcard { w -> w.field(fieldPath).value(pattern).caseInsensitive(true) }
    }


    private fun unknownFilterType(kind: String, value: String, suffix: String = ""): IllegalArgumentException {

        return IllegalArgumentException("Unknown $kind [$value]$suffix")

    }


    private fun escapeWildcards(filter: String?): String {

        return (filter ?: "").replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?")

    }


    private fun createSortBuilders(
        searchModel: AgGridSearchModel,
        fieldNameMapper: (String) -> String,
        sortOptionsBuilder: SortOptions.Builder
    ): SortOptions.Builder {

        searchModel.sortModel.forEach { sortModelItem ->

            val sortOrder = when (sortModelItem.sort) {
                "asc" -> SortOrder.Asc
                "desc" -> SortOrder.Desc
                else -> throw IllegalArgumentException("Unknown sort order ${sortModelItem.sort}")
            }

            val fieldPath = fieldNameMapper.invoke(sortModelItem.colId)

            sortOptionsBuilder.field { f ->
                f.field(fieldPath)
                    .order(sortOrder)
            }

        }

        return sortOptionsBuilder

    }


}
