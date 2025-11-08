package org.maiaframework.gen.spec.definition

import org.maiaframework.domain.persist.SortDirection

data class SortIndexAndDirection(
    val sortIndex: Int,
    val sortDirection: SortDirection
)
