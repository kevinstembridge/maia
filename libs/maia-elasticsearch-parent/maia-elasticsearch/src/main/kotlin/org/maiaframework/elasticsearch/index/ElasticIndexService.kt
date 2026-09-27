package org.maiaframework.elasticsearch.index

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.Level
import org.maiaframework.common.logging.getLogger
import org.maiaframework.elasticsearch.index.model.EsIndexBaseName
import org.maiaframework.elasticsearch.index.model.EsIndexHealthResponseDto
import org.maiaframework.elasticsearch.index.model.EsIndexStateResponseDto
import org.maiaframework.elasticsearch.index.model.EsIndexVersion
import org.maiaframework.elasticsearch.index.model.IndexBaseNameAndVersion
import org.maiaframework.elasticsearch.index.model.IndexResolvedName
import java.security.Principal


class ElasticIndexService(
    private val client: ElasticsearchClient,
    private val controlRegistry: EsIndexControlRegistry,
    private val esIndexActiveVersionManager: EsIndexActiveVersionManager
) {


    private val logger = getLogger<ElasticIndexService>()


    fun getIndicesState(): List<EsIndexStateResponseDto> {

        val stateDtosByName = mutableMapOf<IndexResolvedName, EsIndexStateResponseDto>()

        collectHealthDtos(stateDtosByName)
        collectSummaryDtos(stateDtosByName)

        return stateDtosByName.values.toList().sortedBy { it.indexName }

    }


    private fun collectHealthDtos(stateDtosByName: MutableMap<IndexResolvedName, EsIndexStateResponseDto>) {

        getIndexHealthsFromCluster().forEach { (indexName, indexHealthDto) ->

            stateDtosByName.compute(indexName) { name: IndexResolvedName, existingStateDto: EsIndexStateResponseDto? ->
                existingStateDto?.copy(health = indexHealthDto) ?: EsIndexStateResponseDto(exists = true, health = indexHealthDto, indexName = name, managedIndexInfo = null)
            }

        }

    }


    private fun getIndexHealthsFromCluster(): Map<IndexResolvedName, EsIndexHealthResponseDto> {

        val clusterHealthResponse = this.client.cluster().health { h -> h.level(Level.Indices) }

        return clusterHealthResponse.indices().map { entry ->
            Pair(
                IndexResolvedName(entry.key),
                EsIndexHealthResponseDto(entry.value.status().name)
            )
        }.toMap()

    }


    private fun collectSummaryDtos(stateDtosByName: MutableMap<IndexResolvedName, EsIndexStateResponseDto>) {

        controlRegistry.getAllIndexSummaries().forEach { indexSummary ->

            stateDtosByName.compute(indexSummary.indexResolvedName) { indexName: IndexResolvedName, existingStateDto: EsIndexStateResponseDto? ->
                existingStateDto?.copy(managedIndexInfo = indexSummary)
                    ?: EsIndexStateResponseDto(
                        exists = false,
                        health = null,
                        indexName,
                        managedIndexInfo = indexSummary
                    )
            }

        }

    }


    fun createIndex(indexBaseNameAndVersion: IndexBaseNameAndVersion, principal: Principal) {

        logger.info("BEGIN: createIndex(), indexName=$indexBaseNameAndVersion, principal=${principal.name}")
        val control = this.controlRegistry.getIndexControl(indexBaseNameAndVersion)
        control.createIndex()

    }


    fun setIndexActiveVersion(
        indexBaseName: EsIndexBaseName,
        indexVersion: EsIndexVersion,
        principal: Principal
    ) {

        logger.info("BEGIN: setIndexActiveVersion(), indexBaseName=$indexBaseName, indexVersion=$indexVersion, principal=${principal.name}")
        this.esIndexActiveVersionManager.setActiveVersion(indexBaseName, indexVersion, principal)

    }


}
