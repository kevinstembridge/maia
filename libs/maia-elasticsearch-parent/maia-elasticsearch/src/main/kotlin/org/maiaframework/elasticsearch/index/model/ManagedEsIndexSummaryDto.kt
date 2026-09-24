package org.maiaframework.elasticsearch.index.model

import org.maiaframework.elasticsearch.index.EsIndexName


data class ManagedEsIndexSummaryDto(
    val indexName: EsIndexName,
    val description: String,
    val isActiveVersion: Boolean
)
