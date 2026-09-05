package org.maiaframework.gen.schemacheck.actual

import org.maiaframework.gen.spec.definition.jdbc.TableColumnName

data class ActualForeignKeyDef(
    val columnName: TableColumnName,
    val referencedSchemaAndTable: String,
    val referencedColumn: String,
)
