package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.SearchDtoDef
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class SearchDtoSearchServiceRenderer(
    private val searchDtoDef: SearchDtoDef
): AbstractKotlinRenderer(
    searchDtoDef.searchServiceClassDef
) {


    init {

        addConstructorArg(aClassField("dtoRepo", searchDtoDef.dtoRepoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function findById`()
        `render function search`()
        `render function count`()
        `render function findAll`()

    }


    private fun `render function findById`() {

        if (this.searchDtoDef.generateFindById.isFalse()) {
            return
        }

        addImportFor(Fqcns.MAHANA_DOMAIN_ID)

        appendLine("""
            |
            |
            |    fun findById(id: DomainId): ${searchDtoDef.uqcn}? {
            |
            |        return this.dtoRepo.findById(id)
            |
            |    }""".trimMargin())

    }


    private fun `render function search`() {

        val searchModelFqcn = when (this.searchDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> Fqcns.MAHANA_AG_GRID_SEARCH_MODEL
            SearchModelType.MAHANA -> Fqcns.MAHANA_SEARCH_MODEL
        }

        addImportFor(Fqcns.MAHANA_SEARCH_RESULT_PAGE)
        addImportFor(searchModelFqcn)

        appendLine("""
            |
            |
            |    fun search(searchModel: ${searchModelFqcn.uqcn}): SearchResultPage<${this.searchDtoDef.uqcn}> {
            |
            |        return this.dtoRepo.getRows(searchModel)
            |
            |    }""".trimMargin())

    }


    private fun `render function count`() {

        val searchModelFqcn = when (this.searchDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> Fqcns.MAHANA_AG_GRID_SEARCH_MODEL
            SearchModelType.MAHANA -> Fqcns.MAHANA_SEARCH_MODEL
        }

        addImportFor(searchModelFqcn)

        appendLine("""
            |
            |
            |    fun count(searchModel: ${searchModelFqcn.uqcn}): Long {
            |
            |        return this.dtoRepo.countRows(searchModel)
            |
            |    }""".trimMargin())

    }


    private fun `render function findAll`() {

        if (this.searchDtoDef.withGeneratedFindAllFunction == WithGeneratedFindAllFunction.FALSE) {
            return
        }

        val searchModelFqcn = when (this.searchDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> Fqcns.MAHANA_AG_GRID_SEARCH_MODEL
            SearchModelType.MAHANA -> Fqcns.MAHANA_SEARCH_MODEL
        }

        val sortModelItemFqcn = when (this.searchDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> Fqcns.MAHANA_AG_GRID_SORT_MODEL_ITEM
            SearchModelType.MAHANA -> Fqcns.MAHANA_SEARCH_SORT_MODEL_ITEM
        }

        val defaultSortModel = searchDtoDef.defaultSortModel

        addImportFor(sortModelItemFqcn)

        blankLine()
        blankLine()
        appendLine("    fun findAll(): List<${searchDtoDef.uqcn}> {")
        blankLine()

        if (defaultSortModel.isEmpty()) {

            appendLine("        val sortModel = emptyList<${sortModelItemFqcn.uqcn}>()")

        } else if (defaultSortModel.size == 1) {

            val sortModelItem = defaultSortModel.first()
            appendLine("        val sortModel = listOf(${sortModelItemFqcn.uqcn}(\"${sortModelItem.fieldName}\", \"${sortModelItem.sortIndexAndDirection}\"))")

        } else {

            appendLine("        val sortModel = listOf(")

            defaultSortModel.forEach { fieldSortModel ->
                appendLine("            ${sortModelItemFqcn.uqcn}(\"${fieldSortModel.fieldName}\", \"${fieldSortModel.sortIndexAndDirection.sortDirection}\")")
            }

            appendLine("        )")

        }

        blankLine()
        appendLine("        val searchModel = ${searchModelFqcn.uqcn}(")

        when (searchDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> {
                addImportFor(Fqcns.JACKSON_JSON_NODE_FACTORY)
                appendLine("            JsonNodeFactory.instance.objectNode(),")
            }
            SearchModelType.MAHANA -> appendLine("            emptyList(),")
        }

        appendLine("            sortModel,")
        appendLine("            0,")
        appendLine("            null")
        appendLine("        )")
        blankLine()
        appendLine("        val searchResult = this.dtoRepo.getRows(searchModel)")
        blankLine()
        appendLine("        return searchResult.results")
        blankLine()
        appendLine("    }")

    }


}
