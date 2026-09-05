package org.maiaframework.gen.schemacheck


data class SchemaDiffReport(val tables: List<TableDiff>) {

    val hasErrors: Boolean by lazy { tables.any { it.hasErrors } }

}
