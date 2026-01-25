package org.maiaframework.gen.spec.definition.lang

import org.maiaframework.gen.persist.BsonCompatibleType
import org.maiaframework.gen.persist.HazelcastCompatibleType
import org.maiaframework.gen.spec.definition.BooleanTypeDef
import org.maiaframework.gen.spec.definition.BooleanValueClassDef
import org.maiaframework.gen.spec.definition.DataClassDef
import org.maiaframework.gen.spec.definition.EntityPkAndNameDef
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.EsDocMappingType
import org.maiaframework.gen.spec.definition.ForeignKeyFieldDef
import org.maiaframework.gen.spec.definition.IntTypeDef
import org.maiaframework.gen.spec.definition.IntValueClassDef
import org.maiaframework.gen.spec.definition.LongTypeDef
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.SimpleResponseDtoDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.StringValueClassDef
import org.maiaframework.gen.spec.definition.TypescriptCompatibleType
import org.maiaframework.jdbc.JdbcCompatibleType

object FieldTypes {


    val int = IntFieldType()


    val long = LongFieldType()


    val double = DoubleFieldType()


    val boolean = BooleanFieldType()


    val string = StringFieldType()


    val url = UrlFieldType()


    fun booleanType(booleanTypeDef: BooleanTypeDef) = BooleanTypeFieldType(booleanTypeDef)


    fun stringType(stringTypeDef: StringTypeDef) = StringTypeFieldType(stringTypeDef)


    fun intType(intTypeDef: IntTypeDef) = IntTypeFieldType(intTypeDef)


    fun longType(longTypeDef: LongTypeDef) = LongTypeFieldType(longTypeDef)


    fun enum(enumDef: EnumDef) = EnumFieldType(enumDef)


    fun foreignKey(foreignKeyFieldDef: ForeignKeyFieldDef) = ForeignKeyFieldType(foreignKeyFieldDef)


    fun booleanValueClass(valueClassDef: BooleanValueClassDef) = BooleanValueClassFieldType(valueClassDef)


    fun intValueClass(valueClassDef: IntValueClassDef) = IntValueClassFieldType(valueClassDef)


    fun stringValueClass(valueClassDef: StringValueClassDef) = StringValueClassFieldType(valueClassDef)


    fun esDoc(esDocDef: EsDocDef) = EsDocFieldType(esDocDef)


    fun dataClass(dataClassDef: DataClassDef) = DataClassFieldType(dataClassDef)


    val objectId = ObjectIdFieldType()


    val domainId = DomainIdFieldType()


    val instant = InstantFieldType()


    val localDate = LocalDateFieldType()


    val period = PeriodFieldType()


    fun byFqcn(
        fqcn: Fqcn,
        bsonCompatibleType: BsonCompatibleType? = null,
        typescriptCompatibleType: TypescriptCompatibleType? = null,
        jdbcCompatibleType: JdbcCompatibleType? = null,
        sqlType: String? = null,
        elasticMappingType: EsDocMappingType? = null,
        hazelcastCompatibleType: HazelcastCompatibleType? = null
    ) = FqcnFieldType(
        fqcn,
        bsonCompatibleType,
        typescriptCompatibleType,
        jdbcCompatibleType,
        sqlType,
        elasticMappingType,
        hazelcastCompatibleType
    )


    fun requestDto(requestDtoDef: RequestDtoDef) = RequestDtoFieldType(requestDtoDef)


    fun responseDto(responseDtoDef: SimpleResponseDtoDef) = SimpleResponseDtoFieldType(responseDtoDef)


    fun idAndName(idAndNameDef: EntityPkAndNameDef) = IdAndNameFieldType(idAndNameDef)


    fun list(fieldType: FieldType) = ListFieldType(fieldType)


    fun set(fieldType: FieldType) = SetFieldType(fieldType)


