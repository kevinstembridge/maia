package org.maiaframework.elasticsearch.index

import org.maiaframework.elasticsearch.index.model.EsIndexBaseName
import org.maiaframework.elasticsearch.index.model.EsIndexName
import org.maiaframework.elasticsearch.index.model.EsIndexVersion
import org.maiaframework.elasticsearch.index.model.IndexBaseNameAndVersion
import org.maiaframework.elasticsearch.index.model.IndexResolvedName
import org.maiaframework.lang.text.StringFunctions
import java.util.regex.Pattern

object EsIndexNameFactory {

    private val indexNamePattern = Pattern.compile("^(.*?)(?:_v0*(\\d+))?\$")

    private val cachedInstances = mutableMapOf<String, EsIndexName>()


    fun indexNameFrom(esIndexName: String): EsIndexName {

        return this.cachedInstances.computeIfAbsent(esIndexName) {

            val matcher = indexNamePattern.matcher(esIndexName)

            matcher.matches()

            val baseName = matcher.group(1)
            val rawVersion = matcher.group(2)?.toInt() ?: 1
            val version = EsIndexVersion(rawVersion)

            EsIndexName(EsIndexBaseName(baseName), version)

        }

    }


    fun indexNameFrom(indexBaseNameAndVersion: IndexBaseNameAndVersion): IndexResolvedName {

        return indexNameFrom(indexBaseNameAndVersion.baseName, indexBaseNameAndVersion.version)

    }


    fun indexNameFrom(indexBaseName: EsIndexBaseName, indexVersion: EsIndexVersion): IndexResolvedName {

        val versionSuffix = "_v${StringFunctions.padWithLeadingZeroes(indexVersion.value, 4)}"
        return IndexResolvedName("${indexBaseName}$versionSuffix")

    }


}
