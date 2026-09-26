package org.maiaframework.elasticsearch

import org.maiaframework.elasticsearch.index.model.EsIndexName

data class EsDocIdAndSource(
        val id: String,
        val source: String,
        val indexName: EsIndexName
)
