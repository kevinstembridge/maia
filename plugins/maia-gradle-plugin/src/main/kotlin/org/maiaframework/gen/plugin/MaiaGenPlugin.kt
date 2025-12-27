package org.maiaframework.gen.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin



abstract class MaiaGenPlugin : Plugin<Project> {


    override fun apply(project: Project) {

        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply("io.spring.dependency-management")

        val extension = project.extensions.create("maia", MaiaGenExtension::class.java)

        extension.kotlinOutputDir.convention(project.layout.projectDirectory.dir("src/generated/kotlin/main"))
        extension.resourcesOutputDir.convention(project.layout.projectDirectory.dir("src/generated/resources/main"))
        extension.sqlCreateScriptsDir.convention(project.layout.projectDirectory.dir("src/generated/sql"))
        extension.typescriptOutputDir.convention(project.layout.projectDirectory.dir("src/generated/typescript"))
        extension.createTablesSqlScriptRenderedFilePath.convention("create_entity_tables.sql")

        project.configurations.resolvable("maiaGenImplementation") {
            fromDependencyCollector(extension.dependencies.getImplementation())
        }

        project.tasks.register("generateMaiaModel", GenerateModelTask::class.java) {
            createTableSqlScriptRenderedFilePath.set(extension.createTablesSqlScriptRenderedFilePath)
            description = "Generates Maia model classes from the provided specification files."
            group = "build"
            generatorClasspath.from(project.configurations.getByName("maiaGenImplementation"))
            moduleType.set(extension.moduleType)
            specificationClassNames.set(extension.specificationClassNames)
            sqlCreateScriptDir.set(extension.sqlCreateScriptsDir)
            srcMainKotlinDir.set(extension.kotlinOutputDir)
            srcMainResourcesDir.set(extension.resourcesOutputDir)
            typescriptOutputDir.set(extension.typescriptOutputDir)
        }

    }


}
