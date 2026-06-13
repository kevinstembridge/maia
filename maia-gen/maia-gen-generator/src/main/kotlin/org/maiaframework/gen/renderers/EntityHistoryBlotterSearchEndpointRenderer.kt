package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class EntityHistoryBlotterSearchEndpointRenderer(
    private val def: EntityHistoryBlotterDef
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
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)

        if (!def.isJoinEntityHistory) {
            addImportFor(Fqcns.SPRING_PATH_VARIABLE)
            addImportFor(Fqcns.MAIA_DOMAIN_ID)
        }

        val params = if (def.isJoinEntityHistory) {
            "@RequestBody searchModel: AgGridSearchModel"
        } else {
            "@PathVariable entityId: DomainId,\n        @RequestBody searchModel: AgGridSearchModel"
        }
        val args = if (def.isJoinEntityHistory) "searchModel" else "entityId, searchModel"

        append("""
            |
            |
            |    @PostMapping("${def.searchEndpointPath}", produces = [MediaType.APPLICATION_JSON_VALUE])
            |    fun search(
            |        $params
            |    ): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        return this.searchService.search($args)
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function count`() {

        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)

        if (!def.isJoinEntityHistory) {
            addImportFor(Fqcns.SPRING_PATH_VARIABLE)
            addImportFor(Fqcns.MAIA_DOMAIN_ID)
        }

        val params = if (def.isJoinEntityHistory) {
            "@RequestBody searchModel: AgGridSearchModel"
        } else {
            "@PathVariable entityId: DomainId,\n        @RequestBody searchModel: AgGridSearchModel"
        }
        val args = if (def.isJoinEntityHistory) "searchModel" else "entityId, searchModel"

        append("""
            |
            |
            |    @PostMapping("${def.countEndpointPath}")
            |    fun count(
            |        $params
            |    ): Long {
            |
            |        return this.searchService.count($args)
            |
            |    }
            |""".trimMargin())

    }


}
