package org.maiaframework.gen.generator

import org.maiaframework.gen.spec.definition.ModelDef
import java.lang.reflect.InvocationTargetException


abstract class AbstractModuleGenerator(
    protected val modelGeneratorContext: ModelGeneratorContext
) {


    protected val kotlinOutputDir = modelGeneratorContext.srcMainKotlinOutputDir


    protected val resourcesOutputDir = modelGeneratorContext.srcMainResourcesDir


    protected val typescriptOutputDir = modelGeneratorContext.typescriptOutputDir


    protected lateinit var modelDef: ModelDef


    fun generateSource(modelDef: ModelDef) {

        try {

            this.modelDef = modelDef
            onGenerateSource()

        } catch (e: InvocationTargetException) {
            throw e.targetException
        } catch (e: ExceptionInInitializerError) {
            throw e.cause!!
        }

    }


    protected abstract fun onGenerateSource()


}
