package org.maiaframework.gen.schema.expected

import org.maiaframework.gen.spec.definition.jdbc.TableColumnName

data class ExpectedTableDef(
    val schemaAndTableName: String,
    val columns: List<ExpectedColumnDef>,
    val foreignKeys: List<ExpectedForeignKeyDef>,
    val indexes: List<ExpectedIndexDef>,
    val compositeForeignKeys: List<ExpectedCompositeForeignKeyDef> = emptyList(),
    // GIST-backed indexes that Postgres creates as a side effect of the single-effective-record
    // exclusion constraints CreateTableSqlRenderer generates. Kept separate from `indexes` (which
    // CreateTableSqlRenderer also uses to render plain CREATE INDEX statements and to decide which
    // indexes need an exclusion constraint) so schema-check can recognize these as expected without
    // the renderer mistaking them for indexes that need their own exclusion constraint.
    val exclusionIndexes: List<ExpectedIndexDef> = emptyList(),
) {


    fun primaryKeyColumnNames(): List<TableColumnName> {

        return columns.filter { it.isPrimaryKey }.map { it.name }

    }


}
