package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

sealed class SearchableDtoFieldDefExp(
    val classFieldDef: ClassFieldDef,
    val sortIndexAndDirection: SortIndexAndDirection? = null
) {


    val fieldSortModel: FieldSortModel? = sortIndexAndDirection?.let { FieldSortModel(classFieldDef.classFieldName, it) }


}


class SimpleSearchableDtoFieldDef(
    val isFilterable: Boolean,
    val responseDtoFieldDef: ResponseDtoFieldDef,
    val entityAndField: EntityAndField,
    val fieldPath: FieldPath,
    sortIndexAndDirection: SortIndexAndDirection?
): SearchableDtoFieldDefExp(
    responseDtoFieldDef.classFieldDef,
    sortIndexAndDirection
) {


    val tableName = entityAndField.tableName


    val schemaAndTableName = entityAndField.schemaAndTableName


    val databaseColumn = entityAndField.databaseColumnName


    val fieldPathLength = fieldPath.length


    val entityFieldDef = entityAndField.entityFieldDef


    val isForeignKeyRef = this.fieldPathLength > 1


    val nullability = this.responseDtoFieldDef.nullability


    val displayName = this.entityFieldDef.classFieldDef.displayName


    fun copyWithFieldName(dtoFieldName: String): SimpleSearchableDtoFieldDef {

        return SimpleSearchableDtoFieldDef(
            this.isFilterable,
            this.responseDtoFieldDef.copyWithFieldName(dtoFieldName),
            this.entityAndField,
            this.fieldPath,
            this.sortIndexAndDirection
        )

    }


}


class ManyToManySearchableDtoFieldDefExp(
    classFieldDef: ClassFieldDef,
    sortIndexAndDirection: SortIndexAndDirection? = null
) : SearchableDtoFieldDefExp(
    classFieldDef,
    sortIndexAndDirection
)
