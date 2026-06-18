package org.maiaframework.gen.spec.definition.lang

import org.maiaframework.gen.spec.definition.BooleanTypeDef
import org.maiaframework.gen.spec.definition.BooleanValueClassDef
import org.maiaframework.gen.spec.definition.DataClassDef
import org.maiaframework.gen.spec.definition.EntityPkAndNameDef
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.EsDocMappingType
import org.maiaframework.gen.spec.definition.EsDocMappingTypes
import org.maiaframework.gen.spec.definition.ForeignKeyFieldDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.HazelcastCompatibleType
import org.maiaframework.gen.spec.definition.IntTypeDef
import org.maiaframework.gen.spec.definition.IntValueClassDef
import org.maiaframework.gen.spec.definition.JoinFetchDtoDef
import org.maiaframework.gen.spec.definition.LongTypeDef
import org.maiaframework.gen.spec.definition.ReadonlyArrayTypescriptType
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.SimpleResponseDtoDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.StringValueClassDef
import org.maiaframework.gen.spec.definition.TypescriptCompatibleType
import org.maiaframework.gen.spec.definition.TypescriptCompatibleTypes
import org.maiaframework.jdbc.JdbcCompatibleType


sealed class FieldType(
    val fqcn: Fqcn,
    val typescriptCompatibleType: TypescriptCompatibleType?,
    val sqlType: String?,
    val elasticMappingType: EsDocMappingType?,
    val hazelcastCompatibleType: HazelcastCompatibleType?,
    parameters: List<FieldType> = emptyList(),
    val defaultFormFieldValue: String?
) {


    val uqcn = fqcn.uqcn


    val unqualifiedToString: String = if (parameters.isEmpty()) {
        fqcn.unqualifiedToString
    } else {
        fqcn.unqualifiedToString + parameters.map { it.unqualifiedToString }.joinToString(prefix = "<", separator = ", ", postfix = ">")
    }


    abstract val jdbcCompatibleType: JdbcCompatibleType


    override fun toString(): String {

        return unqualifiedToString

    }


    abstract fun unwrap(): FieldType


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FieldType

        return fqcn == other.fqcn
    }


    override fun hashCode(): Int {

        return fqcn.hashCode()

    }


}


class BooleanFieldType internal constructor() : FieldType(
    Fqcn.BOOLEAN,
    TypescriptCompatibleTypes.boolean,
    "BIT",
    EsDocMappingTypes.boolean,
    HazelcastCompatibleType.BOOLEAN,
    defaultFormFieldValue = "false"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.boolean


    override fun unwrap(): FieldType {
        return this
    }

}


class BooleanTypeFieldType internal constructor(
    val booleanTypeDef: BooleanTypeDef
) : FieldType(
    booleanTypeDef.fqcn,
    TypescriptCompatibleTypes.boolean,
    "BIT",
    EsDocMappingTypes.boolean,
    HazelcastCompatibleType.BOOLEAN,
    defaultFormFieldValue = "false"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.boolean

    override fun unwrap(): FieldType {
        return FieldTypes.boolean
    }


}


class IntFieldType internal constructor() : FieldType(
    Fqcn.INT,
    TypescriptCompatibleTypes.number,
    "INTEGER",
    EsDocMappingTypes.long,
    HazelcastCompatibleType.INT32,
    defaultFormFieldValue = "0"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.integer


    override fun unwrap(): FieldType {
        return this
    }


}


class IntTypeFieldType internal constructor(
    val intTypeDef: IntTypeDef
) : FieldType(
    intTypeDef.fqcn,
    TypescriptCompatibleTypes.number,
    "INTEGER",
    EsDocMappingTypes.long,
    HazelcastCompatibleType.INT32,
    defaultFormFieldValue = "0"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.integer


    override fun unwrap(): FieldType {
        return FieldTypes.int
    }


}


class DoubleFieldType internal constructor() : FieldType(
    Fqcn.DOUBLE,
    TypescriptCompatibleTypes.number,
    "NUMERIC",
    EsDocMappingTypes.double,
    HazelcastCompatibleType.FLOAT64,
    defaultFormFieldValue = "0.0"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.decimal


    override fun unwrap(): FieldType {
        return this
    }


}


class LongFieldType internal constructor() : FieldType(
    Fqcn.LONG,
    TypescriptCompatibleTypes.number,
    "BIGINT",
    EsDocMappingTypes.long,
    HazelcastCompatibleType.INT64,
    defaultFormFieldValue = "0"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.bigint


    override fun unwrap(): FieldType {
        return this
    }


}


