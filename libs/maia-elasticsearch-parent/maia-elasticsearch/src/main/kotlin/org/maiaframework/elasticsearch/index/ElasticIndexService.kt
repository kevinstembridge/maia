package org.maiaframework.elasticsearch.index

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.Level
import org.maiaframework.common.logging.getLogger
import org.springframework.stereotype.Component
import java.security.Principal

@Component
class ElasticIndexService(
    private val client: ElasticsearchClient,
    private val controlRegistry: EsIndexControlRegistry,
    private val esIndexNameFactory: EsIndexNameFactory,
    private val esIndexActiveVersionManager: EsIndexActiveVersionManager
) {


    private val logger = getLogger<ElasticIndexService>()


    fun getIndicesState(): List<EsIndexStateDto> {

        val stateDtosByName = mutableMapOf<EsIndexName, EsIndexStateDto>()

        collectHealthDtos(stateDtosByName)
        collectSummaryDtos(stateDtosByName)

        return stateDtosByName.values.toList().sortedBy { it.indexName }

    }


    private fun collectHealthDtos(stateDtosByName: MutableMap<EsIndexName, EsIndexStateDto>) {

        getIndexHealthsFromCluster().forEach { (indexName, indexHealthDto) ->

            stateDtosByName.compute(indexName) { name: EsIndexName, existingStateDto: EsIndexStateDto? ->
                existingStateDto?.copy(health = indexHealthDto) ?: EsIndexStateDto(name, null, indexHealthDto)
            }

        }

    }


    private fun getIndexHealthsFromCluster(): Map<EsIndexName, EsIndexHealthDto> {

        val clusterHealthResponse = this.client.cluster().health { h -> h.level(Level.Indices) }

        return clusterHealthResponse.indices().map { entry ->
            val indexName = this.esIndexNameFactory.indexNameFrom(entry.key)
            Pair(
                indexName,
                EsIndexHealthDto(indexName, entry.value.status().name)
            )
        }.toMap()

    }


    private fun collectSummaryDtos(stateDtosByName: MutableMap<EsIndexName, EsIndexStateDto>) {

        val indexSummaries = this.controlRegistry.getAllIndexSummaries()
        indexSummaries.forEach { indexSummary ->

            stateDtosByName.compute(indexSummary.indexName) { indexName: EsIndexName, existingStateDto: EsIndexStateDto? ->
                existingStateDto?.copy(summary = indexSummary) ?: EsIndexStateDto(indexName, indexSummary, null)
            }

        }

    }


    fun createIndex(indexName: EsIndexName, principal: Principal) {

        logger.info("BEGIN: createIndex(), indexName=$indexName, principal=${principal.name}")
        val control = this.controlRegistry.getIndexControl(indexName)
        control.createIndex()

    }


    fun setIndexActiveVersion(indexName: EsIndexName, principal: Principal) {

        logger.info("BEGIN: setIndexActiveVersion(), indexName=$indexName, principal=${principal.name}")
        this.esIndexActiveVersionManager.setActiveVersion(indexName, principal)

    }


}
