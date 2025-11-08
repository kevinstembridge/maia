package org.maiaframework.toggles

import com.hazelcast.config.Config
import com.hazelcast.config.YamlConfigBuilder
import org.maiaframework_toggles.hazelcast.Mahana_togglesHazelcastConfig
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
        hazelcastConfig: Mahana_togglesHazelcastConfig
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
