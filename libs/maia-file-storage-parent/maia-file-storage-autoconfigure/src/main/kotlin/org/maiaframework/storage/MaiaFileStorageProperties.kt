package org.maiaframework.storage

import org.springframework.boot.context.properties.ConfigurationProperties
import java.io.File

@ConfigurationProperties("maia.file-storage")
data class MaiaFileStorageProperties(
    val local: Local = Local()
) {

    data class Local(
        val baseDir: File? = null
    )

}
