package org.maiaframework.csv.diff

import java.util.concurrent.atomic.AtomicInteger

class CsvData(
    fileShortName: String,
    val columnNames: List<String>,
    private val rows: List<CsvRow>
) {


    init {

        this.rows.forEach { row ->

            if (row.size() != this.columnNames.size) {
                throw IllegalArgumentException("row [${row.rowNum}] in file [$fileShortName] expected columns $columnNames but was:\n$row")
            }

        }

    }


    fun getColumnsFiltered(filter: (String) -> Boolean): List<String> {

        return this.columnNames.filter(filter)

    }


    fun mapRowsBy(keyFieldColumnNames: List<String>): Map<String, List<CsvRow>> {

        val toKey: (CsvRow) -> String = { row: CsvRow -> keyFieldColumnNames.map { row.getColumnValue(it) }.joinToString(" | ") }
        val groupBy: Map<String, List<CsvRow>> = rows.groupBy(toKey)
        return groupBy.mapValues { it.value.sorted() }

    }


    class CsvRow(private val rowData: Map<String, String?>, val rowNum: Int) : Comparable<CsvRow> {


        internal fun size(): Int {
            return rowData.size
        }


        fun getColumnValue(columnName: String): String? {
            return rowData[columnName]
        }


        override fun compareTo(other: CsvRow): Int {

            return Integer.compare(this.rowNum, other.rowNum)

        }


    }

    companion object {


        fun createCsvData(lines: MutableList<List<String?>>, fileShortName: String, transformers: List<CsvDataTransformer>): CsvData {

            val allColumnNames: List<String> = lines.removeAt(0).map { trim(it)!! }

            val rowNum = AtomicInteger(1)

            val allRows = lines.map { row ->

                val fields = mutableMapOf<String, String?>()
                allColumnNames.zip(row).forEach { fields[it.first] = it.second }

                CsvRow(fields, rowNum.getAndIncrement())

            }

            var transformedColumns = allColumnNames
            var transformedRows = allRows

            for (transformer in transformers) {
                transformedColumns = transformer.transformColumnNames(allColumnNames)
                transformedRows = transformer.transformRow(allRows)
            }

            return CsvData(fileShortName, transformedColumns, transformedRows)

        }


        private fun trim(str: String?): String? {

            return str?.trim()?.replace("\uFEFF", "")

        }


    }

}
