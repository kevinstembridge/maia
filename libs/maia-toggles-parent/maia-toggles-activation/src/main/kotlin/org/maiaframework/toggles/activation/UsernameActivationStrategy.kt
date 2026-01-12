package org.maiaframework.toggles.activation

import org.springframework.security.core.context.SecurityContextHolder

class UsernameActivationStrategy : ActivationStrategy {


    override fun isActive(parameters: List<ActivationStrategyParameter>): Boolean {

        val currentUsername = SecurityContextHolder.getContext().authentication?.name

        val usernamesFromStrategy: List<String> = parameters.filter { it.name == "usernames" }.flatMap { it.value.split(",") }

        return usernamesFromStrategy.any { it == currentUsername }

    }


}
