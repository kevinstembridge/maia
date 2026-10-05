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

            val existingBean = this.strategiesByName.put(beanName, bean)

            if (existingBean != null) {
                throw RuntimeException("Duplicate ActivationStrategy: springBeanName='$beanName', type='${bean.javaClass.name}'")
            }

        }

    }


    fun getStrategiesFor(activationStrategyDescriptors: List<ActivationStrategyDescriptor>): List<() -> Boolean> {

        return activationStrategyDescriptors.map { descriptor ->

            val strategy = strategiesByName[descriptor.id]

            ({ strategy?.isActive(descriptor.parameters) ?: true })

        }

    }


    fun getStrategyDefinitions(): List<ActivationStrategyDefinition> {

        return this.strategiesByName
            .map { (id, strategy) -> ActivationStrategyDefinition(id, strategy.description, strategy.parameterDefinitions) }
            .sortedBy { it.id }

    }


    /**
     * @return a description of each problem found, or an empty list if the descriptors are valid.
     */
    fun validate(activationStrategyDescriptors: List<ActivationStrategyDescriptor>): List<String> {

        return activationStrategyDescriptors.flatMap { descriptor ->

            val strategy = this.strategiesByName[descriptor.id]
                ?: return@flatMap listOf("Unknown activation strategy '${descriptor.id}'")

            val definitions = strategy.parameterDefinitions
            val definedNames = definitions.map { it.name }.toSet()

            val unknownParameters = descriptor.parameters
                .filter { it.name !in definedNames }
                .map { "Strategy '${descriptor.id}' does not accept a parameter named '${it.name}'" }

            val missingParameters = definitions
                .filter { definition -> definition.required && descriptor.parameters.none { it.name == definition.name && it.value.isNotBlank() } }
                .map { "Strategy '${descriptor.id}' requires a value for parameter '${it.name}'" }

            unknownParameters + missingParameters

        }

    }


}
