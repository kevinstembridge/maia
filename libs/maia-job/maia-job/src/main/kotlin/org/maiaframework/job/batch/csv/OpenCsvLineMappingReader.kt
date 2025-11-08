package org.maiaframework.job.batch.csv

import org.maiaframework.job.batch.BatchItemReader
import org.maiaframework.job.batch.BatchItemStream
import com.opencsv.processor.RowProcessor
import org.maiaframework.csv.CSVReaderHeaderAware
import org.maiaframework.csv.CsvRow
import org.maiaframework.lang.text.StringFunctions
import java.io.Reader


class OpenCsvLineMappingReader<T>(
    reader: Reader,
    private val lineMapper: (CsvRow) -> T,
    dropTrailingComma: Boolean = false
) : BatchItemReader<T>, BatchItemStream {


    private val normalizeHeader: (input: String) -> String = { input ->

        val chars = input.trim().mapNotNull { ch ->

            when {
                // This is the range of ascii printable characters, excluding the DEL character.
                ch in ' '..'~' -> ch
                else -> null
            }

        }.toCharArray()

        String(chars)

    }


    private val rowProcessor: RowProcessor = object : RowProcessor {


        override fun processColumnItem(column: String?): String? {

            return StringFunctions.stripToNull(column)

        }


        override fun processRow(row: Array<String?>) {
            for (i in row.indices) {
                row[i] = processColumnItem(row[i])
            }
        }


    }


    private val csvReader = CSVReaderHeaderAware(
        reader,
        rowProcessor = rowProcessor,
        headerTransform = normalizeHeader,
        dropTrailingComma = dropTrailingComma
    )


    //    private val header: Array<String?> = mapReader.getHeader(true).map {
//        it?.trim { ch -> ch <= ' ' || ch >= '\uFEFF'}
//    }.toTypedArray()


    override fun openItemStream() {
        // Do nothing
    }


    override fun readItem(): T? {

        val row = this.csvReader.readNextRow()
            ?: return null

        return lineMapper.invoke(row)

    }


    override fun closeItemStream() {
        this.csvReader.close()
    }


}
