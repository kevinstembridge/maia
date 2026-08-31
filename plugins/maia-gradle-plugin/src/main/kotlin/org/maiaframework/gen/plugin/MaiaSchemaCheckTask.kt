package org.maiaframework.gen.plugin

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.Inject

abstract class MaiaSchemaCheckTask : DefaultTask() {

    @get:InputFiles
    abstract val schemaCheckClasspath: ConfigurableFileCollection

    @get:Input
    abstract val applicationSpecClassName: Property<String>

    @get:Input
    abstract val jdbcUrl: Property<String>

    @get:Input
    abstract val username: Property<String>

    @get:Input
    @get:Optional
    abstract val password: Property<String>

    @get:Input
    abstract val outputFormat: Property<String>

    @get:OutputFile
    @get:Optional
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val ignoreErrors: Property<Boolean>

    @get:Inject
    abstract val workerExecutor: org.gradle.workers.WorkerExecutor

    @Suppress("ObjectLiteralToLambda")
    @TaskAction
    fun checkSchema() {

        val workQueue = workerExecutor.classLoaderIsolation {
            classpath.from(schemaCheckClasspath)
        }

        workQueue.submit(MaiaSchemaCheckWorkAction::class.java, object : Action<MaiaSchemaCheckWorkParameters> {
            override fun execute(parameters: MaiaSchemaCheckWorkParameters) {
                parameters.applicationSpecClassName.set(applicationSpecClassName)
                parameters.jdbcUrl.set(jdbcUrl)
                parameters.username.set(username)
                parameters.password.set(password)
                parameters.outputFormat.set(outputFormat)
                parameters.outputFile.set(outputFile)
                parameters.ignoreErrors.set(ignoreErrors)
            }
        })

    }

}
