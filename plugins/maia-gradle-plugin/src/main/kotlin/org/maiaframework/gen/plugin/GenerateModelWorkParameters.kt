package org.maiaframework.gen.plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface GenerateModelWorkParameters : WorkParameters {


    val specificationClassName: Property<String>


    val moduleGeneratorClassName: Property<String>


    val srcMainKotlinDir: DirectoryProperty


    val srcMainResourcesDir: DirectoryProperty


    val typescriptOutputDir: DirectoryProperty


    val sqlCreateScriptDir: DirectoryProperty


    val createTableSqlScriptRenderedFilePath: Property<String>


}
