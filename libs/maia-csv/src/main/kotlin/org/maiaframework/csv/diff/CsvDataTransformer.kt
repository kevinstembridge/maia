package org.maiaframework.csv.diff

interface CsvDataTransformer {


    fun transformColumnNames(allColumnNames: List<String>): List<String>


    fun transformRow(rows: List<CsvData.CsvRow>): List<CsvData.CsvRow>


}
