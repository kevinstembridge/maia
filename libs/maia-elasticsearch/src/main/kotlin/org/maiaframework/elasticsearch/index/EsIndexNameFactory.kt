package org.maiaframework.elasticsearch.index

import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class EsIndexNameFactory {

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


}
