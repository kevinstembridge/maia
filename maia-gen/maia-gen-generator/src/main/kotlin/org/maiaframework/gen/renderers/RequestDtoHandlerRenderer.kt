package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.RequestDtoDef

class RequestDtoHandlerRenderer(private val requestDtoDef: RequestDtoDef) : AbstractKotlinRenderer(requestDtoDef.handlerClassDef) {


    override fun renderFunctions() {

        renderMethod_handle()

    }


    private fun renderMethod_handle() {

        blankLine()
        blankLine()
        appendLine("    fun handle${this.requestDtoDef.uqcn}(requestDto: ${this.requestDtoDef.uqcn})")
        blankLine()

    }


}
