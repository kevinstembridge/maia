package org.maiaframework.gen.schemacheck

import org.maiaframework.gen.schema.ExpectedColumnDef
import org.maiaframework.gen.schema.ExpectedForeignKeyDef
import org.maiaframework.gen.schema.ExpectedIndexDef
import org.maiaframework.gen.schema.ExpectedTableDef

enum class TableStatus { OK, MISSING, EXTRA, MISMATCHED }

data class ColumnMismatch(
    val name: String,
    val expectedType: String,
    val actualType: String,
    val expectedNullable: Boolean,
    val actualNullable: Boolean,
)

data class PrimaryKeyMismatch(val expected: List<String>, val actual: List<String>)

data class TableDiff(
    val schemaAndTableName: String,
    val status: TableStatus,
    val missingColumns: List<ExpectedColumnDef> = emptyList(),
    val extraColumns: List<String> = emptyList(),
    val mismatchedColumns: List<ColumnMismatch> = emptyList(),
    val primaryKeyMismatch: PrimaryKeyMismatch? = null,
    val missingForeignKeys: List<ExpectedForeignKeyDef> = emptyList(),
    val extraForeignKeys: List<String> = emptyList(),
    val missingIndexes: List<ExpectedIndexDef> = emptyList(),
    val extraIndexes: List<String> = emptyList(),
) {

    val hasErrors: Boolean
        get() = status == TableStatus.MISSING
                || missingColumns.isNotEmpty()
                || mismatchedColumns.isNotEmpty()
                || primaryKeyMismatch != null
                || missingForeignKeys.isNotEmpty()
                || missingIndexes.isNotEmpty()

}

data class SchemaDiffReport(val tables: List<TableDiff>) {

    val hasErrors: Boolean get() = tables.any { it.hasErrors }

}

class SchemaComparator {

    fun compare(expected: List<ExpectedTableDef>, actual: List<ActualTableDef>): SchemaDiffReport {

        val actualByName = actual.associateBy { it.schemaAndTableName }
        val expectedNames = expected.map { it.schemaAndTableName }.toSet()

        val matchedDiffs = expected.map { expectedTable ->
            val actualTable = actualByName[expectedTable.schemaAndTableName]
            if (actualTable == null) {
                TableDiff(expectedTable.schemaAndTableName, TableStatus.MISSING)
            } else {
                diffTable(expectedTable, actualTable)
            }
        }

        val extraTableDiffs = actual
            .filterNot { expectedNames.contains(it.schemaAndTableName) }
            .map { TableDiff(it.schemaAndTableName, TableStatus.EXTRA) }

        return SchemaDiffReport(matchedDiffs.plus(extraTableDiffs))

    }

    private fun diffTable(expected: ExpectedTableDef, actual: ActualTableDef): TableDiff {

        val expectedColumnsByName = expected.columns.associateBy { it.name }
        val actualColumnsByName = actual.columns.associateBy { it.name }

        val missingColumns = expected.columns.filter { !actualColumnsByName.containsKey(it.name) }
        val extraColumns = actual.columns.map { it.name }.filter { !expectedColumnsByName.containsKey(it) }

        val mismatchedColumns = expected.columns.mapNotNull { expectedColumn ->
            val actualColumn = actualColumnsByName[expectedColumn.name] ?: return@mapNotNull null
            if (actualColumn.postgresType != expectedColumn.postgresType || actualColumn.nullable != expectedColumn.nullable) {
                ColumnMismatch(
                    name = expectedColumn.name,
                    expectedType = expectedColumn.postgresType,
                    actualType = actualColumn.postgresType,
                    expectedNullable = expectedColumn.nullable,
                    actualNullable = actualColumn.nullable,
                )
            } else {
                null
            }
        }

        val primaryKeyMismatch = if (expected.primaryKeyColumns != actual.primaryKeyColumns) {
            PrimaryKeyMismatch(expected.primaryKeyColumns, actual.primaryKeyColumns)
        } else {
            null
        }

        val actualForeignKeyColumns = actual.foreignKeys.map { it.columnName }.toSet()
        val missingForeignKeys = expected.foreignKeys.filter { it.columnName !in actualForeignKeyColumns }
        val expectedForeignKeyColumns = expected.foreignKeys.map { it.columnName }.toSet()
        val extraForeignKeys = actual.foreignKeys.map { it.columnName }.filter { it !in expectedForeignKeyColumns }

        val actualIndexColumnSets = actual.indexes.map { it.columns.toSet() }.toSet()
        val missingIndexes = expected.indexes.filter { it.columns.toSet() !in actualIndexColumnSets }
        val expectedIndexColumnSets = expected.indexes.map { it.columns.toSet() }.toSet()
        val extraIndexes = actual.indexes.filter { it.columns.toSet() !in expectedIndexColumnSets }.map { it.name }

        val status = if (missingColumns.isEmpty() && mismatchedColumns.isEmpty() && primaryKeyMismatch == null
            && missingForeignKeys.isEmpty() && missingIndexes.isEmpty()
            && extraColumns.isEmpty() && extraForeignKeys.isEmpty() && extraIndexes.isEmpty()
        ) {
            TableStatus.OK
        } else {
            TableStatus.MISMATCHED
        }

        return TableDiff(
            schemaAndTableName = expected.schemaAndTableName,
            status = status,
            missingColumns = missingColumns,
            extraColumns = extraColumns,
            mismatchedColumns = mismatchedColumns,
            primaryKeyMismatch = primaryKeyMismatch,
            missingForeignKeys = missingForeignKeys,
            extraForeignKeys = extraForeignKeys,
            missingIndexes = missingIndexes,
            extraIndexes = extraIndexes,
        )

    }

}
