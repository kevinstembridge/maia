package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class SearchableTableDtoJdbcRepoRenderer(
    private val searchableDtoDef: SearchableDtoDef
): AbstractKotlinRenderer(
    searchableDtoDef.dtoRepoClassDef
) {


    init {

        addConstructorArg(aClassField("dao", searchableDtoDef.dtoDaoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function findById`()
        `render function getRows`()
        `render function countRows`()

    }


    private fun `render function findById`() {

        if (this.searchableDtoDef.generateFindById.isFalse()) {
            return
        }

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        blankLine()
        blankLine()
        appendLine("    fun findById(id: DomainId): ${searchableDtoDef.uqcn}? {")
        blankLine()
        appendLine("        return this.dao.findById(id)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function getRows`() {


        val searchModelTypeFqcn = when (this.searchableDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> Fqcns.MAIA_AG_GRID_SEARCH_MODEL
            SearchModelType.MAIA -> Fqcns.MAIA_SEARCH_MODEL
        }

        val searchableExceptionFqcn = when (this.searchableDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> Fqcns.MAIA_AG_GRID_SEARCHABLE_EXCEPTION
            SearchModelType.MAIA -> Fqcns.MAIA_SEARCHABLE_EXCEPTION
        }

        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)
        addImportFor(searchableExceptionFqcn)
        addImportFor(searchModelTypeFqcn)

        blankLine()
        blankLine()
        appendLine("    fun getRows(searchModel: ${searchModelTypeFqcn.uqcn}): SearchResultPage<${this.searchableDtoDef.uqcn}> {")
        blankLine()
        appendLine("        try {")
        appendLine("            return this.dao.search(searchModel)")
        appendLine("        } catch (e: Exception) {")
        appendLine("            throw ${searchableExceptionFqcn.uqcn}(searchModel, e)")
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


    private fun `render function countRows`() {


        val searchModelTypeFqcn = when (this.searchableDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> Fqcns.MAIA_AG_GRID_SEARCH_MODEL
            SearchModelType.MAIA -> Fqcns.MAIA_SEARCH_MODEL
        }

        val searchableExceptionFqcn = when (this.searchableDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> Fqcns.MAIA_AG_GRID_SEARCHABLE_EXCEPTION
            SearchModelType.MAIA -> Fqcns.MAIA_SEARCHABLE_EXCEPTION
        }

        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)
        addImportFor(searchableExceptionFqcn)
        addImportFor(searchModelTypeFqcn)

        blankLine()
        blankLine()
        appendLine("    fun countRows(searchModel: ${searchModelTypeFqcn.uqcn}): Long {")
        blankLine()
        appendLine("        try {")
        appendLine("            return this.dao.count(searchModel)")
        appendLine("        } catch (e: Exception) {")
        appendLine("            throw ${searchableExceptionFqcn.uqcn}(searchModel, e)")
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


}
