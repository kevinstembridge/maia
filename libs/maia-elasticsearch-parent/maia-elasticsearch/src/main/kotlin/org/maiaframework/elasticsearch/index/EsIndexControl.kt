package org.maiaframework.elasticsearch.index

import org.maiaframework.elasticsearch.index.model.IndexBaseNameAndVersion


interface EsIndexControl {


    val indexBaseNameAndVersion: IndexBaseNameAndVersion


    val indexDescription: String


    fun createIndex()


}