class LongTypeFieldType internal constructor(
    val longTypeDef: LongTypeDef
) : FieldType(
    longTypeDef.fqcn,
    TypescriptCompatibleTypes.number,
    "BIGINT",
    EsDocMappingTypes.long,
    HazelcastCompatibleType.INT64,
    defaultFormFieldValue = "0"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.bigint


    override fun unwrap(): FieldType {
        return FieldTypes.long
    }


}


class UrlFieldType internal constructor() : FieldType(
    Fqcn.URL,
    TypescriptCompatibleTypes.string,
    "VARCHAR",
    EsDocMappingTypes.text,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "'https://example.com'"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.text


    override fun unwrap(): FieldType {
        return FieldTypes.string
    }


}


class StringFieldType internal constructor() : FieldType(
    Fqcn.STRING,
    TypescriptCompatibleTypes.string,
    "VARCHAR",
    EsDocMappingTypes.text,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.text


    override fun unwrap(): FieldType {
        return this
    }


}


class StringTypeFieldType(
    val stringTypeDef: StringTypeDef
) : FieldType(
    stringTypeDef.fqcn,
    TypescriptCompatibleTypes.string,
    "VARCHAR",
    EsDocMappingTypes.text,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.text


    override fun unwrap(): FieldType {
        return FieldTypes.string
    }


}


class DomainIdFieldType internal constructor() : FieldType(
    Fqcns.MAIA_DOMAIN_ID,
    TypescriptCompatibleTypes.string,
    "OTHER",
    EsDocMappingTypes.keyword,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.uuid


    override fun unwrap(): FieldType {
        return this
    }


}


class ObjectIdFieldType internal constructor() : FieldType(
    Fqcn.valueOf("org.bson.types.ObjectId"),
    TypescriptCompatibleTypes.string,
    null,
    EsDocMappingTypes.keyword,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.text


    override fun unwrap(): FieldType {
        return this
    }


}

class EnumFieldType(
    val enumDef: EnumDef,
) : FieldType(
    enumDef.fqcn,
    TypescriptCompatibleTypes.enum,
    "VARCHAR",
    EsDocMappingTypes.keyword,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = enumDef.defaultFormFieldValue
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.text


    override fun unwrap(): FieldType {
        return FieldTypes.string
    }


}


class EsDocFieldType(
    val esDocDef: EsDocDef,
) : FieldType(
    esDocDef.fqcn,
    TypescriptCompatibleTypes.string,
    null,
    EsDocMappingTypes.text,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "{}"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.jsonb


    override fun unwrap(): FieldType {
        return this
    }


}


class DataClassFieldType(
    val dataClassDef: DataClassDef,
) : FieldType(
    dataClassDef.fqcn,
    TypescriptCompatibleTypes.object_,
    null,
    EsDocMappingTypes.`object`,
    HazelcastCompatibleType.COMPACT,
    defaultFormFieldValue = "{}"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.jsonb


    override fun unwrap(): FieldType {
        return this
    }


}


class InstantFieldType internal constructor() : FieldType(
    Fqcn.INSTANT,
    TypescriptCompatibleTypes.string,
    "TIMESTAMP",
    EsDocMappingTypes.text,
    HazelcastCompatibleType.OFFSET_DATE_TIME,
    defaultFormFieldValue = "''"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.timestamp_with_time_zone


    override fun unwrap(): FieldType {
        return this
    }


}


class LocalDateFieldType internal constructor() : FieldType(
    Fqcn.LOCAL_DATE,
    TypescriptCompatibleTypes.string,
    "DATE",
    EsDocMappingTypes.date,
    HazelcastCompatibleType.LOCAL_DATE,
    defaultFormFieldValue = "''"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.date


    override fun unwrap(): FieldType {
        return this
    }


}


class PeriodFieldType internal constructor() : FieldType(
    Fqcn.PERIOD,
    TypescriptCompatibleTypes.string,
    "VARCHAR",
    EsDocMappingTypes.text,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.text


    override fun unwrap(): FieldType {
        return this
    }


}


class ListFieldType internal constructor(
    val parameterFieldType: FieldType
) : FieldType(
    Fqcn.LIST,
    typescriptCompatibleType = ReadonlyArrayTypescriptType(parameterFieldType),
    sqlType = null,
    elasticMappingType = null,
    hazelcastCompatibleType = null,
    parameters = listOf(parameterFieldType),
    defaultFormFieldValue = "[]"
) {


    override val jdbcCompatibleType: JdbcCompatibleType
        get() = jdbcCompatibleTypeForListOrSet(parameterFieldType)


    override fun unwrap(): FieldType {
        return this
    }


}


