package org.maiaframework.gen.spec.definition.lang

import org.maiaframework.gen.persist.BsonCompatibleType
import org.maiaframework.gen.spec.definition.HazelcastCompatibleType
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
import org.maiaframework.gen.spec.definition.IntTypeDef
import org.maiaframework.gen.spec.definition.IntValueClassDef
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
    val bsonCompatibleType: BsonCompatibleType?,
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
    BsonCompatibleType.BOOLEAN,
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
    BsonCompatibleType.BOOLEAN,
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
    BsonCompatibleType.INT,
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
    BsonCompatibleType.INT,
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
    BsonCompatibleType.DOUBLE,
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
    BsonCompatibleType.LONG,
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
    BsonCompatibleType.LONG,
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
    BsonCompatibleType.STRING,
    TypescriptCompatibleTypes.string,
    "VARCHAR",
    EsDocMappingTypes.text,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "https://example.com"
) {


    override val jdbcCompatibleType: JdbcCompatibleType = JdbcCompatibleType.text


    override fun unwrap(): FieldType {
        return FieldTypes.string
    }


}


class StringFieldType internal constructor() : FieldType(
    Fqcn.STRING,
    BsonCompatibleType.STRING,
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
    BsonCompatibleType.STRING,
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
    BsonCompatibleType.DOMAIN_ID,
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
    BsonCompatibleType.OBJECT_ID,
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
    BsonCompatibleType.STRING,
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
    BsonCompatibleType.STRING,
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
    BsonCompatibleType.DOCUMENT,
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
    BsonCompatibleType.INSTANT,
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
    BsonCompatibleType.LOCAL_DATE,
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
    BsonCompatibleType.PERIOD,
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
    parameterFieldType.bsonCompatibleType,
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
        is IdAndNameFieldType -> TODO("YAGNI?")
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
        is PkAndNameListFieldType -> throw IllegalStateException("PkAndNameListFieldType cannot be used as a list element type")
        is RequestDtoFieldType -> TODO("YAGNI?")
        is SetFieldType -> TODO("YAGNI?")
        is SimpleResponseDtoFieldType -> TODO("YAGNI?")
        is StringFieldType -> JdbcCompatibleType.text_array
        is StringTypeFieldType -> JdbcCompatibleType.text_array
        is StringValueClassFieldType -> JdbcCompatibleType.text_array
        is UrlFieldType -> JdbcCompatibleType.text_array
    }

}


class SetFieldType internal constructor(
    val parameterFieldType: FieldType
) : FieldType(
    Fqcn.SET,
    parameterFieldType.bsonCompatibleType,
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


class MapFieldType internal constructor(
    val keyFieldType: FieldType,
    val valueFieldType: FieldType
) : FieldType(
    Fqcn.MAP,
    BsonCompatibleType.DOCUMENT,
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
    valueClassDef.underlyingFieldType.bsonCompatibleType,
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
    valueClassDef.underlyingFieldType.bsonCompatibleType,
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
    valueClassDef.underlyingFieldType.bsonCompatibleType,
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
    Fqcns.MAIA_DOMAIN_ID,
    BsonCompatibleType.DOMAIN_ID,
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


class RequestDtoFieldType internal constructor(
    val requestDtoDef: RequestDtoDef
) : FieldType(
    requestDtoDef.fqcn,
    BsonCompatibleType.DOCUMENT,
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
    BsonCompatibleType.DOCUMENT,
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


class IdAndNameFieldType internal constructor(
    val idAndNameDef: EntityPkAndNameDef
) : FieldType(
    idAndNameDef.dtoDef.fqcn,
    BsonCompatibleType.DOCUMENT,
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
    bsonCompatibleType: BsonCompatibleType?,
    typescriptCompatibleType: TypescriptCompatibleType?,
    private val providedJdbcCompatibleType: JdbcCompatibleType?,
    sqlType: String?,
    elasticMappingType: EsDocMappingType?,
    hazelcastCompatibleType: HazelcastCompatibleType?
): FieldType(
    fqcn,
    bsonCompatibleType,
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


class PkAndNameListFieldType(
    val entityPkAndNameDef: EntityPkAndNameDef
) : FieldType(
    fqcn = Fqcn.LIST,
    bsonCompatibleType = null,
    typescriptCompatibleType = null,
    sqlType = null,
    elasticMappingType = null,
    hazelcastCompatibleType = null,
    parameters = listOf(FieldTypes.byFqcn(entityPkAndNameDef.pkAndNameDtoFqcn)),
    defaultFormFieldValue = "[]"
) {

    override val jdbcCompatibleType: JdbcCompatibleType
        get() = throw IllegalStateException("PkAndNameListFieldType has no JDBC type")

    override fun unwrap(): FieldType = this

}
