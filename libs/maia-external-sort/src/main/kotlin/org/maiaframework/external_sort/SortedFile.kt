package org.maiaframework.external_sort

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

class SortedFile(
        private val file: File,
        val charset: Charset,
        val gzipped: Boolean,
        val lineCount: Int
) {


    fun delete() {
        this.file.delete()
    }


    fun getBuffer(): SortedFileReader {
        return SortedFileReader(this)
    }


    internal fun bufferedReader(): BufferedReader {

        val fis = FileInputStream(this.file)

        val inputStream = if (this.gzipped) {
            GZIPInputStream(fis, 2048)
        } else {
            fis
        }

        return BufferedReader(InputStreamReader(inputStream, this.charset))

    }


}
