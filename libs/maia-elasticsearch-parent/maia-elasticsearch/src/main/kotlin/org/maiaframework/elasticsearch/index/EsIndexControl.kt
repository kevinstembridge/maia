package org.maiaframework.elasticsearch.index

interface EsIndexControl {

    val indexName: EsIndexName

    val indexDescription: String

    val isActiveVersion: Boolean

    fun createIndex()

}
