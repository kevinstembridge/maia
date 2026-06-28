package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


class TimelineBlotterRowDtoRepoRenderer(
    private val def: TimelineBlotterDef
) : AbstractKotlinRenderer(
    def.repoClassDef
) {


    init {

        addConstructorArg(aClassField("dao", def.daoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

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
