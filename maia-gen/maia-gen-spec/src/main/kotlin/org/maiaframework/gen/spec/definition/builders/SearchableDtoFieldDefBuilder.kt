package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.domain.persist.SortDirection
import org.maiaframework.gen.spec.definition.EntityAndField
import org.maiaframework.gen.spec.definition.FieldDisplayName
import org.maiaframework.gen.spec.definition.FieldPath
import org.maiaframework.gen.spec.definition.ResponseDtoFieldDef
import org.maiaframework.gen.spec.definition.SimpleSearchableDtoFieldDef
import org.maiaframework.gen.spec.definition.SortIndexAndDirection
import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


@MaiaDslMarker
class SearchableDtoFieldDefBuilder(
    private val classFieldName: ClassFieldName,
    private val entityAndField: EntityAndField,
    private val fieldPath: FieldPath,
    private val caseSensitive: CaseSensitive,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
) {


    private val classFieldDef = entityAndField.entityFieldDef.classFieldDef


    private var fieldDisplayName: FieldDisplayName? = null


    private var nullability = entityAndField.entityFieldDef.classFieldDef.nullability


    private var sortIndexAndDirection: SortIndexAndDirection? = null


    private var isMasked = false


    private var isFilterable = true


    private val fieldReaderParameterizedType: ParameterizedType? = null


    private val fieldWriterParameterizedType: ParameterizedType? = null


    private val fieldReaderClassName: ParameterizedType?
        get() = this.fieldReaderParameterizedType
            ?: this.defaultFieldTypeFieldReaderProvider.invoke(this.classFieldDef.fieldType)


    private val fieldWriterClassName: ParameterizedType?
        get() = this.fieldWriterParameterizedType
            ?: this.defaultFieldTypeFieldWriterProvider.invoke(this.classFieldDef.fieldType)


    fun build(): SimpleSearchableDtoFieldDef {

        val classFieldDef = this.entityAndField.entityFieldDef.classFieldDef
        val displayName = this.fieldDisplayName ?: classFieldDef.displayName

        val fieldReaderClassName = fieldReaderClassName
        val fieldWriterClassName = fieldWriterClassName

        val responseDtoFieldDef = ResponseDtoFieldDef(
            this.classFieldName,
            displayName,
            this.entityAndField.databaseColumnName,
            classFieldDef.fieldType,
            this.nullability,
            this.isMasked,
            this.caseSensitive,
            fieldReaderClassName,
            fieldWriterClassName
        )

        return SimpleSearchableDtoFieldDef(
            this.isFilterable,
            responseDtoFieldDef,
            this.entityAndField,
            this.fieldPath,
            this.sortIndexAndDirection
        )

    }


    fun displayName(displayName: String) {
        this.fieldDisplayName = FieldDisplayName(displayName)
    }


    fun masked(): SearchableDtoFieldDefBuilder {

        this.isMasked = true
        return this

    }


    fun notFilterable() {
        this.isFilterable = false
    }


    fun isDefaultSortField(index: Int = 0, sortDirection: SortDirection = SortDirection.asc) {
        this.sortIndexAndDirection = SortIndexAndDirection(index, sortDirection)
    }


    fun nullable() {

        this.nullability = Nullability.NULLABLE

    }


}
