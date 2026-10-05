package org.maiaframework.toggles.activation

fun interface ActivationStrategy {


    fun isActive(parameters: List<ActivationStrategyParameter>): Boolean


    /**
     * A human-readable explanation of what this strategy does, shown in admin UIs.
     */
    val description: String?
        get() = null


    /**
     * The parameters that this strategy accepts. Descriptors saved for this strategy are
     * validated against these definitions.
     */
    val parameterDefinitions: List<ActivationStrategyParameterDefinition>
        get() = emptyList()


}
