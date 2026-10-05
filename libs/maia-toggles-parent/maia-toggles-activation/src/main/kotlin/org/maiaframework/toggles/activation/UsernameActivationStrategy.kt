package org.maiaframework.toggles.activation

import org.springframework.security.core.context.SecurityContextHolder

class UsernameActivationStrategy : ActivationStrategy {


    override val description = "Active only for the listed users."


    override val parameterDefinitions = listOf(
        ActivationStrategyParameterDefinition(
            name = "usernames",
            description = "Comma-separated list of usernames",
            required = true
        )
    )


    override fun isActive(parameters: List<ActivationStrategyParameter>): Boolean {

        val currentUsername = SecurityContextHolder.getContext().authentication?.name

        val usernamesFromStrategy: List<String> = parameters.filter { it.name == "usernames" }.flatMap { it.value.split(",") }

        return usernamesFromStrategy.any { it == currentUsername }

    }


}
