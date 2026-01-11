package org.maiaframework.gen.plugin

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


private const val MAIA_GENERATION_TASK_NAME = "maiaGeneration"


abstract class MaiaGenerationPlugin : Plugin<Project> {


    override fun apply(project: Project) {

        `apply the JavaLibrary and Kotlin plugins`(project)

        val extension = `add the MaiaGenExtension to the project`(project)

        `register the maiaGenImplementation configuration`(project, extension)

        `register the generateMaiaModel task`(project, extension)

        `configure the IdeaPlugin for generated output dirs`(project, extension)

        `add the generated output dirs as SourceSets`(project, extension)

        `the KotlinCompile task depends on maiaGeneration`(project)

        `the processResources task depends on maiaGeneration`(project)

        `include the generated output dirs when the Gradle clean task is run`(project, extension)

    }


    private fun `apply the JavaLibrary and Kotlin plugins`(project: Project) {

        project.pluginManager.apply {
            apply(JavaLibraryPlugin::class.java)
            apply("org.jetbrains.kotlin.jvm")
        }

    }


    private fun `add the MaiaGenExtension to the project`(project: Project): MaiaGenerationExtension {

        return project.extensions.create("maia", MaiaGenerationExtension::class.java).apply {

            createTablesSqlScriptPrefix.convention("create_entity_tables")
            sqlCreateScriptsDir.convention(project.layout.projectDirectory.dir("src/generated/sql"))
            srcMainKotlinOutputDir.convention(project.layout.projectDirectory.dir("src/generated/kotlin/main"))
            srcMainResourcesOutputDir.convention(project.layout.projectDirectory.dir("src/generated/resources/main"))
            srcTestKotlinOutputDir.convention(project.layout.projectDirectory.dir("src/generated/kotlin/test"))
            srcTestResourcesOutputDir.convention(project.layout.projectDirectory.dir("src/generated/resources/test"))
            typescriptOutputDir.convention(project.layout.projectDirectory.dir("src/generated/typescript"))

        }

    }


    private fun `register the maiaGenImplementation configuration`(
        project: Project,
        extension: MaiaGenerationExtension
    ) {

        project.configurations.register("maiaGenImplementation") {
            fromDependencyCollector(extension.getDependencies().getImplementation())
        }

    }


    private fun `register the generateMaiaModel task`(
        project: Project,
        extension: MaiaGenerationExtension
    ) {

        project.tasks.register(MAIA_GENERATION_TASK_NAME, MaiaGenerationTask::class.java) {

            description = "Generates source code based on a Maia model specification."
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
    }


    private fun `configure the IdeaPlugin for generated output dirs`(
        project: Project,
        extension: MaiaGenerationExtension
    ) {

        project.plugins.withType(IdeaPlugin::class, object : Action<IdeaPlugin> {

            override fun execute(ideaPlugin: IdeaPlugin) {

                ideaPlugin.model.module {
                    generatedSourceDirs.add(extension.srcMainKotlinOutputDir.get().asFile)
                    generatedSourceDirs.add(extension.srcMainResourcesOutputDir.get().asFile)
                    generatedSourceDirs.add(extension.srcTestKotlinOutputDir.get().asFile)
                    generatedSourceDirs.add(extension.srcTestResourcesOutputDir.get().asFile)
                }

            }

        })

    }


    private fun `add the generated output dirs as SourceSets`(
        project: Project,
        extension: MaiaGenerationExtension
    ) {

        project.plugins.withType(KotlinBasePlugin::class, object : Action<KotlinBasePlugin> {

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

    }


    private fun `the KotlinCompile task depends on maiaGeneration`(project: Project) {

        project.tasks.withType<KotlinCompile> {
            dependsOn(MAIA_GENERATION_TASK_NAME)
        }

    }


    private fun `the processResources task depends on maiaGeneration`(project: Project) {

        project.tasks.withType<ProcessResources> {
            dependsOn(MAIA_GENERATION_TASK_NAME)
        }

    }


    private fun `include the generated output dirs when the Gradle clean task is run`(
        project: Project,
        extension: MaiaGenerationExtension
    ) {

        with(project) {
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
