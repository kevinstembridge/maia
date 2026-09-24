package org.maiaframework.elasticsearch.index.model

import org.maiaframework.elasticsearch.index.EsIndexName


data class EsIndexStateDto(
    val indexName: EsIndexName,
    val summary: ManagedEsIndexSummaryDto?,
    val health: EsIndexHealthDto?
) {


    val indexExists = this.health != null


}
