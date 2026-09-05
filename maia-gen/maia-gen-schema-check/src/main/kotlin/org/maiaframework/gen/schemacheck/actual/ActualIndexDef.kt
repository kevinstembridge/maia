package org.maiaframework.gen.schemacheck.actual

import org.maiaframework.gen.spec.definition.jdbc.TableColumnName

data class ActualIndexDef(
    val name: String,
    val columns: List<TableColumnName>,
    val unique: Boolean,
)
