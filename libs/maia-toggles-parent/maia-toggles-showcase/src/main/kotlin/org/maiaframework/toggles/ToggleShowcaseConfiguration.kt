package org.maiaframework.toggles

import com.hazelcast.config.Config
import com.hazelcast.config.YamlConfigBuilder
import org.maiaframework.jdbc.JdbcOps
import org.maiaframework.toggles.activation.ActivationStrategy
import org.maiaframework.toggles.hazelcast.HazelcastConfig
import org.maiaframework.toggles.sample.SampleFeatureOne
import org.maiaframework.toggles.sample.SampleFeatureTwo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations

@Configuration
class ToggleShowcaseConfiguration {


    @Bean
    fun jdbcOps(jdbcOps: NamedParameterJdbcOperations): JdbcOps {

        return JdbcOps(jdbcOps)

    }


    @Bean
    fun alwaysActiveStrategy(): ActivationStrategy {

        return ActivationStrategy { true }

    }


    @Bean
    fun alwaysInactiveStrategy(): ActivationStrategy {

        return ActivationStrategy { false }

    }


    @Bean
    fun toggleProvider(): FeatureToggleProvider {

        return FeatureToggleProvider(
            SampleFeatureOne,
            SampleFeatureTwo,
        )

    }


    @Bean
    fun hazelcastConfig(
        togglesHazelcastConfig: HazelcastConfig
    ): Config {

        val config: Config = YamlConfigBuilder().build()

        val compactSerializationConfig = config.serializationConfig.compactSerializationConfig

        togglesHazelcastConfig.serializers.forEach {
            compactSerializationConfig.addSerializer(it)
        }

        togglesHazelcastConfig.mapConfigs.forEach {
            config.addMapConfig(it)
        }

        return config

    }


}
