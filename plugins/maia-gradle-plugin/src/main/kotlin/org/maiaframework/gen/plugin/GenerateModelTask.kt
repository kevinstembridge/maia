package org.maiaframework.gen.plugin

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.Inject

abstract class GenerateModelTask : DefaultTask() {

    @get:InputFiles
    abstract val generatorClasspath: ConfigurableFileCollection


    @get:Input
    abstract val specificationClassNames: ListProperty<String>


    @get:Input
    abstract val moduleGeneratorClassName: Property<String>


    @get:OutputDirectory
    @get:Optional
    abstract val srcMainKotlinDir: DirectoryProperty


    @get:OutputDirectory
    @get:Optional
    abstract val srcTestKotlinDir: DirectoryProperty


    @get:OutputDirectory
    @get:Optional
    abstract val srcMainResourcesDir: DirectoryProperty


    @get:OutputDirectory
    @get:Optional
    abstract val srcTestResourcesDir: DirectoryProperty


    @get:OutputDirectory
    @get:Optional
    abstract val typescriptOutputDir: DirectoryProperty


    @get:OutputDirectory
    @get:Optional
    abstract val sqlCreateScriptsDir: DirectoryProperty


    @get:OutputFile
    @get:Optional
    abstract val createTablesSqlScriptPrefix: Property<String>


    @get:Inject
    abstract val workerExecutor: org.gradle.workers.WorkerExecutor


    @TaskAction
    fun generateModel() {

        val workQueue = workerExecutor.classLoaderIsolation {
            classpath.from(generatorClasspath)
        }

        if (specificationClassNames.get().isEmpty()) {
            throw RuntimeException("No specification class names have been provided. Please set the 'specificationClassNames' property to a non-empty list of fully qualified class names.")
        }

        specificationClassNames.get().forEach { specificationClassName ->
            workQueue.submit(GenerateModelWorkAction::class.java, object : Action<GenerateModelWorkParameters> {
                override fun execute(parameters: GenerateModelWorkParameters) {
                    parameters.createTableSqlScriptPrefix.set(createTablesSqlScriptPrefix)
                    parameters.moduleGeneratorClassName.set(moduleGeneratorClassName)
                    parameters.specificationClassName.set(specificationClassName)
                    parameters.sqlCreateScriptDir.set(sqlCreateScriptsDir)
                    parameters.srcMainKotlinDir.set(srcMainKotlinDir)
                    parameters.srcMainResourcesDir.set(srcMainResourcesDir)
                    parameters.srcTestKotlinDir.set(srcTestKotlinDir)
                    parameters.srcTestResourcesDir.set(srcTestResourcesDir)
                    parameters.typescriptOutputDir.set(typescriptOutputDir)
                }
            })
        }

    }


}
