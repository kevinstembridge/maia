package org.maiaframework.elasticsearch.search

import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.CountRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.elasticsearch.index.model.IndexResolvedName
import org.maiaframework.json.JsonNodeExtensions.getDouble
import org.maiaframework.json.JsonNodeExtensions.getString
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
            if (filterModelItem.value.has("operator")) {
                addCompoundFilter(boolQueryBuilder, fieldPath, filterModelItem.value)
            } else {
                addConditionFilter(boolQueryBuilder, fieldPath, filterModelItem.value)
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

        val conditionQueries = filterModelItem.get("conditions").values().map { condition ->
            Query.of { q -> q.bool { b -> addConditionFilter(b, fieldPath, condition); b } }
        }

        when (operator) {
            "and" -> boolQueryBuilder.must { m -> m.bool { b -> b.must(conditionQueries) } }
            "or" -> boolQueryBuilder.must { m -> m.bool { b -> b.should(conditionQueries).minimumShouldMatch("1") } }
            else -> throw IllegalArgumentException("Unknown conditional operator '$operator'")
        }

    }


    /** Adds one single-condition filter (a text or number condition) of a field to [boolQueryBuilder]. */
    private fun addConditionFilter(
        boolQueryBuilder: BoolQuery.Builder,
        fieldPath: String,
        filterModelItem: JsonNode
    ) {

        if (filterModelItem.getStringOrNull("filterType") == "number") {
            addNumberFilter(boolQueryBuilder, fieldPath, filterModelItem)
            return
        }
        val filter = filterModelItem.getStringOrNull("filter")
        when (filterModelItem.getStringOrNull("type")) {
            "contains" -> boolQueryBuilder.must { m ->
                m.wildcard { w -> w.field(fieldPath).value("*${escapeWildcards(filter)}*").caseInsensitive(true) }
            }
            "notEqual" -> boolQueryBuilder.mustNot { m ->
                m.term { t -> t.field(fieldPath).value(filter) }
            }
            "notContains" -> boolQueryBuilder.mustNot { m ->
                m.wildcard { w -> w.field(fieldPath).value("*${escapeWildcards(filter)}*").caseInsensitive(true) }
            }
            "startsWith" -> boolQueryBuilder.must { m ->
                m.wildcard { w -> w.field(fieldPath).value("${escapeWildcards(filter)}*").caseInsensitive(true) }
            }
            "endsWith" -> boolQueryBuilder.must { m ->
                m.wildcard { w -> w.field(fieldPath).value("*${escapeWildcards(filter)}").caseInsensitive(true) }
            }
            "true", "false" -> boolQueryBuilder.must { m ->
                m.term { t -> t.field(fieldPath).value(filterModelItem.getStringOrNull("type").toBoolean()) }
            }
            else -> boolQueryBuilder.must { m -> m.term { t -> t.field(fieldPath).value(filter) } }
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
