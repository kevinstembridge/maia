package org.maiaframework.elasticsearch.index

import org.maiaframework.elasticsearch.index.model.EsIndexName


interface EsIndexControl {


    val indexName: EsIndexName


    val indexDescription: String


    val isActiveVersion: Boolean


    fun createIndex()

    
}
