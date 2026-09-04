package org.maiaframework.gen.schema.expected

data class ExpectedForeignKeyDef(
    val columnName: String,
    val referencedSchemaAndTable: String,
    val referencedColumn: String,
)