class SetFieldType internal constructor(
    val parameterFieldType: FieldType
) : FieldType(
    Fqcn.SET,
    typescriptCompatibleType = null,
    sqlType = null,
    elasticMappingType = null,
    hazelcastCompatibleType = null,
    parameters = listOf(parameterFieldType),
    defaultFormFieldValue = "[]"
) {
    override val jdbcCompatibleType: JdbcCompatibleType
        get() = jdbcCompatibleTypeForListOrSet(parameterFieldType)


    override fun unwrap(): FieldType {
        return this
    }


}


private fun jdbcCompatibleTypeForListOrSet(parameterFieldType: FieldType): JdbcCompatibleType {

    return when (parameterFieldType) {
        is BooleanFieldType -> JdbcCompatibleType.boolean_array
        is BooleanTypeFieldType -> JdbcCompatibleType.boolean_array
        is BooleanValueClassFieldType -> JdbcCompatibleType.boolean_array
        is DataClassFieldType -> JdbcCompatibleType.jsonb
        is DomainIdFieldType -> JdbcCompatibleType.uuid_array
        is DoubleFieldType -> JdbcCompatibleType.decimal_array
        is EnumFieldType -> JdbcCompatibleType.text_array
        is EsDocFieldType -> TODO("YAGNI?")
        is ForeignKeyFieldType -> TODO("YAGNI?")
        is FqcnFieldType -> TODO("YAGNI?")
        is JoinFetchDtoFieldType -> TODO("YAGNI?")
        is PkAndNameFieldType -> TODO("YAGNI?")
        is InstantFieldType -> TODO("YAGNI?")
        is IntFieldType -> JdbcCompatibleType.integer_array
        is IntTypeFieldType -> JdbcCompatibleType.integer_array
        is IntValueClassFieldType -> JdbcCompatibleType.integer_array
        is ListFieldType -> TODO("YAGNI?")
        is LocalDateFieldType -> TODO("YAGNI?")
        is LongFieldType -> JdbcCompatibleType.integer_array
        is LongTypeFieldType -> JdbcCompatibleType.integer_array
        is MapFieldType -> TODO("YAGNI?")
        is ObjectIdFieldType -> TODO("YAGNI?")
        is PeriodFieldType -> JdbcCompatibleType.text_array
        is RequestDtoFieldType -> TODO("YAGNI?")
        is SetFieldType -> TODO("YAGNI?")
        is SimpleResponseDtoFieldType -> TODO("YAGNI?")
        is StringFieldType -> JdbcCompatibleType.text_array
        is StringTypeFieldType -> JdbcCompatibleType.text_array
        is StringValueClassFieldType -> JdbcCompatibleType.text_array
        is UrlFieldType -> JdbcCompatibleType.text_array
    }

}


class MapFieldType internal constructor(
    val keyFieldType: FieldType,
    val valueFieldType: FieldType
) : FieldType(
    Fqcn.MAP,
    typescriptCompatibleType = TypescriptCompatibleTypes.record(keyFieldType, valueFieldType),
    sqlType = null,
    elasticMappingType = null,
    hazelcastCompatibleType = null,
    parameters = listOf(keyFieldType, valueFieldType),
    defaultFormFieldValue = "{}"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.jsonb


    override fun unwrap(): FieldType {
        return this
    }


}


class BooleanValueClassFieldType internal constructor(
    val valueClassDef: BooleanValueClassDef
) : FieldType(
    valueClassDef.fqcn,
    valueClassDef.underlyingFieldType.typescriptCompatibleType,
    valueClassDef.underlyingFieldType.sqlType,
    valueClassDef.underlyingFieldType.elasticMappingType,
    valueClassDef.underlyingFieldType.hazelcastCompatibleType,
    defaultFormFieldValue = "false"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.boolean


    override fun unwrap(): FieldType {
        return FieldTypes.boolean
    }


}


class IntValueClassFieldType internal constructor(
    val valueClassDef: IntValueClassDef
) : FieldType(
    valueClassDef.fqcn,
    valueClassDef.underlyingFieldType.typescriptCompatibleType,
    valueClassDef.underlyingFieldType.sqlType,
    valueClassDef.underlyingFieldType.elasticMappingType,
    valueClassDef.underlyingFieldType.hazelcastCompatibleType,
    defaultFormFieldValue = "0"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = valueClassDef.underlyingFieldType.jdbcCompatibleType


    override fun unwrap(): FieldType {
        return FieldTypes.int
    }


}


