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
import org.maiaframework.json.JsonNodeExtensions.getDouble
import org.maiaframework.json.JsonNodeExtensions.getString
import org.maiaframework.json.JsonNodeExtensions.getLocalDate
import org.maiaframework.json.JsonNodeExtensions.getStringOrNull
import tools.jackson.databind.JsonNode

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

        searchModel.filterModel.properties().forEach { filterModelItem ->
            val fieldPath = fieldNameMapper.invoke(filterModelItem.key)
            val blankType = filterModelItem.value.getStringOrNull("type")
            if (blankType == "blank" || blankType == "notBlank") {
                addBlankFilter(boolQueryBuilder, fieldPath, blank = blankType == "blank")
                return@forEach
            }
            if (filterModelItem.value.getStringOrNull("filterType") == "number") {
                addNumberFilter(boolQueryBuilder, fieldPath, filterModelItem.value)
                return@forEach
            }
            val filter = filterModelItem.value.getStringOrNull("filter")
            if (filterModelItem.value.getStringOrNull("filterType") == "date") {
                addDateFilter(boolQueryBuilder, fieldPath, filterModelItem.value)
                return@forEach
            }
            if (filterModelItem.value.getStringOrNull("filterType") == "set") {
                boolQueryBuilder.must { m -> buildSetQuery(fieldPath, filterModelItem.value.get("values"), m) }
                return@forEach
            }
            when (filterModelItem.value.getStringOrNull("type")) {
                "contains" -> boolQueryBuilder.must { m ->
                    m.wildcard { w -> w.field(fieldPath).value("*${escapeWildcards(filter)}*").caseInsensitive(true) }
                }
                "notEqual" -> {
                    mustExistIfScalarText(boolQueryBuilder, fieldPath)
                    boolQueryBuilder.mustNot { m ->
                        m.term { t -> t.field(fieldPath).value(filter) }
                    }
                }
                "notContains" -> {
                    mustExistIfScalarText(boolQueryBuilder, fieldPath)
                    boolQueryBuilder.mustNot { m ->
                        m.wildcard { w -> w.field(fieldPath).value("*${escapeWildcards(filter)}*").caseInsensitive(true) }
                    }
                }
                "startsWith" -> boolQueryBuilder.must { m ->
                    m.wildcard { w -> w.field(fieldPath).value("${escapeWildcards(filter)}*").caseInsensitive(true) }
                }
                "endsWith" -> boolQueryBuilder.must { m ->
                    m.wildcard { w -> w.field(fieldPath).value("*${escapeWildcards(filter)}").caseInsensitive(true) }
                }
                "true", "false" -> boolQueryBuilder.must { m ->
                    m.term { t -> t.field(fieldPath).value(filterModelItem.value.getStringOrNull("type").toBoolean()) }
                }
                else -> boolQueryBuilder.must { m -> m.term { t -> t.field(fieldPath).value(filter) } }
            }
        }

        return boolQueryBuilder

    }


    /**
     * A scalar text field is indexed with a `.raw` keyword sub-field, which is how it is told apart from array
     * and non-text fields here (only the mapped path is available).
     */
    private fun isScalarText(fieldPath: String): Boolean = fieldPath.endsWith(".raw")


    /**
     * Negative text filters exclude null/missing rows, as SQL does (a bare must_not would match them). Not applied
     * to array fields: a missing field there is an empty array, which SQL's `not any(...)` still includes.
     */
    private fun mustExistIfScalarText(boolQueryBuilder: BoolQuery.Builder, fieldPath: String) {

        if (isScalarText(fieldPath)) {
            boolQueryBuilder.must { m -> m.exists { e -> e.field(fieldPath) } }
        }

    }


    /**
     * Blank is null/missing, plus the empty string for scalar text; ES does not index an empty array, so an
     * empty array is missing and therefore Blank too. notBlank is the exact complement.
     */
    private fun addBlankFilter(boolQueryBuilder: BoolQuery.Builder, fieldPath: String, blank: Boolean) {

        val exists = Query.of { q -> q.exists { e -> e.field(fieldPath) } }
        val emptyString = Query.of { q -> q.term { t -> t.field(fieldPath).value("") } }

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
        val filter = filterModelItem.getDouble("filter")

        when (type) {
            "equals" -> boolQueryBuilder.must { m -> m.term { t -> t.field(fieldPath).value(filter) } }
            "notEqual" -> boolQueryBuilder
                .must { m -> m.exists { e -> e.field(fieldPath) } }
                .mustNot { m -> m.term { t -> t.field(fieldPath).value(filter) } }
            "greaterThan" -> boolQueryBuilder.must { m -> m.range { r -> r.number { n -> n.field(fieldPath).gt(filter) } } }
            "greaterThanOrEqual" -> boolQueryBuilder.must { m -> m.range { r -> r.number { n -> n.field(fieldPath).gte(filter) } } }
            "lessThan" -> boolQueryBuilder.must { m -> m.range { r -> r.number { n -> n.field(fieldPath).lt(filter) } } }
            "lessThanOrEqual" -> boolQueryBuilder.must { m -> m.range { r -> r.number { n -> n.field(fieldPath).lte(filter) } } }
            "inRange" -> {
                val filterTo = filterModelItem.getDouble("filterTo")
                boolQueryBuilder.must { m -> m.range { r -> r.number { n -> n.field(fieldPath).gte(filter).lt(filterTo) } } }
            }
            else -> throw IllegalArgumentException("Unknown filter type [$type]")
        }

    }


    /**
     * Date filters use UTC day boundaries, matching the Postgres converter: equals is [d, d+1), notEqual its
     * negation (rows with no value are excluded, so it is two ranges rather than a must_not), greaterThan is
     * >= d+1, lessThan is < d and inRange is [from, to+1).
     */
    private fun addDateFilter(boolQueryBuilder: BoolQuery.Builder, fieldPath: String, filterNode: JsonNode) {

        val dateFrom = filterNode.getLocalDate("dateFrom")

        fun range(gte: java.time.LocalDate?, lt: java.time.LocalDate?): Query = Query.of { q ->
            q.range { r ->
                r.date { d ->
                    d.field(fieldPath).timeZone("UTC")
                    gte?.let { d.gte(it.toString()) }
                    lt?.let { d.lt(it.toString()) }
                    d
                }
            }
        }

        when (val type = filterNode.getStringOrNull("type")) {
            "equals" -> boolQueryBuilder.must(range(dateFrom, dateFrom.plusDays(1)))
            "notEqual" -> boolQueryBuilder.must { m ->
                m.bool { b -> b.should(range(null, dateFrom)).should(range(dateFrom.plusDays(1), null)).minimumShouldMatch("1") }
            }
            "greaterThan" -> boolQueryBuilder.must(range(dateFrom.plusDays(1), null))
            "lessThan" -> boolQueryBuilder.must(range(null, dateFrom))
            "inRange" -> boolQueryBuilder.must(range(dateFrom, filterNode.getLocalDate("dateTo").plusDays(1)))
            else -> throw IllegalArgumentException("Unknown filter type [$type]")
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
                b.should { s -> s.bool { nb -> nb.mustNot { mn -> mn.exists { e -> e.field(fieldPath) } } } }
            }

            if (values.isEmpty() && !includesBlanks) {
                b.must { m -> m.matchNone { it } }
            } else {
                b.minimumShouldMatch("1")
            }

            b

        }

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
