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


    fun mapRowsBy(
        keyFieldColumnNames: List<String>
    ): Map<String, List<CsvRow>> {

        val toKey: (CsvRow) -> String = { row: CsvRow -> keyFieldColumnNames.map { row.getColumnValue(it) }.joinToString(" | ") }
        val groupBy: Map<String, List<CsvRow>> = rows.groupBy(toKey)
        return groupBy.mapValues { it.value.sorted() }

    }


    class CsvRow(
        private val rowData: Map<String, String?>,
        val rowNum: Int
    ) : Comparable<CsvRow> {


        internal fun size(): Int {
            return rowData.size
        }


        fun getColumnValue(columnName: String): String? {
            return rowData[columnName]
        }


        override fun compareTo(other: CsvRow): Int {

            return this.rowNum.compareTo(other.rowNum)

        }


    }

    companion object {


        fun createCsvData(
            lines: List<List<String?>>,
            fileShortName: String,
            transformers: List<CsvDataTransformer>
        ): CsvData {

            val columnNames = getColumnNames(lines.first(), transformers)
            val transformedRows = getRows(lines, columnNames, transformers)

            return CsvData(fileShortName, columnNames, transformedRows)

        }


        private fun getColumnNames(
            firstLine: List<String?>,
            transformers: List<CsvDataTransformer>
        ): List<String> {

            val allColumnNames: List<String> = firstLine.map { trim(it)!! }

            return transformers.fold(allColumnNames) { acc, transformer -> transformer.transformColumnNames(acc) }

        }


        private fun getRows(
            lines: List<List<String?>>,
            columnNames: List<String>,
            transformers: List<CsvDataTransformer>
        ): List<CsvRow> {

            val rowNum = AtomicInteger(1)

            val allRows = lines.drop(1).map { row ->

                val fields = mutableMapOf<String, String?>()
                columnNames.zip(row).forEach { fields[it.first] = it.second }

                CsvRow(fields, rowNum.getAndIncrement())

            }

            return transformers.fold(allRows) { acc, transformer -> transformer.transformRow(acc) }

        }


        private fun trim(str: String?): String? {

            return str?.trim()?.replace("\uFEFF", "")

        }


    }

}
