package org.maiaframework.csv.diff

import org.maiaframework.csv.CsvFileWriter
import org.maiaframework.csv.TabularFileWriter
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors

interface DiffReporter {


    fun onDifferenceFound(compoundKey: String, data1: List<CsvData.CsvRow>, data2: List<CsvData.CsvRow>)

    fun onCompletion()

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

            dataColumnNames.forEach { dataColumnName -> outputColumnNames.add("$dataColumnName(${config.sourceName1})")}
            dataColumnNames.forEach { dataColumnName -> outputColumnNames.add("$dataColumnName(${config.sourceName2})")}

            this.outputWriter = outputFileWriter(outputColumnNames)

        }


        override fun onDifferenceFound(compoundKey: String, data1: List<CsvData.CsvRow>, data2: List<CsvData.CsvRow>) {

            val outputRow = mutableListOf<Any?>()

            config.keyFieldColumnNames.forEach { keyFieldColumnName ->

                val keyFieldValue = listOf(data1, data2)
                        .flatten()
                        .map { d -> d.getColumnValue(keyFieldColumnName) }
                        .firstOrNull()

                outputRow.add(keyFieldValue)

            }

            when {
                data1.isEmpty() -> {
                    outputRow.add("missing in ${config.sourceName1}")
                    outputRow.add("missing in ${config.sourceName1}")
                }
                data2.isEmpty() -> {
                    outputRow.add("missing in ${config.sourceName2}")
                    outputRow.add("missing in ${config.sourceName2}")
                }
                else -> {
                    val differingFields = dataColumnNames.filter { col -> getValues(data1, col) != getValues(data2, col) }
                    outputRow.add(differingFields.joinToString(", "))
                }
            }

        }


        private fun getValues(data: List<CsvData.CsvRow>, columnName: String): Optional<String> {

            if (data.isEmpty()) {
                return Optional.empty()
            }

            return if (data.size == 1) {
                data.stream().findFirst().flatMap { r -> Optional.ofNullable(r.getColumnValue(columnName)) }
            } else {
                Optional.of(data.stream().map { r -> r.getColumnValue(columnName) }.collect(Collectors.toList()).toString())
            }

        }


        override fun onCompletion() {

            outputWriter.close()
            println("\nComparison written to: ${outputWriter.outputFile.absolutePath}")

        }


    }


    class DifferencesToTwoDiffableCsvFiles(
            configuration: CsvDifferConfiguration,
            outputDir: String,
            outputFileName: String,
            private val dataColumnNames: List<String>
    ): DiffReporter {

        private val writerSource1: CsvFileWriter
        private val writerSource2: CsvFileWriter

        init {

            val outputColumnNames = mutableListOf<String>()
            outputColumnNames.add(configuration.keyFieldColumnNames.joinToString(" | "))
            outputColumnNames.add("occurrence")
            outputColumnNames.addAll(dataColumnNames)

            this.writerSource1 = CsvFileWriter.createCsvWriter(File(outputDir), String.format(outputFileName, configuration.sourceName1), outputColumnNames)
            this.writerSource2 = CsvFileWriter.createCsvWriter(File(outputDir), String.format(outputFileName, configuration.sourceName2), outputColumnNames)

        }


        override fun onDifferenceFound(compoundKey: String, data1: List<CsvData.CsvRow>, data2: List<CsvData.CsvRow>) {

            val size = Math.max(data1.size, data2.size)

            for (i in 0 until size) {

                val occurrence1 = if (data1.size > i) Optional.of(data1[i]) else Optional.empty()
                val occurrence2 = if (data2.size > i) Optional.of(data2[i]) else Optional.empty()

                val comparisonRow1 = mutableListOf<Any?>()
                comparisonRow1.add(compoundKey)

                for (col in dataColumnNames) {
                    comparisonRow1.add(occurrence1.map { v -> v.getColumnValue(col) }.orElse(null))
                }

                writerSource1.writeRow(comparisonRow1)

                val comparisonRow2 = mutableListOf<Any?>()
                comparisonRow2.add(compoundKey)

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


    companion object {

        val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")!!


        fun differencesToSingleCsvFile(config: CsvDifferConfiguration, dataColumnNames: List<String>): DifferencesToSingleFile {

            val outputFile = File(config.differencesOutputFileDirectory, config.outputFileName + ".csv")

            return DifferencesToSingleFile(config, dataColumnNames, { headers -> CsvFileWriter.createCsvWriter(outputFile, headers) })

        }


        fun differencesToTwoDiffableCsvFiles(configuration: CsvDifferConfiguration, dataColumnNames: List<String>): DiffReporter {

            val timestamp = timestampFormatter.format(Instant.now())

            val outputDirectory = configuration.differencesOutputFileDirectory
            val outputFileNamePattern = "differences_%s_$timestamp.csv"
            return DifferencesToTwoDiffableCsvFiles(configuration, outputDirectory, outputFileNamePattern, dataColumnNames)

        }


    }


}
