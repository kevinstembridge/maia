package org.maiaframework.jdbc

import org.maiaframework.domain.persist.SchemaName

data class SchemaAndTableName(
    val schemaName: SchemaName,
    val tableName: TableName
) {


    override fun toString(): String {
        return "${schemaName}.$tableName"
    }


}
