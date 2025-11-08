package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.BooleanValueClassDef
import org.maiaframework.gen.spec.definition.DtoBaseName
import org.maiaframework.gen.spec.definition.DtoCharacteristic
import org.maiaframework.gen.spec.definition.DtoSuffix
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.SimpleResponseDtoDef
import org.maiaframework.gen.spec.definition.SimpleResponseDtoFieldDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.StringValueClassDef
import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


@MaiaDslMarker
class SimpleResponseDtoDefBuilder(
    private val packageName: PackageName,
    private val dtoBaseName: DtoBaseName,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
) {

    private val fieldDefBuilders = mutableListOf<SimpleResponseDtoFieldDefBuilder>()


    fun build(): SimpleResponseDtoDef {

        val fieldDefs = buildFieldDefs()
        val dtoSuffix = DtoSuffix("ResponseDto")

        return SimpleResponseDtoDef(
            this.dtoBaseName,
            dtoSuffix,
            this.packageName,
            fieldDefs,
            DtoCharacteristic.RESPONSE_DTO
        )

    }


    private fun buildFieldDefs(): List<SimpleResponseDtoFieldDef> {

        return this.fieldDefBuilders.map { it.build() }

    }


    fun field(
        fieldName: String,
        fieldType: FieldType,
        caseSensitive: Boolean = true,
        init: (SimpleResponseDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), fieldType, caseSensitive)
        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        valueClassDef: StringValueClassDef,
        caseSensitive: Boolean = true,
        init: (SimpleResponseDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), valueClassDef, caseSensitive)
        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        valueClassDef: BooleanValueClassDef,
        init: (SimpleResponseDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), valueClassDef)
        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        enumDef: EnumDef,
        init: (SimpleResponseDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), enumDef)
        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        responseDtoDef: SimpleResponseDtoDef,
        init: (SimpleResponseDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), responseDtoDef)
        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        esDocDef: EsDocDef,
        init: (SimpleResponseDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), esDocDef)
        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        stringTypeDef: StringTypeDef,
        init: (SimpleResponseDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(ClassFieldName(fieldName), stringTypeDef)
        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        listFieldType: ListFieldType,
        caseSensitive: Boolean = true,
        init: (SimpleResponseDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(
            ClassFieldName(fieldName),
            listFieldType,
            caseSensitive
        )

        init?.invoke(builder)

    }


    fun field(
        fieldName: String,
        mapFieldType: MapFieldType,
        caseSensitive: Boolean = true,
        init: (SimpleResponseDtoFieldDefBuilder.() -> Unit)? = null
    ) {

        val builder = newFieldDefBuilder(
            ClassFieldName(fieldName),
            mapFieldType,
            caseSensitive
        )

        init?.invoke(builder)

    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        fieldType: FieldType,
        caseSensitive: Boolean
    ): SimpleResponseDtoFieldDefBuilder {

        return add(
            SimpleResponseDtoFieldDefBuilder(
                classFieldName,
                fieldType,
                this,
                CaseSensitive(caseSensitive)
            )
        )

    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        stringTypeDef: StringTypeDef,
        caseSensitive: Boolean = true
    ): SimpleResponseDtoFieldDefBuilder {

        return add(
            SimpleResponseDtoFieldDefBuilder(
                classFieldName,
                FieldTypes.stringType(stringTypeDef),
                this,
                CaseSensitive(caseSensitive)
            )
        )

    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        enumDef: EnumDef,
        caseSensitive: Boolean = true
    ): SimpleResponseDtoFieldDefBuilder {

        return add(
            SimpleResponseDtoFieldDefBuilder(
                classFieldName,
                FieldTypes.enum(enumDef),
                this,
                CaseSensitive(caseSensitive)
            )
        )


    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        responseDtoDef: SimpleResponseDtoDef,
        caseSensitive: Boolean = true
    ): SimpleResponseDtoFieldDefBuilder {

        return add(
            SimpleResponseDtoFieldDefBuilder(
                classFieldName,
                FieldTypes.responseDto(responseDtoDef),
                this,
                CaseSensitive(caseSensitive)
            )
        )


    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        esDocDef: EsDocDef,
        caseSensitive: Boolean = true
    ): SimpleResponseDtoFieldDefBuilder {

        return add(
            SimpleResponseDtoFieldDefBuilder(
                classFieldName,
                FieldTypes.esDoc(esDocDef),
                this,
                CaseSensitive(caseSensitive)
            )
        )


    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        valueClassDef: StringValueClassDef,
        caseSensitive: Boolean = true
    ): SimpleResponseDtoFieldDefBuilder {

        return add(
            SimpleResponseDtoFieldDefBuilder(
                classFieldName,
                FieldTypes.stringValueClass(valueClassDef),
                this,
                CaseSensitive(caseSensitive)
            )
        )


    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        valueClassDef: BooleanValueClassDef
    ): SimpleResponseDtoFieldDefBuilder {

        return add(
            SimpleResponseDtoFieldDefBuilder(
                classFieldName,
                FieldTypes.booleanValueClass(valueClassDef),
                this,
                CaseSensitive.FALSE
            )
        )


    }


    private fun add(builder: SimpleResponseDtoFieldDefBuilder): SimpleResponseDtoFieldDefBuilder {

        this.fieldDefBuilders.add(builder)
        return builder

    }


}
