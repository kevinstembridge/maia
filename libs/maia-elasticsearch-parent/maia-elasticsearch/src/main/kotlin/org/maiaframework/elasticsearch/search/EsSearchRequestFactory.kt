package org.maiaframework.elasticsearch.search

import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder
import tools.jackson.databind.JsonNode
import co.elastic.clients.elasticsearch.core.CountRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.elasticsearch.index.model.IndexResolvedName
import org.maiaframework.json.JsonNodeExtensions.getStringOrNull

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
            if (filterModelItem.value.getStringOrNull("filterType") == "set") {
                boolQueryBuilder.must { m -> buildSetQuery(fieldPath, filterModelItem.value.get("values"), m) }
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
