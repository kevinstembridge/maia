package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldName

data class FieldSortModel(
    val fieldName: ClassFieldName,
    val sortIndexAndDirection: SortIndexAndDirection
)
