package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType


class TimelineBlotterRowDtoTypescriptRenderer(
    private val def: TimelineBlotterDef
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(def.packageName)


    override fun renderedFilePath(): String {
        return "$genDir/${def.tsRowDtoClassName}.ts"
    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("export interface ${def.tsRowDtoClassName} {")
        appendLine("    eventTimestamp: string;")
        appendLine("    eventType: string;")
        appendLine("    changeType?: string;")
        appendLine("    version?: number;")

        def.entityHistoryColumns.forEach { col ->
            val fieldName = col.classFieldDef.classFieldName.value
            val tsType = toTypescriptType(col.classFieldDef.fieldType)
            appendLine("    $fieldName?: $tsType;")
        }

        def.joinDefs.forEach { joinDef ->
            appendLine("    ${joinDef.rightFkDtoFieldName}?: string;")
            appendLine("    ${joinDef.displayFieldDtoFieldName}?: string;")
        }

        appendLine("}")

    }


    private fun toTypescriptType(fieldType: FieldType): String {
        return when (fieldType) {
            is IntFieldType, is IntTypeFieldType, is IntValueClassFieldType,
            is LongFieldType, is LongTypeFieldType -> "number"
            is EnumFieldType -> "string"
            is InstantFieldType -> "string"
            is DomainIdFieldType -> "string"
            else -> {
                val tsCompatible = fieldType.typescriptCompatibleType
                tsCompatible?.value ?: "string"
            }
        }
    }


}
