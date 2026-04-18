package org.maiaframework.elasticsearch

import org.maiaframework.elasticsearch.index.EsIndexName

data class EsDocHolder<T>(
        val id: String,
        val doc: T,
        val indexName: org.maiaframework.elasticsearch.index.EsIndexName
)
