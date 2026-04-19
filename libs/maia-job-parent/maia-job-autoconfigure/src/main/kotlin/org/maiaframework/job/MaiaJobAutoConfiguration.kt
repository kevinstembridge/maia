package org.maiaframework.job

import org.maiaframework.jdbc.JdbcOps
import org.maiaframework.json.JsonFacade
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import tools.jackson.databind.json.JsonMapper
import javax.sql.DataSource

@AutoConfiguration(
    after = [
        DataSourceAutoConfiguration::class,
        JdbcTemplateAutoConfiguration::class,
        FlywayAutoConfiguration::class,
    ]
)
@ConditionalOnClass(MaiaJob::class)
class MaiaJobAutoConfiguration {


    @Bean
    fun jobJsonFacade(jsonMapper: JsonMapper): JsonFacade {

        return JsonFacade(jsonMapper)

    }


    @Bean
    @ConditionalOnMissingBean
    fun maiaJobRegistry(): MaiaJobRegistry {

        return MaiaJobRegistry()

    }


    @Bean
    @ConditionalOnMissingBean
    fun maiaJobService(
        maiaJobRegistry: MaiaJobRegistry,
        jobExecutionRepo: JobExecutionRepo
    ): MaiaJobService {

        return MaiaJobService(maiaJobRegistry, jobExecutionRepo)

    }


    @Configuration
    class JobDaoConfiguration {


        @Bean
        @ConditionalOnMissingBean
        fun jobExecutionRepo(jobExecutionDao: JobExecutionDao): JobExecutionRepo {

            return JobExecutionRepo(jobExecutionDao)

        }


        @Bean
        @ConditionalOnMissingBean
        fun jobExecutionDao(
            @MaiaJobsDataSource jobDataSourceProvider: ObjectProvider<DataSource>,
            dataSourceProvider: ObjectProvider<DataSource>,
            jobJsonFacade: JsonFacade,
            jsonMapper: JsonMapper
        ): JobExecutionDao {

            val jdbcOps = jobJdbcOps(jobDataSourceProvider, dataSourceProvider)
            return JobExecutionDao(JobExecutionEntityFieldConverter(), jdbcOps, jobJsonFacade, jsonMapper)

        }


        private fun jobJdbcOps(
            @MaiaJobsDataSource jobDataSourceProvider: ObjectProvider<DataSource>,
            dataSourceProvider: ObjectProvider<DataSource>
        ): JdbcOps {

            val dataSource = jobDataSourceProvider.ifAvailable
                ?: dataSourceProvider.ifUnique
                ?: throw IllegalStateException("Expecting to find a DataSource.")

            return JdbcOps(NamedParameterJdbcTemplate(dataSource))

        }


    }


}
