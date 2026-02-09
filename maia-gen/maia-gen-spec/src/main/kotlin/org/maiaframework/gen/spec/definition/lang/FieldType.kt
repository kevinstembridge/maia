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
    val jdbcCompatibleType: JdbcCompatibleType?,
    val sqlType: String?,
    val elasticMappingType: EsDocMappingType?,
    val hazelcastCompatibleType: HazelcastCompatibleType?,
    parameters: List<FieldType> = emptyList(),
    val defaultFormFieldValue: String
) {


    val uqcn = fqcn.uqcn


    val unqualifiedToString: String = if (parameters.isEmpty()) {
        fqcn.unqualifiedToString
    } else {
        fqcn.unqualifiedToString + parameters.map { it.unqualifiedToString }.joinToString(prefix = "<", separator = ", ", postfix = ">")
    }


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
    JdbcCompatibleType.boolean,
    "BIT",
    EsDocMappingTypes.boolean,
    HazelcastCompatibleType.BOOLEAN,
    defaultFormFieldValue = "false"
) {

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
    JdbcCompatibleType.boolean,
    "BIT",
    EsDocMappingTypes.boolean,
    HazelcastCompatibleType.BOOLEAN,
    defaultFormFieldValue = "false"
) {


    override fun unwrap(): FieldType {
        return FieldTypes.boolean
    }


}


class IntFieldType internal constructor() : FieldType(
    Fqcn.INT,
    BsonCompatibleType.INT,
    TypescriptCompatibleTypes.number,
    JdbcCompatibleType.integer,
    "INTEGER",
    EsDocMappingTypes.long,
    HazelcastCompatibleType.INT32,
    defaultFormFieldValue = "0"
) {


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
    JdbcCompatibleType.integer,
    "INTEGER",
    EsDocMappingTypes.long,
    HazelcastCompatibleType.INT32,
    defaultFormFieldValue = "0"
) {


    override fun unwrap(): FieldType {
        return FieldTypes.int
    }


}


class DoubleFieldType internal constructor() : FieldType(
    Fqcn.DOUBLE,
    BsonCompatibleType.DOUBLE,
    TypescriptCompatibleTypes.number,
    JdbcCompatibleType.decimal,
    "NUMERIC",
    EsDocMappingTypes.double,
    HazelcastCompatibleType.FLOAT64,
    defaultFormFieldValue = "0.0"
) {


    override fun unwrap(): FieldType {
        return this
    }


}


class LongFieldType internal constructor() : FieldType(
    Fqcn.LONG,
    BsonCompatibleType.LONG,
    TypescriptCompatibleTypes.number,
    JdbcCompatibleType.bigint,
    "BIGINT",
    EsDocMappingTypes.long,
    HazelcastCompatibleType.INT64,
    defaultFormFieldValue = "0"
) {

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
    JdbcCompatibleType.bigint,
    "BIGINT",
    EsDocMappingTypes.long,
    HazelcastCompatibleType.INT64,
    defaultFormFieldValue = "0"
) {


    override fun unwrap(): FieldType {
        return FieldTypes.long
    }


}


class UrlFieldType internal constructor() : FieldType(
    Fqcn.URL,
    BsonCompatibleType.STRING,
    TypescriptCompatibleTypes.string,
    JdbcCompatibleType.text,
    "VARCHAR",
    EsDocMappingTypes.text,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "https://example.com"
) {


    override fun unwrap(): FieldType {
        return FieldTypes.string
    }


}


class StringFieldType internal constructor() : FieldType(
    Fqcn.STRING,
    BsonCompatibleType.STRING,
    TypescriptCompatibleTypes.string,
    JdbcCompatibleType.text,
    "VARCHAR",
    EsDocMappingTypes.text,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


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
    JdbcCompatibleType.text,
    "VARCHAR",
    EsDocMappingTypes.text,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


    override fun unwrap(): FieldType {
        return FieldTypes.string
    }


}


class DomainIdFieldType internal constructor() : FieldType(
    Fqcns.MAIA_DOMAIN_ID,
    BsonCompatibleType.DOMAIN_ID,
    TypescriptCompatibleTypes.string,
    JdbcCompatibleType.uuid,
    "OTHER",
    EsDocMappingTypes.keyword,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


    override fun unwrap(): FieldType {
        return this
    }


}


class ObjectIdFieldType internal constructor() : FieldType(
    Fqcn.valueOf("org.bson.types.ObjectId"),
    BsonCompatibleType.OBJECT_ID,
    TypescriptCompatibleTypes.string,
    JdbcCompatibleType.text,
    null,
    EsDocMappingTypes.keyword,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


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
    JdbcCompatibleType.text,
    "VARCHAR",
    EsDocMappingTypes.keyword,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


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
    JdbcCompatibleType.jsonb,
    null,
    EsDocMappingTypes.text,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "{}"
) {


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
    JdbcCompatibleType.jsonb,
    null,
    EsDocMappingTypes.`object`,
    HazelcastCompatibleType.COMPACT,
    defaultFormFieldValue = "{}"
) {


    override fun unwrap(): FieldType {
        return this
    }


}


