package org.maiaframework.gen.schema.expected

data class ExpectedCompositeForeignKeyDef(
    val columnNames: List<String>,
    val referencedSchemaAndTable: String,
    val referencedColumns: List<String>,
)
