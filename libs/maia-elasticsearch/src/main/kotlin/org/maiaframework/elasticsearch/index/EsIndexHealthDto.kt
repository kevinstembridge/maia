package org.maiaframework.elasticsearch.index

data class EsIndexHealthDto(
        val indexName: EsIndexName,
        val status: String)
