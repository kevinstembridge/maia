package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ConstructorArg

class SearchableDtoSearchConverterRenderer(private val searchableDtoDef: SearchableDtoDef) : AbstractKotlinRenderer(searchableDtoDef.searchableDtoSearchConverterClassDef) {


    init {

        addConstructorArg(ConstructorArg(aClassField("fieldConverter", searchableDtoDef.fieldConverterClassDef.fqcn).build()))
        addConstructorArg(ConstructorArg(aClassField("fieldNameConverter", searchableDtoDef.fieldNameConverterClassDef.fqcn).build()))
        addConstructorArg(ConstructorArg(aClassField("objectMapper", Fqcns.JACKSON_OBJECT_MAPPER).build()))

    }


    override fun renderPreClassFields() {

        addImportFor(searchableDtoDef.dtoRootEntityDef.metaClassDef.fqcn)

        blankLine()
        appendLine("    private val fieldNames = setOf(${searchableDtoDef.allFields.asSequence().map { "\"${it.classFieldName}\"" }.joinToString(", ")})")

        if (this.searchableDtoDef.hasLookupFields) {

            addImportFor(Fqcns.MAIA_MONGO_LOOKUP_DESCRIPTOR)
            addImportFor(Fqcns.MAIA_FOREIGN_FIELD)

            blankLine()
            appendLine("    private val rootDtoFieldNames = setOf(")

            this.searchableDtoDef.rootDtoFields.forEach { field ->
                appendLine("            \"${field.classFieldName}\",")
            }

            appendLine("    )")
            blankLine()
            appendLine("    private val lookupDescriptors = listOf(")

            this.searchableDtoDef.lookupDefs.forEach { lookupDef ->

                addImportFor(lookupDef.foreignKeyEntityDef.metaClassDef.fqcn)

                appendLine("            MongoLookupDescriptor(")
                appendLine("                    ${lookupDef.foreignKeyEntityDef.metaClassDef.uqcn}.COLLECTION_NAME,")
                appendLine("                    \"${lookupDef.localFieldClassFieldName}\",")
                appendLine("                    \"${lookupDef.foreignLookupFieldDef.tableColumnName}\",")
                appendLine("                    setOf(")

                lookupDef.lookupFieldDefs.forEach { lookupFieldDef ->
                    appendLine("                        ForeignField(\"${lookupFieldDef.dtoFieldName}\", \"${lookupFieldDef.foreignFieldDef.tableColumnName}\"),")
                }

                appendLine("                    )")
                appendLine("            ),")
            }

            appendLine("    )")

        }

        blankLine()
        appendLine("    override val typeDiscriminators = ${searchableDtoDef.dtoRootEntityDef.metaClassDef.uqcn}.TYPE_DISCRIMINATORS")
        blankLine()
        val caseInsensitiveFields = searchableDtoDef.caseInsensitiveFields
        val joinToString = caseInsensitiveFields.asSequence().map { "\"${it.classFieldName}\"" }.joinToString(", ")
        appendLine("    override val caseInsensitiveQueryFieldNames = setOf<String>($joinToString)")

    }


    override fun renderFunctions() {

        renderMethod_convert()

    }


    private fun renderMethod_convert() {

        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_CONVERTER)
        addImportFor(Fqcns.JACKSON_JSON_NODE)

        if (this.searchableDtoDef.hasLookupFields) {

            addImportFor(Fqcns.MONGO_AGGREGATION_SEARCH_REQUEST)

            blankLine()
            blankLine()
            appendLine("    fun convert(searchJson: JsonNode): MongoAggregationSearchRequest {")
            blankLine()
            appendLine("        return convertToAggregatePipeline(")
            appendLine("                searchJson,")
            appendLine("                this.rootDtoFieldNames,")
            appendLine("                this.fieldNames,")
            appendLine("                this.lookupDescriptors")
            appendLine("        )")
            blankLine()
            appendLine("    }")

        } else {
            
            addImportFor(Fqcns.MONGO_SEARCH_REQUEST)
            
            blankLine()
            blankLine()
            appendLine("    fun convert(searchJson: JsonNode): MongoSearchRequest {")
            blankLine()
            appendLine("        return convert(searchJson, this.fieldNames)")
            blankLine()
            appendLine("    }")
            
        }

    }


}
