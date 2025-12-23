package org.maiaframework.csv.diff.reporter

import org.maiaframework.csv.CsvFileWriter
import org.maiaframework.csv.diff.CsvData
import org.maiaframework.csv.diff.CsvDataDiff
import org.maiaframework.csv.diff.CsvDifferConfiguration
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

interface DiffReporter {


    fun onDifferenceFound(csvDataDiff: CsvDataDiff)


    fun onCompletion()


    companion object {


        val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")!!


        fun differencesToSingleCsvFile(
            config: CsvDifferConfiguration,
            dataColumnNames: List<String>
        ): DifferencesToSingleFile {

            val outputFile = File(config.differencesOutputFileDirectory, config.outputFileName + ".csv")

            return DifferencesToSingleFile(config, dataColumnNames, { headers -> CsvFileWriter.Companion.createCsvWriter(outputFile, headers) })

        }


        fun differencesToTwoDiffableCsvFiles(
            configuration: CsvDifferConfiguration,
            dataColumnNames: List<String>
        ): DiffReporter {

            val timestamp = timestampFormatter.format(Instant.now())

            val outputDirectory = configuration.differencesOutputFileDirectory
            val outputFileNamePattern = "differences_%s_$timestamp.csv"
            return DifferencesToTwoDiffableCsvFiles(configuration, outputDirectory, outputFileNamePattern, dataColumnNames)

        }


    }


}
