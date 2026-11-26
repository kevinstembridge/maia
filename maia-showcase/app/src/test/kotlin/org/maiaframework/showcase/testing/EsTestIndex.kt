package org.maiaframework.showcase.testing

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.GetRequest
import org.maiaframework.domain.DomainId
import org.maiaframework.elasticsearch.index.IndexNameFactory
import org.maiaframework.elasticsearch.index.model.IndexBaseNameAndVersion


/**
 * Test-side handle on one Elasticsearch index used by a Blotter Playwright test: creates the index from the generated
 * mapping when it is missing, refreshes it so freshly written documents are searchable, and reads a document's `_version`.
 */
class EsTestIndex(
    private val elasticsearchClient: ElasticsearchClient,
    indexNameFactory: IndexNameFactory,
    indexBaseNameAndVersion: IndexBaseNameAndVersion,
    private val createIndex: () -> Unit
) {


    private val indexName = indexNameFactory.indexNameFrom(indexBaseNameAndVersion).value


    fun ensureExists() {

        if (!elasticsearchClient.indices().exists { r -> r.index(indexName) }.value()) {
            createIndex()
        }

    }


    fun refresh() {

        elasticsearchClient.indices().refresh { r -> r.index(indexName) }

    }


    /** The Elasticsearch `_version` of the document with [id], which increases on every write of that document. */
    fun documentVersion(id: DomainId): Long {

        return elasticsearchClient.get(
            { r: GetRequest.Builder -> r.index(indexName).id(id.value) },
            Map::class.java
        ).version()!!

    }


}
