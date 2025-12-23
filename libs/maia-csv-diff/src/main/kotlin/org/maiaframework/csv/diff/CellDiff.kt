package org.maiaframework.csv.diff

data class CellDiff(
    val columnName: String,
    val value1: String?,
    val value2: String?
)
