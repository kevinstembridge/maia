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

        val params = if (def.isJoinEntityHistory) "searchModel: AgGridSearchModel" else "entityId: DomainId, searchModel: AgGridSearchModel"
        val args = if (def.isJoinEntityHistory) "searchModel" else "entityId, searchModel"

        append("""
            |
            |
            |    fun search($params): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        return this.repo.getRows($args)
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function count`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)

        val params = if (def.isJoinEntityHistory) "searchModel: AgGridSearchModel" else "entityId: DomainId, searchModel: AgGridSearchModel"
        val args = if (def.isJoinEntityHistory) "searchModel" else "entityId, searchModel"

        append("""
            |
            |
            |    fun count($params): Long {
            |
            |        return this.repo.countRows($args)
            |
            |    }
            |""".trimMargin())

    }


}
