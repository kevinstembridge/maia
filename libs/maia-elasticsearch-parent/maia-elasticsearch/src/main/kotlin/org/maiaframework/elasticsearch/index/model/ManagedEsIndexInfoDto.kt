package org.maiaframework.elasticsearch.index.model


data class ManagedEsIndexInfoDto(
    val indexName: EsIndexName,
    val description: String,
    val isActiveVersion: Boolean
)
