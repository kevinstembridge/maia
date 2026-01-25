package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.DocumentMapperDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.domain.types.CollectionName

class TableDtoDocumentMapperRenderer(
    private val documentMapperDef: DocumentMapperDef
): AbstractKotlinRenderer(
    documentMapperDef.classDef
) {


    init {

        documentMapperDef.fieldDefs.forEach { fieldDef ->

            val fieldReaderClassField = fieldDef.fieldReaderClassField

            if (fieldReaderClassField != null) {
                addConstructorArg(fieldReaderClassField)
            }

            val fieldWriterClassField = fieldDef.fieldWriterClassField

            if (fieldWriterClassField != null) {
                addConstructorArg(fieldWriterClassField)
            }

        }

    }


    override fun renderPreClassFields() {

        addImportFor(CollectionName::class.java)
        blankLine()
        appendLine("    private val collectionName = CollectionName(\"${this.documentMapperDef.tableName}\")")

    }


    override fun renderFunctions() {

        renderMethod_mapDocument()

    }


    private fun renderMethod_mapDocument() {

        addImportFor(Fqcns.BSON_DOCUMENT)

        blankLine()
        blankLine()
        appendLine("    fun mapDocument(document: Document): ${this.documentMapperDef.dtoFqcn.uqcn} {")
        blankLine()

        this.documentMapperDef.fieldDefs.forEach { renderReadField(it.classFieldDef, it.dbColumnFieldDef, this) }

        renderCallToConstructor()
        blankLine()
        appendLine("    }")

    }


    private fun renderCallToConstructor() {

        blankLine()
        append("        return ${this.documentMapperDef.dtoFqcn.uqcn}(")

        val constructorArgs = this.documentMapperDef.fieldDefs.joinToString(",") {
            fieldDef -> "\n                " + fieldDef.classFieldDef.classFieldName
        }

        append(constructorArgs)
        append(")")
        newLine()

    }


}
