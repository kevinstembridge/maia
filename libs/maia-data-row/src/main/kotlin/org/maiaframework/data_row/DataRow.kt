package org.maiaframework.data_row

data class DataRow(
    val lineNumber: Long,
    val recordNumber: Long,
    val cells: Map<String, String?>
)
