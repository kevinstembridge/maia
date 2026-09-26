package org.maiaframework.elasticsearch.index.model


data class ManagedEsIndexInfoDto(
    val indexBaseName: EsIndexBaseName,
    val indexVersion: EsIndexVersion,
    val indexResolvedName: IndexResolvedName,
    val description: String,
    val isActiveVersion: Boolean
)
