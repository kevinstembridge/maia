package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.ResponseDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


class SearchRequestSearchParserRenderer(private val responseDtoDef: ResponseDtoDef) : AbstractKotlinRenderer(responseDtoDef.searchParserClassDef) {


    init {

        addConstructorArg(ClassFieldDef.aClassField("fieldNameConverter", this.responseDtoDef.searchRequestFieldNameConverterClassDef.fqcn).privat().build())
        addConstructorArg(ClassFieldDef.aClassField("fieldConverter", this.responseDtoDef.searchRequestFieldConverterClassDef.fqcn).privat().build())

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.MAHANA_SEARCH_REQUEST_PARSER)

        blankLine()
        appendLine("    private val searchRequestParser = SearchRequestParser(fieldNameConverter, fieldConverter)")

    }


    override fun renderFunctions() {

        renderMethod_parseSearchRequest()

    }


    private fun renderMethod_parseSearchRequest() {

        addImportFor(Fqcns.MAHANA_MONGO_PAGEABLE_SEARCH_REQUEST)

        blankLine()
        blankLine()
        appendLine("    fun parseSearchRequest(rawSearchJson: String): MongoPageableSearchRequest {")
        blankLine()
        appendLine("        return this.searchRequestParser.parseSearchJson(rawSearchJson)")
        blankLine()
        appendLine("    }")

    }


}
