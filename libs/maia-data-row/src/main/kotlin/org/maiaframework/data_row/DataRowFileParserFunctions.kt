package org.maiaframework.data_row


fun delimitedLineSplitter(
    delimiter: Regex = ",".toRegex(),
    hasTrailingDelimiter: Boolean
): (String) -> List<String?> {

    return { line ->

        val numberToDrop = if (hasTrailingDelimiter) 1 else 0
        line.split(delimiter).dropLast(numberToDrop)

    }

}



fun parseDataRowFileHeader(
    lineSplitter: (String) -> List<String?>,
    cellProcessor: (String) -> String = { s -> s }
): (String) -> Map<Int, String> {

    return { line: String ->

        lineSplitter.invoke(line)
            .filterNotNull() // For a header, we rely on the lineSplitter making sure that no headers are blank
            .map(cellProcessor)
            .mapIndexed { index, cell -> index to cell }
            .toMap()

    }


}


fun parseDataRow(
    columnWidths: List<Int>,
    hasTrailingDelimiter: Boolean = true,
    cellProcessor: (String?) -> String? = { s -> s }
): (String) -> List<String?> {

    return { line: String ->

        checkLineLength(line, columnWidths, hasTrailingDelimiter)

        var currentIndex = 0

        columnWidths.map { columnWidth ->

            val subSequence = line.subSequence(currentIndex until currentIndex + columnWidth)
            currentIndex += (columnWidth + (if (hasTrailingDelimiter) 1 else 0))
            val cell = subSequence.toString()
            cellProcessor.invoke(cell)

        }

    }

}


private fun checkLineLength(
    line: String,
    columnWidths: List<Int>,
    hasTrailingDelimiter: Boolean
) {

    val expectedLineLength = columnWidths.sum() + ( if (hasTrailingDelimiter) columnWidths.size else 0)

    if (line.length != expectedLineLength) {
        throw IllegalArgumentException("Expecting line of length ${line.length} to be of length $expectedLineLength. line=$line. columnWidths=$columnWidths")
    }

}
