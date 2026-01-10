package org.maiaframework.gen.plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface MaiaGenerationWorkParameters : WorkParameters {


    val specificationClassName: Property<String>


    val moduleGeneratorClassName: Property<String>


    val srcMainKotlinDir: DirectoryProperty


    val srcTestKotlinDir: DirectoryProperty


    val srcMainResourcesDir: DirectoryProperty


    val srcTestResourcesDir: DirectoryProperty


    val typescriptOutputDir: DirectoryProperty


    val sqlCreateScriptDir: DirectoryProperty


    val createTableSqlScriptPrefix: Property<String>


}
