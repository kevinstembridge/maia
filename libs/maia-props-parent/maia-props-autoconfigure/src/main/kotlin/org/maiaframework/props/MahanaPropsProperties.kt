package org.maiaframework.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("maia.props")
data class MahanaPropsProperties(
    val repoType: MaiaPropsRepoType = MaiaPropsRepoType.JDBC
)
