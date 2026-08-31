package org.maiaframework.gen.schemacheck

data class ActualColumnDef(
    val name: String,
    val postgresType: String,
    val nullable: Boolean,
)

data class ActualForeignKeyDef(
    val columnName: String,
    val referencedSchemaAndTable: String,
    val referencedColumn: String,
)

data class ActualIndexDef(
    val name: String,
    val columns: List<String>,
    val unique: Boolean,
)

data class ActualTableDef(
    val schemaAndTableName: String,
    val columns: List<ActualColumnDef>,
    val primaryKeyColumns: List<String>,
    val foreignKeys: List<ActualForeignKeyDef>,
    val indexes: List<ActualIndexDef>,
    val primaryKeyConstraintName: String? = null,
)
