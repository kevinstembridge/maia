package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.ResponseDtoDef

class ResponseDtoRepoRenderer(private val responseDtoDef: ResponseDtoDef) : AbstractKotlinRenderer(responseDtoDef.repoClassDef) {


    override fun renderFunctions() {

        renderMethod_find()

    }


    private fun renderMethod_find() {

        addImportFor(Fqcns.MAIA_MONGO_PAGEABLE_SEARCH_REQUEST)
        addImportFor(Fqcns.SPRING_PAGE)

        blankLine()
        blankLine()
        appendLine("    fun search(searchRequest: MongoPageableSearchRequest): Page<${this.responseDtoDef.dtoDef.uqcn}>")
        blankLine()

    }


}
