package org.maiaframework.csv.diff

data class CsvDataPair(
    val data1: CsvData,
    val data2: CsvData,
    private val configuration: CsvDifferConfiguration
) {


    private val data1MappedByKey = data1MappedByKey()


    private val data2MappedByKey = data2MappedByKey()


    val uniqueSortedKeys = setOf(data1MappedByKey.keys, data2MappedByKey.keys).flatten().sorted()


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

    }


    fun getNonKeyColumnNames(): List<String> {

        return data1.columnNames.filterNot { configuration.isKeyColumn(it) }

    }


    fun data1MappedByKey(): Map<String, List<CsvData.CsvRow>> {

        return data1.mapRowsBy(configuration.keyFieldColumnNames)

    }


    fun data2MappedByKey(): Map<String, List<CsvData.CsvRow>> {

        return data2.mapRowsBy(configuration.keyFieldColumnNames)

    }


    fun rowsFromSource1ByKey(key: String): List<CsvData.CsvRow> {

        return data1MappedByKey[key].orEmpty()

    }


    fun rowsFromSource2ByKey(key: String): List<CsvData.CsvRow> {

        return data2MappedByKey[key].orEmpty()

    }


    fun isIgnoredColumn(columnName: String): Boolean {

        return configuration.isIgnoredColumn(columnName)

    }


    fun getDifferingFieldsByKey(key: String): List<CellDiff> {

        val nonKeyColumnNames = getNonKeyColumnNames()
        val rowsInSource1 = rowsFromSource1ByKey(key)
        val rowsInSource2 = rowsFromSource2ByKey(key)

        return nonKeyColumnNames
            .filterNot { isIgnoredColumn(it) }
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


    fun getDifferingFieldNamesByKey_old(key: String): Map<String, Pair<String?, String?>> {

        val nonKeyColumnNames = getNonKeyColumnNames()
        val rowsInSource1 = rowsFromSource1ByKey(key)
        val rowsInSource2 = rowsFromSource2ByKey(key)

        return nonKeyColumnNames
            .filterNot { isIgnoredColumn(it) }
            .mapNotNull { columnName ->

                val value1 = getValues(rowsInSource1, columnName)
                val value2 = getValues(rowsInSource2, columnName)

                if (value1 != value2) {
                    columnName to Pair(value1, value2)
                } else {
                    null
                }

            }.toMap()

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
