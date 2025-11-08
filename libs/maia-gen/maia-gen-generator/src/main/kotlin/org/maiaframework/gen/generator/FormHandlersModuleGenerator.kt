package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.FormModelHandlerRenderer
import org.maiaframework.gen.spec.definition.FormModelDef
import org.maiaframework.gen.spec.definition.ModelDef


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
