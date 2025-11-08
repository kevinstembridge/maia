package org.maiaframework.gen.renderers

import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.domain.search.SearchModel
import org.maiaframework.gen.spec.definition.DataSourceType
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.SearchDtoDef
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class DtoSearchEndpointRenderer(
    private val searchDtoDef: SearchDtoDef
): AbstractKotlinRenderer(
    searchDtoDef.searchEndpointClassDef
) {


    init {

        addConstructorArg(aClassField("searchService", searchDtoDef.searchServiceFqcn) { privat() }.build())

    }


    override fun renderFunctions() {

        `render function findById`()
        `render function search`()
        `render function count`()
        `render function findAll`()

    }


    private fun `render function findById`() {

        if (searchDtoDef.generateFindById.isFalse()) {
            return
        }

        addImportFor(Fqcns.MAHANA_DOMAIN_ID)
        addImportFor(Fqcns.SPRING_GET_MAPPING)
        addImportFor(Fqcns.SPRING_HTTP_STATUS)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_PATH_VARIABLE)
        addImportFor(Fqcns.SPRING_RESPONSE_STATUS_EXCEPTION)

        blankLine()
        blankLine()
        appendLine("    @GetMapping(\"${this.searchDtoDef.findByIdServerSideApiUrl}\", produces = [MediaType.APPLICATION_JSON_VALUE])")
        appendLine("    fun search(@PathVariable id: DomainId): ${searchDtoDef.fqcn.uqcn} {")
        blankLine()
        appendLine("        return this.searchService.findById(id)")
        appendLine("            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)")
        blankLine()
        appendLine("    }")



    }


    private fun `render function search`() {

        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)

        val returnType = when (searchDtoDef.dataSourceType) {
            DataSourceType.DATABASE -> {
                addImportFor(Fqcns.MAHANA_SEARCH_RESULT_PAGE)
                Fqcns.MAHANA_SEARCH_RESULT_PAGE
            }
            DataSourceType.ELASTIC_SEARCH -> {
                addImportFor(Fqcns.MAHANA_INDEX_SEARCH_RESULTS)
                Fqcns.MAHANA_INDEX_SEARCH_RESULTS
            }
        }

        val searchModelType = when (this.searchDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> {
                addImportFor<AgGridSearchModel>()
                "AgGridSearchModel"
            }
            SearchModelType.MAHANA -> {
                addImportFor<SearchModel>()
                "SearchModel"
            }
        }

        blankLine()
        blankLine()
        appendLine("    @PostMapping(\"${this.searchDtoDef.searchApiUrl}\", produces = [MediaType.APPLICATION_JSON_VALUE])")
        appendLine("    fun search(@RequestBody searchModel: $searchModelType): ${returnType.uqcn}<${searchDtoDef.fqcn.uqcn}> {")
        blankLine()
        appendLine("        return this.searchService.search(searchModel)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function count`() {

        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)

        val searchModelType = when (this.searchDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> {
                addImportFor<AgGridSearchModel>()
                "AgGridSearchModel"
            }
            SearchModelType.MAHANA -> {
                addImportFor<SearchModel>()
                "SearchModel"
            }
        }

        blankLine()
        blankLine()
        appendLine("    @PostMapping(\"${this.searchDtoDef.countApiUrl}\")")
        appendLine("    fun count(@RequestBody searchModel: $searchModelType): Long {")
        blankLine()
        appendLine("        return this.searchService.count(searchModel)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findAll`() {

        if (this.searchDtoDef.withGeneratedFindAllFunction == WithGeneratedFindAllFunction.FALSE) {
            return
        }

        addImportFor(Fqcns.SPRING_GET_MAPPING)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)

        blankLine()
        blankLine()
        appendLine("    @GetMapping(\"${searchDtoDef.findAllApiUrl}\", produces = [MediaType.APPLICATION_JSON_VALUE])")
        appendLine("    fun findAll(): List<${searchDtoDef.fqcn.uqcn}> {")
        blankLine()
        appendLine("        return this.searchService.findAll()")
        blankLine()
        appendLine("    }")

    }


}
