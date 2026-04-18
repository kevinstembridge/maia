package org.maiaframework.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.maiaframework.elasticsearch.index.EsIndexActiveVersionManager
import org.maiaframework.elasticsearch.index.EsIndexControlRegistry
import org.maiaframework.elasticsearch.index.EsIndexNameFactory
import org.maiaframework.elasticsearch.index.EsIndexNameOverrider
import org.maiaframework.elasticsearch.index.ElasticIndexHelper
import org.maiaframework.elasticsearch.index.ElasticIndexService
import org.maiaframework.elasticsearch.search.EsSearchExecutor
import org.maiaframework.elasticsearch.search.EsSearchRequestFactory
import org.maiaframework.props.Props
import org.maiaframework.props.PropsManager
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(ElasticsearchClient::class)
class MaiaElasticsearchAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun esIndexNameFactory(): EsIndexNameFactory = EsIndexNameFactory()

    @Bean
    @ConditionalOnMissingBean
    fun esIndexActiveVersionManager(props: Props, propsManager: PropsManager): EsIndexActiveVersionManager =
        EsIndexActiveVersionManager(props, propsManager)

    @Bean
    @ConditionalOnMissingBean
    fun esIndexNameOverrider(props: Props, esIndexNameFactory: EsIndexNameFactory): EsIndexNameOverrider =
        EsIndexNameOverrider(props, esIndexNameFactory)

    @Bean
    @ConditionalOnMissingBean
    fun esIndexControlRegistry(): EsIndexControlRegistry = EsIndexControlRegistry()

    @Bean
    @ConditionalOnMissingBean
    fun elasticIndexHelper(client: ElasticsearchClient): ElasticIndexHelper = ElasticIndexHelper(client)

    @Bean
    @ConditionalOnMissingBean
    fun elasticIndexService(
        client: ElasticsearchClient,
        controlRegistry: EsIndexControlRegistry,
        esIndexNameFactory: EsIndexNameFactory,
        esIndexActiveVersionManager: EsIndexActiveVersionManager
    ): ElasticIndexService = ElasticIndexService(client, controlRegistry, esIndexNameFactory, esIndexActiveVersionManager)

    @Bean
    @ConditionalOnMissingBean
    fun esSearchRequestFactory(): EsSearchRequestFactory = EsSearchRequestFactory()

    @Bean
    @ConditionalOnMissingBean
    fun esSearchExecutor(
        esSearchRequestFactory: EsSearchRequestFactory,
        esClient: ElasticsearchClient
    ): EsSearchExecutor = EsSearchExecutor(esSearchRequestFactory, esClient)

}
