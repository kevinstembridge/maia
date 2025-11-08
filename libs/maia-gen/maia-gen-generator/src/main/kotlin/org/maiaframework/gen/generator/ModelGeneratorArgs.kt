package org.maiaframework.gen.generator

import java.io.File

class ModelGeneratorArgs(args: Array<String>) {


    val specificationClassNames: List<String>


    val generatedSourceDir: File


    val kotlinOutputDir: File


    val resourcesOutputDir: File


    val kotlinTestOutputDir: File


    val resourcesTestOutputDir: File


    val typescriptOutputDir: File


    val sqlCreateScriptsDir: File


    val createTablesSqlScriptRenderedFilePath: String


    init {

        val argsMap = args.map { it.split("=") }.associate { Pair(it[0], it[1]) }

        val specificationClassNamesCsv = argsMap["specificationClassNames"]
            ?: throw IllegalArgumentException("Expecting an argument named specificationClassNames=<...>, a comma-separated list of fully qualified class names")

        this.specificationClassNames = specificationClassNamesCsv.split(",")

        this.generatedSourceDir = File(argsMap["generatedSourceDir"] ?: "src/generated")
        this.kotlinOutputDir = File(this.generatedSourceDir, "kotlin/main")
        this.resourcesOutputDir = File(this.generatedSourceDir, "resources/main")
        this.kotlinTestOutputDir = File(this.generatedSourceDir, "kotlin/test")
        this.resourcesTestOutputDir = File(this.generatedSourceDir, "resources/test")
        this.typescriptOutputDir = File(this.generatedSourceDir, "typescript/main")
        this.sqlCreateScriptsDir = File(this.generatedSourceDir, argsMap["sqlCreateScriptsDir"] ?: "sql")
        this.createTablesSqlScriptRenderedFilePath = argsMap["createTablesSqlScriptName"] ?: "create_entity_tables.sql"

    }


}
