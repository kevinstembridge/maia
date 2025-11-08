package org.maiaframework.csv

import java.io.File

interface TabularFileWriter {

    fun writeRow(row: List<*>)

    fun close()

    val outputFile: File

}
