package org.maiaframework.elasticsearch.search

import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.CountRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.elasticsearch.index.model.IndexResolvedName
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
            val filter = filterModelItem.value.getStringOrNull("filter")
            if (filterModelItem.value.getStringOrNull("filterType") == "date") {
                addDateFilter(boolQueryBuilder, fieldPath, filterModelItem.value)
                return@forEach
            }
            when (filterModelItem.value.getStringOrNull("type")) {
                "contains" -> boolQueryBuilder.must { m ->
                    m.wildcard { w -> w.field(fieldPath).value("*${escapeWildcards(filter)}*").caseInsensitive(true) }
                }
                else -> boolQueryBuilder.must { m -> m.term { t -> t.field(fieldPath).value(filter) } }
            }
        }

        return boolQueryBuilder

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
