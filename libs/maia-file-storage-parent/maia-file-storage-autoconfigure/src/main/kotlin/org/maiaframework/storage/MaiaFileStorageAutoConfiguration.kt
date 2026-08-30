package org.maiaframework.storage

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

@AutoConfiguration
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


}
