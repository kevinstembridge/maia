package org.maiaframework.staging.csv

data class CsvPersistableField(
    val csvHeaderName: String,
    val tableColumnName: String,
    val value: String?
)
