package org.maiaframework.showcase.config

import com.hazelcast.config.Config
import com.hazelcast.config.YamlConfigBuilder
import maia_props.hazelcast.Maia_propsHazelcastConfig
import org.maiaframework.jdbc.JdbcOps
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations


@Configuration
@ComponentScan(basePackages = [
    "org.maiaframework.json",
    "maia_props"
])
class MaiaShowcaseAppConfiguration {


    @Bean
    fun jdbcOps(jdbcOps: NamedParameterJdbcOperations): JdbcOps {

        return JdbcOps(jdbcOps)

    }


    @Bean
    fun createNewConfig(
        propsHazelcastConfig: Maia_propsHazelcastConfig
    ): Config {

        val config: Config = YamlConfigBuilder().build()

        val compactSerializationConfig = config.serializationConfig.compactSerializationConfig

        config.apply {
//            addMapConfig(loginEmailAddressByUserIdCacheConfig)
//            addMapConfig(userDetailsByLoginEmailAddressCacheConfig)
        }

        propsHazelcastConfig.serializers.forEach {
            compactSerializationConfig.addSerializer(it)
        }

        propsHazelcastConfig.mapConfigs.forEach {
            config.addMapConfig(it)
        }

        return config

    }


}
