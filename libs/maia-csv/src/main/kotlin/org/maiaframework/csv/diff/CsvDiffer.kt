package org.maiaframework.csv.diff


class CsvDiffer {


    fun diffCsvFiles(configuration: CsvDifferConfiguration): DiffSummary {

        configuration.assertFilesNotSame()

        val data1 = getCsvData(configuration, configuration.sourceConfig1)
        val data2 = getCsvData(configuration, configuration.sourceConfig2)

        confirmColumnHeadersMatch(configuration, data1, data2)

        val nonKeyColumnNames = data1.getColumnsFiltered { configuration.isNotKeyColumn(it) }

        val differenceReporter = makeDifferenceReporter(configuration, nonKeyColumnNames)

        try {
            return writeComparisonTo(differenceReporter, data1, data2, configuration)
        } finally {
            differenceReporter.onCompletion()
        }

    }


    private fun confirmColumnHeadersMatch(configuration: CsvDifferConfiguration, data1: CsvData, data2: CsvData) {

        val columnNames1 = data1.columnNames
        val columnNames2 = data2.columnNames

        if (columnNames1 != columnNames2) {
            throw IllegalArgumentException(
                    "columns/headers in the two files for resource " + configuration.diffTaskName + " do not match (after filtering) :\n    "
                        + configuration.sourceName1 + " = " + columnNames1 + "\n    "
                        + configuration.sourceName2 + " = " + columnNames2)
        }

    }


    private fun getCsvData(configuration: CsvDifferConfiguration, sourceConfig: SourceConfig): CsvData {

        val lines = CsvHelper.readLines(sourceConfig.file, sourceConfig.name, configuration.csvPreference)
        return CsvData.createCsvData(lines.toMutableList(), sourceConfig.name, sourceConfig.transformers)

    }


    private fun makeDifferenceReporter(configuration: CsvDifferConfiguration, dataColumnNames: List<String>): DiffReporter {

        return when (configuration.diffReportStyle) {
            DiffReportStyle.CSV_FILE -> DiffReporter.differencesToSingleCsvFile(configuration, dataColumnNames)
            DiffReportStyle.TWO_DIFFABLE_CSV_FILES -> DiffReporter.differencesToTwoDiffableCsvFiles(configuration, dataColumnNames)
        }

    }


    private fun writeComparisonTo(
            diffReporter: DiffReporter,
            data1: CsvData,
            data2: CsvData,
            configuration: CsvDifferConfiguration
    ): DiffSummary {

        val data1Mapped = data1.mapRowsBy(configuration.keyFieldColumnNames)
        val data2Mapped = data2.mapRowsBy(configuration.keyFieldColumnNames)
        val compoundKeys = uniqueSortedKeys(data1Mapped, data2Mapped)

        val comparisonSummary = DiffSummary()

        val printStatus = {
            println("Rows processed: ${comparisonSummary.getTotalCompared()}\nDifferences Found: ${comparisonSummary.differencesFound}")
        }

        for (compoundKey in compoundKeys) {

            val rowsInSource1 = data1Mapped[compoundKey] ?: emptyList()
            val rowsInSource2 = data2Mapped[compoundKey] ?: emptyList()

            if (rowsInSource1 != rowsInSource2) {

                diffReporter.onDifferenceFound(compoundKey, rowsInSource1, rowsInSource2)

                comparisonSummary.incrementDifferences()
            }

            comparisonSummary.incrementTotalCompared()

            if (comparisonSummary.getTotalCompared() % 2000 == 0) {
                printStatus()
            }

        }

        println("\nComparison finished [${configuration.diffTaskName}]")
        printStatus()
        println()

        return comparisonSummary

    }


    companion object {


        internal fun uniqueSortedKeys(m1: Map<String, *>, m2: Map<String, *>): List<String> {

            return setOf(m1.keys, m2.keys).flatten().sorted()

        }


    }


}
