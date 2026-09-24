package org.maiaframework.elasticsearch.index.model


data class EsIndexStateDto(
    val indexName: String,
    val managedIndexInfo: ManagedEsIndexInfoDto?,
    val health: EsIndexHealthDto?
) {


    val exists = this.health != null


}
