package org.maiaframework.elasticsearch.index

import org.maiaframework.props.Props
import org.maiaframework.props.PropsManager
import org.springframework.stereotype.Component
import java.security.Principal

@Component
class EsIndexActiveVersionManager(private val props: Props, private val propsManager: PropsManager) {


    fun activeVersion(esIndexBaseName: EsIndexBaseName): EsIndexVersion {

        val value = props.getIntOrNull(propertyKey(esIndexBaseName)) ?: 1
        return EsIndexVersion(value)

    }


    fun isActive(indexName: EsIndexName): Boolean {

        val activeVersion = activeVersion(indexName.esIndexBaseName)

        return indexName.indexVersion == activeVersion

    }


    fun setActiveVersion(indexName: EsIndexName, principal: Principal) {

        this.propsManager.setProperty(
                propertyKey(indexName.esIndexBaseName),
                indexName.indexVersion.value.toString(),
                principal.name,
                comment = null)

    }


    private fun propertyKey(esIndexBaseName: EsIndexBaseName) = "app.elasticsearch.indices.$esIndexBaseName.active_version"


}
