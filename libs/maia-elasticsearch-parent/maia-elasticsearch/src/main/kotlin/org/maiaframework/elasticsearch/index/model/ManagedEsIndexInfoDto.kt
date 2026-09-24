package org.maiaframework.elasticsearch.index.model

import org.maiaframework.elasticsearch.index.EsIndexName


data class ManagedEsIndexInfoDto(
    val indexName: EsIndexName,
    val description: String,
    val isActiveVersion: Boolean
)
