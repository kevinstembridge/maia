package org.maiaframework.elasticsearch.index

data class EsIndexStateDto(
        val indexName: EsIndexName,
        val summary: EsIndexSummaryDto?,
        val health: EsIndexHealthDto?) {

    val indexExists = this.health != null

}
