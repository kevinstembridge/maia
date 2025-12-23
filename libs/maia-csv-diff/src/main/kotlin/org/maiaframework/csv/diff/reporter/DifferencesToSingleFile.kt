package org.maiaframework.csv.diff.reporter

import org.maiaframework.csv.TabularFileWriter
import org.maiaframework.csv.diff.CsvData
import org.maiaframework.csv.diff.CsvDataDiff
import org.maiaframework.csv.diff.CsvDiffFixture

class DifferencesToSingleFile(
    private val fixture: CsvDiffFixture,
    outputFileWriter: (List<String>) -> TabularFileWriter
): DiffReporter {


    private val dataColumnNames = fixture.nonKeyColumnNames


    private val outputWriter: TabularFileWriter


    init {

        val outputColumnNames = mutableListOf<String>()

        fixture.keyFieldColumnNames.forEach {keyField -> outputColumnNames.add("$keyField (key)")}

        outputColumnNames.add("differingFields")
        outputColumnNames.add("differingFieldValues [${fixture.sourceName1} | ${fixture.sourceName2}]")

        dataColumnNames.forEach { dataColumnName ->
            outputColumnNames.add("$dataColumnName(${fixture.sourceName1})")
            outputColumnNames.add("$dataColumnName(${fixture.sourceName2})")
        }

        this.outputWriter = outputFileWriter(outputColumnNames)

    }


    override fun onDifferenceFound(csvDataDiff: CsvDataDiff) {

        val outputRow = mutableListOf<Any?>()

        addKeyFieldsToOutputRow(csvDataDiff.rowsInSource1, csvDataDiff.rowsInSource2, outputRow)

        when {
            csvDataDiff.rowsInSource1.isEmpty() -> {
                outputRow.add("missing in ${fixture.sourceName1}")
                outputRow.add("missing in ${fixture.sourceName1}")
            }

            csvDataDiff.rowsInSource2.isEmpty() -> {
                outputRow.add("missing in ${fixture.sourceName2}")
                outputRow.add("missing in ${fixture.sourceName2}")
            }

            else -> {

                val differingFields = csvDataDiff.differingFields

                val differingFieldNames = differingFields.joinToString(", ") { it.columnName }

                outputRow.add(differingFieldNames)

                val differingFieldValues = differingFields
                    .filterNot { fixture.isIgnoredColumn(it.columnName) }
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

        fixture.keyFieldColumnNames.forEach { keyFieldColumnName ->

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
