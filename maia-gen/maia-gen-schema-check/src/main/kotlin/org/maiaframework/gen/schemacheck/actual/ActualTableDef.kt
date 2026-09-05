package org.maiaframework.gen.schemacheck.actual

import org.maiaframework.gen.spec.definition.jdbc.TableColumnName

data class ActualTableDef(
    val schemaAndTableName: String,
    val columnDefs: List<ActualColumnDef>,
    val primaryKeyColumnNames: List<TableColumnName>,
    val foreignKeys: List<ActualForeignKeyDef>,
    val indexes: List<ActualIndexDef>,
    val primaryKeyConstraintName: String? = null,
)
