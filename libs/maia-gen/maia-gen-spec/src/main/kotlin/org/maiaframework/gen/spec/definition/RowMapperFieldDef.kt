package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.Nullability

data class RowMapperFieldDef(
    val entityFieldDef: EntityFieldDef,
    val nullability: Nullability,
    val resultSetFieldName: String? = null
)
