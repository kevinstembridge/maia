package org.maiaframework.elasticsearch.index

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import org.slf4j.LoggerFactory

abstract class AbstractEsIndexControl(
    private val client: ElasticsearchClient,
    private val esIndexActiveVersionManager: org.maiaframework.elasticsearch.index.EsIndexActiveVersionManager,
    private val esIndexNameProvider: org.maiaframework.elasticsearch.index.EsIndexNameOverrider
): org.maiaframework.elasticsearch.index.EsIndexControl {


    private val logger = LoggerFactory.getLogger(AbstractEsIndexControl::class.java)


    override val isActiveVersion: Boolean
        get() = this.esIndexActiveVersionManager.isActive(this.indexName)


    protected abstract val typeMapping: TypeMapping


    override fun createIndex() {

        val indexName = this.esIndexNameProvider.indexName(this.indexName).asString

        logger.info("BEGIN: createIndex() for $indexName")

        val createIndexResponse = client.indices().create { r -> r.index(indexName).mappings(this.typeMapping) }

        // TODO should I be doing something with the response?

    }


}
