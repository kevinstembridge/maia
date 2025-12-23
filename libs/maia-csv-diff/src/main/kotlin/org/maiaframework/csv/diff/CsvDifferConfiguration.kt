package org.maiaframework.csv.diff

import org.supercsv.prefs.CsvPreference
import java.time.Instant
import java.time.format.DateTimeFormatter

class CsvDifferConfiguration(
    val diffTaskName: String,
    val sourceConfig1: SourceConfig,
    val sourceConfig2: SourceConfig,
    private val diffSettings: CsvDiffSettings,
    val diffReportStyle: DiffReportStyle = DiffReportStyle.CSV_FILE,
    outputFileName: String?
) {


    val sourceName1 = this.sourceConfig1.name


    val sourceName2 = this.sourceConfig2.name


    val outputFileName = outputFileName
        ?: "${diffTaskName}_diff_${sourceName1}_${sourceName2}_${FILE_TIMESTAMP_FORMATTER.format(Instant.now())}"


    val csvPreference: CsvPreference = this.diffSettings.csvPreference


    val keyFieldColumnNames = this.diffSettings.keyFieldColumnNames


    val differencesOutputFileDirectory: String = this.sourceConfig1.file.parent


    fun isNotKeyColumn(columnName: String): Boolean {

        return diffSettings.keyFieldColumnNames.contains(columnName) == false

    }


    fun isKeyColumn(columnName: String): Boolean {

        return diffSettings.keyFieldColumnNames.contains(columnName)

    }


    fun isIgnoredColumn(columnName: String): Boolean {

        return diffSettings.ignoredColumnNames.contains(columnName)

    }


    companion object {

        private val FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm")

    }


}