    fun mapFieldType(keyFieldType: FieldType, valueFieldType: FieldType) = MapFieldType(keyFieldType, valueFieldType)
    
    
    fun FieldType.canHaveLengthConstraint(): Boolean = when (this) {
        is BooleanFieldType -> false
        is BooleanTypeFieldType -> false
        is BooleanValueClassFieldType -> false
        is DataClassFieldType -> false
        is DomainIdFieldType -> true
        is DoubleFieldType -> false
        is EnumFieldType -> true
        is EsDocFieldType -> false
        is ForeignKeyFieldType -> false
        is FqcnFieldType -> false
        is IdAndNameFieldType -> false
        is InstantFieldType -> false
        is IntFieldType -> false
        is IntTypeFieldType -> false
        is IntValueClassFieldType -> false
        is ListFieldType -> false
        is LocalDateFieldType -> false
        is LongFieldType -> false
        is LongTypeFieldType -> false
        is MapFieldType -> false
        is ObjectIdFieldType -> true
        is PeriodFieldType -> true
        is RequestDtoFieldType -> false
        is SetFieldType -> false
        is SimpleResponseDtoFieldType -> false
        is StringFieldType -> true
        is StringTypeFieldType -> true
        is StringValueClassFieldType -> true
        is UrlFieldType -> true
    }

    
    fun FieldType.isNumeric(): Boolean {
        
        return when (this) {
            is BooleanFieldType -> false
            is BooleanTypeFieldType -> false
            is BooleanValueClassFieldType -> false
            is DataClassFieldType -> false
            is DomainIdFieldType -> false
            is DoubleFieldType -> true
            is EnumFieldType -> false
            is EsDocFieldType -> false
            is ForeignKeyFieldType -> false
            is FqcnFieldType -> false
            is IdAndNameFieldType -> false
            is InstantFieldType -> false
            is IntFieldType -> true
            is IntTypeFieldType -> true
            is IntValueClassFieldType -> true
            is ListFieldType -> false
            is LocalDateFieldType -> false
            is LongFieldType -> true
            is LongTypeFieldType -> true
            is MapFieldType -> false
            is ObjectIdFieldType -> false
            is PeriodFieldType -> false
            is RequestDtoFieldType -> false
            is SetFieldType -> false
            is SimpleResponseDtoFieldType -> false
            is StringFieldType -> false
            is StringTypeFieldType -> false
            is StringValueClassFieldType -> false
            is UrlFieldType -> false
        }
        
    }


    fun FieldType.isStringBased(): Boolean {

        return when (this) {
            is BooleanFieldType -> false
            is BooleanTypeFieldType -> false
            is BooleanValueClassFieldType -> false
            is DataClassFieldType -> false
            is DomainIdFieldType -> false
            is DoubleFieldType -> false
            is EnumFieldType -> true
            is EsDocFieldType -> false
            is ForeignKeyFieldType -> false
            is FqcnFieldType -> false
            is IdAndNameFieldType -> false
            is InstantFieldType -> false
            is IntFieldType -> false
            is IntTypeFieldType -> false
            is IntValueClassFieldType -> false
            is ListFieldType -> false
            is LocalDateFieldType -> false
            is LongFieldType -> false
            is LongTypeFieldType -> false
            is MapFieldType -> false
            is ObjectIdFieldType -> true
            is PeriodFieldType -> true
            is RequestDtoFieldType -> false
            is SetFieldType -> false
            is SimpleResponseDtoFieldType -> false
            is StringFieldType -> true
            is StringTypeFieldType -> true
            is StringValueClassFieldType -> true
            is UrlFieldType -> true
        }

    }


    fun FieldType.isValueFieldWrapper(): Boolean {

        return when (this) {
            is BooleanFieldType -> false
            is BooleanTypeFieldType -> true
            is BooleanValueClassFieldType -> true
            is DataClassFieldType -> false
            is DomainIdFieldType -> false
            is DoubleFieldType -> false
            is EnumFieldType -> false
            is EsDocFieldType -> false
            is ForeignKeyFieldType -> false
            is FqcnFieldType -> false
            is IdAndNameFieldType -> false
            is InstantFieldType -> false
            is IntFieldType -> false
            is IntTypeFieldType -> true
            is IntValueClassFieldType -> true
            is ListFieldType -> false
            is LocalDateFieldType -> false
            is LongFieldType -> false
            is LongTypeFieldType -> true
            is MapFieldType -> false
            is ObjectIdFieldType -> false
            is PeriodFieldType -> false
            is RequestDtoFieldType -> false
            is SetFieldType -> false
            is SimpleResponseDtoFieldType -> false
            is StringFieldType -> false
            is StringTypeFieldType -> true
            is StringValueClassFieldType -> true
            is UrlFieldType -> false
        }

    }


    class MapFieldTypeBuilder(private val keyParameterizedType: FieldType) {


        fun to(valueFqcn: Fqcn): MapFieldType {

            return mapFieldType(this.keyParameterizedType, byFqcn(valueFqcn))

        }


        fun to(fieldType: FieldType): MapFieldType {

            return mapFieldType(this.keyParameterizedType, fieldType)

        }


        fun to(stringTypeDef: StringTypeDef): MapFieldType {

            return MapFieldType(
                this.keyParameterizedType,
                stringType(stringTypeDef)
            )

        }


        fun to(setFieldType: SetFieldType): MapFieldType {

            return MapFieldType(
                this.keyParameterizedType,
                setFieldType
            )

        }


        fun to(fieldType: SimpleResponseDtoFieldType): MapFieldType {

            return MapFieldType(
                this.keyParameterizedType,
                fieldType
            )

        }


    }


}
