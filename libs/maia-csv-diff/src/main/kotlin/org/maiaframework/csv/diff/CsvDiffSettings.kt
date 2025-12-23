package org.maiaframework.csv.diff

import org.supercsv.prefs.CsvPreference

data class CsvDiffSettings(
    val keyFieldColumnNames: List<String>,
    val csvPreference: CsvPreference = CsvPreference.STANDARD_PREFERENCE,
    val ignoredColumnNames: List<String> = emptyList()
)
