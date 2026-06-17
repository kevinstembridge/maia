package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.FormModelHandlerRenderer
import org.maiaframework.gen.spec.definition.FormModelDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)
        val moduleGenerator = FormHandlersModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
        moduleGenerator.generateSource(moduleGeneratorFixture.applicationModelDef)

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

        this.applicationModelDef.formModelDefs.forEach { this.renderFormModelHandler(it) }

    }


    private fun renderFormModelHandler(formModelDef: FormModelDef) {

        FormModelHandlerRenderer(formModelDef).renderToDir(this.kotlinOutputDir)

    }


}
