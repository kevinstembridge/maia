package org.maiaframework.csv.diff

import org.maiaframework.csv.diff.reporter.DiffReporter


object CsvDiffer {


    fun diffCsvFiles(configuration: CsvDifferConfiguration): DiffSummary {

        configuration.assertFilesNotSame()

        val csvDataPair = loadCsvData(configuration)

        val nonKeyColumnNames = csvDataPair.getNonKeyColumnNames()

        val differenceReporter = makeDifferenceReporter(configuration, nonKeyColumnNames)

        try {
            return writeComparisonTo(differenceReporter, csvDataPair, configuration)
        } finally {
            differenceReporter.onCompletion()
        }

    }


    private fun loadCsvData(configuration: CsvDifferConfiguration): CsvDataPair {

        val data1 = getCsvData(configuration, configuration.sourceConfig1)
        val data2 = getCsvData(configuration, configuration.sourceConfig2)

        return CsvDataPair(data1, data2, configuration)

    }


    private fun getCsvData(
        configuration: CsvDifferConfiguration,
        sourceConfig: SourceConfig
    ): CsvData {

        val lines = CsvHelper.readLines(sourceConfig.file, sourceConfig.name, configuration.csvPreference)
        return CsvData.createCsvData(lines.toMutableList(), sourceConfig.name, sourceConfig.transformers)

    }


    private fun makeDifferenceReporter(
        configuration: CsvDifferConfiguration,
        dataColumnNames: List<String>
    ): DiffReporter {

        return when (configuration.diffReportStyle) {
            DiffReportStyle.CSV_FILE -> DiffReporter.differencesToSingleCsvFile(configuration, dataColumnNames)
            DiffReportStyle.TWO_DIFFABLE_CSV_FILES -> DiffReporter.differencesToTwoDiffableCsvFiles(configuration, dataColumnNames)
        }

    }


    private fun writeComparisonTo(
        diffReporter: DiffReporter,
        csvDataPair: CsvDataPair,
        configuration: CsvDifferConfiguration
    ): DiffSummary {

        val comparisonSummary = DiffSummary()

        val printStatus = {
            println("Rows processed: ${comparisonSummary.getTotalCompared()}\nDifferences Found: ${comparisonSummary.differencesFound}")
        }

        for (compoundKey in csvDataPair.uniqueSortedKeys) {

            val diff = checkForDifferences(compoundKey, csvDataPair)

            if (diff != null) {

                diffReporter.onDifferenceFound(diff)
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


    private fun checkForDifferences(
        key: String,
        csvDataPair: CsvDataPair
    ): CsvDataDiff? {

        val rowsInSource1 = csvDataPair.rowsFromSource1ByKey(key)
        val rowsInSource2 = csvDataPair.rowsFromSource2ByKey(key)

        val differingFields = csvDataPair.getDifferingFieldsByKey(key)

        if (differingFields.isEmpty()) {
            return null
        }

        return CsvDataDiff(
            key,
            differingFields,
            rowsInSource1,
            rowsInSource2
        )

    }


}
