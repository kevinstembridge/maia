package org.maiaframework.gen.spec.definition

data class SearchableDtoFieldDef(
    val isFilterable: Boolean,
    val responseDtoFieldDef: ResponseDtoFieldDef,
    val entityAndField: EntityAndField,
    private val fieldPath: FieldPath,
    private val sortIndexAndDirection: SortIndexAndDirection?
): Comparable<SearchableDtoFieldDef> {

    val tableName = entityAndField.tableName

    val schemaAndTableName = entityAndField.schemaAndTableName

    val databaseColumn = entityAndField.databaseColumnName

    val fieldPathLength = fieldPath.length

    val entityFieldDef = entityAndField.entityFieldDef

    val classFieldDef = this.responseDtoFieldDef.classFieldDef

    val classFieldName = classFieldDef.classFieldName

    val fieldSortModel: FieldSortModel? = sortIndexAndDirection?.let { FieldSortModel(classFieldName, it) }

    val isForeignKeyRef = this.fieldPathLength > 1


    override fun compareTo(other: SearchableDtoFieldDef): Int {
        return this.responseDtoFieldDef.compareTo(other.responseDtoFieldDef)
    }


    fun copyWithFieldName(dtoFieldName: String): SearchableDtoFieldDef {
        return copy(responseDtoFieldDef = responseDtoFieldDef.copyWithFieldName(dtoFieldName))
    }


}
