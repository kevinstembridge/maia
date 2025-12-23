package org.maiaframework.csv.diff

import org.maiaframework.csv.diff.reporter.DiffReporter


object CsvDiffer {


    fun diffCsvFiles(configuration: CsvDifferConfiguration): DiffSummary {

        val csvDiffFixture = loadCsvDiffFixture(configuration)

        val nonKeyColumnNames = csvDiffFixture.getNonKeyColumnNames()

        val differenceReporter = makeDifferenceReporter(configuration, nonKeyColumnNames)

        try {
            return writeComparisonTo(differenceReporter, csvDiffFixture)
        } finally {
            differenceReporter.onCompletion()
        }

    }


    private fun loadCsvDiffFixture(configuration: CsvDifferConfiguration): CsvDiffFixture {

        val data1 = getCsvData(configuration, configuration.sourceConfig1)
        val data2 = getCsvData(configuration, configuration.sourceConfig2)

        return CsvDiffFixture(data1, data2, configuration)

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
        csvDiffFixture: CsvDiffFixture
    ): DiffSummary {

        val comparisonSummary = DiffSummary()

        val printStatus = {
            println("Rows processed: ${comparisonSummary.getTotalCompared()}\nDifferences Found: ${comparisonSummary.differencesFound}")
        }

        for (compoundKey in csvDiffFixture.uniqueSortedKeys) {

            val diff = checkForDifferences(compoundKey, csvDiffFixture)

            if (diff != null) {

                diffReporter.onDifferenceFound(diff)
                comparisonSummary.incrementDifferences()

            }

            comparisonSummary.incrementTotalCompared()

            if (comparisonSummary.getTotalCompared() % 2000 == 0) {
                printStatus()
            }

        }

        println("\nComparison finished [${csvDiffFixture.configuration.diffTaskName}]")
        printStatus()
        println()

        return comparisonSummary

    }


    private fun checkForDifferences(
        key: String,
        csvDiffFixture: CsvDiffFixture
    ): CsvDataDiff? {

        val rowsInSource1 = csvDiffFixture.rowsFromSource1ByKey(key)
        val rowsInSource2 = csvDiffFixture.rowsFromSource2ByKey(key)

        val differingFields = csvDiffFixture.getDifferingFieldsByKey(key)

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
