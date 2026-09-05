package org.maiaframework.gen.schemacheck

import org.maiaframework.gen.schema.expected.ExpectedTableDef
import org.maiaframework.gen.schemacheck.actual.ActualTableDef


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
        val actualColumnsByName = actual.columnDefs.associateBy { it.name }

        val missingColumns = expected.columns.filter { !actualColumnsByName.containsKey(it.name) }
        val extraColumns = actual.columnDefs.map { it.name }.filter { !expectedColumnsByName.containsKey(it) }

        val mismatchedColumns = expected.columns.mapNotNull { expectedColumn ->
            val actualColumn = actualColumnsByName[expectedColumn.name] ?: return@mapNotNull null
            if (actualColumn.postgresType != expectedColumn.postgresType || actualColumn.nullable != expectedColumn.nullable) {
                ColumnMismatch(
                    name = expectedColumn.name.value,
                    expectedType = expectedColumn.postgresType,
                    actualType = actualColumn.postgresType,
                    expectedNullable = expectedColumn.nullable,
                    actualNullable = actualColumn.nullable,
                )
            } else {
                null
            }
        }

        val primaryKeyMismatch = if (expected.primaryKeyColumnNames() != actual.primaryKeyColumnNames) {
            PrimaryKeyMismatch(expected.primaryKeyColumnNames(), actual.primaryKeyColumnNames, actual.primaryKeyConstraintName)
        } else {
            null
        }

        val actualForeignKeyColumns = actual.foreignKeys.map { it.columnName }.toSet()

        val missingForeignKeys = expected.foreignKeys.filter { it.columnName !in actualForeignKeyColumns }
        val missingCompositeForeignKeys = expected.compositeForeignKeys.filter { composite ->
            composite.columnNames.any { it !in actualForeignKeyColumns }
        }

        val expectedForeignKeyColumns = expected.foreignKeys.map { it.columnName }
            .plus(expected.compositeForeignKeys.flatMap { it.columnNames })
            .toSet()
        val extraForeignKeys = actual.foreignKeys.map { it.columnName }.filter { it !in expectedForeignKeyColumns }

        val actualIndexColumnSets = actual.indexes.map { it.columns.toSet() }.toSet()
        val missingIndexes = expected.indexes.filter { it.columns.toSet() !in actualIndexColumnSets }
        val expectedIndexColumnSets = expected.indexes.map { it.columns.toSet() }.toSet()
        val extraIndexes = actual.indexes.filter { it.columns.toSet() !in expectedIndexColumnSets }.map { it.name }

        val status = if (missingColumns.isEmpty() && mismatchedColumns.isEmpty() && primaryKeyMismatch == null
            && missingForeignKeys.isEmpty() && missingCompositeForeignKeys.isEmpty() && missingIndexes.isEmpty()
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
            missingCompositeForeignKeys = missingCompositeForeignKeys,
            missingIndexes = missingIndexes,
            extraIndexes = extraIndexes,
        )

    }

}
