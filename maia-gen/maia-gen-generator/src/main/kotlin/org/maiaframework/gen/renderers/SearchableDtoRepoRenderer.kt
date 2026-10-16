package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.flags.SearchModelType
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class SearchableDtoRepoRenderer(
    private val searchableDtoDef: SearchableDtoDef
): AbstractKotlinRenderer(
    searchableDtoDef.dtoRepoClassDef
) {


    private val esEnabled = searchableDtoDef.esDocDefs.isNotEmpty()


    init {

        addConstructorArg(aClassField("dao", searchableDtoDef.dtoDaoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function findById`()
        `render function findByIds`()
        `render function findAllAsSequence`()
        `render functions findRootIdsVia`()
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


    private fun `render function findByIds`() {

        if (esEnabled.not()) {
            return
        }

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        blankLine()
        blankLine()
        appendLine("    fun findByIds(ids: Collection<DomainId>): List<${searchableDtoDef.uqcn}> {")
        blankLine()
        appendLine("        return this.dao.findByIds(ids)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function findAllAsSequence`() {

        if (esEnabled.not()) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    fun findAllAsSequence(): Sequence<${searchableDtoDef.uqcn}> {")
        blankLine()
        appendLine("        return this.dao.findAllAsSequence()")
        blankLine()
        appendLine("    }")

    }


    private fun `render functions findRootIdsVia`() {

        EsReverseJoins.reverseJoinsFor(searchableDtoDef).forEach { reverseJoin ->

            addImportFor(Fqcns.MAIA_DOMAIN_ID)

            blankLine()
            blankLine()
            appendLine("    fun ${reverseJoin.functionName}(id: DomainId): List<DomainId> {")
            blankLine()
            appendLine("        return this.dao.${reverseJoin.functionName}(id)")
            blankLine()
            appendLine("    }")

        }

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
