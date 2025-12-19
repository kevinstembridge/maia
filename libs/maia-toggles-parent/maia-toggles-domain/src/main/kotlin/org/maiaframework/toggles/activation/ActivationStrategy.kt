package org.maiaframework.toggles.activation

interface ActivationStrategy {


    fun isActive(parameters: List<ActivationStrategyParameter>): Boolean


}
