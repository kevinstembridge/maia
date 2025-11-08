package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.DataRowStagingEntityFieldDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.Nullability
import java.time.LocalDate

class StagingEntityExtensionsRenderer(private val entityDef: EntityDef) : AbstractKotlinRenderer(entityDef.stagingEntityExtensionsClassDef) {


    override fun renderFunctions() {

        entityDef.stagingEntityFieldDefs.forEach { field -> renderField(field) }

    }


    private fun renderField(field: DataRowStagingEntityFieldDef) {

        addImportFor(Fqcns.MAHANA_STAGED_FIELD_META)

        val nullableSuffix = when (field.nullability) {
            Nullability.NULLABLE -> "?"
            Nullability.NOT_NULLABLE -> ""
        }

        blankLine()
        blankLine()

        if (field.isNullableString) {
            appendLine("    val ${entityDef.entityUqcn}.${field.entityFieldDef.classFieldName}Ext: String? get() = ${field.classFieldName}")
            return
        }

        appendLine("    val ${entityDef.entityUqcn}.${field.entityFieldDef.classFieldName}Ext: ${field.expectedFieldDef.fieldType.unqualifiedToString}$nullableSuffix")
        appendLine("        get() {")
        blankLine()
        appendLine("            val rawValue = ${field.classFieldName}")

        if (field.isNotNullable) {

            addImportFor(Fqcns.MAHANA_STAGED_FIELD_MISSING_EXCEPTION)

            blankLine()
            appendLine("            if (rawValue.isNullOrBlank()) {")
            appendLine("                throw StagedFieldMissingException(StagedFieldMeta(")
            appendLine("                    id,")
            appendLine("                    rawValue,")
            appendLine("                    ${entityDef.metaClassDef.uqcn}.SCHEMA_AND_TABLE_NAME,")
            appendLine("                    \"${field.entityFieldDef.tableColumnName}\",")
            appendLine("                    \"${field.dataRowHeaderName}\",")
            appendLine("                    ${entityDef.metaClassDef.uqcn}.${field.classFieldName},")
            appendLine("                    ${field.expectedFieldDef.fieldType.unqualifiedToString}::class.java,")
            appendLine("                    this.lineNumber,")
            appendLine("                    this.fileStorageId")
            appendLine("                ))")
            appendLine("            }")
            blankLine()
        }

        if (field.isString) {

            appendLine("            return rawValue")

        } else if (field.isInt) {

            addImportFor(Fqcns.MAHANA_STAGED_FIELD_NUMBER_FORMAT_EXCEPTION)

            appendLine("            try {")
            appendLine("                return rawValue$nullableSuffix.toInt()")
            appendLine("            } catch (e: NumberFormatException) {")
            appendLine("                throw StagedFieldNumberFormatException(StagedFieldMeta(")
            appendLine("                   id,")
            appendLine("                   rawValue,")
            appendLine("                   ${entityDef.metaClassDef.uqcn}.SCHEMA_AND_TABLE_NAME,")
            appendLine("                   \"${field.entityFieldDef.tableColumnName}\",")
            appendLine("                   \"${field.dataRowHeaderName}\",")
            appendLine("                   ${entityDef.metaClassDef.uqcn}.${field.classFieldName},")
            appendLine("                   ${field.expectedFieldDef.fieldType.unqualifiedToString}::class.java,")
            appendLine("                   this.lineNumber,")
            appendLine("                   this.fileStorageId")
            appendLine("               ))")
            appendLine("           }")

        } else if (field.isLocalDate) {

            addImportFor(Fqcns.JAVA_DATE_TIME_FORMATTER)
            addImportFor(Fqcns.JAVA_DATE_TIME_PARSE_EXCEPTION)
            addImportFor(LocalDate::class.java)
            addImportFor(Fqcns.MAHANA_STAGED_FIELD_DATE_FORMAT_EXCEPTION)

            val formatter = field.dateTimeFormatterConstant

            val formatterText = if (formatter == null) {
                ""
            } else {
                ", DateTimeFormatter.${formatter.name}"
            }

            val formatterToString = if (formatter == null) {
                "DateTimeFormatter.ISO_LOCAL_DATE.toString()"
            } else {
                "DateTimeFormatter.${formatter.name}.toString()"
            }

            appendLine("            try {")

            if (field.isNullable) {
                appendLine("                return rawValue?.let { LocalDate.parse(it$formatterText) }")
            } else {
                appendLine("                return LocalDate.parse(rawValue$formatterText)")
            }

            appendLine("            } catch (e: DateTimeParseException) {")
            appendLine("                throw StagedFieldDateFormatException(")
            appendLine("                    StagedFieldMeta(")
            appendLine("                        id,")
            appendLine("                        rawValue,")
            appendLine("                        ${entityDef.metaClassDef.uqcn}.SCHEMA_AND_TABLE_NAME,")
            appendLine("                        \"${field.entityFieldDef.tableColumnName}\",")
            appendLine("                        \"${field.dataRowHeaderName}\",")
            appendLine("                        ${entityDef.metaClassDef.uqcn}.${field.classFieldName},")
            appendLine("                        ${field.expectedFieldDef.fieldType.unqualifiedToString}::class.java,")
            appendLine("                        this.lineNumber,")
            appendLine("                        this.fileStorageId")
            appendLine("                  ),")
            appendLine("                  $formatterToString,")
            appendLine("                  e")
            appendLine("              )")
            appendLine("           }")
        }

        blankLine()
        appendLine("        }")



        /*

            try {
                return rawValue.toInt()
            } catch (e: NumberFormatException) {
                throw StagedFieldNumberFormatException(StagedFieldMeta(
                    id,
                    rawValue,
                    FaaAircraftReferenceStagingEntityMeta.SCHEMA_AND_TABLE_NAME,
                    "cruising_speed_in_mph",
                    "SPEED",
                    FaaAircraftReferenceStagingEntityMeta.cruisingSpeedInMph,
                    Int::class.java,
                    this.lineNumber,
                    this.fileStorageId
                ))
            }



         */

    }


}
