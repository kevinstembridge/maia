package org.maiaframework.elasticsearch.index.model


data class EsIndexStateDto(
    val indexName: IndexResolvedName,
    val managedIndexInfo: ManagedEsIndexInfoDto?,
    val health: EsIndexHealthDto?,
    val exists: Boolean
)
