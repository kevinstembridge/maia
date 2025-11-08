package org.maiaframework.jdbc


import org.maiaframework.domain.DomainId


class InvalidFieldException(
    val id: DomainId,
    val tableName: TableName?,
    val dbColumn: DbColumn,
    val classFieldName: String,
    cause: Throwable
) : RuntimeException("Invalid value in field: collection = $tableName, dbColumn = $dbColumn, id = $id, classField = $classFieldName.", cause)
