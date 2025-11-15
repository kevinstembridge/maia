package org.maiaframework.job.batch.csv

import org.maiaframework.job.batch.BatchItemReader
import org.supercsv.io.CsvMapReader
import org.supercsv.prefs.CsvPreference
import java.io.Reader


class CsvLineMappingReader_old<T>(
        reader: Reader,
        private val lineMapper: (Map<String, Any?>, Int) -> T,
        csvPreference: CsvPreference = CsvPreference.STANDARD_PREFERENCE
): BatchItemReader<T> {


    private val mapReader = CsvMapReader(reader, csvPreference)


    private val header: Array<String?> = mapReader.getHeader(true).map {
        it?.trim { ch -> ch <= ' ' || ch >= '\uFEFF'}
    }.toTypedArray()


    private val cellProcessors = Array(header.size) { TrimmingCellProcessor.INSTANCE }


    override fun readItem(): T? {

        val line = this.mapReader.read(header, cellProcessors)
            ?: return null

        val lineNumber = mapReader.lineNumber

        return lineMapper.invoke(line, lineNumber)

    }


    fun close() {

        this.mapReader.close()

    }


}
