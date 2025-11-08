package org.maiaframework.elasticsearch

interface EsDocMapper<T> {

    fun mapEsDoc(sourceMap: Map<String, *>): T

}
