package org.maiaframework.gen.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin


abstract class MaiaGenPlugin : Plugin<Project> {


    override fun apply(project: Project) {

        with(project) {

            pluginManager.apply {
                apply(JavaLibraryPlugin::class.java)
                apply("org.jetbrains.kotlin.jvm")
            }

            val extension = extensions.create("maia", MaiaGenExtension::class.java).apply {

                kotlinOutputDir.convention(project.layout.projectDirectory.dir("src/generated/kotlin/main"))
                resourcesOutputDir.convention(project.layout.projectDirectory.dir("src/generated/resources/main"))
                sqlCreateScriptsDir.convention(project.layout.projectDirectory.dir("src/generated/sql"))
                typescriptOutputDir.convention(project.layout.projectDirectory.dir("src/generated/typescript"))
                createTablesSqlScriptRenderedFilePath.convention("create_entity_tables.sql")

            }

            configurations.register("maiaGenImplementation") {
                fromDependencyCollector(extension.dependencies.getImplementation())
            }

            tasks.register("generateMaiaModel", GenerateModelTask::class.java) {
                createTableSqlScriptRenderedFilePath.set(extension.createTablesSqlScriptRenderedFilePath)
                description = "Generates Maia model classes from the provided specification files."
                group = "build"
                generatorClasspath.from(project.configurations.getByName("maiaGenImplementation"))
                specificationClassNames.set(extension.specificationClassNames)
                moduleGeneratorClassName.set(extension.moduleGeneratorClassName)
                sqlCreateScriptDir.set(extension.sqlCreateScriptsDir)
                srcMainKotlinDir.set(extension.kotlinOutputDir)
                srcMainResourcesDir.set(extension.resourcesOutputDir)
                typescriptOutputDir.set(extension.typescriptOutputDir)
            }

        }

    }


}
