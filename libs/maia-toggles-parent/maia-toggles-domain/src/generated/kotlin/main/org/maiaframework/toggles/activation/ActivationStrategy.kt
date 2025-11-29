package org.maiaframework.toggles.activation

fun interface ActivationStrategy {


    fun isActive(parameters: List<ActivationStrategyParameter>): Boolean


}
