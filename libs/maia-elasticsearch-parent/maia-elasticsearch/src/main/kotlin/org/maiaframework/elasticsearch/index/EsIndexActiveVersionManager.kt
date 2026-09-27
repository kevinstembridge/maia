package org.maiaframework.elasticsearch.index

import org.maiaframework.elasticsearch.index.model.EsIndexBaseName
import org.maiaframework.elasticsearch.index.model.EsIndexName
import org.maiaframework.elasticsearch.index.model.EsIndexVersion
import org.maiaframework.props.Props
import org.maiaframework.props.PropsManager
import java.security.Principal

class EsIndexActiveVersionManager(private val props: Props, private val propsManager: PropsManager) {


    fun activeVersion(esIndexBaseName: EsIndexBaseName): EsIndexVersion {

        val value = props.getIntOrNull(propertyKey(esIndexBaseName)) ?: 1
        return EsIndexVersion(value)

    }


    fun isActive(
        indexName: EsIndexBaseName,
        indexVersion: EsIndexVersion
    ): Boolean {

        val activeVersion = activeVersion(indexName)

        return indexVersion == activeVersion

    }


    fun setActiveVersion(
        indexBaseName: EsIndexBaseName,
        indexVersion: EsIndexVersion,
        principal: Principal
    ) {

        this.propsManager.setProperty(
                propertyKey(indexBaseName),
                indexVersion.value.toString(),
                principal.name,
                comment = null,
                reviewDate = null)

    }


    private fun propertyKey(esIndexBaseName: EsIndexBaseName) = "app.elasticsearch.indices.$esIndexBaseName.active_version"


}
