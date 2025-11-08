package org.maiaframework.toggles

import com.hazelcast.config.Config
import com.hazelcast.config.YamlConfigBuilder
import maia_toggles.hazelcast.Maia_togglesHazelcastConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToggleConfiguration {


    @Bean
    fun toggleProvider(): FeatureToggleProvider {

        return FeatureToggleProvider(SampleFeature)

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
