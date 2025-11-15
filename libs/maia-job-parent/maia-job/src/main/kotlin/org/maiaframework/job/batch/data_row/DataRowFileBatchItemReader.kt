package org.maiaframework.job.batch.data_row

import org.maiaframework.job.batch.BatchItemReader
import org.maiaframework.job.batch.BatchItemStream
import com.opencsv.processor.RowProcessor
import org.maiaframework.data_row.DataRow
import org.maiaframework.data_row.DataRowFileParser
import org.maiaframework.lang.text.StringFunctions
import java.io.Reader


class DataRowFileBatchItemReader<T>(
    reader: Reader,
    headerParser: (String) -> Map<Int, String>,
    dataRowParser: (String) -> List<String?>,
    private val lineMapper: (DataRow) -> T
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


    private val fileParser = DataRowFileParser(
        reader,
        headerParser = headerParser,
        dataRowParser = dataRowParser
    )


    override fun openItemStream() {
        // Do nothing
    }


    override fun readItem(): T? {

        val row = this.fileParser.readNextRow()
            ?: return null

        return lineMapper.invoke(row)

    }


    override fun closeItemStream() {
        this.fileParser.close()
    }


}
