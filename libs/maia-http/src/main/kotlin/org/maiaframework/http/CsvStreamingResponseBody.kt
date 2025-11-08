package org.maiaframework.http

import org.maiaframework.csv.CsvWriterHelper
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import org.supercsv.io.CsvListWriter
import org.supercsv.io.ICsvListWriter
import org.supercsv.prefs.CsvPreference
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.UncheckedIOException
import java.util.stream.Stream

class CsvStreamingResponseBody<T>(private val csvWriterHelper: CsvWriterHelper<T>, private val rows: Stream<T>) : StreamingResponseBody {


    override fun writeTo(outputStream: OutputStream) {

        val csvListWriter = CsvListWriter(OutputStreamWriter(outputStream), CsvPreference.STANDARD_PREFERENCE)

        try {

            writeHeaderTo(csvListWriter)
            this.rows.forEach(writeRowTo(csvListWriter))

        } finally {
            csvListWriter.close()
        }

    }


    private fun writeHeaderTo(listWriter: ICsvListWriter) {

        val headerNames = this.csvWriterHelper.getHeaderNames()
        listWriter.writeHeader(*headerNames)

    }


    private fun writeRowTo(listWriter: ICsvListWriter): (T) -> Unit {

        return { t ->
            try {
                listWriter.write(this.csvWriterHelper.getColumnsFrom(t))
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        }

    }


}
