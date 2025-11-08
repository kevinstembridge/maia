package org.maiaframework.jdbc

import org.maiaframework.types.StringType


class TableName(
    /**
     * The name of the table as per the database object, not as per any view that we want to reference instead.
     */
    val rawTableName: String,
    /**
     * For tables that we want to reference via a view, we set this field.
     */
    val viewName: String? = null
) : StringType<TableName>(viewName ?: rawTableName) {


    fun withSuffix(suffix: String): TableName {

        return TableName("$rawTableName$suffix", this.viewName?.let { "$it$suffix" })

    }


}
