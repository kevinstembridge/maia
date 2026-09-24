package org.maiaframework.elasticsearch.index

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import org.slf4j.LoggerFactory

abstract class AbstractEsIndexControl(
    private val client: ElasticsearchClient,
    private val esIndexActiveVersionManager: EsIndexActiveVersionManager
): EsIndexControl {


    private val logger = LoggerFactory.getLogger(AbstractEsIndexControl::class.java)


    override val isActiveVersion: Boolean
        get() = this.esIndexActiveVersionManager.isActive(this.indexName)


    protected abstract val typeMapping: TypeMapping


    override fun createIndex() {

        logger.info("BEGIN: createIndex() for ${this.indexName}")

        val createIndexResponse = client.indices().create { r -> r.index(this.indexName.resolvedName).mappings(this.typeMapping) }

        // TODO should I be doing something with the response?

    }


}
