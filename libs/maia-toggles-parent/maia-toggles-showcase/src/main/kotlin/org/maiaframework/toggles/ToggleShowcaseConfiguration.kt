package org.maiaframework.toggles

import com.hazelcast.config.Config
import com.hazelcast.config.YamlConfigBuilder
import maia_toggles.hazelcast.Maia_togglesHazelcastConfig
import org.maiaframework.toggles.activation.ActivationStrategy
import org.maiaframework.toggles.sample.SampleFeatureOne
import org.maiaframework.toggles.sample.SampleFeatureTwo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToggleShowcaseConfiguration {


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
    fun createNewConfig(
        hazelcastConfig: Maia_togglesHazelcastConfig
    ): Config {

        val config: Config = YamlConfigBuilder().build()

        val compactSerializationConfig = config.serializationConfig.compactSerializationConfig

        hazelcastConfig.serializers.forEach {
            compactSerializationConfig.addSerializer(it)
        }

        hazelcastConfig.mapConfigs.forEach {
            config.addMapConfig(it)
        }

        return config

    }


}
