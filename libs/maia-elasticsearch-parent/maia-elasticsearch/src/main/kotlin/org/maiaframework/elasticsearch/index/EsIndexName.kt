package org.maiaframework.elasticsearch.index

import com.fasterxml.jackson.annotation.JsonValue

data class EsIndexName(
        val esIndexBaseName: EsIndexBaseName,
        val indexVersion: EsIndexVersion
): Comparable<EsIndexName> {

    @get:JsonValue
    val asString = "${esIndexBaseName}${indexVersion}"


    override fun compareTo(other: EsIndexName): Int {

        return compareBy<EsIndexName> { it.esIndexBaseName }.thenBy { it.indexVersion.value }.compare(this, other)

    }


    fun withSuffix(suffix: String): EsIndexName {

        return EsIndexName(this.esIndexBaseName.withSuffix(suffix), this.indexVersion)

    }


    override fun toString(): String {
        return asString
    }


}
