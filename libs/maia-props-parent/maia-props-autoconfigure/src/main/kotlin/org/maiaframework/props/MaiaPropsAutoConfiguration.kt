package org.maiaframework.props

import org.maiaframework.props.repo.DatabasePropsRepo
import org.maiaframework.props.repo.InMemoryPropsRepo
import org.maiaframework.props.repo.PropsRepo
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
import org.springframework.core.env.Environment
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
@ConditionalOnClass(Props::class)
@EnableConfigurationProperties(MahanaPropsProperties::class)
class MaiaPropsAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    fun props(propsRepo: PropsRepo, environment: Environment): Props {

        return Props(propsRepo, environment)

    }


    @Bean
    @ConditionalOnMissingBean
    fun propsManager(propsRepo: PropsRepo): PropsManager {

        return PropsManager(propsRepo)

    }


    @Configuration
    @ConditionalOnMissingBean(PropsRepo::class)
    class PropsRepoConfiguration(private val properties: MahanaPropsProperties) {


        @Bean
        @ConditionalOnMissingBean
        fun propsRepo(
            @MaiaPropsDataSource propsDataSourceProvider: ObjectProvider<DataSource>,
            dataSourceProvider: ObjectProvider<DataSource>
        ): PropsRepo {

            return when (this.properties.repoType) {
                MaiaPropsRepoType.IN_MEMORY -> InMemoryPropsRepo()
                MaiaPropsRepoType.JDBC -> jdbcPropsRepo(propsDataSourceProvider, dataSourceProvider)
            }

        }


        private fun jdbcPropsRepo(
            propsDataSourceProvider: ObjectProvider<DataSource>,
            dataSourceProvider: ObjectProvider<DataSource>
        ): PropsRepo {

            val jdbcOps = propsJdbcOps(propsDataSourceProvider, dataSourceProvider)
            val propsHistoryDao = PropsHistoryDao(PropsHistoryEntityFieldConverter(), jdbcOps)
            val propsDao = PropsDao(PropsEntityFieldConverter(), propsHistoryDao, jdbcOps)

            return DatabasePropsRepo(propsDao, propsHistoryDao)

        }


        private fun propsJdbcOps(
            @MaiaPropsDataSource propsDataSourceProvider: ObjectProvider<DataSource>,
            dataSourceProvider: ObjectProvider<DataSource>
        ): JdbcOps {

            val dataSource = propsDataSourceProvider.ifAvailable
                ?: dataSourceProvider.ifUnique
                ?: throw IllegalStateException("Expecting to find a DataSource.")

            val jdbcOps = NamedParameterJdbcTemplate(dataSource)
            return JdbcOps(jdbcOps)

        }


    }


}
