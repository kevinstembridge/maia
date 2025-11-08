package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.domain.types.CollectionName
import org.maiaframework.gen.spec.definition.SearchModelType

class SearchableDtoMongoRepoRenderer(
    private val searchableDtoDef: SearchableDtoDef
): AbstractKotlinRenderer(
    searchableDtoDef.dtoRepoClassDef
) {


    override fun renderPreClassFields() {

        addImportFor(Fqcns.MAIA_MONGO_COLLECTION_FACADE)

        blankLine()
        blankLine()

        appendLine("    private val mongoCollectionFacade = MongoCollectionFacade(CollectionName(\"${searchableDtoDef.tableName}\"), mongoClientFacade)")

    }


    override fun renderConstructor() {

        addImportFor(Fqcns.MAIA_MONGO_CLIENT_FACADE)
        addImportFor(CollectionName::class.java)
        addImportFor(searchableDtoDef.dtoSearchConverterClassDef.fqcn)
        addImportFor(searchableDtoDef.documentMapperClassDef.fqcn)

        appendLine("(")
        appendLine("    private val searchConverter: ${searchableDtoDef.dtoSearchConverterClassDef.uqcn},")
        appendLine("    private val documentMapper: ${searchableDtoDef.documentMapperClassDef.uqcn},")
        appendLine("    mongoClientFacade: MongoClientFacade")
        appendLine(")")

    }


    override fun renderFunctions() {

        `render function getRows`()

    }


    private fun `render function getRows`() {

        val searchModelFqcn = when (this.searchableDtoDef.searchModelType) {
            SearchModelType.AG_GRID -> Fqcns.MAIA_AG_GRID_SEARCH_MODEL
            SearchModelType.MAIA -> Fqcns.MAIA_SEARCH_MODEL
        }

        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)
        addImportFor(searchModelFqcn)

        blankLine()
        blankLine()
        appendLine("    fun getRows(searchModel: ${searchModelFqcn.uqcn}): SearchResultPage<${this.searchableDtoDef.uqcn}> {")
        blankLine()

        if (this.searchableDtoDef.hasLookupFields) {
            addImportFor(Fqcns.MONGO_AGGREGATION_SEARCH_REQUEST)
            appendLine("        val searchRequest: MongoAggregationSearchRequest = this.searchConverter.convert(searchModel)")
        } else {
            addImportFor(Fqcns.MONGO_SEARCH_REQUEST)
            appendLine("        val searchRequest: MongoSearchRequest = this.searchConverter.convert(searchModel)")
        }

        appendLine("        return this.mongoCollectionFacade.search(searchRequest) { this.documentMapper.mapDocument(it) }")
        blankLine()
        appendLine("    }")

    }


}
