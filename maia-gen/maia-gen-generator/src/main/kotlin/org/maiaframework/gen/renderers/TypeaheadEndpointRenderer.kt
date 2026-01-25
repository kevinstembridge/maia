package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class TypeaheadEndpointRenderer(private val typeaheadDef: TypeaheadDef) : AbstractKotlinRenderer(typeaheadDef.endpointClassDef) {


    private val esDocUqcn = this.typeaheadDef.esDocDef.uqcn


    init {

        val serviceFqcn = typeaheadDef.serviceClassDef.fqcn

        addConstructorArg(ClassFieldDef.aClassField("typeaheadService", serviceFqcn).privat().build())

    }


    override fun renderFunctions() {

        renderFunction_search()

    }


    private fun renderFunction_search() {

        addImportFor(Fqcns.SPRING_GET_MAPPING)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_REQUEST_PARAM)
        addImportFor(Fqcns.MAIA_SEARCH_TERM)

        blankLine()
        blankLine()
        appendLine("    @GetMapping(\"${typeaheadDef.endpointUrl}\", produces = [MediaType.APPLICATION_JSON_VALUE])")
        appendPreAuthorize()
        appendLine("    fun search(@RequestParam(\"q\") q: String): List<$esDocUqcn> {")
        blankLine()
        appendLine("        return this.typeaheadService.search(SearchTerm(q))")
        blankLine()
        appendLine("    }")

    }


    private fun appendPreAuthorize() {

//        this.typeaheadDef.withPreAuthorize?.let { expression ->
//            addImportFor(Fqcns.SPRING_SECURITY_PRE_AUTHORIZE)
//            appendLine("    @PreAuthorize(\"$expression\")")
//        }

    }


}
