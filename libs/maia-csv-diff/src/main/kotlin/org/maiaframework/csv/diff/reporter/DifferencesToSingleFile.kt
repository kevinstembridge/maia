package org.maiaframework.csv.diff.reporter

import org.maiaframework.csv.TabularFileWriter
import org.maiaframework.csv.diff.CsvData
import org.maiaframework.csv.diff.CsvDataDiff
import org.maiaframework.csv.diff.CsvDifferConfiguration

class DifferencesToSingleFile(
    private val config: CsvDifferConfiguration,
    private val dataColumnNames: List<String>,
    outputFileWriter: (List<String>) -> TabularFileWriter
): DiffReporter {

    private val outputWriter: TabularFileWriter

    init {

        val outputColumnNames = mutableListOf<String>()

        config.keyFieldColumnNames.forEach {keyField -> outputColumnNames.add("$keyField (key)")}

        outputColumnNames.add("differingFields")
        outputColumnNames.add("differingFieldValues [${config.sourceName1} | ${config.sourceName2}]")

        dataColumnNames.forEach { dataColumnName ->
            outputColumnNames.add("$dataColumnName(${config.sourceName1})")
            outputColumnNames.add("$dataColumnName(${config.sourceName2})")
        }

        this.outputWriter = outputFileWriter(outputColumnNames)

    }


    override fun onDifferenceFound(csvDataDiff: CsvDataDiff) {

        val outputRow = mutableListOf<Any?>()

        addKeyFieldsToOutputRow(csvDataDiff.rowsInSource1, csvDataDiff.rowsInSource2, outputRow)

        when {
            csvDataDiff.rowsInSource1.isEmpty() -> {
                outputRow.add("missing in ${config.sourceName1}")
                outputRow.add("missing in ${config.sourceName1}")
            }

            csvDataDiff.rowsInSource2.isEmpty() -> {
                outputRow.add("missing in ${config.sourceName2}")
                outputRow.add("missing in ${config.sourceName2}")
            }

            else -> {

                val differingFields = csvDataDiff.differingFields

                val differingFieldNames = differingFields.joinToString(", ") { it.columnName }

                outputRow.add(differingFieldNames)

                val differingFieldValues = differingFields
                    .filterNot { config.isIgnoredColumn(it.columnName) }
                    .map { differingField ->
                        "${differingField.value1} vs ${differingField.value2}"
                    }

                outputRow.add(differingFieldValues.joinToString(" | "))

            }

        }

        dataColumnNames.forEach { col ->

            val value1 = getValues(csvDataDiff.rowsInSource1, col)
            outputRow.add(value1)

            val value2 = getValues(csvDataDiff.rowsInSource2, col)
            outputRow.add(value2)

        }

        this.outputWriter.writeRow(outputRow)

    }

    private fun addKeyFieldsToOutputRow(
        rowsInSource1: List<CsvData.CsvRow>,
        rowsInSource2: List<CsvData.CsvRow>,
        outputRow: MutableList<Any?>
    ) {

        config.keyFieldColumnNames.forEach { keyFieldColumnName ->

            val keyFieldValue = listOf(rowsInSource1, rowsInSource2)
                .flatten()
                .map { d -> d.getColumnValue(keyFieldColumnName) }
                .firstOrNull()

            outputRow.add(keyFieldValue)

        }

    }


    private fun getValues(
        data: List<CsvData.CsvRow>,
        columnName: String
    ): String? {

        if (data.isEmpty()) {
            return null
        }

        return if (data.size == 1) {
            data.first().getColumnValue(columnName)
        } else {
            data.map { r -> r.getColumnValue(columnName) }.toString()
        }

    }


    override fun onCompletion() {

        outputWriter.close()
        println("\nComparison written to: ${outputWriter.outputFile.absolutePath}")

    }


}
