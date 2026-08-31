package org.maiaframework.gen.plugin

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface MaiaSchemaCheckWorkParameters : WorkParameters {

    val applicationSpecClassName: Property<String>

    val jdbcUrl: Property<String>

    val username: Property<String>

    val password: Property<String>

    val outputFormat: Property<String>

    val outputFile: RegularFileProperty

    val ignoreErrors: Property<Boolean>

}
