package org.maiaframework.staging.csv

import org.maiaframework.domain.DomainId

data class CsvPersistableRecord(
    val fileStorageId: DomainId,
    val lineNumber: Long,
    val fields: List<CsvPersistableField>
)
