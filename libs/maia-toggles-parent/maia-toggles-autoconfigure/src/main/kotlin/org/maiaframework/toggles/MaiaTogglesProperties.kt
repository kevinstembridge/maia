package org.maiaframework.toggles

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("org.maiaframework.toggles")
data class MaiaTogglesProperties(
    val repoType: MaiaTogglesRepoType = MaiaTogglesRepoType.JDBC
)
