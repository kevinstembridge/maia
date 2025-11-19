package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.FormModelHandlerRenderer
import org.maiaframework.gen.spec.definition.FormModelDef
import org.maiaframework.gen.spec.definition.ModelDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = FormHandlersModuleGenerator(it, moduleGeneratorFixture.modelGeneratorContext)
            modelGenerator.generateSource()

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class FormHandlersModuleGenerator(
    modelDef: ModelDef,
    modelGeneratorContext: ModelGeneratorContext
): AbstractModuleGenerator(
    modelDef,
    modelGeneratorContext
) {


    override fun onGenerateSource() {

        renderFormModels()

    }


    private fun renderFormModels() {

        this.modelDef.formModelDefs.forEach { this.renderFormModelHandler(it) }

    }


    private fun renderFormModelHandler(formModelDef: FormModelDef) {

        val renderer = FormModelHandlerRenderer(formModelDef)
        renderer.renderToDir(this.kotlinOutputDir)

    }


}
