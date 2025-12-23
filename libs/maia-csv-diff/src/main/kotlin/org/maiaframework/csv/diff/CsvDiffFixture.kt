package org.maiaframework.csv.diff

data class CsvDiffFixture(
    val data1: CsvData,
    val data2: CsvData,
    val configuration: CsvDifferConfiguration
) {


    val diffTaskName = configuration.diffTaskName


    val diffReportStyle = configuration.diffReportStyle


    val outputFileName = configuration.outputFileName


    val differencesOutputFileDirectory = configuration.differencesOutputFileDirectory


    val sourceName1 = configuration.sourceName1


    val sourceName2 = configuration.sourceName2


    val keyFieldColumnNames = configuration.keyFieldColumnNames


    private val data1MappedByKey = data1.mapRowsBy(configuration.keyFieldColumnNames)


    private val data2MappedByKey = data2.mapRowsBy(configuration.keyFieldColumnNames)


    val uniqueSortedKeys = setOf(data1MappedByKey.keys, data2MappedByKey.keys).flatten().sorted()


    val nonKeyColumnNames = data1.columnNames.filterNot<String> { configuration.isKeyColumn(it) }


    init {

        val columnNames1 = data1.columnNames.filterNot { configuration.isIgnoredColumn(it) }
        val columnNames2 = data2.columnNames.filterNot { configuration.isIgnoredColumn(it) }

        if (columnNames1 != columnNames2) {
            throw IllegalArgumentException(
                "columns/headers in the two files for resource " + configuration.diffTaskName + " do not match (after filtering) :\n    "
                        + configuration.sourceName1 + " = " + columnNames1 + "\n    "
                        + configuration.sourceName2 + " = " + columnNames2
            )
        }

        if (configuration.sourceConfig1.file == configuration.sourceConfig2.file) {
            throw AssertionError("No point comparing a file with itself: ${configuration.sourceConfig1.file.absolutePath}")
        }

    }


    fun rowsFromSource1ByKey(key: String): List<CsvData.CsvRow> {

        return data1MappedByKey[key].orEmpty()

    }


    fun rowsFromSource2ByKey(key: String): List<CsvData.CsvRow> {

        return data2MappedByKey[key].orEmpty()

    }


    fun getDifferingFieldsByKey(key: String): List<CellDiff> {

        val nonKeyColumnNames = data1.columnNames.filterNot { configuration.isKeyColumn(it) }
        val rowsInSource1 = rowsFromSource1ByKey(key)
        val rowsInSource2 = rowsFromSource2ByKey(key)

        return nonKeyColumnNames
            .filterNot { configuration.isIgnoredColumn(it) }
            .mapNotNull { columnName ->

                val value1 = getValues(rowsInSource1, columnName)
                val value2 = getValues(rowsInSource2, columnName)

                if (value1 != value2) {
                    CellDiff(columnName, value1, value2)
                } else {
                    null
                }

            }

    }


    private fun getValues(
        rows: List<CsvData.CsvRow>,
        columnName: String
    ): String? {

        if (rows.isEmpty()) {
            return null
        }

        return if (rows.size == 1) {
            rows.first().getColumnValue(columnName)
        } else {
            rows.map { r -> r.getColumnValue(columnName) }.toString()
        }

    }


}