class StringValueClassFieldType internal constructor(
    val valueClassDef: StringValueClassDef
) : FieldType(
    valueClassDef.fqcn,
    valueClassDef.underlyingFieldType.typescriptCompatibleType,
    valueClassDef.underlyingFieldType.sqlType,
    valueClassDef.underlyingFieldType.elasticMappingType,
    valueClassDef.underlyingFieldType.hazelcastCompatibleType,
    defaultFormFieldValue = "''"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = valueClassDef.underlyingFieldType.jdbcCompatibleType


    override fun unwrap(): FieldType {
        return FieldTypes.string
    }


}


class ForeignKeyFieldType internal constructor(
    val foreignKeyFieldDef: ForeignKeyFieldDef
) : FieldType(
    foreignKeyFieldDef.foreignEntityDef.primaryKeyFields.first().classFieldDef.fieldType.fqcn,
    TypescriptCompatibleTypes.string,
    foreignKeyFieldDef.foreignEntityDef.primaryKeyFields.first().classFieldDef.fieldType.sqlType,
    EsDocMappingTypes.keyword,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {

    val pkFieldType: FieldType
        get() = foreignKeyFieldDef.foreignEntityDef.primaryKeyFields.first().classFieldDef.fieldType

    override val jdbcCompatibleType: JdbcCompatibleType
        get() = pkFieldType.jdbcCompatibleType

    override fun unwrap(): FieldType {
        return this
    }


}


class RequestDtoFieldType internal constructor(
    val requestDtoDef: RequestDtoDef
) : FieldType(
    requestDtoDef.fqcn,
    TypescriptCompatibleTypes.any,
    sqlType = null,
    EsDocMappingTypes.`object`,
    HazelcastCompatibleType.COMPACT,
    defaultFormFieldValue = """
        |{
        |${requestDtoDef.dtoFieldDefs.joinToString(",\n") { "            ${it.classFieldDef.classFieldName}: ${it.classFieldDef.defaultFormFieldValue}" }}
        |        }""".trimMargin()
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.jsonb


    override fun unwrap(): FieldType {
        return this
    }


}


class SimpleResponseDtoFieldType internal constructor(
    val responseDtoDef: SimpleResponseDtoDef
) : FieldType(
    responseDtoDef.fqcn,
    TypescriptCompatibleTypes.dto(responseDtoDef.dtoDef.fieldType),
    sqlType = "OTHER",
    EsDocMappingTypes.`object`,
    HazelcastCompatibleType.COMPACT,
    defaultFormFieldValue = """
        |{
        |${responseDtoDef.dtoDef.allFieldsSorted.joinToString(",\n") {"            ${it.classFieldName}: ${it.defaultFormFieldValue}" }}
        |        }""".trimMargin()
) {

    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.jsonb

    override fun unwrap(): FieldType {
        return this
    }


}


class PkAndNameFieldType internal constructor(
    val pkAndNameDef: EntityPkAndNameDef
) : FieldType(
    pkAndNameDef.dtoDef.fqcn,
    TypescriptCompatibleTypes.object_,
    sqlType = null,
    EsDocMappingTypes.`object`,
    HazelcastCompatibleType.COMPACT,
    defaultFormFieldValue = "{}"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.jsonb


    override fun unwrap(): FieldType {
        return this
    }


}


class FqcnFieldType internal constructor(
    fqcn: Fqcn,
    typescriptCompatibleType: TypescriptCompatibleType?,
    private val providedJdbcCompatibleType: JdbcCompatibleType?,
    sqlType: String?,
    elasticMappingType: EsDocMappingType?,
    hazelcastCompatibleType: HazelcastCompatibleType?
): FieldType(
    fqcn,
    typescriptCompatibleType,
    sqlType,
    elasticMappingType,
    hazelcastCompatibleType,
    defaultFormFieldValue = "'"
) {

    override val jdbcCompatibleType: JdbcCompatibleType
        get() = providedJdbcCompatibleType
            ?: throw IllegalStateException("FQCN field types must have a JDBC compatible type")


    override fun unwrap(): FieldType {
        return this
    }


}


class JoinFetchDtoFieldType(
    val joinFetchDtoDef: JoinFetchDtoDef
) : FieldType(
    joinFetchDtoDef.fqcn,
    TypescriptCompatibleTypes.object_,
    sqlType = null,
    EsDocMappingTypes.`object`,
    HazelcastCompatibleType.COMPACT,
    defaultFormFieldValue = "{}"
) {

    val typescriptImport = joinFetchDtoDef.typescriptImport


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.jsonb


    override fun unwrap(): FieldType {
        return this
    }


}
