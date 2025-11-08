package org.maiaframework.csv

import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import com.opencsv.ICSVParser
import com.opencsv.RFC4180ParserBuilder
import com.opencsv.processor.RowProcessor
import com.opencsv.validators.LineValidator
import com.opencsv.validators.RowValidator
import java.io.Reader
import java.util.Locale


class CSVReaderHeaderAware(
    reader: Reader,
    skipLines: Int = 0,
    parser: ICSVParser = RFC4180ParserBuilder().build(),
    keepCR: Boolean = false,
    verifyReader: Boolean = true,
    multilineLimit: Int = 0,
    errorLocale: Locale = Locale.getDefault(),
    lineValidators: List<LineValidator> = emptyList(),
    rowValidators: List<RowValidator> = emptyList(),
    rowProcessor: RowProcessor? = null,
    dropTrailingComma: Boolean = false,
    private val headerTransform: (String) -> String = { s -> s }
) {


    private val headerIndex: Map<Int, String>


    private val csvReader: CSVReader


    private val dropNumber = if (dropTrailingComma) 1 else 0


    init {

        val csvReaderBuilder = CSVReaderBuilder(reader)
            .withSkipLines(skipLines)
            .withVerifyReader(verifyReader)
            .withCSVParser(parser)
            .withKeepCarriageReturn(keepCR)
            .withVerifyReader(verifyReader)
            .withMultilineLimit(multilineLimit)
            .withErrorLocale(errorLocale)
            .withRowProcessor(rowProcessor)

        lineValidators.forEach { csvReaderBuilder.withLineValidator(it) }
        rowValidators.forEach { csvReaderBuilder.withRowValidator(it) }

        this.csvReader = csvReaderBuilder.build()

        this.headerIndex = initializeHeader()


    }


    private fun initializeHeader(): Map<Int, String> {

        val headers = this.csvReader.readNextSilently()

        return headers.dropLast(dropNumber)
            .mapIndexed { index, header ->
                Pair(index, this.headerTransform.invoke(header))
            }
            .toMap()

    }


    fun readNextRow(): CsvRow? {

        val row = this.csvReader.readNext()?.dropLast(this.dropNumber)
            ?: return null

        val cells = row.mapIndexed { index, s -> Pair(headerFor(index), s) }.toMap()

        return CsvRow(
            this.csvReader.linesRead,
            this.csvReader.recordsRead,
            cells
        )

    }


    private fun headerFor(index: Int) = this.headerIndex[index]
        ?: throw IllegalStateException("No header found for index $index in headers $headerIndex")


    fun close() {

        this.csvReader.close()

    }


}
