package org.maiaframework.toggles

import com.fasterxml.jackson.databind.ObjectMapper
import org.maiaframework.json.JsonFacade
import org.maiaframework.toggles.repo.DatabaseToggleRepo
import org.maiaframework.toggles.repo.InMemoryToggleRepo
import org.maiaframework.toggles.repo.ToggleRepo
import org.maiaframework.jdbc.JdbcOps
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
@ConditionalOnClass(Toggles::class)
@EnableConfigurationProperties(MaiaTogglesProperties::class)
class MaiaTogglesAutoConfiguration {


    @Bean
    fun toggleObjectMapper(): ObjectMapper {

        return ObjectMapper()

    }


    @Bean
    fun toggleJsonFacade(objectMapper: ObjectMapper): JsonFacade {

        return JsonFacade(objectMapper)

    }


    @Bean
    @ConditionalOnMissingBean
    fun toggleService(toggleDao: FeatureToggleDao): ToggleService {

        return ToggleService(toggleDao)

    }


    @Bean
    @ConditionalOnMissingBean
    fun toggles(toggleRepo: ToggleRepo): Toggles {

        return Toggles(toggleRepo)

    }


    @Bean
    @ConditionalOnMissingBean
    fun toggleRegistry(toggleProvider: FeatureToggleProvider): ToggleRegistry {

        return ToggleRegistry(toggleProvider)

    }


    @Bean
    @ConditionalOnMissingBean
    fun toggleSyncer(featureToggleDao: FeatureToggleDao): ToggleSyncer {

        return ToggleSyncer(featureToggleDao)

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
    @ConditionalOnMissingBean(ToggleRepo::class)
    class TogglesRepoConfiguration(private val properties: MaiaTogglesProperties) {


        //        @Bean
//        @ConditionalOnMissingBean
        fun toggleRepo(
            @MaiaTogglesDataSource togglesDataSourceProvider: ObjectProvider<DataSource>,
            dataSourceProvider: ObjectProvider<DataSource>,
            toggleJsonFacade: JsonFacade,
            toggleObjectMapper: ObjectMapper
        ): ToggleRepo {

            return when (this.properties.repoType) {
                MaiaTogglesRepoType.IN_MEMORY -> InMemoryToggleRepo()
                MaiaTogglesRepoType.JDBC -> jdbcToggleRepo(
                    togglesDataSourceProvider,
                    dataSourceProvider,
                    toggleJsonFacade,
                    toggleObjectMapper
                )
            }

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


        private fun jdbcToggleRepo(
            toggleDataSourceProvider: ObjectProvider<DataSource>,
            dataSourceProvider: ObjectProvider<DataSource>,
            toggleJsonFacade: JsonFacade,
            toggleObjectMapper: ObjectMapper
        ): ToggleRepo {

            val jdbcOps = toggleJdbcOps(toggleDataSourceProvider, dataSourceProvider)
            val toggleHistoryDao = FeatureToggleHistoryDao(FeatureToggleHistoryEntityFieldConverter(), jdbcOps, toggleJsonFacade, toggleObjectMapper)
            val toggleDao = FeatureToggleDao(FeatureToggleEntityFieldConverter(), toggleHistoryDao, jdbcOps, toggleJsonFacade, toggleObjectMapper)

            return DatabaseToggleRepo(
                toggleDao,
                toggleHistoryDao
            )

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
