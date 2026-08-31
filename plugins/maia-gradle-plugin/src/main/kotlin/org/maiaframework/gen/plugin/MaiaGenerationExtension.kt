package org.maiaframework.gen.plugin

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Nested
import javax.inject.Inject

abstract class MaiaGenerationExtension @Inject constructor(objects: ObjectFactory) {


    val specificationClassName = objects.listProperty(String::class.java)


    val moduleGeneratorClassName = objects.property(String::class.java)


    val srcMainKotlinOutputDir = objects.directoryProperty()


    val srcTestKotlinOutputDir = objects.directoryProperty()


    val srcMainResourcesOutputDir = objects.directoryProperty()


    val srcTestResourcesOutputDir = objects.directoryProperty()


    val typescriptOutputDir = objects.directoryProperty()


    val sqlCreateScriptsDir = objects.directoryProperty()


    val createTablesSqlScriptPrefix = objects.property(String::class.java)


    val schemaCheckApplicationSpecClassName = objects.property(String::class.java)


    val schemaCheckJdbcUrl = objects.property(String::class.java)


    val schemaCheckUsername = objects.property(String::class.java)


    val schemaCheckPassword = objects.property(String::class.java)


    val schemaCheckOutputFormat = objects.property(String::class.java)


    val schemaCheckOutputFile = objects.fileProperty()


    val schemaCheckIgnoreErrors = objects.property(Boolean::class.java)


    val schemaCheckFixSqlOutputFile = objects.fileProperty()


    @Nested
    abstract fun getDependencies(): MaiaGenerationDependencies // = objects.newInstance(MaiaGenerationDependencies::class.java)


    fun dependencies(action: Action<MaiaGenerationDependencies>) {
        action.execute(getDependencies())
    }


}
