package org.maiaframework.jwt

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties("maia.jwt")
data class JwtProperties(
        val secret: String,
        val expirationTimeInMinutes: Int = 0
)
