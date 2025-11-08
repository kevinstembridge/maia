package org.maiaframework.dao.mongo

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties("maia.gen")
data class MaiaGenProperties(
    val mongoClientUri: String,
    val defaultDatabaseName: String
)
