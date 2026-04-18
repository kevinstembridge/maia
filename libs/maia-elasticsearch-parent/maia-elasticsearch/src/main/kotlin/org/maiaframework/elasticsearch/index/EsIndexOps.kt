package org.maiaframework.elasticsearch.index

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch.core.BulkRequest
import co.elastic.clients.elasticsearch.core.DeleteResponse
import co.elastic.clients.elasticsearch.core.GetResponse
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import org.maiaframework.common.logging.getLogger
import org.maiaframework.elasticsearch.EsDocHolder
import org.maiaframework.elasticsearch.EsPaginationHelper
import org.maiaframework.metrics.JobMetrics
import org.springframework.stereotype.Component


@Component
class EsIndexOps(private val client: ElasticsearchClient, private val paginationHelper: EsPaginationHelper) {


    private val logger = getLogger<EsIndexOps>()


    fun <T> findById(
        id: String,
        indexName: EsIndexName,
        clazz: Class<T>
    ): GetResponse<T> {

        return this.client.get({ r -> r.index(indexName.asString).id(id) }, clazz)

    }


    fun deleteById(
        id: String,
        indexName: EsIndexName
    ): DeleteResponse {

        return this.client.delete { r -> r.index(indexName.asString).id(id) }

    }


    fun deleteByIds(
        ids: Collection<String>,
        indexName: EsIndexName
    ) {

        val builder = BulkRequest.Builder()

        ids.forEach { id ->
            builder.operations { op ->
                op.delete { d ->
                    d.index(indexName.asString).id(id)
                }
            }
        }

        this.client.bulk(builder.build())

    }


    fun bulkUpsert(items: List<EsDocHolder<*>>) {

        if (items.isEmpty()) {
            return
        }

        val bulk = BulkRequest.Builder()

        items.forEach { item ->
            bulk.operations { op ->
                op.index { idx ->
                    idx.index(item.indexName.asString).id(item.id).document(item.doc)
                }
            }
        }

        val bulkResponse = this.client.bulk(bulk.build())

        if (bulkResponse.errors()) {

            bulkResponse.items().forEach { item ->
                if (item.error() != null) {
                    logger.error(item.error()!!.reason())
                }
            }

        }

    }


    fun removeDeletedRecordsFromIndex(
        currentIds: Set<String>,
        indexName: EsIndexName,
        chunkSize: Int,
        jm: JobMetrics
    ) {

        val idsToBeDeleted = mutableSetOf<String>()
        val counterRatio = jm.getOrCreateCounterRatio("idsToBeDeleted")

        this.paginationHelper.paginate(
            buildSearchRequestFunction(indexName),
            Any::class.java
        ) { hitsMetadata ->

            hitsMetadata.hits().forEach { searchHit ->

                counterRatio.denominator.inc()
                val id = searchHit.id() ?: throw IllegalStateException("Expecting id field to not be null")

                if (currentIds.contains(id) == false) {
                    counterRatio.numerator.inc()
                    idsToBeDeleted.add(id)
                }

            }

        }

        idsToBeDeleted.chunked(chunkSize).forEach { ids ->
            jm.timeChildJob("bulkDeleteChunk") {
                deleteByIds(ids, indexName)
            }
        }

    }


    private fun buildSearchRequestFunction(
        indexName: EsIndexName
    ): (SearchRequest.Builder) -> Unit {

        return { searchRequestBuilder ->

            searchRequestBuilder
                .index(indexName.asString)
                .query { q ->
                    q.matchAll { m ->
                        m
                    }
                }.sort { s ->
                    s.field { b ->
                        b.field("_doc").order(SortOrder.Asc)
                    }
                }.fields(emptyList())
        }

    }


    fun upsert(esDoc: EsDocHolder<*>) {

        this.client.index { builder ->
            builder.index(esDoc.indexName.asString)
                .id(esDoc.id)
                .document(esDoc.doc)
        }

    }


}
