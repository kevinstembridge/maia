package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.SearchDtoDef
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class ElasticSearchDtoSearchServiceRenderer(
    private val searchDtoDef: SearchDtoDef,
    private val esDocDef: EsDocDef
): AbstractKotlinRenderer(
    searchDtoDef.searchServiceClassDef
) {


    init {

        addConstructorArg(ClassFieldDef.aClassField("esSearchExecutor", Fqcns.MAHANA_ES_SEARCH_EXECUTOR).privat().build())
        addConstructorArg(ClassFieldDef.aClassField("esIndex", esDocDef.esIndexClassDef.fqcn).privat().build())
        addConstructorArg(ClassFieldDef.aClassField("esDocMapper", searchDtoDef.esDocMapperClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function search`()
        `render function count`()

    }


    private fun `render function search`() {

        val searchModelFqcn = when (searchDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> Fqcns.MAHANA_AG_GRID_SEARCH_MODEL
            SearchModelType.MAHANA -> Fqcns.MAHANA_SEARCH_MODEL
        }

        addImportFor(Fqcns.MAHANA_INDEX_SEARCH_RESULTS)
        addImportFor(searchModelFqcn)

        appendLine("""
            |
            |
            |    fun search(searchModel: ${searchModelFqcn.uqcn}): IndexSearchResults<${this.searchDtoDef.uqcn}> {
            |
            |        return this.esSearchExecutor.search(
            |            searchModel,
            |            this.esIndex.indexName(),
            |            ${searchDtoDef.fieldNameMapperClassDef.uqcn}.mappingFunction,
            |            ${this.esDocDef.uqcn}::class.java,
            |            { esDoc -> esDocMapper.mapEsDoc(esDoc) }
            |        )
            |
            |    }""".trimMargin())

    }


    private fun `render function count`() {

        val searchModelFqcn = when (searchDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> Fqcns.MAHANA_AG_GRID_SEARCH_MODEL
            SearchModelType.MAHANA -> Fqcns.MAHANA_SEARCH_MODEL
        }

        addImportFor(Fqcns.MAHANA_INDEX_SEARCH_RESULTS)
        addImportFor(searchModelFqcn)

        appendLine("""
            |
            |
            |    fun count(searchModel: ${searchModelFqcn.uqcn}): Long {
            |
            |        return this.esSearchExecutor.count(
            |            searchModel,
            |            this.esIndex.indexName(),
            |            ${searchDtoDef.fieldNameMapperClassDef.uqcn}.mappingFunction
            |        )
            |
            |    }""".trimMargin())

    }


}
