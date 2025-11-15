package org.maiaframework.job.batch.csv

import org.maiaframework.job.batch.BatchItemReader
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.Reader


class CommonsCsvLineMappingReader<T>(
        reader: Reader,
        private val lineMapper: (CSVRecord) -> T
): BatchItemReader<T> {


    private val csvParser = CSVFormat.RFC4180.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setTrim(true)
        .setAllowMissingColumnNames(true)
        .build()
        .parse(reader)


    private val csvRecordIterator = csvParser.iterator()


//    private val header: Array<String?> = mapReader.getHeader(true).map {
//        it?.trim { ch -> ch <= ' ' || ch >= '\uFEFF'}
//    }.toTypedArray()


    override fun readItem(): T? {

        if (csvRecordIterator.hasNext() == false) {
            return null
        }

        val csvRecord = this.csvRecordIterator.next()

        return lineMapper.invoke(csvRecord)

    }


    fun close() {

        this.csvParser.close()

    }


}
