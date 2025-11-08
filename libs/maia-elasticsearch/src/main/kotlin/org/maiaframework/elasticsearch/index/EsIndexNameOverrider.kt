package org.maiaframework.elasticsearch.index

import org.maiaframework.common.logging.getLogger
import org.maiaframework.props.Props
import org.springframework.stereotype.Component

/**
 * This class provides the names of the Elasticsearch indices in the application but its
 * main purpose is to allow us to override these names if we choose. This is only currently
 * useful in the case where we want to use a single Elasticsearch instance for both a running
 * application and some tests. When we can afford the hardware to run multiple Elasticsearch
 * environments we should stop doing this. (Or if we get around to running ES in Docker)
 */
@Component
class EsIndexNameOverrider(private val props: Props, private val esIndexNameFactory: EsIndexNameFactory) {

    private val logger = getLogger<EsIndexNameOverrider>()


    fun indexName(indexName: EsIndexName): EsIndexName {

        val property = this.props.getStringOrNull("app.elasticsearch.indices.${indexName.esIndexBaseName}.name")

        if (property.isNullOrBlank()) {

            if (this.props.isSpringProfileActive("es_index_suffix_bb")) {
                return indexName.withSuffix("_bb")
            }

            if (this.props.isSpringProfileActive("es_index_suffix_e2e")) {
                return indexName.withSuffix("_e2e")
            }

            if (this.props.isSpringProfileActive("es_index_suffix_it")) {
                return indexName.withSuffix("_it")
            }

            if (this.props.isSpringProfileActive("es_index_suffix_jobs")) {
                return indexName.withSuffix("_jobs")
            }

            return indexName

        } else {

            val overridingIndexName = this.esIndexNameFactory.indexNameFrom(property)
            logger.debug("found overriding index name [{}] from property [{}]", overridingIndexName, property)
            return overridingIndexName

        }

    }


}
