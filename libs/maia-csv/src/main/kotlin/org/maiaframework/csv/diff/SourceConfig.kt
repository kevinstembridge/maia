package org.maiaframework.csv.diff

import java.io.File

data class SourceConfig(
        val file: File,
        val name: String,
        val transformers: List<CsvDataTransformer>)
