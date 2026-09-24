package org.maiaframework.elasticsearch.index

class EsIndexNameLookup(
    private val esIndexActiveVersionManager: EsIndexActiveVersionManager
) {


    private val theMap = mutableMapOf<EsIndexBaseName, MutableMap<EsIndexVersion, EsIndexName>>()


    fun indexName(esIndexBaseName: EsIndexBaseName): EsIndexName {

        val activeVersion = this.esIndexActiveVersionManager.activeVersion(esIndexBaseName)

        val innerMap = this.theMap.computeIfAbsent(esIndexBaseName) { mutableMapOf() }
        return innerMap.computeIfAbsent(activeVersion) { version -> EsIndexName(esIndexBaseName, version) }

    }


}
