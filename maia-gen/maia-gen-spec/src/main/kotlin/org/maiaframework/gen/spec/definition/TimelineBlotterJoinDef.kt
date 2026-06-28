package org.maiaframework.gen.spec.definition

import org.maiaframework.lang.text.StringFunctions


class TimelineBlotterJoinDef(
    val manyToManyEntityDef: ManyToManyEntityDef,
    val displayFieldEntityDef: EntityDef,
    val displayFieldName: String,
    entityDef: EntityDef
) {


    val joinTableSchemaAndTable: String = manyToManyEntityDef.entityDef.schemaAndTableName


    val entityFkColumnName: String = manyToManyEntityDef.idTableColumnName(entityDef)


    private val rightSide: ReferencedEntity = manyToManyEntityDef.otherSideFrom(entityDef)


    val rightFkColumnName: String = manyToManyEntityDef.idTableColumnName(rightSide.entityDef)


    val rightFkDtoFieldName: String = rightSide.fieldName + "Id"


    val rightFkSqlAlias: String = StringFunctions.toSnakeCase(rightSide.fieldName) + "_id"


    val displayFieldDtoFieldName: String = rightSide.fieldName + "DisplayName"


    val displayFieldSqlAlias: String = StringFunctions.toSnakeCase(rightSide.fieldName) + "_display_name"


    val rightEntitySchemaAndTable: String = displayFieldEntityDef.schemaAndTableName


    val rightEntityDisplayFieldColumnName: String = StringFunctions.toSnakeCase(displayFieldName)


}
