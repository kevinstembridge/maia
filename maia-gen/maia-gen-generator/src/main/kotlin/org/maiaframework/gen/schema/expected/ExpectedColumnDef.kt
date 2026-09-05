package org.maiaframework.gen.schema.expected

import org.maiaframework.gen.spec.definition.jdbc.TableColumnName


data class ExpectedColumnDef(
    val name: TableColumnName,
    val postgresType: String,
    val nullable: Boolean,
    val isPrimaryKey: Boolean,
)
