package org.maiaframework.elasticsearch

import org.maiaframework.elasticsearch.index.model.EsIndexName

data class EsDocHolder<T>(
        val id: String,
        val doc: T,
        val indexName: EsIndexName
)
