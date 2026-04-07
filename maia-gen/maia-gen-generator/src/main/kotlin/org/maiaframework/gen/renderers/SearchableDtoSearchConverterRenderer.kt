package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ConstructorArg


class SearchableDtoSearchConverterRenderer(
    private val searchableDtoDef: SearchableDtoDef
) : AbstractKotlinRenderer(
    searchableDtoDef.dtoSearchConverterClassDef
) {


    init {

        addConstructorArg(
            ConstructorArg(
                ClassFieldDef.aClassField(
                    "fieldConverter",
                    searchableDtoDef.fieldConverterClassDef.fqcn
                ).build()
            )
        )
        addConstructorArg(
            ConstructorArg(
                ClassFieldDef.aClassField(
                    "fieldNameConverter",
                    searchableDtoDef.fieldNameConverterClassDef.fqcn
                ).build()
            )
        )
        addConstructorArg(
            ConstructorArg(
                ClassFieldDef.aClassField("jsonMapper", Fqcns.JACKSON_JSON_MAPPER).build()
            )
        )

    }


    override fun renderPreClassFields() {

        addImportFor(searchableDtoDef.dtoRootEntityDef.metaClassDef.fqcn)

        blankLine()
        appendLine(
            "    private val fieldNames = setOf(${
                searchableDtoDef.allFields.asSequence().map { "\"${it.classFieldName}\"" }.joinToString(", ")
            })"
        )

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

        addImportFor(Fqcns.MAIA_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)

        blankLine()
        blankLine()
        appendLine("    fun convert(searchModel: SearchModel): MongoSearchRequest {")
        blankLine()
        appendLine("        return convert(searchModel, this.fieldNames)")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    fun convert(searchModel: AgGridSearchModel): MongoSearchRequest {")
        blankLine()
        appendLine("        TODO()")
        blankLine()
        appendLine("    }")

    }


}
