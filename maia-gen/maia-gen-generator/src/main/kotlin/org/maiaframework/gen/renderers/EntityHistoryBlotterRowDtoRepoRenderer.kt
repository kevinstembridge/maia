package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class EntityHistoryBlotterRowDtoRepoRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractKotlinRenderer(
    def.repoClassDef
) {


    init {

        addConstructorArg(aClassField("dao", def.daoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function getRows`()
        `render function countRows`()

    }


    private fun `render function getRows`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCHABLE_EXCEPTION)

        append("""
            |
            |
            |    fun getRows(entityId: DomainId, searchModel: AgGridSearchModel): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        try {
            |            return this.dao.search(entityId, searchModel)
            |        } catch (e: Exception) {
            |            throw AgGridSearchableException(searchModel, e)
            |        }
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function countRows`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCHABLE_EXCEPTION)

        append("""
            |
            |
            |    fun countRows(entityId: DomainId, searchModel: AgGridSearchModel): Long {
            |
            |        try {
            |            return this.dao.count(entityId, searchModel)
            |        } catch (e: Exception) {
            |            throw AgGridSearchableException(searchModel, e)
            |        }
            |
            |    }
            |""".trimMargin())

    }


}
