package org.maiaframework.elasticsearch.index

import org.maiaframework.elasticsearch.index.model.EsIndexName
import org.maiaframework.elasticsearch.index.model.IndexBaseNameAndVersion


interface EsIndexControl {


    val indexBaseNameAndVersion: IndexBaseNameAndVersion


    val indexName: EsIndexName


    val indexDescription: String


    val isActiveVersion: Boolean


    fun createIndex()


}
