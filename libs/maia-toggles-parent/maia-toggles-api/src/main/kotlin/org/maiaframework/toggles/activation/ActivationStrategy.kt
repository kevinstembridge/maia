package org.maiaframework.toggles.activation

interface ActivationStrategy {


    val name: String


    fun isActive(parameters: List<ActivationStrategyParameter>): Boolean


}
