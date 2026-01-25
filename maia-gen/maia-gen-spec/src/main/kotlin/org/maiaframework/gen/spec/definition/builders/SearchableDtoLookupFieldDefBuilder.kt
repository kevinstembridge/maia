package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.SearchableDtoLookupFieldDef
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


@MaiaDslMarker
class SearchableDtoLookupFieldDefBuilder(
    private val foreignFieldDef: EntityFieldDef,
    private val parentBuilder: SearchableDtoLookupDefBuilder,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
) {


    private var dtoFieldName: String? = null

    private var fieldReaderParameterizedType: ParameterizedType? = null
    private var fieldWriterParameterizedType: ParameterizedType? = null


    private val fieldReaderClassName: ParameterizedType?
        get() = this.fieldReaderParameterizedType
            ?: this.foreignFieldDef.fieldReaderParameterizedType
            ?: this.defaultFieldTypeFieldReaderProvider(this.foreignFieldDef.classFieldDef.fieldType)


    private val fieldWriterClassName: ParameterizedType?
        get() = this.fieldWriterParameterizedType
            ?: this.foreignFieldDef.fieldWriterParameterizedType
            ?: this.defaultFieldTypeFieldWriterProvider(this.foreignFieldDef.classFieldDef.fieldType)


    fun asDtoField(dtoFieldName: String): SearchableDtoLookupFieldDefBuilder {

        this.dtoFieldName = dtoFieldName
        return this

    }


    fun fieldReader(fieldReaderFqcn: Fqcn): SearchableDtoLookupFieldDefBuilder {

        return fieldReader(ParameterizedType(fieldReaderFqcn))

    }


    fun fieldReader(fieldReaderParameterizedType: ParameterizedType): SearchableDtoLookupFieldDefBuilder {

        this.fieldReaderParameterizedType = fieldReaderParameterizedType
        return this

    }


    fun fieldWriter(fieldReaderFqcn: Fqcn): SearchableDtoLookupFieldDefBuilder {

        return fieldWriter(ParameterizedType(fieldReaderFqcn))

    }


    fun fieldWriter(fieldWriterParameterizedType: ParameterizedType): SearchableDtoLookupFieldDefBuilder {

        this.fieldWriterParameterizedType = fieldWriterParameterizedType
        return this

    }


    fun and(): SearchableDtoLookupDefBuilder {

        return this.parentBuilder

    }


    fun build(): SearchableDtoLookupFieldDef {

        return SearchableDtoLookupFieldDef(
            this.foreignFieldDef,
            this.dtoFieldName,
            this.fieldReaderClassName,
            this.fieldWriterClassName
        )

    }


}
