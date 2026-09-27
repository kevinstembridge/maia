package org.maiaframework.elasticsearch.index

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import org.slf4j.LoggerFactory

abstract class AbstractEsIndexControl(
    private val client: ElasticsearchClient
): EsIndexControl {


    private val logger = LoggerFactory.getLogger(AbstractEsIndexControl::class.java)


    protected abstract val typeMapping: TypeMapping


    override fun createIndex() {

        logger.info("BEGIN: createIndex() for ${this.indexName}")

        val resolvedName = EsIndexNameFactory.indexNameFrom(this.indexBaseNameAndVersion)
        val createIndexResponse = client.indices().create { r -> r.index(resolvedName.value).mappings(this.typeMapping) }

        // TODO should I be doing something with the response?

    }


}
