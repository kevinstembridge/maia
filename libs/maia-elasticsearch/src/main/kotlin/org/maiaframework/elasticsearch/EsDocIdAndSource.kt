package org.maiaframework.elasticsearch

import org.maiaframework.elasticsearch.index.EsIndexName

data class EsDocIdAndSource(
        val id: String,
        val source: String,
        val indexName: org.maiaframework.elasticsearch.index.EsIndexName
)
