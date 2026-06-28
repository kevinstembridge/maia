package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


class TimelineBlotterSearchServiceRenderer(
    private val def: TimelineBlotterDef
) : AbstractKotlinRenderer(
    def.searchServiceClassDef
) {


    init {

        addConstructorArg(aClassField("repo", def.repoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

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
