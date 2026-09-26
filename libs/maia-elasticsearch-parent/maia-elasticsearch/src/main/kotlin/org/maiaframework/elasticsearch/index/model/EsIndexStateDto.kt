package org.maiaframework.elasticsearch.index.model


data class EsIndexStateDto(
    val indexName: IndexResolvedName,
    val managedIndexInfo: ManagedEsIndexInfoResponseDto?,
    val health: EsIndexHealthResponseDto?,
    val exists: Boolean
)
