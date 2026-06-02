package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType


/**
 * Renders the TypeScript interface DTO for a history blotter row.
 *
 * All enum types (top-level or as list/set element types) are rendered as `string`
 * because the history blotter shares the ChangeType enum and other enums from the
 * maia-domain library which do not have generated TypeScript counterparts.
 */
class EntityHistoryBlotterRowDtoTypescriptRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(def.packageName)


    override fun renderedFilePath(): String {
        return "$genDir/${def.tsRowDtoClassName}.ts"
    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("export interface ${def.tsRowDtoClassName} {")

        def.rowDtoClassDef.allFieldsSorted.forEach { fieldDef ->
            val fieldName = fieldDef.classFieldName.value
            val nullableClause = if (fieldDef.nullable) "?" else ""
            val tsType = toTypescriptType(fieldDef.fieldType)
            appendLine("    $fieldName$nullableClause: $tsType;")
        }

        appendLine("}")

    }


    private fun toTypescriptType(fieldType: FieldType): String {
        return when (fieldType) {
            is BooleanFieldType -> "boolean"
            is BooleanTypeFieldType -> "boolean"
            is BooleanValueClassFieldType -> "boolean"
            is IntFieldType -> "number"
            is IntTypeFieldType -> "number"
            is IntValueClassFieldType -> "number"
            is LongFieldType -> "number"
            is LongTypeFieldType -> "number"
            is EnumFieldType -> "string"
            is ListFieldType -> "ReadonlyArray<${toTypescriptType(fieldType.parameterFieldType)}>"
            else -> {
                val tsCompatible = fieldType.typescriptCompatibleType
                tsCompatible?.value ?: "string"
            }
        }
    }


}
