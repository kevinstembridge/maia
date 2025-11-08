package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.DtoBaseName
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.ResponseDtoDef
import org.maiaframework.gen.spec.definition.ResponseDtoFieldDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.flags.WithProvidedFieldConverter
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.jdbc.TableName


@MaiaDslMarker
class ResponseDtoDefBuilder(
    private val packageName: PackageName,
    private val dtoBaseName: DtoBaseName,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
) {

    private val fieldDefBuilders = mutableListOf<ResponseDtoFieldDefBuilder>()
    private var withPreAuthorize: WithPreAuthorize? = null
    private var tableName: TableName? = null
    private var withProvidedFieldConverter = WithProvidedFieldConverter.FALSE


    fun build(): ResponseDtoDef {

        val fieldDefs = buildFieldDefs()

        return ResponseDtoDef(
            this.dtoBaseName,
            this.packageName,
            this.tableName!!,
            fieldDefs,
            this.withPreAuthorize,
            this.withProvidedFieldConverter
        )

    }


    private fun buildFieldDefs(): List<ResponseDtoFieldDef> {

        return this.fieldDefBuilders.map { it.build() }

    }


    fun field(
        fieldName: String,
        fieldType: FieldType,
        caseSensitive: Boolean = true
    ): ResponseDtoFieldDefBuilder {

        return newFieldDefBuilder(ClassFieldName(fieldName), fieldType, caseSensitive)

    }


    fun field(fieldName: String, enumDef: EnumDef): ResponseDtoFieldDefBuilder {

        return newFieldDefBuilder(ClassFieldName(fieldName), enumDef)

    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        fieldType: FieldType,
        caseSensitive: Boolean
    ): ResponseDtoFieldDefBuilder {

        return add(
            ResponseDtoFieldDefBuilder(
                classFieldName,
                fieldType,
                this,
                CaseSensitive(caseSensitive),
                this.defaultFieldTypeFieldReaderProvider,
                this.defaultFieldTypeFieldWriterProvider
            )
        )

    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        stringTypeDef: StringTypeDef,
        caseSensitive: Boolean = true
    ): ResponseDtoFieldDefBuilder {

        return add(
            ResponseDtoFieldDefBuilder(
                classFieldName,
                stringTypeDef,
                this,
                CaseSensitive(caseSensitive),
                this.defaultFieldTypeFieldReaderProvider,
                this.defaultFieldTypeFieldWriterProvider
            )
        )

    }


    private fun newFieldDefBuilder(
        classFieldName: ClassFieldName,
        enumDef: EnumDef,
        caseSensitive: Boolean = true
    ): ResponseDtoFieldDefBuilder {

        return add(
            ResponseDtoFieldDefBuilder(
                classFieldName,
                enumDef,
                this,
                CaseSensitive(caseSensitive),
                this.defaultFieldTypeFieldReaderProvider,
                this.defaultFieldTypeFieldWriterProvider
            )
        )


    }


    private fun add(builder: ResponseDtoFieldDefBuilder): ResponseDtoFieldDefBuilder {

        this.fieldDefBuilders.add(builder)
        return builder

    }


    fun field(
        fieldName: String,
        stringTypeDef: StringTypeDef
    ): ResponseDtoFieldDefBuilder {

        return newFieldDefBuilder(
            ClassFieldName(fieldName),
            stringTypeDef
        )

    }


    fun field(
        fieldName: String,
        listFieldType: ListFieldType,
        caseSensitive: Boolean = true
    ): ResponseDtoFieldDefBuilder {

        return newFieldDefBuilder(
            ClassFieldName(fieldName),
            listFieldType,
            caseSensitive
        )

    }


    fun field(
        fieldName: String,
        mapFieldType: MapFieldType,
        caseSensitive: Boolean = true
    ): ResponseDtoFieldDefBuilder {

        return newFieldDefBuilder(
            ClassFieldName(fieldName),
            mapFieldType,
            caseSensitive
        )

    }


    fun withPreAuthorize(expression: String): ResponseDtoDefBuilder {

        this.withPreAuthorize = WithPreAuthorize(expression)
        return this

    }


    fun tableName(tableName: TableName): ResponseDtoDefBuilder {

        this.tableName = tableName
        return this

    }


    fun withProvidedFieldConverter(): ResponseDtoDefBuilder {

        this.withProvidedFieldConverter = WithProvidedFieldConverter.TRUE
        return this

    }


}
