package org.maiaframework.csv.diff

import org.supercsv.io.CsvListReader
import org.supercsv.prefs.CsvPreference
import java.io.*

object CsvHelper {


    fun readLines(file: File, fileShortName: String, preference: CsvPreference): List<List<String?>> {

        try {
            return readLines(FileReader(file), fileShortName, preference)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    }


    fun readLines(reader: Reader, fileShortName: String, preference: CsvPreference): List<List<String?>> {

        val csvListReader = CsvListReader(reader, preference)

        val lines = readAllLines(csvListReader)
        if (lines.isEmpty()) {
            throw RuntimeException("file $fileShortName is empty")
        }

        return lines

    }


    fun readAllLines(reader: CsvListReader): List<List<String?>> {

        try {
            val lines = mutableListOf<List<String?>>()
            var currentLine: List<String>? = reader.read()

            while (currentLine != null) {
                lines.add(currentLine)
                currentLine = reader.read()
            }

            return lines.toList()

        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    }


}
