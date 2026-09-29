package org.maiaframework.elasticsearch.index

import org.maiaframework.elasticsearch.index.model.IndexBaseName
import org.maiaframework.elasticsearch.index.model.IndexVersion
import org.maiaframework.props.Props
import org.maiaframework.props.PropsManager
import java.security.Principal

class EsIndexActiveVersionManager(private val props: Props, private val propsManager: PropsManager) {


    fun activeVersion(indexBaseName: IndexBaseName): IndexVersion {

        val value = props.getIntOrNull(propertyKey(indexBaseName)) ?: 1
        return IndexVersion(value)

    }


    fun isActive(
        indexName: IndexBaseName,
        indexVersion: IndexVersion
    ): Boolean {

        val activeVersion = activeVersion(indexName)

        return indexVersion == activeVersion

    }


    fun setActiveVersion(
        indexBaseName: IndexBaseName,
        indexVersion: IndexVersion,
        principal: Principal
    ) {

        this.propsManager.setProperty(
                propertyKey(indexBaseName),
                indexVersion.value.toString(),
                principal.name,
                comment = null,
                reviewDate = null)

    }


    private fun propertyKey(indexBaseName: IndexBaseName) = "app.elasticsearch.indices.$indexBaseName.active_version"


}
