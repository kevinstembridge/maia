package org.maiaframework.toggles.activation

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class ActivationStrategyRegistry : ApplicationContextAware, InitializingBean {


    private lateinit var applicationContext: ApplicationContext


    private val strategiesByName = mutableMapOf<String, ActivationStrategy>()


    override fun setApplicationContext(applicationContext: ApplicationContext) {

        this.applicationContext = applicationContext

    }


    override fun afterPropertiesSet() {

        this.applicationContext.getBeansOfType(ActivationStrategy::class.java).forEach { (beanName, bean) ->

            val existingBean = this.strategiesByName.put(bean.name, bean)

            if (existingBean != null) {
                throw RuntimeException("Duplicate ActivationStrategy: name='${bean.name}', springBeanName='$beanName', type='${bean.javaClass.name}'")
            }

        }

    }


    fun getStrategiesFor(activationStrategyDescriptors: List<ActivationStrategyDescriptor>): List<() -> Boolean> {

        return activationStrategyDescriptors.map { descriptor ->

            val strategy = strategiesByName[descriptor.id]

            ({ strategy?.isActive(descriptor.parameters) ?: true })

        }

    }


}
