package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.FormModelHandlerRenderer
import org.maiaframework.gen.spec.definition.FormModelDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = FormHandlersModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
            modelGenerator.generateSource(it)

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class FormHandlersModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
): AbstractModuleGenerator(
    maiaGenerationContext
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
