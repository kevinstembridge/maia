package org.maiaframework.elasticsearch.index.model


data class EsIndexName(
    val esIndexBaseName: EsIndexBaseName,
    val indexVersion: EsIndexVersion
) : Comparable<EsIndexName> {


    override fun compareTo(other: EsIndexName): Int {

        return compareBy<EsIndexName> { it.esIndexBaseName }.thenBy { it.indexVersion.value }.compare(this, other)

    }


}
