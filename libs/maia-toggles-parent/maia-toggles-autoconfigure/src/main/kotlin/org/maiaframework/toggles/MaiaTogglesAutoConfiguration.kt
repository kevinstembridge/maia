package org.maiaframework.toggles

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hazelcast.core.HazelcastInstance
import maia_toggles.hazelcast.Maia_togglesHazelcastConfig
import org.maiaframework.jdbc.JdbcOps
import org.maiaframework.json.JsonFacade
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

@AutoConfiguration(
    after = [
        DataSourceAutoConfiguration::class,
        JdbcTemplateAutoConfiguration::class,
        FlywayAutoConfiguration::class
    ]
)
@Configuration
@ConditionalOnClass(TogglesImpl::class)
@EnableConfigurationProperties(MaiaTogglesProperties::class)
class MaiaTogglesAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    fun jacksonKotlinModule(): KotlinModule {

        return KotlinModule.Builder().build()

    }


    @Bean
    @ConditionalOnMissingBean
    fun jacksonJavaTimeModule(): JavaTimeModule {
        return JavaTimeModule()
    }


    @Bean
    fun toggleJsonFacade(objectMapper: ObjectMapper): JsonFacade {

        return JsonFacade(objectMapper)

    }


    @Bean
    fun featureToggleSerializer(): FeatureToggleSerializer {

        return FeatureToggleSerializer()

    }


    @Bean
    fun toggleHazelcastConfig(featureToggleSerializer: FeatureToggleSerializer): Maia_togglesHazelcastConfig {

        return Maia_togglesHazelcastConfig(featureToggleSerializer)

    }


    @Bean
    @ConditionalOnMissingBean
    fun toggleService(
        toggleRepo: FeatureToggleRepo,
        toggles: Toggles,
        toggleRegistry: ToggleRegistry
    ): ToggleService {

        return ToggleService(toggleRepo, toggles, toggleRegistry)

    }


    @Bean
    @ConditionalOnMissingBean
    fun toggles(toggleRepo: FeatureToggleRepo): Toggles {

        return TogglesImpl(toggleRepo)

    }


    @Bean
    @ConditionalOnMissingBean
    fun toggleRegistry(toggleProvider: FeatureToggleProvider): ToggleRegistry {

        return ToggleRegistry(toggleProvider)

    }


    @Bean
    @ConditionalOnMissingBean
    fun toggleSyncer(featureToggleRepo: FeatureToggleRepo): ToggleSyncer {

        return ToggleSyncer(featureToggleRepo)

    }


    @Bean
    @ConditionalOnMissingBean
    fun toggleBootstrap(
        toggleRegistry: ToggleRegistry,
        toggleSyncer: ToggleSyncer
    ): ToggleBootstrap {

        return ToggleBootstrap(toggleRegistry, toggleSyncer)

    }


    @Configuration
    class TogglesRepoConfiguration() {


        @Bean
        @ConditionalOnMissingBean
        fun featureToggleRepo(
            featureToggleDao: FeatureToggleDao,
            hazelcastInstance: HazelcastInstance
        ): FeatureToggleRepo {

            return FeatureToggleRepo(featureToggleDao, hazelcastInstance)

        }


        @Bean
        @ConditionalOnMissingBean
        fun toggleHistoryDao(
            toggleDataSourceProvider: ObjectProvider<DataSource>,
            dataSourceProvider: ObjectProvider<DataSource>,
            toggleJsonFacade: JsonFacade,
            toggleObjectMapper: ObjectMapper
        ): FeatureToggleHistoryDao {

            val jdbcOps = toggleJdbcOps(toggleDataSourceProvider, dataSourceProvider)
            return FeatureToggleHistoryDao(FeatureToggleHistoryEntityFieldConverter(), jdbcOps, toggleJsonFacade, toggleObjectMapper)

        }


        @Bean
        @ConditionalOnMissingBean
        fun toggleDao(
            toggleDataSourceProvider: ObjectProvider<DataSource>,
            dataSourceProvider: ObjectProvider<DataSource>,
            toggleJsonFacade: JsonFacade,
            toggleObjectMapper: ObjectMapper,
            toggleHistoryDao: FeatureToggleHistoryDao
        ): FeatureToggleDao {

            val jdbcOps = toggleJdbcOps(toggleDataSourceProvider, dataSourceProvider)
            return FeatureToggleDao(FeatureToggleEntityFieldConverter(), toggleHistoryDao, jdbcOps, toggleJsonFacade, toggleObjectMapper)

        }


        private fun toggleJdbcOps(
            @MaiaTogglesDataSource togglesDataSourceProvider: ObjectProvider<DataSource>,
            dataSourceProvider: ObjectProvider<DataSource>
        ): JdbcOps {

            val dataSource = togglesDataSourceProvider.ifAvailable
                ?: dataSourceProvider.ifUnique
                ?: throw IllegalStateException("Expecting to find a DataSource.")

            val jdbcOps = NamedParameterJdbcTemplate(dataSource)
            return JdbcOps(jdbcOps)

        }


    }


}
