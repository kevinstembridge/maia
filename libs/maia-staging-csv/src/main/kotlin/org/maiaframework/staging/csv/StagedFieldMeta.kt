package org.maiaframework.staging.csv

import org.maiaframework.domain.DomainId
import org.maiaframework.jdbc.SchemaAndTableName

data class StagedFieldMeta(
    val id: DomainId,
    val value: String?,
    val schemaAndTableName: SchemaAndTableName,
    val tableColumnName: String,
    val csvHeaderName: String,
    val classFieldName: String,
    val fieldClass: Class<*>,
    val lineNumber: Long,
    val fileStorageId: DomainId
)
