package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.BooleanValueClassDef
import org.maiaframework.gen.spec.definition.DtoBaseName
import org.maiaframework.gen.spec.definition.DtoCharacteristic
import org.maiaframework.gen.spec.definition.DtoSuffix
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.HazelcastDtoDef
import org.maiaframework.gen.spec.definition.SimpleResponseDtoDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.StringValueClassDef
import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.lang.SetFieldType


@MaiaDslMarker
class HazelcastDtoDefBuilder(
    private val packageName: PackageName,
    private val dtoBaseName: DtoBaseName,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
) {


    private val fieldDefBuilders = mutableListOf<DtoFieldDefBuilder>()


    fun build(): HazelcastDtoDef {

        val fieldDefs = buildFieldDefs()

        return HazelcastDtoDef(
            this.dtoBaseName,
            DtoSuffix("HzDto"),
            this.packageName,
            fieldDefs,
            DtoCharacteristic.RESPONSE_DTO,
            DtoCharacteristic.HAZELCAST
        )

    }


    private fun buildFieldDefs(): List<ClassFieldDef> {

        return this.fieldDefBuilders.map { it.build() }.map { it.classFieldDef }

    }


    fun field(
        fieldName: String,
        fieldType: FieldType,
        caseSensitive: Boolean = true,
        init: DtoFieldDefBuilder.() -> Unit = {}
    ): DtoFieldDefBuilder {

        val fieldBuilder = DtoFieldDefBuilder(ClassFieldName(fieldName), fieldType, CaseSensitive(caseSensitive))
        return initFieldBuilder(fieldBuilder, init)

    }


    fun field(
        fieldName: String,
        enumDef: EnumDef,
        init: DtoFieldDefBuilder.() -> Unit = {}
    ): DtoFieldDefBuilder {

        val fieldBuilder = DtoFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.enum(enumDef),
            CaseSensitive.TRUE
        )

        return initFieldBuilder(fieldBuilder, init)

    }


    fun field(
        fieldName: String,
        responseDtoDef: SimpleResponseDtoDef,
        init: DtoFieldDefBuilder.() -> Unit = {}
    ): DtoFieldDefBuilder {

        val fieldBuilder = DtoFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.responseDto(responseDtoDef),
            CaseSensitive.TRUE
        )

        return initFieldBuilder(fieldBuilder, init)

    }


    fun field(
        fieldName: String,
        esDocDef: EsDocDef,
        init: DtoFieldDefBuilder.() -> Unit = {}
    ): DtoFieldDefBuilder {

        val fieldBuilder = DtoFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.esDoc(esDocDef),
            CaseSensitive.TRUE
        )

        return initFieldBuilder(fieldBuilder, init)

    }


    fun field(
        fieldName: String,
        stringTypeDef: StringTypeDef,
        init: DtoFieldDefBuilder.() -> Unit = {}
    ): DtoFieldDefBuilder {

        val fieldBuilder = DtoFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.stringType(stringTypeDef),
            CaseSensitive.TRUE
        )

        return initFieldBuilder(fieldBuilder, init)

    }


    fun field(
        fieldName: String,
        stringValueClassDef: StringValueClassDef,
        init: DtoFieldDefBuilder.() -> Unit = {}
    ): DtoFieldDefBuilder {

        val fieldBuilder = DtoFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.stringValueClass(stringValueClassDef),
            CaseSensitive.TRUE
        )

        return initFieldBuilder(fieldBuilder, init)

    }


    fun field(
        fieldName: String,
        booleanValueClassDef: BooleanValueClassDef,
        init: DtoFieldDefBuilder.() -> Unit = {}
    ): DtoFieldDefBuilder {

        val fieldBuilder = DtoFieldDefBuilder(
            ClassFieldName(fieldName),
            FieldTypes.booleanValueClass(booleanValueClassDef),
            CaseSensitive.TRUE
        )

        return initFieldBuilder(fieldBuilder, init)

    }


    fun field(
        fieldName: String,
        listFieldType: ListFieldType,
        caseSensitive: Boolean = true,
        init: DtoFieldDefBuilder.() -> Unit = {}
    ): DtoFieldDefBuilder {

        val fieldBuilder = DtoFieldDefBuilder(
            ClassFieldName(fieldName),
            listFieldType,
            CaseSensitive(caseSensitive)
        )

        return initFieldBuilder(fieldBuilder, init)

    }


    fun field(
        fieldName: String,
        setFieldType: SetFieldType,
        caseSensitive: Boolean = true,
        init: DtoFieldDefBuilder.() -> Unit = {}
    ): DtoFieldDefBuilder {

        val fieldBuilder = DtoFieldDefBuilder(
            ClassFieldName(fieldName),
            setFieldType,
            CaseSensitive(caseSensitive)
        )

        return initFieldBuilder(fieldBuilder, init)

    }


    fun field(
        fieldName: String,
        mapFieldType: MapFieldType,
        caseSensitive: Boolean = true,
        init: DtoFieldDefBuilder.() -> Unit = {}
    ): DtoFieldDefBuilder {

        val fieldBuilder = DtoFieldDefBuilder(
            ClassFieldName(fieldName),
            mapFieldType,
            CaseSensitive(caseSensitive)
        )

        return initFieldBuilder(fieldBuilder, init)

    }


    private fun initFieldBuilder(
        fieldBuilder: DtoFieldDefBuilder,
        init: DtoFieldDefBuilder.() -> Unit
    ): DtoFieldDefBuilder {

        fieldBuilder.init()
        fieldDefBuilders.add(fieldBuilder)
        return fieldBuilder

    }


}
