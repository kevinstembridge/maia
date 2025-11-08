package org.maiaframework.gen.generator

import java.io.File

data class ModelGeneratorContext(
    val kotlinOutputDir: File,
    val srcMainResourcesDir: File,
    val typescriptOutputDir: File,
    val sqlCreateScriptsDir: File,
    val createTablesSqlScriptRenderedFilePath: String,
)
