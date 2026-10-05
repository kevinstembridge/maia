package org.maiaframework.toggles.activation

data class ActivationStrategyDefinition(
    val id: String,
    val description: String?,
    val parameters: List<ActivationStrategyParameterDefinition>
)
