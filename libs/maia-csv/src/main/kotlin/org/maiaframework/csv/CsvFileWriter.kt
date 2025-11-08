package org.maiaframework.csv

import org.maiaframework.common.io.FileUtil
import org.slf4j.LoggerFactory
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference
import java.io.File
import java.io.FileWriter

class CsvFileWriter(
        private val csvListWriter: CsvListWriter,
        private val headers: List<String>,
        private val actionOnClose: () -> Unit,
        override val outputFile: File
): TabularFileWriter {


    init {
        writeRow(headers)
    }


    override fun writeRow(row: List<*>) {

        if (row.size != headers.size) {
            throw IllegalArgumentException("row.size [${row.size}] != headers.size [${headers.size}]")
        }

        csvListWriter.write(row)

    }


    override fun close() {

        csvListWriter.close()
        actionOnClose.invoke()

    }


    companion object {


        private val logger = LoggerFactory.getLogger(CsvFileWriter::class.java)


        fun createCsvWriter(outputDir: File, fileName: String, headers: List<String>): CsvFileWriter {

            FileUtil.createDirIfNotExists(outputDir)
            val outputFile = File(outputDir, fileName)
            return createCsvWriter(outputFile, headers)

        }


        fun createCsvWriter(outputFile: File, headers: List<String>): CsvFileWriter {

            logger.info("Writing CSV to $outputFile")

            val writer = FileWriter(outputFile)
            val csvListWriter = CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE)

            return CsvFileWriter(csvListWriter,
                    headers,
                    {logger.info("CSV written to ${outputFile.absolutePath}")},
                    outputFile)

        }


    }


}
