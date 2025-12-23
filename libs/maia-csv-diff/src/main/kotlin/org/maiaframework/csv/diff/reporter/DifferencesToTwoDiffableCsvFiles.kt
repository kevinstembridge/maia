package org.maiaframework.csv.diff.reporter

import org.maiaframework.csv.CsvFileWriter
import org.maiaframework.csv.diff.CsvDataDiff
import org.maiaframework.csv.diff.CsvDiffFixture
import java.io.File
import java.util.*

class DifferencesToTwoDiffableCsvFiles(
    fixture: CsvDiffFixture,
    outputDir: String,
    outputFileName: String
): DiffReporter {


    private val dataColumnNames = fixture.nonKeyColumnNames


    private val writerSource1: CsvFileWriter


    private val writerSource2: CsvFileWriter


    init {

        val outputColumnNames = mutableListOf<String>()
        outputColumnNames.add(fixture.keyFieldColumnNames.joinToString(" | "))
        outputColumnNames.add("occurrence")
        outputColumnNames.addAll(dataColumnNames)

        this.writerSource1 = CsvFileWriter.createCsvWriter(File(outputDir), String.format(outputFileName, fixture.sourceName1), outputColumnNames)
        this.writerSource2 = CsvFileWriter.createCsvWriter(File(outputDir), String.format(outputFileName, fixture.sourceName2), outputColumnNames)

    }


    override fun onDifferenceFound(csvDataDiff: CsvDataDiff) {

        val data1 = csvDataDiff.rowsInSource1
        val data2 = csvDataDiff.rowsInSource2

        val size = Math.max(data1.size, data2.size)

        for (i in 0 until size) {

            val occurrence1 = if (data1.size > i) Optional.of(data1[i]) else Optional.empty()
            val occurrence2 = if (data2.size > i) Optional.of(data2[i]) else Optional.empty()

            val comparisonRow1 = mutableListOf<Any?>()
            comparisonRow1.add(csvDataDiff.key)

            for (col in dataColumnNames) {
                comparisonRow1.add(occurrence1.map { v -> v.getColumnValue(col) }.orElse(null))
            }

            writerSource1.writeRow(comparisonRow1)

            val comparisonRow2 = mutableListOf<Any?>()
            comparisonRow2.add(csvDataDiff.key)

            for (col in dataColumnNames) {
                comparisonRow2.add(occurrence2.map { v -> v.getColumnValue(col) }.orElse(null))
            }

            writerSource2.writeRow(comparisonRow2)

        }

    }


    override fun onCompletion() {

        writerSource1.close()
        writerSource2.close()

    }


}
