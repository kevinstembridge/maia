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

                createTablesSqlScriptPrefix.convention("create_entity_tables.sql")
                sqlCreateScriptsDir.convention(project.layout.projectDirectory.dir("src/generated/sql"))
                srcMainKotlinOutputDir.convention(project.layout.projectDirectory.dir("src/generated/kotlin/main"))
                srcMainResourcesOutputDir.convention(project.layout.projectDirectory.dir("src/generated/resources/main"))
                srcTestKotlinOutputDir.convention(project.layout.projectDirectory.dir("src/generated/kotlin/test"))
                srcTestResourcesOutputDir.convention(project.layout.projectDirectory.dir("src/generated/resources/test"))
                typescriptOutputDir.convention(project.layout.projectDirectory.dir("src/generated/typescript"))

            }

            configurations.register("maiaGenImplementation") {
                fromDependencyCollector(extension.dependencies.getImplementation())
            }

            tasks.register("generateMaiaModel", GenerateModelTask::class.java) {

                description = "Generates Maia model classes from the provided specification files."
                group = "build"

                outputs.dir(extension.sqlCreateScriptsDir)
                outputs.dir(extension.srcMainKotlinOutputDir)
                outputs.dir(extension.srcMainResourcesOutputDir)
                outputs.dir(extension.srcTestKotlinOutputDir)
                outputs.dir(extension.srcTestResourcesOutputDir)
                outputs.dir(extension.typescriptOutputDir)

                generatorClasspath.from(project.configurations.getByName("maiaGenImplementation"))

                createTablesSqlScriptPrefix.set(extension.createTablesSqlScriptPrefix)
                moduleGeneratorClassName.set(extension.moduleGeneratorClassName)
                specificationClassNames.set(extension.specificationClassNames)
                sqlCreateScriptsDir.set(extension.sqlCreateScriptsDir)
                srcMainKotlinDir.set(extension.srcMainKotlinOutputDir)
                srcMainResourcesDir.set(extension.srcMainResourcesOutputDir)
                srcTestKotlinDir.set(extension.srcTestKotlinOutputDir)
                srcTestResourcesDir.set(extension.srcTestResourcesOutputDir)
                typescriptOutputDir.set(extension.typescriptOutputDir)

            }

            tasks.named("compileKotlin") {
                dependsOn("generateMaiaModel")
            }

        }

    }


}
