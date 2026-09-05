package org.maiaframework.gen.schemacheck

import org.maiaframework.gen.spec.definition.jdbc.TableColumnName


data class PrimaryKeyMismatch(
    val expected: List<TableColumnName>,
    val actual: List<TableColumnName>,
    val actualConstraintName: String? = null
)
