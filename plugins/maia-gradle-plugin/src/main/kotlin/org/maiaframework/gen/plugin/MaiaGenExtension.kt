package org.maiaframework.gen.plugin

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Nested
import javax.inject.Inject

abstract class MaiaGenExtension @Inject constructor(objects: ObjectFactory) {


    val specificationClassNames = objects.listProperty(String::class.java)


    val moduleGeneratorClassName = objects.property(String::class.java)


    val srcMainKotlinOutputDir = objects.directoryProperty()


    val srcTestKotlinOutputDir = objects.directoryProperty()


    val srcMainResourcesOutputDir = objects.directoryProperty()


    val srcTestResourcesOutputDir = objects.directoryProperty()


    val typescriptOutputDir = objects.directoryProperty()


    val sqlCreateScriptsDir = objects.directoryProperty()


    val createTablesSqlScriptPrefix = objects.property(String::class.java)


    @Nested
    val dependencies: MaiaGenDependencies = objects.newInstance(MaiaGenDependencies::class.java)


    fun dependencies(action: Action<MaiaGenDependencies>) {
        action.execute(dependencies)
    }


}
