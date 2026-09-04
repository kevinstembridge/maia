package org.maiaframework.gen.schema.expected

data class ExpectedTableDef(
    val schemaAndTableName: String,
    val columns: List<ExpectedColumnDef>,
    val primaryKeyColumns: List<String>,
    val foreignKeys: List<ExpectedForeignKeyDef>,
    val indexes: List<ExpectedIndexDef>,
    val compositeForeignKeys: List<ExpectedCompositeForeignKeyDef> = emptyList(),
)
