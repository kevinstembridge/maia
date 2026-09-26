package org.maiaframework.elasticsearch

import org.maiaframework.elasticsearch.index.model.IndexResolvedName

data class EsDocHolder<T>(
    val id: String,
    val doc: T,
    val indexName: IndexResolvedName
)
