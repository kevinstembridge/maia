package org.maiaframework.gen.generator

import org.maiaframework.gen.spec.definition.ModelDef
import java.lang.reflect.InvocationTargetException


abstract class AbstractModuleGenerator(
    protected val maiaGenerationContext: MaiaGenerationContext
) {


    protected val kotlinOutputDir = maiaGenerationContext.srcMainKotlinOutputDir


    protected val resourcesOutputDir = maiaGenerationContext.srcMainResourcesDir


    protected val typescriptOutputDir = maiaGenerationContext.typescriptOutputDir


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
