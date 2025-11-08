package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.FormModelDef

class FormModelHandlerRenderer(formModelDef: FormModelDef) : AbstractKotlinRenderer(formModelDef.handlerClassDef) {


    private val modelUqcn = formModelDef.uqcn


    override fun renderFunctions() {

        renderMethod_handle()

    }


    private fun renderMethod_handle() {

        addImportFor(Fqcns.SPRING_MODEL_AND_VIEW)
        addImportFor(Fqcns.SPRING_BINDING_RESULT)

        blankLine()
        blankLine()
        appendLine("    fun handle$modelUqcn(formModel: $modelUqcn, bindingResult: BindingResult): ModelAndView")
        blankLine()

    }


}
