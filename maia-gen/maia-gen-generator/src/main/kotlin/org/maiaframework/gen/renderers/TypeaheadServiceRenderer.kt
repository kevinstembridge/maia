package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.Uqcn

class TypeaheadServiceRenderer(private val typeaheadDef: TypeaheadDef) : AbstractKotlinRenderer(typeaheadDef.serviceClassDef) {


    private val esDocUqcn: Uqcn = typeaheadDef.esDocDef.uqcn


    init {

        addConstructorArg(ClassFieldDef.aClassField("elasticClient", Fqcns.ELASTIC_CLIENT).privat().build())
        addConstructorArg(ClassFieldDef.aClassField("esIndex", typeaheadDef.esIndexClassDef.fqcn).privat().build())
        addConstructorArg(ClassFieldDef.aClassField("props", Fqcns.MAIA_PROPS).privat().build())

    }


    override fun renderFunctions() {

        `render function search`()
        `render function searchRequest`()
        `render function searchSize`()
        `render function buildResults`()

    }


    private fun `render function search`() {

        // TODO capture metrics
        appendLine("""
            |
            |
            |    fun search(searchTerm: SearchTerm): List<${esDocUqcn}> {
            |
            |        val searchRequest = searchRequest(searchTerm)
            |        val searchResponse = this.elasticClient.search(searchRequest, ${this.typeaheadDef.esDocDef.uqcn}::class.java)
            |        val searchResults = buildResults(searchResponse)
            |        return searchResults
            |
            |    }""".trimMargin())

    }


    private fun `render function buildResults`() {

        addImportFor(Fqcns.ELASTIC_SEARCH_SEARCH_RESPONSE)
        addImportFor(this.typeaheadDef.esDocDef.esDocMapperClassDef.fqcn)

        blankLine()
        blankLine()
        appendLine("    private fun buildResults(searchResponse: SearchResponse<$esDocUqcn>): List<${esDocUqcn}> {")
        blankLine()
        appendLine("        return searchResponse.hits()")
        appendLine("                .hits()")
        appendLine("                .mapNotNull { it.source() }")
        appendLine("                .sortedBy { it.${this.typeaheadDef.sortByFieldName}.toString() }")
        blankLine()
        appendLine("    }")

    }


    private fun `render function searchRequest`() {

        addImportFor(Fqcns.ELASTIC_SEARCH_SEARCH_REQUEST)
        addImportFor(Fqcns.ELASTIC_SEARCH_TEXT_QUERY_TYPE)
        addImportFor(Fqcns.MAIA_SEARCH_TERM)

        val searchTermFieldName = this.typeaheadDef.searchTermFieldName

        appendLine("""
            |
            |
            |    private fun searchRequest(searchTerm: SearchTerm): SearchRequest {
            |
            |        return SearchRequest.of { r ->
            |            r.index(this.esIndex.indexName().asString)
            |                .query { q ->
            |                    q.multiMatch { m ->
            |                        m.query(searchTerm.value)
            |                            .fields(
            |                                "$searchTermFieldName",
            |                                "$searchTermFieldName._2gram",
            |                                "$searchTermFieldName._3gram"
            |                            ).type(TextQueryType.BoolPrefix)
            |                    }
            |                }.size(searchSize())
            |        }
            |
            |    }""".trimMargin())

    }


    private fun `render function searchSize`() {

        appendLine("""
            |
            |
            |    private fun searchSize(): Int {
            |
            |       return this.props.getIntOrNull("typeahead.search_size.${this.typeaheadDef.esDocDef.fqcn}") ?: 40
            |
            |   }""".trimMargin())

    }


}
