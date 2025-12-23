package org.maiaframework.csv.diff

data class CsvDataDiff(
    val key: String,
    val differingFields: List<CellDiff>,
    val rowsInSource1: List<CsvData.CsvRow>,
    val rowsInSource2: List<CsvData.CsvRow>
)
