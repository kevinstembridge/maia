package org.maiaframework.gen.plugin

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


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

            plugins.withType(IdeaPlugin::class, object : Action<IdeaPlugin> {

                override fun execute(ideaPlugin: IdeaPlugin) {

                    ideaPlugin.model.module {
                        generatedSourceDirs.add(extension.srcMainKotlinOutputDir.get().asFile)
                        generatedSourceDirs.add(extension.srcMainResourcesOutputDir.get().asFile)
                        generatedSourceDirs.add(extension.srcTestKotlinOutputDir.get().asFile)
                        generatedSourceDirs.add(extension.srcTestResourcesOutputDir.get().asFile)
                    }

                }

            })

            plugins.withType(KotlinBasePlugin::class, object: Action<KotlinBasePlugin> {

                override fun execute(plugin: KotlinBasePlugin) {

                    val sourceSetsContainer = project.properties["sourceSets"] as SourceSetContainer

                    sourceSetsContainer.getByName("main").apply {
                        java.srcDirs(extension.srcMainKotlinOutputDir)
                        java.srcDirs(extension.srcMainResourcesOutputDir)
                    }

                    sourceSetsContainer.getByName("test").apply {
                        java.srcDirs(extension.srcTestKotlinOutputDir)
                        java.srcDirs(extension.srcTestResourcesOutputDir)
                    }

                }

            })

            tasks.withType<KotlinCompile> {
                dependsOn("generateMaiaModel")
            }

            tasks.named("clean") {
                delete(extension.sqlCreateScriptsDir)
                delete(extension.srcMainKotlinOutputDir)
                delete(extension.srcMainResourcesOutputDir)
                delete(extension.srcTestKotlinOutputDir)
                delete(extension.srcTestResourcesOutputDir)
                delete(extension.typescriptOutputDir)
            }

        }

    }


}
