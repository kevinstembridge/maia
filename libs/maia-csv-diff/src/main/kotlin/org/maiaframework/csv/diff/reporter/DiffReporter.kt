package org.maiaframework.csv.diff.reporter

import org.maiaframework.csv.CsvFileWriter
import org.maiaframework.csv.diff.CsvDataDiff
import org.maiaframework.csv.diff.CsvDiffFixture
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

interface DiffReporter {


    fun onDifferenceFound(csvDataDiff: CsvDataDiff)


    fun onCompletion()


    companion object {


        val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")!!


        fun differencesToSingleCsvFile(fixture: CsvDiffFixture): DifferencesToSingleFile {

            val outputDir = fixture.differencesOutputFileDirectory
            val fileName = fixture.outputFileName + ".csv"

            val outputFile = File(outputDir, fileName)

            return DifferencesToSingleFile(fixture) { headers ->
                CsvFileWriter.createCsvWriter(outputFile, headers)
            }

        }


        fun differencesToTwoDiffableCsvFiles(fixture: CsvDiffFixture): DiffReporter {

            val timestamp = timestampFormatter.format(Instant.now())

            val outputDirectory = fixture.differencesOutputFileDirectory
            val outputFileNamePattern = "differences_%s_$timestamp.csv"
            return DifferencesToTwoDiffableCsvFiles(fixture, outputDirectory, outputFileNamePattern)

        }


    }


}
