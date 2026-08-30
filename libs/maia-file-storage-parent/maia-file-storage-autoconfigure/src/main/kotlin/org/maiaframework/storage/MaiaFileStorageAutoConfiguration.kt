package org.maiaframework.storage

import org.maiaframework.jdbc.JdbcOps
import org.maiaframework.storage.hazelcast.HazelcastConfig
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

@AutoConfiguration(
    after = [
        DataSourceAutoConfiguration::class,
        JdbcTemplateAutoConfiguration::class,
        FlywayAutoConfiguration::class
    ]
)
@ConditionalOnClass(FileStorage::class)
@EnableConfigurationProperties(MaiaFileStorageProperties::class)
class MaiaFileStorageAutoConfiguration(private val properties: MaiaFileStorageProperties) {


    @Bean
    @Profile("fileStorage_local")
    @ConditionalOnMissingBean(FileStorage::class)
    fun localFileStorage(fileStorageEntryDao: FileStorageEntryDao): FileStorage {

        val baseDir = this.properties.local.baseDir
            ?: throw IllegalStateException("Expecting maia.file-storage.local.base-dir to be configured.")

        return LocalFileStorage(baseDir, fileStorageEntryDao)

    }


    @Bean
    @ConditionalOnMissingBean(HazelcastConfig::class)
    fun fileStorageHazelcastConfig(): HazelcastConfig {

        return HazelcastConfig()

    }


    @Configuration
    @ConditionalOnMissingBean(FileStorageEntryDao::class)
    class FileStorageEntryDaoConfiguration {


        @Bean
        @ConditionalOnMissingBean
        fun fileStorageEntryDao(dataSource: DataSource): FileStorageEntryDao {

            val jdbcOps = JdbcOps(NamedParameterJdbcTemplate(dataSource))

            return FileStorageEntryDao(FileStorageEntryEntityFieldConverter(), jdbcOps)

        }


    }


}
