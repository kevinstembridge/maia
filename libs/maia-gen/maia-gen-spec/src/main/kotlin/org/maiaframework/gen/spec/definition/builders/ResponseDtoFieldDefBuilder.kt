package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.ResponseDtoFieldDef
import org.maiaframework.gen.spec.definition.SimpleTypeDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.ValueClassDef
import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


@MaiaDslMarker
class ResponseDtoFieldDefBuilder private constructor(
    private val classFieldName: ClassFieldName,
    private val fieldType: FieldType,
    private val simpleTypeDef: SimpleTypeDef?,
    private val valueClassDef: ValueClassDef?,
    private val responseDtoDefBuilder: ResponseDtoDefBuilder,
    private val caseSensitive: CaseSensitive,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
) {


    private var optional = false
    private var nullability = Nullability.NOT_NULLABLE
    private var isMasked = false
    private var fieldPath: String? = null
    private val fieldReaderParameterizedType: ParameterizedType? = null
    private val fieldWriterParameterizedType: ParameterizedType? = null


    private val fieldReaderClassName: ParameterizedType?
        get() = this.fieldReaderParameterizedType ?: this.defaultFieldTypeFieldReaderProvider(this.fieldType)


    private val fieldWriterClassName: ParameterizedType?
        get() = this.fieldWriterParameterizedType ?: this.defaultFieldTypeFieldWriterProvider(this.fieldType)


    constructor(
        classFieldName: ClassFieldName,
        enumDef: EnumDef,
        responseDtoDefBuilder: ResponseDtoDefBuilder,
        caseSensitive: CaseSensitive,
        defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
        defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
    ) : this(
        classFieldName,
        FieldTypes.enum(enumDef),
        null,
        null,
        responseDtoDefBuilder,
        caseSensitive,
        defaultFieldTypeFieldReaderProvider,
        defaultFieldTypeFieldWriterProvider
    )


    constructor(
        classFieldName: ClassFieldName,
        stringTypeDef: StringTypeDef,
        responseDtoDefBuilder: ResponseDtoDefBuilder,
        caseSensitive: CaseSensitive,
        defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
        defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
    ) : this(
        classFieldName,
        FieldTypes.stringType(stringTypeDef),
        stringTypeDef,
        null,
        responseDtoDefBuilder,
        caseSensitive,
        defaultFieldTypeFieldReaderProvider,
        defaultFieldTypeFieldWriterProvider
    )


    constructor(
        classFieldName: ClassFieldName,
        valueClassDef: ValueClassDef,
        responseDtoDefBuilder: ResponseDtoDefBuilder,
        caseSensitive: CaseSensitive,
        defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
        defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
    ) : this(
        classFieldName,
        valueClassDef.underlyingFieldType,
        null,
        valueClassDef,
        responseDtoDefBuilder,
        caseSensitive,
        defaultFieldTypeFieldReaderProvider,
        defaultFieldTypeFieldWriterProvider
    )


    constructor(
        classFieldName: ClassFieldName,
        fieldType: FieldType,
        responseDtoDefBuilder: ResponseDtoDefBuilder,
        caseSensitive: CaseSensitive,
        defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
        defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
    ) : this(
        classFieldName,
        fieldType,
        null,
        null,
        responseDtoDefBuilder,
        caseSensitive,
        defaultFieldTypeFieldReaderProvider,
        defaultFieldTypeFieldWriterProvider
    ) {

    }


    fun optional(): ResponseDtoFieldDefBuilder {

        this.optional = true
        return this

    }


    fun nullable(): ResponseDtoFieldDefBuilder {

        this.nullability = Nullability.NULLABLE
        return this

    }


    fun build(): ResponseDtoFieldDef {

        val fieldReaderClassName = fieldReaderClassName
        val fieldWriterClassName = fieldWriterClassName

        return ResponseDtoFieldDef(
            this.classFieldName,
            TableColumnName(this.fieldPath!!),
            this.fieldType,
            this.nullability,
            this.isMasked,
            this.caseSensitive,
            fieldReaderClassName,
            fieldWriterClassName
        )

    }


    fun masked(): ResponseDtoFieldDefBuilder {

        this.isMasked = true
        return this

    }


    fun and(): ResponseDtoDefBuilder {

        return this.responseDtoDefBuilder

    }


    fun collectionFieldPath(fieldPath: String): ResponseDtoFieldDefBuilder {

        this.fieldPath = fieldPath
        return this

    }


}
