package org.maiaframework.elasticsearch.index

import org.maiaframework.elasticsearch.index.model.IndexBaseName
import org.maiaframework.elasticsearch.index.model.IndexVersion
import org.maiaframework.elasticsearch.index.model.IndexResolvedName


class EsIndexNameLookup(
    private val esIndexActiveVersionManager: EsIndexActiveVersionManager
) {


    private val theMap = mutableMapOf<IndexBaseName, MutableMap<IndexVersion, IndexResolvedName>>()


    fun activeVersionIndexName(indexBaseName: IndexBaseName): IndexResolvedName {

        val activeVersion = this.esIndexActiveVersionManager.activeVersion(indexBaseName)

        val innerMap = this.theMap.computeIfAbsent(indexBaseName) { mutableMapOf() }
        return innerMap.computeIfAbsent(activeVersion) { _ ->
            EsIndexNameFactory.indexNameFrom(indexBaseName, activeVersion)
        }

    }


}
