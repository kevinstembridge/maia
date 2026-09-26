package org.maiaframework.elasticsearch.index

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.Level
import org.maiaframework.common.logging.getLogger
import org.maiaframework.elasticsearch.index.model.EsIndexBaseName
import org.maiaframework.elasticsearch.index.model.EsIndexHealthDto
import org.maiaframework.elasticsearch.index.model.EsIndexName
import org.maiaframework.elasticsearch.index.model.EsIndexStateDto
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


    fun getIndicesState(): List<EsIndexStateDto> {

        val stateDtosByName = mutableMapOf<IndexResolvedName, EsIndexStateDto>()

        collectHealthDtos(stateDtosByName)
        collectSummaryDtos(stateDtosByName)

        return stateDtosByName.values.toList().sortedBy { it.indexName }

    }


    private fun collectHealthDtos(stateDtosByName: MutableMap<IndexResolvedName, EsIndexStateDto>) {

        getIndexHealthsFromCluster().forEach { (indexName, indexHealthDto) ->

            stateDtosByName.compute(indexName) { name: IndexResolvedName, existingStateDto: EsIndexStateDto? ->
                existingStateDto?.copy(health = indexHealthDto) ?: EsIndexStateDto(name, null, indexHealthDto, exists = true)
            }

        }

    }


    private fun getIndexHealthsFromCluster(): Map<IndexResolvedName, EsIndexHealthDto> {

        val clusterHealthResponse = this.client.cluster().health { h -> h.level(Level.Indices) }

        return clusterHealthResponse.indices().map { entry ->
            Pair(
                IndexResolvedName(entry.key),
                EsIndexHealthDto(entry.value.status().name)
            )
        }.toMap()

    }


    private fun collectSummaryDtos(stateDtosByName: MutableMap<IndexResolvedName, EsIndexStateDto>) {

        val indexSummaries = this.controlRegistry.getAllIndexSummaries()
        indexSummaries.forEach { indexSummary ->

            stateDtosByName.compute(indexSummary.indexResolvedName) { indexName: IndexResolvedName, existingStateDto: EsIndexStateDto? ->
                existingStateDto?.copy(managedIndexInfo = indexSummary)
                    ?: EsIndexStateDto(indexName, indexSummary, null, exists = false)
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
