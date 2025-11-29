package org.maiaframework.elasticsearch.index

import org.springframework.stereotype.Component

@Component
class EsIndexNameLookup(
    private val esIndexActiveVersionManager: EsIndexActiveVersionManager,
    private val esIndexNameOverrider: EsIndexNameOverrider
) {


    private val theMap = mutableMapOf<EsIndexBaseName, MutableMap<EsIndexVersion, EsIndexName>>()


    fun indexName(esIndexBaseName: EsIndexBaseName): EsIndexName {

        val activeVersion = this.esIndexActiveVersionManager.activeVersion(esIndexBaseName)

        val innerMap = this.theMap.computeIfAbsent(esIndexBaseName) { mutableMapOf() }
        val esIndexName = innerMap.computeIfAbsent(activeVersion) { version -> EsIndexName(esIndexBaseName, version) }

        return this.esIndexNameOverrider.indexName(esIndexName)

    }


}
