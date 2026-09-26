package org.maiaframework.elasticsearch.index

import org.maiaframework.elasticsearch.index.model.EsIndexBaseName
import org.maiaframework.elasticsearch.index.model.EsIndexVersion
import org.maiaframework.elasticsearch.index.model.IndexResolvedName

class EsIndexNameLookup(
    private val esIndexActiveVersionManager: EsIndexActiveVersionManager
) {


    private val theMap = mutableMapOf<EsIndexBaseName, MutableMap<EsIndexVersion, IndexResolvedName>>()


    fun activeVersionIndexName(esIndexBaseName: EsIndexBaseName): IndexResolvedName {

        val activeVersion = this.esIndexActiveVersionManager.activeVersion(esIndexBaseName)

        val innerMap = this.theMap.computeIfAbsent(esIndexBaseName) { mutableMapOf() }
        return innerMap.computeIfAbsent(activeVersion) { _ ->
            EsIndexNameFactory.indexNameFrom(esIndexBaseName, activeVersion)
        }

    }


}
