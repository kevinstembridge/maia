package org.maiaframework.elasticsearch.index

data class EsIndexSummaryDto(
        val indexName: EsIndexName,
        val description: String,
        val isActiveVersion: Boolean)
