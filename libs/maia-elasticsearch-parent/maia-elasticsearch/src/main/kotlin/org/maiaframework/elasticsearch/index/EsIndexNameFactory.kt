package org.maiaframework.elasticsearch.index

import org.maiaframework.elasticsearch.index.model.IndexBaseName
import org.maiaframework.elasticsearch.index.model.IndexVersion
import org.maiaframework.elasticsearch.index.model.IndexBaseNameAndVersion
import org.maiaframework.elasticsearch.index.model.IndexResolvedName
import org.maiaframework.lang.text.StringFunctions


object EsIndexNameFactory {


    fun indexNameFrom(indexBaseNameAndVersion: IndexBaseNameAndVersion): IndexResolvedName {

        return indexNameFrom(indexBaseNameAndVersion.baseName, indexBaseNameAndVersion.version)

    }


    fun indexNameFrom(indexBaseName: IndexBaseName, indexVersion: IndexVersion): IndexResolvedName {

        val versionSuffix = "_v${StringFunctions.padWithLeadingZeroes(indexVersion.value, 4)}"
        return IndexResolvedName("${indexBaseName}$versionSuffix")

    }


}
