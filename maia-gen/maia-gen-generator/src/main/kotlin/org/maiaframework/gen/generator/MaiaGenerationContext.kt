package org.maiaframework.gen.generator

import java.io.File

data class MaiaGenerationContext(
    val srcMainKotlinOutputDir: File,
    val srcTestKotlinOutputDir: File,
    val srcMainResourcesDir: File,
    val srcTestResourcesDir: File,
    val typescriptOutputDir: File,
    val sqlCreateScriptsDir: File,
    val createTablesSqlScriptPrefix: String,
)
