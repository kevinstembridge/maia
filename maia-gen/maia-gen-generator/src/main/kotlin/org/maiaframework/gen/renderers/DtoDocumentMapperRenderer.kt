package org.maiaframework.gen.renderers

import org.maiaframework.domain.types.CollectionName
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.SearchableDtoDef

class DtoDocumentMapperRenderer(
    private val searchableDtoDef: SearchableDtoDef
) : AbstractKotlinRenderer(
    searchableDtoDef.documentMapperClassDef
) {


    init {

        searchableDtoDef.allFields.forEach { searchableDtoFieldDef ->

            searchableDtoFieldDef.entityFieldDef.fieldReaderClassField?.let { addConstructorArg(it) }
            searchableDtoFieldDef.entityFieldDef.fieldWriterClassField?.let { addConstructorArg(it) }

        }

    }


    override fun renderPreClassFields() {

        addImportFor(CollectionName::class.java)
        blankLine()
        appendLine("    private val collectionName = CollectionName(\"${this.searchableDtoDef.tableName}\")")

    }


    override fun renderFunctions() {

        renderMethod_mapDocument()

    }


    private fun renderMethod_mapDocument() {

        addImportFor(Fqcns.BSON_DOCUMENT)

        blankLine()
        blankLine()
        appendLine("    fun mapDocument(document: Document): ${this.searchableDtoDef.uqcn} {")
        blankLine()

        this.searchableDtoDef.allFields.forEach { renderReadField(it.classFieldDef, it.entityFieldDef.dbColumnFieldDef, this) }

        renderCallToConstructor(this.searchableDtoDef)
        blankLine()
        appendLine("    }")

    }


    private fun renderCallToConstructor(searchableDtoDef: SearchableDtoDef) {

        blankLine()
        append("        return ${searchableDtoDef.uqcn}(")

        val constructorArgs = searchableDtoDef.searchDtoDef.dtoDef.allFieldsSorted.joinToString(",") {
            fieldDef -> "\n                " + fieldDef.classFieldName
        }

        append(constructorArgs)
        append(")")
        newLine()

    }


}
