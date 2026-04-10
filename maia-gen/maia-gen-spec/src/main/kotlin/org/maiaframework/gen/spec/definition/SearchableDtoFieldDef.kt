package org.maiaframework.gen.spec.definition


class SearchableDtoFieldDef(
    val isFilterable: Boolean,
    val responseDtoFieldDef: ResponseDtoFieldDef,
    val entityAndField: EntityAndField,
    val fieldPath: FieldPath,
    sortIndexAndDirection: SortIndexAndDirection?
): AbstractSearchableDtoFieldDef(
    responseDtoFieldDef.classFieldDef,
    sortIndexAndDirection
) {


    val tableName = entityAndField.tableName


    val schemaAndTableName = entityAndField.schemaAndTableName


    val databaseColumn = entityAndField.databaseColumnName


    val fieldPathLength = fieldPath.length


    val entityFieldDef = entityAndField.entityFieldDef


    val isForeignKeyRef = this.fieldPathLength > 1


    override val displayName = this.entityFieldDef.classFieldDef.displayName


    override fun copyWithFieldName(dtoFieldName: String): SearchableDtoFieldDef {

        return SearchableDtoFieldDef(
            this.isFilterable,
            this.responseDtoFieldDef.copyWithFieldName(dtoFieldName),
            this.entityAndField,
            this.fieldPath,
            this.sortIndexAndDirection
        )

    }


}
