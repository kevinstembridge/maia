package org.maiaframework.gen.schema.expected

import org.maiaframework.gen.spec.definition.jdbc.TableColumnName

data class ExpectedCompositeForeignKeyDef(
    val columnNames: List<TableColumnName>,
    val referencedSchemaAndTable: String,
    val referencedColumns: List<TableColumnName>,
)
