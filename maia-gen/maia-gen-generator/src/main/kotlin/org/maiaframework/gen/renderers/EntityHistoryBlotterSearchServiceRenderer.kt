package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class EntityHistoryBlotterSearchServiceRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractKotlinRenderer(
    def.searchServiceClassDef
) {


    init {

        addConstructorArg(aClassField("repo", def.repoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function search`()
        `render function count`()

    }


    private fun `render function search`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)

        append("""
            |
            |
            |    fun search(entityId: DomainId, searchModel: AgGridSearchModel): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        return this.repo.getRows(entityId, searchModel)
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function count`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)

        append("""
            |
            |
            |    fun count(entityId: DomainId, searchModel: AgGridSearchModel): Long {
            |
            |        return this.repo.countRows(entityId, searchModel)
            |
            |    }
            |""".trimMargin())

    }


}
