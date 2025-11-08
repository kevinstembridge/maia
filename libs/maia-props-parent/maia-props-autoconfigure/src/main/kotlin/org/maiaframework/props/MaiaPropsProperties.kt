package org.maiaframework.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("maia.props")
data class MaiaPropsProperties(
    val repoType: MaiaPropsRepoType = MaiaPropsRepoType.JDBC
)
