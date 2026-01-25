package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class RequestDtoEndpointRenderer(private val requestDtoDef: RequestDtoDef) : AbstractKotlinRenderer(requestDtoDef.endpointClassDef) {

    init {

        val handlerFqcn = requestDtoDef.handlerClassDef.fqcn

        addConstructorArg(ClassFieldDef.aClassField("handler", handlerFqcn).build())


    }


    override fun renderFunctions() {

        renderMethod_post()

    }


    private fun renderMethod_post() {

        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)
        addImportFor(Fqcns.JAKARTA_VALIDATION_VALID)

        val dtoUqcn = this.requestDtoDef.uqcn

        blankLine()
        blankLine()
        appendLine("    @PostMapping(\"${this.requestDtoDef.requestMappingPath}\")")
        appendPreAuthorize()
        appendLine("    fun post(@RequestBody @Valid requestDto: $dtoUqcn) {")
        blankLine()
        appendLine("        this.handler.handle${dtoUqcn}(requestDto)")
        blankLine()
        appendLine("    }")

    }


    private fun appendPreAuthorize() {

        this.requestDtoDef.preAuthorizeExpression?.let { expression ->
            addImportFor(Fqcns.SPRING_SECURITY_PRE_AUTHORIZE)
            appendLine("    @PreAuthorize(\"$expression\")")
        }

    }


}
