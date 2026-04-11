package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.Nullability


sealed class SearchableDtoFieldDef(
    val classFieldDef: ClassFieldDef,
    val sortIndexAndDirection: SortIndexAndDirection? = null
) {


    abstract fun copyWithFieldName(dtoFieldName: String): SearchableDtoFieldDef


    val classFieldName = classFieldDef.classFieldName


    val fieldSortModel: FieldSortModel? = sortIndexAndDirection?.let { FieldSortModel(classFieldDef.classFieldName, it) }


    abstract val displayName: FieldDisplayName?


    abstract val nullability: Nullability


}


class SimpleSearchableDtoFieldDef(
    val isFilterable: Boolean,
    val responseDtoFieldDef: ResponseDtoFieldDef,
    val entityAndField: EntityAndField,
    val fieldPath: FieldPath,
    sortIndexAndDirection: SortIndexAndDirection?
): SearchableDtoFieldDef(
    responseDtoFieldDef.classFieldDef,
    sortIndexAndDirection
) {


    val tableName = entityAndField.tableName


    val schemaAndTableName = entityAndField.schemaAndTableName


    val databaseColumn = entityAndField.databaseColumnName


    val fieldPathLength = fieldPath.length


    val entityFieldDef = entityAndField.entityFieldDef


    val isForeignKeyRef = this.fieldPathLength > 1


    override val nullability = this.responseDtoFieldDef.nullability


    override val displayName = this.entityFieldDef.classFieldDef.displayName


    override fun copyWithFieldName(dtoFieldName: String): SimpleSearchableDtoFieldDef {

        return SimpleSearchableDtoFieldDef(
            this.isFilterable,
            this.responseDtoFieldDef.copyWithFieldName(dtoFieldName),
            this.entityAndField,
            this.fieldPath,
            this.sortIndexAndDirection
        )

    }


}


class ManyToManySearchableDtoFieldDef(
    classFieldDef: ClassFieldDef,
    sortIndexAndDirection: SortIndexAndDirection? = null,
    override val nullability: Nullability,
    override val displayName: FieldDisplayName? = null
) : SearchableDtoFieldDef(
    classFieldDef,
    sortIndexAndDirection
) {


    override fun copyWithFieldName(dtoFieldName: String): ManyToManySearchableDtoFieldDef {

        return ManyToManySearchableDtoFieldDef(
            this.classFieldDef.withFieldName(dtoFieldName),
            this.sortIndexAndDirection,
            this.nullability,
            this.displayName
        )

    }


}
