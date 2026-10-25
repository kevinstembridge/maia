package org.maiaframework.gen.plugin

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class MaiaDomainDocsTask : DefaultTask() {

    @get:InputFiles
    abstract val domainDocsClasspath: ConfigurableFileCollection

    @get:Input
    abstract val specClassNames: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    @Suppress("ObjectLiteralToLambda")
    @TaskAction
    fun generateDomainDocs() {

        val workQueue = workerExecutor.classLoaderIsolation {
            classpath.from(domainDocsClasspath)
        }

        workQueue.submit(MaiaDomainDocsWorkAction::class.java, object : Action<MaiaDomainDocsWorkParameters> {
            override fun execute(parameters: MaiaDomainDocsWorkParameters) {
                parameters.specClassNames.set(specClassNames)
                parameters.outputDir.set(outputDir)
            }
        })

    }

}
