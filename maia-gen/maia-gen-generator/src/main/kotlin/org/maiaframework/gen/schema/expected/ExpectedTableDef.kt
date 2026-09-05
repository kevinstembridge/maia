package org.maiaframework.gen.schema.expected

import org.maiaframework.gen.spec.definition.jdbc.TableColumnName

data class ExpectedTableDef(
    val schemaAndTableName: String,
    val columns: List<ExpectedColumnDef>,
    val foreignKeys: List<ExpectedForeignKeyDef>,
    val indexes: List<ExpectedIndexDef>,
    val compositeForeignKeys: List<ExpectedCompositeForeignKeyDef> = emptyList(),
) {


    fun primaryKeyColumnNames(): List<TableColumnName> {

        return columns.filter { it.isPrimaryKey }.map { it.name }

    }


}