class InstantFieldType internal constructor() : FieldType(
    Fqcn.INSTANT,
    BsonCompatibleType.INSTANT,
    TypescriptCompatibleTypes.string,
    JdbcCompatibleType.timestamp_with_time_zone,
    "TIMESTAMP",
    EsDocMappingTypes.text,
    HazelcastCompatibleType.OFFSET_DATE_TIME,
    defaultFormFieldValue = "''"
) {


    override fun unwrap(): FieldType {
        return this
    }


}


class LocalDateFieldType internal constructor() : FieldType(
    Fqcn.LOCAL_DATE,
    BsonCompatibleType.LOCAL_DATE,
    TypescriptCompatibleTypes.string,
    JdbcCompatibleType.date,
    "DATE",
    EsDocMappingTypes.date,
    HazelcastCompatibleType.LOCAL_DATE,
    defaultFormFieldValue = "''"
) {


    override fun unwrap(): FieldType {
        return this
    }


}


class PeriodFieldType internal constructor() : FieldType(
    Fqcn.PERIOD,
    BsonCompatibleType.PERIOD,
    TypescriptCompatibleTypes.string,
    JdbcCompatibleType.text,
    "VARCHAR",
    EsDocMappingTypes.text,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


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
    jdbcCompatibleType = parameterFieldType.jdbcCompatibleType,
    sqlType = null,
    elasticMappingType = null,
    hazelcastCompatibleType = null,
    parameters = listOf(parameterFieldType),
    defaultFormFieldValue = "[]"
) {


    override fun unwrap(): FieldType {
        return this
    }


}


class SetFieldType internal constructor(
    val parameterFieldType: FieldType
) : FieldType(
    Fqcn.SET,
    parameterFieldType.bsonCompatibleType,
    typescriptCompatibleType = null,
    jdbcCompatibleType = JdbcCompatibleType.jsonb,
    sqlType = null,
    elasticMappingType = null,
    hazelcastCompatibleType = null,
    parameters = listOf(parameterFieldType),
    defaultFormFieldValue = "[]"
) {


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
    jdbcCompatibleType = JdbcCompatibleType.jsonb,
    sqlType = null,
    elasticMappingType = null,
    hazelcastCompatibleType = null,
    parameters = listOf(keyFieldType, valueFieldType),
    defaultFormFieldValue = "{}"
) {


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
    valueClassDef.underlyingFieldType.jdbcCompatibleType,
    valueClassDef.underlyingFieldType.sqlType,
    valueClassDef.underlyingFieldType.elasticMappingType,
    valueClassDef.underlyingFieldType.hazelcastCompatibleType,
    defaultFormFieldValue = "false"
) {


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
    valueClassDef.underlyingFieldType.jdbcCompatibleType,
    valueClassDef.underlyingFieldType.sqlType,
    valueClassDef.underlyingFieldType.elasticMappingType,
    valueClassDef.underlyingFieldType.hazelcastCompatibleType,
    defaultFormFieldValue = "0"
) {


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
    valueClassDef.underlyingFieldType.jdbcCompatibleType,
    valueClassDef.underlyingFieldType.sqlType,
    valueClassDef.underlyingFieldType.elasticMappingType,
    valueClassDef.underlyingFieldType.hazelcastCompatibleType,
    defaultFormFieldValue = "''"
) {


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
    JdbcCompatibleType.uuid,
    "OTHER",
    EsDocMappingTypes.keyword,
    HazelcastCompatibleType.STRING,
    defaultFormFieldValue = "''"
) {


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
    JdbcCompatibleType.jsonb,
    sqlType = null,
    EsDocMappingTypes.`object`,
    HazelcastCompatibleType.COMPACT,
    defaultFormFieldValue = """
        |{
        |${requestDtoDef.dtoFieldDefs.joinToString(",\n") { "            ${it.classFieldDef.classFieldName}: ${it.classFieldDef.defaultFormFieldValue}" }}
        |        }""".trimMargin()
) {


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
    JdbcCompatibleType.jsonb,
    sqlType = "OTHER",
    EsDocMappingTypes.`object`,
    HazelcastCompatibleType.COMPACT,
    defaultFormFieldValue = """
        |{
        |${responseDtoDef.dtoDef.allFieldsSorted.joinToString(",\n") {"            ${it.classFieldName}: ${it.defaultFormFieldValue}" }}
        |        }""".trimMargin()
) {


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
    JdbcCompatibleType.jsonb,
    sqlType = null,
    EsDocMappingTypes.`object`,
    HazelcastCompatibleType.COMPACT,
    defaultFormFieldValue = "{}"
) {


    override fun unwrap(): FieldType {
        return this
    }


}


class FqcnFieldType internal constructor(
    fqcn: Fqcn,
    bsonCompatibleType: BsonCompatibleType?,
    typescriptCompatibleType: TypescriptCompatibleType?,
    jdbcCompatibleType: JdbcCompatibleType?,
    sqlType: String?,
    elasticMappingType: EsDocMappingType?,
    hazelcastCompatibleType: HazelcastCompatibleType?
): FieldType(
    fqcn,
    bsonCompatibleType,
    typescriptCompatibleType,
    jdbcCompatibleType,
    sqlType,
    elasticMappingType,
    hazelcastCompatibleType,
    defaultFormFieldValue = "'"
) {


    override fun unwrap(): FieldType {
        return this
    }


}
