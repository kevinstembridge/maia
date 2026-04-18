package org.maiaframework.showcase.config

import co.elastic.clients.json.jackson.Jackson3JsonpMapper
import com.hazelcast.config.Config
import com.hazelcast.config.YamlConfigBuilder
import maia.hazelcast.MaiaHazelcastConfig
import maia_props.hazelcast.Maia_propsHazelcastConfig
import org.maiaframework.jdbc.JdbcOps
import org.maiaframework.json.StringTrimmingDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule


@Configuration
@ComponentScan(basePackages = [
    "org.maiaframework.json",
    "org.maiaframework.webapp",
    "maia_props",
    "maia.hazelcast",
    "maia_party.hazelcast"
])
class MaiaShowcaseAppConfiguration {


    @Bean
    fun jackson3JsonpMapper(jsonMapper: JsonMapper): Jackson3JsonpMapper {

        return Jackson3JsonpMapper(jsonMapper)

    }


    @Bean
    fun stringTrimmingModule(): SimpleModule {

        return SimpleModule().addDeserializer(String::class.java, StringTrimmingDeserializer())

    }


    @Bean
    fun jdbcOps(jdbcOps: NamedParameterJdbcOperations): JdbcOps {

        return JdbcOps(jdbcOps)

    }


    @Bean
    fun createNewConfig(
        maiaHazelcastConfig: MaiaHazelcastConfig,
        propsHazelcastConfig: Maia_propsHazelcastConfig
    ): Config {

        val config: Config = YamlConfigBuilder().build()

        val compactSerializationConfig = config.serializationConfig.compactSerializationConfig

        config.apply {
//            addMapConfig(loginEmailAddressByUserIdCacheConfig)
//            addMapConfig(userDetailsByLoginEmailAddressCacheConfig)
        }

        maiaHazelcastConfig.serializers.forEach {
            compactSerializationConfig.addSerializer(it)
        }

        maiaHazelcastConfig.mapConfigs.forEach {
            config.addMapConfig(it)
        }

        propsHazelcastConfig.serializers.forEach {
            compactSerializationConfig.addSerializer(it)
        }

        propsHazelcastConfig.mapConfigs.forEach {
            config.addMapConfig(it)
        }

        return config

    }


    @Bean("angularRoutingExcludedPaths")
    fun angularRoutingExcludedPaths(): Set<String> = setOf("/swagger-ui", "/v3/api-docs", "/webjars")


}
