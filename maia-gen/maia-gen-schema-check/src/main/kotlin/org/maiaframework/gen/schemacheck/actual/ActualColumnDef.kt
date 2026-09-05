package org.maiaframework.gen.schemacheck.actual

import org.maiaframework.gen.spec.definition.jdbc.TableColumnName

data class ActualColumnDef(
    val name: TableColumnName,
    val postgresType: String,
    val nullable: Boolean,
)
