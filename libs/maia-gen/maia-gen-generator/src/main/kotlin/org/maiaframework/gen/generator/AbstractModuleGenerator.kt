package org.maiaframework.gen.generator

import org.maiaframework.gen.spec.definition.ModelDef
import java.lang.reflect.InvocationTargetException


abstract class AbstractModuleGenerator(
    protected val modelDef: ModelDef,
    protected val modelGeneratorContext: ModelGeneratorContext
) {


    protected val kotlinOutputDir = modelGeneratorContext.kotlinOutputDir


    protected val resourcesOutputDir = modelGeneratorContext.srcMainResourcesDir


    protected val typescriptOutputDir = modelGeneratorContext.typescriptOutputDir


    fun generateSource() {

        try {

            onGenerateSource()

        } catch (e: InvocationTargetException) {
            throw e.targetException
        } catch (e: ExceptionInInitializerError) {
            throw e.cause!!
        }

    }


    protected abstract fun onGenerateSource()


}
