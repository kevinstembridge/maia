package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


class TimelineBlotterSearchEndpointRenderer(
    private val def: TimelineBlotterDef
) : AbstractKotlinRenderer(
    def.endpointClassDef
) {


    init {

        addConstructorArg(aClassField("searchService", def.searchServiceClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function search`()
        `render function count`()

    }


    private fun `render function search`() {

        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)
        addImportFor(Fqcns.SPRING_PATH_VARIABLE)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)
        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        append("""
            |
            |
            |    @PostMapping("${def.searchEndpointPath}", produces = [MediaType.APPLICATION_JSON_VALUE])
            |    fun search(
            |        @PathVariable entityId: DomainId,
            |        @RequestBody searchModel: AgGridSearchModel
            |    ): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        return this.searchService.search(entityId, searchModel)
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function count`() {

        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)
        addImportFor(Fqcns.SPRING_PATH_VARIABLE)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        append("""
            |
            |
            |    @PostMapping("${def.countEndpointPath}")
            |    fun count(
            |        @PathVariable entityId: DomainId,
            |        @RequestBody searchModel: AgGridSearchModel
            |    ): Long {
            |
            |        return this.searchService.count(entityId, searchModel)
            |
            |    }
            |""".trimMargin())

    }


}
