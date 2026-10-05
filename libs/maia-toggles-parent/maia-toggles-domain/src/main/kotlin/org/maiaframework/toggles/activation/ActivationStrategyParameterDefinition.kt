package org.maiaframework.toggles.activation

data class ActivationStrategyParameterDefinition(
    val name: String,
    val description: String? = null,
    val required: Boolean = true
)
