package org.maiaframework.csv

data class CsvRow(
    val lineNumber: Long,
    val recordNumber: Long,
    val cells: Map<String, String?>
)
