package org.maiaframework.elasticsearch.index

import com.fasterxml.jackson.annotation.JsonValue


data class EsIndexName(
    val esIndexBaseName: EsIndexBaseName,
    val indexVersion: EsIndexVersion
) : Comparable<EsIndexName> {


    @get:JsonValue
    val resolvedName = "${esIndexBaseName}${indexVersion}"


    override fun compareTo(other: EsIndexName): Int {

        return compareBy<EsIndexName> { it.esIndexBaseName }.thenBy { it.indexVersion.value }.compare(this, other)

    }


    override fun toString(): String {
        return resolvedName
    }


}
