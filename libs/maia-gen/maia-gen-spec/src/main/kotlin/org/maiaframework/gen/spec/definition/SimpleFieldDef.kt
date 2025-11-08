package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.Nullability

data class SimpleFieldDef(
    val fieldType: FieldType,
    val nullability: Nullability,
    val expectedDateFormat: String? = null
)
