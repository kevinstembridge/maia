package org.maiaframework.gen.renderers

import org.maiaframework.gen.persist.HazelcastCompatibleType
import org.maiaframework.gen.spec.definition.CacheableDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
import org.maiaframework.gen.spec.definition.lang.IdAndNameFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LocalDateFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.ObjectIdFieldType
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.UrlFieldType
import java.time.ZoneOffset


class HazelcastSerializerRenderer(
    private val cacheableDef: CacheableDef,
    private val serializedClassDef: ClassDef,
    serializerClassDef: ClassDef
) : AbstractKotlinRenderer(
    serializerClassDef
) {


    private val serializedClassUqcn = serializedClassDef.uqcn


    override fun renderFunctions() {

        `render function getTypeName`()
        `render function getCompactClass`()
        `render function read`()
        `render function write`()

    }


    private fun `render function getTypeName`() {

        appendLine("""
            |
            |
            |    override fun getTypeName(): String {
            |
            |        return "${this.cacheableDef.cacheName}"
            |
            |    }""".trimMargin()
        )

    }


    private fun `render function getCompactClass`() {

        appendLine("""
            |
            |
            |    override fun getCompactClass(): Class<${this.serializedClassUqcn}> {
            |
            |        return ${this.serializedClassUqcn}::class.java
            |
            |    }""".trimMargin()
        )

    }


    private fun `render function read`() {

        addImportFor(Fqcns.HAZELCAST_COMPACT_READER)

        blankLine()
        blankLine()
        appendLine("    override fun read(reader: CompactReader): $serializedClassUqcn {")
        blankLine()

        serializedClassDef.allFieldsSorted.forEach { field ->
            renderReadForField(field)
        }

        blankLine()
        appendLine("        return $serializedClassUqcn(")

        serializedClassDef.allFieldsSorted.forEach { field ->
            appendLine("            ${field.classFieldName},")
        }

        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


    private fun renderReadForField(field: ClassFieldDef) {

        val fieldType = field.fieldType

        when (fieldType) {
            is BooleanFieldType -> `render read for Boolean`(field)
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> `render read for domainId`(field)
            is DoubleFieldType -> TODO()
            is EnumFieldType -> `render read for Enum`(field, fieldType)
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> `render read for domainId`(field)
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> `render read for Instant`(field, fieldType)
            is IntFieldType -> `render read for Int`(field)
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> `render read for List`(field, fieldType)
            is LocalDateFieldType -> `render read for LocalDate`(field, fieldType)
            is LongFieldType -> `render read for Long`(field)
            is LongTypeFieldType -> TODO()
            is MapFieldType -> `render read for Map`(field, fieldType.keyFieldType, fieldType.valueFieldType)
            is ObjectIdFieldType -> `render read for ObjectId`(field)
            is PeriodFieldType -> TODO()
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> `render read for Set`(field, fieldType)
            is SimpleResponseDtoFieldType -> TODO()
            is StringFieldType -> `render read for String`(field)
            is StringTypeFieldType -> `render read for StringType`(field, fieldType)
            is StringValueClassFieldType -> `render read for String Value class`(field, fieldType)
            is UrlFieldType -> `render read for URL`(field)
        }

    }


    private fun `render read for String`(field: ClassFieldDef) {

        `render read for ordinary field`(field)

    }


    private fun `render read for URL`(field: ClassFieldDef) {

        `render read for ordinary field`(field)

    }


    private fun `render read for Int`(field: ClassFieldDef) {

        `render read for ordinary field`(field)

    }


    private fun `render read for Long`(field: ClassFieldDef) {

        `render read for ordinary field`(field)

    }


    private fun `render read for Boolean`(field: ClassFieldDef) {

        `render read for ordinary field`(field)

    }


    private fun `render read for Instant`(field: ClassFieldDef, fieldType: InstantFieldType) {

        `render read for ordinary field`(field)

    }


    private fun `render read for LocalDate`(field: ClassFieldDef, fieldType: LocalDateFieldType) {

        if (field.nullable) {
            addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_LOCAL_DATE_NULLABLE)
            appendLine("        val ${field.classFieldName} = reader.readNullableLocalDate(\"${field.classFieldName}\")")
        } else {
            appendLine("        val ${field.classFieldName} = reader.readLocalDate(\"${field.classFieldName}\")")
        }

    }


    private fun `render read for Set`(field: ClassFieldDef, fieldType: SetFieldType) {

        val keyFieldName = field.classFieldName.value
        val setElementFieldType = fieldType.parameterFieldType

        when (setElementFieldType) {
            is BooleanFieldType -> TODO()
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> `render read for Set of DomainIds`(keyFieldName, setElementFieldType)
            is DoubleFieldType -> TODO()
            is EnumFieldType -> `render read for set of enums`(keyFieldName, setElementFieldType)
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> TODO()
            is IntFieldType -> TODO()
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO("YAGNI?")
            is LocalDateFieldType -> TODO()
            is LongFieldType -> TODO()
            is LongTypeFieldType -> TODO()
            is MapFieldType -> TODO("YAGNI?")
            is ObjectIdFieldType -> `render read for Set of ObjectIds`(keyFieldName, setElementFieldType)
            is PeriodFieldType -> TODO()
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> `render read for Set of Strings`(keyFieldName)
            is StringTypeFieldType -> TODO()
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

    }


    private fun `render read for List`(field: ClassFieldDef, fieldType: ListFieldType) {

        when (fieldType.parameterFieldType) {
            is BooleanTypeFieldType -> TODO()
            is BooleanFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> {
                addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_LIST_OF_COMPACT)
                addImportFor(fieldType.parameterFieldType.fqcn)
                appendLine("        val ${field.classFieldName} = reader.readListOfCompact(\"${field.classFieldName}\", ${fieldType.parameterFieldType.unqualifiedToString}::class.java)")
            }
            is DomainIdFieldType -> TODO()
            is DoubleFieldType -> TODO()
            is EnumFieldType -> TODO()
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> TODO()
            is IntFieldType -> TODO()
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO()
            is LocalDateFieldType -> TODO()
            is LongFieldType -> TODO()
            is LongTypeFieldType -> TODO()
            is MapFieldType -> TODO()
            is ObjectIdFieldType -> TODO()
            is PeriodFieldType -> TODO()
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO()
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> TODO()
            is StringTypeFieldType -> TODO()
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

    }


    private fun `render read for StringType`(field: ClassFieldDef, fieldType: StringTypeFieldType) {

        addImportFor(fieldType)

        val readerFunction = getCompactReaderFunctionFor(fieldType, field.nullable)
        appendLine("        val ${field.classFieldName} = ${fieldType.fqcn.uqcn}(reader.${readerFunction}(\"${field.classFieldName}\"))")

    }


    private fun `render read for String Value class`(field: ClassFieldDef, fieldType: StringValueClassFieldType) {

        addImportFor(fieldType)

        val readerFunction = getCompactReaderFunctionFor(fieldType, field.nullable)

        if (field.nullable) {
            appendLine("        val ${field.classFieldName} = reader.$readerFunction(\"${field.classFieldName}\")?.let { ${fieldType.unqualifiedToString}(it) }")
        } else {
            appendLine("        val ${field.classFieldName} = ${fieldType.fqcn.uqcn}(reader.$readerFunction(\"${field.classFieldName}\"))")
        }

    }


    private fun `render read for URL`(field: ClassFieldDef, fieldType: UrlFieldType) {

        addImportFor(fieldType)

        val readerFunction = getCompactReaderFunctionFor(fieldType, field.nullable)
        appendLine("        val ${field.classFieldName} = ${fieldType.fqcn.uqcn}(reader.${readerFunction}(\"${field.classFieldName}\"))")

    }


    private fun `render read for Set of Strings`(fieldName: String) {

        addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_SET_OF_STRINGS)
        addImportFor(Fqcns.STRING)

        appendLine("        val $fieldName = reader.readSetOfStrings(\"$fieldName\")")

    }


    private fun `render read for List of Strings`(fieldName: String) {

        addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_LIST_OF_STRINGS)
        addImportFor(Fqcns.STRING)

        appendLine("        val $fieldName = reader.readListOfStrings(\"$fieldName\")")

    }


    private fun `render read for ordinary field`(field: ClassFieldDef) {
        val readerFunction = getCompactReaderFunctionFor(field.fieldType, field.nullable)
        appendLine("        val ${field.classFieldName} = reader.${readerFunction}(\"${field.classFieldName}\")")
    }


    private fun `render read for set of enums`(fieldName: String, enumFieldType: EnumFieldType) {

        addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_SET_FROM_STRINGS)
        addImportFor(enumFieldType)

        val enumUqcn = enumFieldType.fqcn.uqcn
        appendLine("        val $fieldName = reader.readSetFromStrings(\"${fieldName}\") { ${enumUqcn}.valueOf(it) }")

    }


    private fun `render read for Map`(
        field: ClassFieldDef,
        keyFieldType: FieldType,
        valueFieldType: FieldType
    ) {

        val keyFieldName = "${field.classFieldName}Keys"
        val valueFieldName = "${field.classFieldName}Values"

        when (keyFieldType) {
            is BooleanFieldType -> TODO("YAGNI?")
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> `render read for List of DomainIds`(keyFieldName, keyFieldType)
            is DoubleFieldType -> TODO()
            is EnumFieldType -> `render read for Set of Enums`(keyFieldName, keyFieldType)
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO("YAGNI?")
            is InstantFieldType -> TODO("YAGNI?")
            is IntFieldType -> TODO("YAGNI?")
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO("YAGNI?")
            is LocalDateFieldType -> TODO("YAGNI?")
            is LongFieldType -> TODO("YAGNI?")
            is LongTypeFieldType -> TODO()
            is MapFieldType -> TODO("YAGNI?")
            is ObjectIdFieldType -> `render read for Set of ObjectIds`(keyFieldName, keyFieldType)
            is PeriodFieldType -> TODO()
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> `render read for List of Strings`(keyFieldName)
            is StringTypeFieldType -> TODO()
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

        when (valueFieldType) {
            is BooleanFieldType -> TODO()
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> TODO()
            is DoubleFieldType -> TODO()
            is EnumFieldType -> {
                appendLine("            val values = reader.readListOfStrings(key.toString()) { ${valueFieldType.unqualifiedToString}.valueOf(it) }")
            }
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> TODO()
            is IntFieldType -> TODO()
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO()
            is LocalDateFieldType -> TODO()
            is LongFieldType -> TODO()
            is LongTypeFieldType -> TODO()
            is MapFieldType -> TODO()
            is ObjectIdFieldType -> TODO()
            is PeriodFieldType -> TODO()
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> {

                val setElementFieldType = valueFieldType.parameterFieldType

                when (setElementFieldType) {
                    is BooleanFieldType -> TODO()
                    is BooleanTypeFieldType -> TODO()
                    is BooleanValueClassFieldType -> TODO()
                    is DataClassFieldType -> TODO()
                    is DomainIdFieldType -> TODO()
                    is DoubleFieldType -> TODO()
                    is EnumFieldType -> {
                        addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_LIST_OF_SETS_FROM_CSV_STRINGS)
                        appendLine("        val ${field.classFieldName}Values = reader.readListOfSetsFromCsvStrings(\"${field.classFieldName}Values\") { ${setElementFieldType.unqualifiedToString}.valueOf(it) }")
                    }
                    is EsDocFieldType -> TODO()
                    is ForeignKeyFieldType -> TODO()
                    is FqcnFieldType -> TODO()
                    is IdAndNameFieldType -> TODO()
                    is InstantFieldType -> TODO()
                    is IntFieldType -> TODO()
                    is IntTypeFieldType -> TODO()
                    is IntValueClassFieldType -> TODO()
                    is ListFieldType -> TODO()
                    is LocalDateFieldType -> TODO()
                    is LongFieldType -> TODO()
                    is LongTypeFieldType -> TODO()
                    is MapFieldType -> TODO()
                    is ObjectIdFieldType -> TODO()
                    is PeriodFieldType -> TODO()
                    is RequestDtoFieldType -> TODO()
                    is SetFieldType -> TODO()
                    is SimpleResponseDtoFieldType -> TODO("YAGNI?")
                    is StringFieldType -> TODO()
                    is StringTypeFieldType -> TODO()
                    is StringValueClassFieldType -> TODO()
                    is UrlFieldType -> TODO()
                }

            }
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> {
                addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_LIST_OF_STRINGS)
                appendLine("        val $valueFieldName = reader.readListOfStrings(\"$valueFieldName\")")
            }
            is StringTypeFieldType -> TODO()
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

        appendLine("        val ${field.classFieldName} = ${field.classFieldName}Keys.zip(${field.classFieldName}Values).toMap()")

    }


    private fun `render read for Set of Enums`(
        keyFieldName: String,
        keyFieldType: FieldType
    ) {

        addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_SET_FROM_STRINGS)
        addImportFor(keyFieldType)

        appendLine("        val $keyFieldName = reader.readSetFromStrings(\"$keyFieldName\") { ${keyFieldType.fqcn.uqcn}.valueOf(it) }")

    }


    private fun `render read for Set of ObjectIds`(keyFieldName: String, keyFieldType: FieldType) {

        addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_SET_FROM_STRINGS)
        addImportFor(Fqcns.OBJECT_ID)

        appendLine("        val $keyFieldName = reader.readSetFromStrings(\"$keyFieldName\") { ${keyFieldType.unqualifiedToString}(it) }")

    }


    private fun `render read for Set of DomainIds`(keyFieldName: String, keyFieldType: FieldType) {

        addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_SET_FROM_STRINGS)
        addImportFor(Fqcns.MAHANA_DOMAIN_ID)

        appendLine("        val $keyFieldName = reader.readSetFromStrings(\"$keyFieldName\") { ${keyFieldType.unqualifiedToString}(it) }")

    }


    private fun `render read for List of DomainIds`(keyFieldName: String, keyFieldType: FieldType) {

        addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_LIST_OF_STRINGS)
        addImportFor(Fqcns.MAHANA_DOMAIN_ID)

        appendLine("        val $keyFieldName = reader.readListOfStrings(\"$keyFieldName\") { ${keyFieldType.unqualifiedToString}(it) }")

    }


    private fun `render read for domainId`(field: ClassFieldDef) {

        addImportFor(field.fieldType)

        if (field.nullable) {
            appendLine("        val ${field.classFieldName} = ${field.unqualifiedToString}(reader.readNullableString(\"${field.classFieldName}\"))")
            appendLine("        val ${field.classFieldName} = reader.readNullableString(\"${field.classFieldName}\")?.let { ${field.unqualifiedToString}(it) })")
        } else {
            addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_STRING_NON_NULL)
            appendLine("        val ${field.classFieldName} = ${field.unqualifiedToString}(reader.readStringNonNull(\"${field.classFieldName}\"))")
        }
    }


    private fun `render read for Enum`(field: ClassFieldDef, enumFieldType: EnumFieldType) {

        addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_SET_FROM_STRINGS)
        addImportFor(enumFieldType)

        val fieldName = field.classFieldName
        val enumUqcn = enumFieldType.fqcn.uqcn

        if (field.nullable) {
            appendLine("        val $fieldName = reader.readNullableString(\"$fieldName\")?.let { ${enumUqcn}.valueOf(it) }")
        } else {
            appendLine("        val $fieldName = reader.readStringNonNull(\"$fieldName\") { ${enumUqcn}.valueOf(it) }")
        }

    }


    private fun `render read for ObjectId`(field: ClassFieldDef) {

        addImportFor(field.fieldType)

        if (field.nullable) {
            appendLine("        val ${field.classFieldName} = ${field.unqualifiedToString}(reader.readNullableString(\"${field.classFieldName}\"))")
            appendLine("        val ${field.classFieldName} = reader.readNullableString(\"${field.classFieldName}\")?.let { ${field.unqualifiedToString}(it) })")
        } else {
            addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_STRING_NON_NULL)
            appendLine("        val ${field.classFieldName} = ${field.unqualifiedToString}(reader.readStringNonNull(\"${field.classFieldName}\"))")
        }

    }


    private fun getCompactReaderFunctionFor(fieldType: FieldType, nullable: Boolean): String {

        val prefix = if (nullable) "readNullable" else "read"
        val suffix = if (nullable) "" else "NonNull"

        if (nullable) {
            when (fieldType.hazelcastCompatibleType) {
                HazelcastCompatibleType.COMPACT -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_COMPACT_NON_NULL)
                HazelcastCompatibleType.INT8 -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_INT8_NON_NULL)
                HazelcastCompatibleType.INT32 -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_INT32_NON_NULL)
                HazelcastCompatibleType.INT64 -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_INT64_NON_NULL)
                HazelcastCompatibleType.FLOAT32 -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_FLOAT32_NON_NULL)
                HazelcastCompatibleType.FLOAT64 -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_FLOAT64_NON_NULL)
                HazelcastCompatibleType.LOCAL_DATE -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_LOCAL_DATE_NULLABLE)
                HazelcastCompatibleType.OFFSET_DATE_TIME -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_OFFSET_DATE_TIME_NON_NULL)
                HazelcastCompatibleType.STRING -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_NULLABLE_STRING)
                else -> {} // do nothing
            }
        } else {
            when (fieldType.hazelcastCompatibleType) {
                HazelcastCompatibleType.COMPACT -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_COMPACT_NON_NULL)
                HazelcastCompatibleType.INT8 -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_INT8_NON_NULL)
                HazelcastCompatibleType.INT32 -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_INT32_NON_NULL)
                HazelcastCompatibleType.INT64 -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_INT64_NON_NULL)
                HazelcastCompatibleType.FLOAT32 -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_FLOAT32_NON_NULL)
                HazelcastCompatibleType.FLOAT64 -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_FLOAT64_NON_NULL)
                HazelcastCompatibleType.LOCAL_DATE -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_LOCAL_DATE_NON_NULL)
                HazelcastCompatibleType.OFFSET_DATE_TIME -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_INSTANT_NON_NULL)
                HazelcastCompatibleType.STRING -> addImportFor(Fqcns.MAHANA_COMPACT_READER_EXTENSION_READ_STRING_NON_NULL)
                else -> {} // do nothing
            }
        }

        return when (fieldType.hazelcastCompatibleType) {
            HazelcastCompatibleType.BOOLEAN -> "${prefix}Boolean"
            HazelcastCompatibleType.COMPACT -> "${prefix}Compact$suffix"
            HazelcastCompatibleType.INT8 -> "${prefix}Int8$suffix"
            HazelcastCompatibleType.INT32 -> "${prefix}Int32$suffix"
            HazelcastCompatibleType.INT64 -> "${prefix}Int64$suffix"
            HazelcastCompatibleType.FLOAT32 -> "${prefix}Float32$suffix"
            HazelcastCompatibleType.FLOAT64 -> "${prefix}Float64$suffix"
            HazelcastCompatibleType.LOCAL_DATE -> "${prefix}Date$suffix"
            HazelcastCompatibleType.OFFSET_DATE_TIME -> "${prefix}Instant$suffix"
            HazelcastCompatibleType.STRING -> "${prefix}String$suffix"
            null -> throw RuntimeException("Not expecting HazelcastCompatibleType to be null for fieldType $fieldType")
        }

    }


    private fun `render function write`() {

        addImportFor(Fqcns.HAZELCAST_COMPACT_WRITER)

        blankLine()
        blankLine()
        appendLine("    override fun write(writer: CompactWriter, dto: $serializedClassUqcn) {")
        blankLine()
        appendLine("        writer.apply {")
        blankLine()

        serializedClassDef.allFieldsSorted.forEach { field ->
            renderWriteForField(field)
        }

        blankLine()
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


    private fun renderWriteForField(field: ClassFieldDef) {

        val fieldType = field.fieldType

        when (fieldType) {
            is BooleanFieldType -> `render write for`(field)
            is BooleanTypeFieldType -> `render write for BooleanType`(field)
            is BooleanValueClassFieldType -> `render write for Boolean Value class`(field)
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> `render write for DomainId`(field)
            is DoubleFieldType -> TODO()
            is EnumFieldType -> `render write for Enum`(field)
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> `render write for DomainId`(field)
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> `render write for Instant`(field)
            is IntFieldType -> `render write for`(field)
            is IntTypeFieldType -> `render write for IntType`(field)
            is IntValueClassFieldType -> `render write for Int Value class`(field)
            is ListFieldType -> `render write for List`(field, fieldType)
            is LocalDateFieldType -> `render write for LocalDate`(field)
            is LongFieldType -> `render write for`(field)
            is LongTypeFieldType -> TODO()
            is MapFieldType -> `render write for Map`(field, field.classFieldName, fieldType)
            is ObjectIdFieldType -> appendLine("            writeString(\"${field.classFieldName}\", dto.${field.classFieldName}.toHexString())")
            is PeriodFieldType -> TODO()
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> `render write for Set`(field, fieldType)
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> `render write for`(field)
            is StringTypeFieldType -> `render write for StringType`(field)
            is StringValueClassFieldType -> `render write for String Value class`(field)
            is UrlFieldType -> `render write for`(field)
        }

    }


    private fun `render write for StringType`(field: ClassFieldDef) {

        `render write for`(field, ".value")

    }


    private fun `render write for BooleanType`(field: ClassFieldDef) {

        `render write for`(field, ".value")

    }


    private fun `render write for IntType`(field: ClassFieldDef) {

        `render write for`(field, ".value")

    }


    private fun `render write for Boolean Value class`(field: ClassFieldDef) {

        `render write for`(field, ".value")

    }


    private fun `render write for Int Value class`(field: ClassFieldDef) {

        `render write for`(field, ".value")

    }


    private fun `render write for String Value class`(field: ClassFieldDef) {

        `render write for`(field, ".value")

    }


    private fun `render write for DomainId`(field: ClassFieldDef) {

        `render write for`(field, ".value")

    }


    private fun `render write for Enum`(field: ClassFieldDef) {

        `render write for`(field, ".name")

    }


    private fun `render write for`(field: ClassFieldDef, suffix: String? = null) {

        val writerFunction = getCompactWriterFunctionFor(field.fieldType, field.nullable)
        val nullSafeOperator = if (field.nullable) "?" else ""

        val suffixToUse = if (suffix.isNullOrBlank()) {
            ""
        } else {
            "$nullSafeOperator$suffix"
        }

        appendLine("            $writerFunction(\"${field.classFieldName}\", dto.${field.classFieldName}$suffixToUse)")

    }


    private fun `render write for Instant`(field: ClassFieldDef) {

        addImportFor(ZoneOffset::class.java)

        val writerFunction = getCompactWriterFunctionFor(field.fieldType, field.nullable)
        appendLine("            $writerFunction(\"${field.classFieldName}\", dto.${field.classFieldName}.atOffset(ZoneOffset.UTC))")

    }


    private fun `render write for LocalDate`(field: ClassFieldDef) {

        val writerFunction = getCompactWriterFunctionFor(field.fieldType, field.nullable)
        appendLine("            $writerFunction(\"${field.classFieldName}\", dto.${field.classFieldName})")

    }


    private fun `render write for List`(field: ClassFieldDef, fieldType: ListFieldType) {

        val listElementFieldType = fieldType.parameterFieldType

        when (listElementFieldType) {
            is BooleanFieldType -> TODO()
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> appendLine("            writeArrayOfCompact(\"${field.classFieldName}\", dto.${field.classFieldName}.toTypedArray())")
            is DomainIdFieldType -> TODO()
            is DoubleFieldType -> TODO()
            is EnumFieldType -> TODO()
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> TODO()
            is IntFieldType -> TODO()
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO()
            is LocalDateFieldType -> TODO()
            is LongFieldType -> TODO()
            is LongTypeFieldType -> TODO()
            is MapFieldType -> TODO()
            is ObjectIdFieldType -> TODO()
            is PeriodFieldType -> TODO()
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO()
            is SimpleResponseDtoFieldType -> TODO()
            is StringFieldType -> TODO()
            is StringTypeFieldType -> TODO()
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

    }


    private fun `render write for Map`(
        field: ClassFieldDef,
        classFieldName: ClassFieldName,
        mapFieldType: MapFieldType
    ) {

        val keyFieldName = "${field.classFieldName}Keys"
        val valueFieldName = "${field.classFieldName}Values"
        val mapEntryKeyType = mapFieldType.keyFieldType
        val mapEntryValueType = mapFieldType.valueFieldType

        when (mapEntryKeyType) {
            is BooleanFieldType -> TODO("YAGNI?")
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> `render write for DomainId keySet`(keyFieldName, classFieldName)
            is DoubleFieldType -> TODO()
            is EnumFieldType -> TODO("YAGNI?")
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO("YAGNI?")
            is InstantFieldType -> TODO("YAGNI?")
            is IntFieldType -> TODO("YAGNI?")
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO("YAGNI?")
            is LocalDateFieldType -> TODO("YAGNI?")
            is LongFieldType -> TODO("YAGNI?")
            is LongTypeFieldType -> TODO()
            is MapFieldType -> TODO("YAGNI?")
            is ObjectIdFieldType -> `render write for Set of ObjectIds`(keyFieldName, classFieldName)
            is PeriodFieldType -> TODO()
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> {
                appendLine("            val $keyFieldName = mutableListOf<${mapEntryKeyType.uqcn}>()")
            }
            is StringTypeFieldType -> TODO()
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

        when (mapEntryValueType) {
            is BooleanFieldType -> TODO()
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> TODO()
            is DoubleFieldType -> TODO()
            is EnumFieldType -> TODO()
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> TODO()
            is IntFieldType -> TODO()
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO()
            is LocalDateFieldType -> TODO()
            is LongFieldType -> TODO()
            is LongTypeFieldType -> TODO()
            is MapFieldType -> TODO()
            is ObjectIdFieldType -> TODO()
            is PeriodFieldType -> TODO()
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> `render write for Map entry Set`(field, mapEntryValueType)
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> {
                appendLine("            val $valueFieldName = mutableListOf<${mapEntryValueType.uqcn}>()")
            }
            is StringTypeFieldType -> TODO()
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

        appendLine("            dto.${field.classFieldName}.forEach { (key, value) ->")
        appendLine("                $keyFieldName.add(key)")
        appendLine("                $valueFieldName.add(value)")
        appendLine("            }")
        addImportFor(Fqcns.MAHANA_COMPACT_WRITER_EXTENSION_WRITE_LIST_OF_STRINGS)
        appendLine("            writeListOfStrings(\"$keyFieldName\", $keyFieldName)")
        appendLine("            writeListOfStrings(\"$valueFieldName\", $valueFieldName)")

    }


    private fun `render write for Map entry Set`(classFieldDef: ClassFieldDef, mapEntryFieldType: FieldType) {

        when (mapEntryFieldType) {
            is BooleanFieldType -> TODO()
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> TODO()
            is DoubleFieldType -> TODO()
            is EnumFieldType -> TODO()
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> TODO()
            is IntFieldType -> TODO()
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO()
            is LocalDateFieldType -> TODO()
            is LongFieldType -> TODO()
            is LongTypeFieldType -> TODO()
            is MapFieldType -> TODO()
            is ObjectIdFieldType -> TODO()
            is PeriodFieldType -> TODO()
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> {
                when (mapEntryFieldType.parameterFieldType) {
                    is BooleanFieldType -> TODO()
                    is BooleanTypeFieldType -> TODO()
                    is BooleanValueClassFieldType -> TODO()
                    is DataClassFieldType -> TODO()
                    is DomainIdFieldType -> TODO()
                    is DoubleFieldType -> TODO()
                    is EnumFieldType -> {
                        addImportFor(Fqcns.MAHANA_COMPACT_WRITER_EXTENSION_WRITE_SET_OF_STRINGS_AS_CSV)
                        appendLine("            writer.writeSetOfStringsAsCsv(\"${classFieldDef.classFieldName}Values\", dto.${classFieldDef.classFieldName}.values) { it.name }")
                    }
                    is EsDocFieldType -> TODO()
                    is ForeignKeyFieldType -> TODO()
                    is FqcnFieldType -> TODO()
                    is IdAndNameFieldType -> TODO()
                    is InstantFieldType -> TODO()
                    is IntFieldType -> TODO()
                    is IntTypeFieldType -> TODO()
                    is IntValueClassFieldType -> TODO()
                    is ListFieldType -> TODO()
                    is LocalDateFieldType -> TODO()
                    is LongFieldType -> TODO()
                    is LongTypeFieldType -> TODO()
                    is MapFieldType -> TODO()
                    is ObjectIdFieldType -> TODO()
                    is PeriodFieldType -> TODO()
                    is RequestDtoFieldType -> TODO()
                    is SetFieldType -> TODO()
                    is SimpleResponseDtoFieldType -> TODO("YAGNI?")
                    is StringFieldType -> TODO()
                    is StringTypeFieldType -> TODO()
                    is StringValueClassFieldType -> TODO()
                    is UrlFieldType -> TODO()
                }
            }
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> TODO()
            is StringTypeFieldType -> TODO()
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

    }


    private fun `render write for Set of DomainIds`(keyFieldName: String, fieldName: ClassFieldName) {
        addImportFor(Fqcns.MAHANA_COMPACT_WRITER_EXTENSION_WRITE_SET_OF_STRINGS)
        appendLine("            writeSetOfStrings(\"$keyFieldName\", dto.${fieldName}.keys.map { it.value }.toSet())")
    }


    private fun `render write for DomainId keySet`(keyFieldName: String, fieldName: ClassFieldName) {
        addImportFor(Fqcns.MAHANA_COMPACT_WRITER_EXTENSION_WRITE_SET_OF_STRINGS)

        appendLine("            val $keyFieldName = mutableListOf<String>()")
        appendLine("            writeSetOfStrings(\"$keyFieldName\", dto.${fieldName}.keys.map { it.value }.toSet())")
    }


    private fun `render write for Set of ObjectIds`(setFieldName: String, classFieldName: ClassFieldName) {

        addImportFor(Fqcns.MAHANA_COMPACT_WRITER_EXTENSION_WRITE_SET_OF_STRINGS)
        appendLine("            writeSetOfStrings(\"$setFieldName\", dto.${classFieldName}.keys.map { it.toHexString() }.toSet())")

    }


    private fun `render write for Set`(field: ClassFieldDef, setFieldType: SetFieldType) {

        val setElementFieldType = setFieldType.parameterFieldType

        when (setElementFieldType) {
            is BooleanFieldType -> TODO()
            is BooleanTypeFieldType -> TODO()
            is BooleanValueClassFieldType -> TODO()
            is DataClassFieldType -> TODO()
            is DomainIdFieldType -> TODO()
            is DoubleFieldType -> TODO()
            is EnumFieldType -> `render write for Set of Enums`(field.classFieldName.value)
            is EsDocFieldType -> TODO()
            is ForeignKeyFieldType -> TODO()
            is FqcnFieldType -> TODO()
            is IdAndNameFieldType -> TODO()
            is InstantFieldType -> TODO()
            is IntFieldType -> TODO()
            is IntTypeFieldType -> TODO()
            is IntValueClassFieldType -> TODO()
            is ListFieldType -> TODO()
            is LocalDateFieldType -> TODO()
            is LongFieldType -> TODO()
            is LongTypeFieldType -> TODO()
            is MapFieldType -> TODO()
            is ObjectIdFieldType -> TODO()
            is PeriodFieldType -> TODO()
            is RequestDtoFieldType -> TODO()
            is SetFieldType -> TODO()
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> TODO()
            is StringTypeFieldType -> TODO()
            is StringValueClassFieldType -> TODO()
            is UrlFieldType -> TODO()
        }

    }


    private fun `render write for Set of Enums`(fieldName: String) {

        addImportFor(Fqcns.MAHANA_COMPACT_WRITER_EXTENSION_WRITE_SET_OF_STRINGS)
        appendLine("            writeSetOfStrings(\"$fieldName\", dto.${fieldName}.map { it.name }.toSet())")

    }


    private fun getCompactWriterFunctionFor(fieldType: FieldType, nullable: Boolean): String {

        val prefix = if (nullable) "writeNullable" else "write"

        return when (fieldType.hazelcastCompatibleType) {
            HazelcastCompatibleType.BOOLEAN -> "writeBoolean"
            HazelcastCompatibleType.COMPACT -> "writeCompact"
            HazelcastCompatibleType.INT8 -> "${prefix}Int8"
            HazelcastCompatibleType.INT32 -> "${prefix}Int32"
            HazelcastCompatibleType.INT64 -> "${prefix}Int64"
            HazelcastCompatibleType.FLOAT32 -> "${prefix}Float32"
            HazelcastCompatibleType.FLOAT64 -> "${prefix}Float64"
            HazelcastCompatibleType.LOCAL_DATE -> {

                if (nullable) {
                    addImportFor(Fqcns.MAHANA_COMPACT_WRITER_EXTENSION_WRITE_NULLABLE_DATE)
                }

                "${prefix}Date"
            }
            HazelcastCompatibleType.OFFSET_DATE_TIME -> "writeTimestampWithTimezone"
            HazelcastCompatibleType.STRING -> "writeString"
            null -> throw RuntimeException("Not expecting HazelcastCompatibleType to be null for fieldType $fieldType")
        }

    }


}
