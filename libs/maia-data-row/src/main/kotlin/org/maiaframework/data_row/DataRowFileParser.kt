package org.maiaframework.data_row

import java.io.BufferedReader
import java.io.LineNumberReader
import java.io.Reader


class DataRowFileParser(
    reader: Reader,
    private val headerParser: (String) -> Map<Int, String>,
    private val dataRowParser: (String) -> List<String?>
) {


    private val lineNumberReader = LineNumberReader(
        if (reader is BufferedReader) reader else BufferedReader(reader))


    private val headerNameByIndex: Map<Int, String> = initHeader()


    private var dataRowsRead: Long = 0


    private fun initHeader(): Map<Int, String> {

        val headerLine = readNextLineRaw()
            ?: throw IllegalArgumentException("Expected file to contain a header row, but it is empty")

        return this.headerParser.invoke(headerLine)

    }


    fun readNextRow(): DataRow? {

        val line = readNextLineRaw()
            ?: return null

        this.dataRowsRead++

        val rowElements = parseLine(line)

        val cells = rowElements.mapIndexed { index, s -> headerFor(index) to s }.toMap()

        return DataRow(
            this.lineNumberReader.lineNumber.toLong(),
            this.dataRowsRead,
            cells
        )

    }


    private fun parseLine(line: String): List<String?> {

        try {
            return dataRowParser.invoke(line)
        } catch (e: Exception) {
            throw RuntimeException("Error reading line ${this.lineNumberReader.lineNumber}. $e", e)
        }

    }


    private fun readNextLineRaw(): String? {

        return lineNumberReader.readLine()

    }


    private fun headerFor(index: Int) = this.headerNameByIndex[index]
        ?: throw IllegalStateException("No header found for index $index in headers $headerNameByIndex")


    fun close() {

        this.lineNumberReader.close()

    }


}
