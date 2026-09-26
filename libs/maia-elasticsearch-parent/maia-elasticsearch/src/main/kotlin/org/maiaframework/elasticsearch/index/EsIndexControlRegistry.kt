package org.maiaframework.elasticsearch.index

import org.maiaframework.elasticsearch.index.model.IndexBaseNameAndVersion
import org.maiaframework.elasticsearch.index.model.ManagedEsIndexInfoResponseDto
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class EsIndexControlRegistry: ApplicationContextAware, InitializingBean {


    private lateinit var applicationContext: ApplicationContext


    private val controlsByName = mutableMapOf<IndexBaseNameAndVersion, EsIndexControl>()


    override fun setApplicationContext(applicationContext: ApplicationContext) {

        this.applicationContext = applicationContext

    }


    override fun afterPropertiesSet() {

        this.applicationContext.getBeansOfType(EsIndexControl::class.java).forEach { (beanName, bean) ->

            val existingControl = this.controlsByName.put(bean.indexBaseNameAndVersion, bean)

            if (existingControl != null) {
                throw RuntimeException("Duplicate control: esIndexName='${bean.indexName}', springBeanName='$beanName', type='${bean.javaClass.name}'")
            }

        }

    }


    fun getIndexControl(indexBaseNameAndVersion: IndexBaseNameAndVersion): EsIndexControl {

        return controlsByName[indexBaseNameAndVersion]
                ?: throw RuntimeException("No index control is registered with name $indexBaseNameAndVersion")

    }


    fun getAllIndexSummaries(): List<ManagedEsIndexInfoResponseDto> {

        return this.controlsByName.map {

            val indexBaseName = it.key.baseName
            val indexVersion = it.key.version
            val indexResolvedName = EsIndexNameFactory.indexNameFrom(indexBaseName, indexVersion)

            ManagedEsIndexInfoResponseDto(
                it.value.indexDescription,
                indexBaseName,
                indexResolvedName,
                indexVersion,
                it.value.isActiveVersion
            )

        }

    }


}
