package org.maiaframework.elasticsearch.index

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.Level
import org.maiaframework.common.logging.getLogger
import org.maiaframework.elasticsearch.index.model.EsIndexHealthDto
import org.maiaframework.elasticsearch.index.model.EsIndexStateDto
import java.security.Principal


class ElasticIndexService(
    private val client: ElasticsearchClient,
    private val controlRegistry: EsIndexControlRegistry,
    private val esIndexActiveVersionManager: EsIndexActiveVersionManager
) {


    private val logger = getLogger<ElasticIndexService>()


    fun getIndicesState(): List<EsIndexStateDto> {

        val stateDtosByName = mutableMapOf<String, EsIndexStateDto>()

        collectHealthDtos(stateDtosByName)
        collectSummaryDtos(stateDtosByName)

        return stateDtosByName.values.toList().sortedBy { it.indexName }

    }


    private fun collectHealthDtos(stateDtosByName: MutableMap<String, EsIndexStateDto>) {

        getIndexHealthsFromCluster().forEach { (indexName, indexHealthDto) ->

            stateDtosByName.compute(indexName) { name: String, existingStateDto: EsIndexStateDto? ->
                existingStateDto?.copy(health = indexHealthDto) ?: EsIndexStateDto(name, null, indexHealthDto, exists = true)
            }

        }

    }


    private fun getIndexHealthsFromCluster(): Map<String, EsIndexHealthDto> {

        val clusterHealthResponse = this.client.cluster().health { h -> h.level(Level.Indices) }

        return clusterHealthResponse.indices().map { entry ->
            Pair(
                entry.key,
                EsIndexHealthDto(entry.value.status().name)
            )
        }.toMap()

    }


    private fun collectSummaryDtos(stateDtosByName: MutableMap<String, EsIndexStateDto>) {

        val indexSummaries = this.controlRegistry.getAllIndexSummaries()
        indexSummaries.forEach { indexSummary ->

            stateDtosByName.compute(indexSummary.indexName.resolvedName) { indexName: String, existingStateDto: EsIndexStateDto? ->
                existingStateDto?.copy(managedIndexInfo = indexSummary)
                    ?: EsIndexStateDto(indexName, indexSummary, null, exists = false)
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
