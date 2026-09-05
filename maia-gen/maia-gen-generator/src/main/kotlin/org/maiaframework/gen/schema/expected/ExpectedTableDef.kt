package org.maiaframework.gen.schema.expected

data class ExpectedTableDef(
    val schemaAndTableName: String,
    val columns: List<ExpectedColumnDef>,
    val foreignKeys: List<ExpectedForeignKeyDef>,
    val indexes: List<ExpectedIndexDef>,
    val compositeForeignKeys: List<ExpectedCompositeForeignKeyDef> = emptyList(),
) {


    fun primaryKeyColumnNames(): List<String> {

        return columns.filter { it.isPrimaryKey }.map { it.name.value }

    }


}
