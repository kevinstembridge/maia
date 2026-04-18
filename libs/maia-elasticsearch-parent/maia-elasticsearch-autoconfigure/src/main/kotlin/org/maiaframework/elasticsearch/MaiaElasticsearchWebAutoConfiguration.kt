package org.maiaframework.elasticsearch

import ElasticSearchIndicesEndpoint
import org.maiaframework.elasticsearch.index.ElasticIndexService
import org.maiaframework.elasticsearch.index.EsIndexNameFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(ElasticSearchIndicesEndpoint::class)
class MaiaElasticsearchWebAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    fun elasticSearchIndicesEndpoint(
        elasticIndexService: ElasticIndexService,
        esIndexNameFactory: EsIndexNameFactory
    ): ElasticSearchIndicesEndpoint {

        return ElasticSearchIndicesEndpoint(elasticIndexService, esIndexNameFactory)

    }


}
