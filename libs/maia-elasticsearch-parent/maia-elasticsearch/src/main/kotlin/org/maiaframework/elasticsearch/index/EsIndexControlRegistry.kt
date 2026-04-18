package org.maiaframework.elasticsearch.index

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class EsIndexControlRegistry: ApplicationContextAware, InitializingBean {


    private lateinit var applicationContext: ApplicationContext

    private val controlsByName = mutableMapOf<EsIndexName, EsIndexControl>()


    override fun setApplicationContext(applicationContext: ApplicationContext) {

        this.applicationContext = applicationContext

    }


    override fun afterPropertiesSet() {

        this.applicationContext.getBeansOfType(EsIndexControl::class.java).forEach { (beanName, bean) ->

            val existingControl = this.controlsByName.put(bean.indexName, bean)

            if (existingControl != null) {
                throw RuntimeException("Duplicate control: esIndexName='${bean.indexName}', springBeanName='$beanName', type='${bean.javaClass.name}'")
            }

        }

    }


    fun getIndexControl(indexName: EsIndexName): EsIndexControl {

        return controlsByName[indexName]
                ?: throw RuntimeException("No index control is registered with name $indexName")

    }


    fun getAllIndexSummaries(): List<EsIndexSummaryDto> {

        return this.controlsByName.map { EsIndexSummaryDto(it.key, it.value.indexDescription, it.value.isActiveVersion) }

    }


}
