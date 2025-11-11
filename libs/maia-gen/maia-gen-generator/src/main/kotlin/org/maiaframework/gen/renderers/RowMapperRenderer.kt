package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.RowMapperDef
import org.maiaframework.gen.spec.definition.RowMapperFunctions
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class RowMapperRenderer(
    private val rowMapperDef: RowMapperDef
): AbstractKotlinRenderer(
    rowMapperDef.classDef
) {


    init {

        if (rowMapperDef.fieldDefs.any { it.entityFieldDef.classFieldDef.isMap }) {
            addConstructorArg(ClassFieldDef.aClassField("objectMapper", Fqcns.JACKSON_OBJECT_MAPPER).privat().build())
        }

    }


    override fun renderFunctions() {

        addImportFor(Fqcns.MAIA_RESULT_SET_ADAPTER)

        blankLine()
        blankLine()
        appendLine("    override fun mapRow(rsa: ResultSetAdapter): ${rowMapperDef.uqcn} {")
        blankLine()
        appendLine("        return ${rowMapperDef.uqcn}(")

        rowMapperDef.fieldDefs.forEach { rowMapperFieldDef ->

            val entityFieldDef = rowMapperFieldDef.entityFieldDef
            val foreignKeyFieldDef = entityFieldDef.foreignKeyFieldDef

            if (foreignKeyFieldDef == null || rowMapperDef.isForEditDto == false) {

                RowMapperFunctions.renderRowMapperField(rowMapperFieldDef, indentSize = 16, orElseText = "", ::addImportFor, ::appendLine)

            } else {

                val idAndNameDef = foreignKeyFieldDef.foreignEntityDef.entityIdAndNameDef
                val idEntityFieldDef = idAndNameDef.idEntityFieldDef

                val idResultSetFieldName = "${foreignKeyFieldDef.foreignKeyFieldName}Id"
                val nameResultSetFieldName = "${foreignKeyFieldDef.foreignKeyFieldName}Name"

                addImportFor(idAndNameDef.dtoDef.fqcn)

                appendLine("            ${idAndNameDef.dtoUqcn}(")
                RowMapperFunctions.renderRowMapperField(idEntityFieldDef, idResultSetFieldName, nullable = false, indentSize = 16, orElseText = "", ::addImportFor, ::appendLine)
                RowMapperFunctions.renderRowMapperField(idAndNameDef.nameEntityFieldDef, nameResultSetFieldName, nullable = false, indentSize = 16, orElseText = "(blank)", ::addImportFor, ::appendLine)
                appendLine("            ),")

            }

        }

        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


}
