package org.maiaframework.gen.schemacheck

import org.maiaframework.gen.schema.expected.ExpectedColumnDef
import org.maiaframework.gen.schema.expected.ExpectedCompositeForeignKeyDef
import org.maiaframework.gen.schema.expected.ExpectedForeignKeyDef
import org.maiaframework.gen.schema.expected.ExpectedIndexDef
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName


data class TableDiff(
    val schemaAndTableName: String,
    val status: TableStatus,
    val missingColumns: List<ExpectedColumnDef> = emptyList(),
    val extraColumns: List<TableColumnName> = emptyList(),
    val mismatchedColumns: List<ColumnMismatch> = emptyList(),
    val primaryKeyMismatch: PrimaryKeyMismatch? = null,
    val missingForeignKeys: List<ExpectedForeignKeyDef> = emptyList(),
    val extraForeignKeys: List<TableColumnName> = emptyList(),
    val missingCompositeForeignKeys: List<ExpectedCompositeForeignKeyDef> = emptyList(),
    val missingIndexes: List<ExpectedIndexDef> = emptyList(),
    val extraIndexes: List<String> = emptyList(),
) {

    val hasErrors: Boolean
        get() = status == TableStatus.MISSING
                || missingColumns.isNotEmpty()
                || mismatchedColumns.isNotEmpty()
                || primaryKeyMismatch != null
                || missingForeignKeys.isNotEmpty()
                || missingCompositeForeignKeys.isNotEmpty()
                || missingIndexes.isNotEmpty()

    val hasWarnings: Boolean
        get() = status == TableStatus.EXTRA
                || extraColumns.isNotEmpty()
                || extraForeignKeys.isNotEmpty()
                || extraIndexes.isNotEmpty()

}
