package org.maiaframework.csv.diff

import org.maiaframework.csv.diff.reporter.DiffReporter


object CsvDiffer {


    fun diffCsvFiles(configuration: CsvDifferConfiguration): DiffSummary {

        val fixture = prepareFixture(configuration)

        return performDiff(fixture)

    }


    private fun prepareFixture(configuration: CsvDifferConfiguration): CsvDiffFixture {

        val data1 = loadCsvData(configuration, configuration.sourceConfig1)
        val data2 = loadCsvData(configuration, configuration.sourceConfig2)

        return CsvDiffFixture(data1, data2, configuration)

    }


    private fun performDiff(fixture: CsvDiffFixture): DiffSummary {

        return makeDifferenceReporter(fixture).use { diffReporter ->
            performDiff(fixture, diffReporter)
        }

    }


    private fun loadCsvData(
        configuration: CsvDifferConfiguration,
        sourceConfig: SourceConfig
    ): CsvData {

        val lines = CsvHelper.readLines(sourceConfig.file, sourceConfig.name, configuration.csvPreference)
        return CsvData.createCsvData(lines, sourceConfig.name, sourceConfig.transformers)

    }


    private fun makeDifferenceReporter(fixture: CsvDiffFixture): DiffReporter {

        return when (fixture.diffReportStyle) {
            DiffReportStyle.CSV_FILE -> DiffReporter.differencesToSingleCsvFile(fixture)
            DiffReportStyle.TWO_DIFFABLE_CSV_FILES -> DiffReporter.differencesToTwoDiffableCsvFiles(fixture)
        }

    }


    private fun performDiff(
        csvDiffFixture: CsvDiffFixture,
        diffReporter: DiffReporter
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

        println("\nComparison finished [${csvDiffFixture.diffTaskName}]")
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
