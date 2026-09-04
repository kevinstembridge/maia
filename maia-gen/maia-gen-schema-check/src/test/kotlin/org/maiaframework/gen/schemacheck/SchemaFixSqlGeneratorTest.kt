package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.gen.schema.expected.ExpectedColumnDef
import org.maiaframework.gen.schema.expected.ExpectedCompositeForeignKeyDef
import org.maiaframework.gen.schema.expected.ExpectedForeignKeyDef
import org.maiaframework.gen.schema.expected.ExpectedIndexDef

class SchemaFixSqlGeneratorTest {

    @Test
    fun `reports no drift found when nothing needs fixing`() {

        val report = SchemaDiffReport(listOf(TableDiff("app.widget", TableStatus.OK)))

        val sql = SchemaFixSqlGenerator.generate(report)

        assertThat(sql).contains("No drift found")
        assertThat(sql).doesNotContain("ALTER TABLE")

    }

    @Test
    fun `adds a nullable missing column directly`() {

        val table = TableDiff(
            "app.widget", TableStatus.MISMATCHED,
            missingColumns = listOf(ExpectedColumnDef("notes", "text", nullable = true)),
        )

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(table)))

        assertThat(sql).contains("ALTER TABLE app.widget ADD COLUMN notes text;")
        assertThat(sql).doesNotContain("TODO")

    }

    @Test
    fun `adds a non-nullable missing column with a commented-out back-fill and SET NOT NULL`() {

        val table = TableDiff(
            "app.widget", TableStatus.MISMATCHED,
            missingColumns = listOf(ExpectedColumnDef("archived_at", "timestamptz", nullable = false)),
        )

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(table)))

        assertThat(sql).contains("ALTER TABLE app.widget ADD COLUMN archived_at timestamptz;")
        assertThat(sql).contains("-- TODO: back-fill 'archived_at' on existing rows, then:")
        assertThat(sql).contains("-- ALTER TABLE app.widget ALTER COLUMN archived_at SET NOT NULL;")

    }

    @Test
    fun `fixes a mismatched column type with an ALTER COLUMN TYPE USING cast`() {

        val table = TableDiff(
            "app.widget", TableStatus.MISMATCHED,
            mismatchedColumns = listOf(ColumnMismatch("count", "integer", "bigint", false, false)),
        )

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(table)))

        assertThat(sql).contains("ALTER TABLE app.widget ALTER COLUMN count TYPE integer USING count::integer;")

    }

    @Test
    fun `relaxes a column to nullable directly`() {

        val table = TableDiff(
            "app.widget", TableStatus.MISMATCHED,
            mismatchedColumns = listOf(ColumnMismatch("name", "text", "text", expectedNullable = true, actualNullable = false)),
        )

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(table)))

        assertThat(sql).contains("ALTER TABLE app.widget ALTER COLUMN name DROP NOT NULL;")

    }

    @Test
    fun `tightens a column to not-null behind a commented-out back-fill TODO`() {

        val table = TableDiff(
            "app.widget", TableStatus.MISMATCHED,
            mismatchedColumns = listOf(ColumnMismatch("name", "text", "text", expectedNullable = false, actualNullable = true)),
        )

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(table)))

        assertThat(sql).contains("-- TODO: back-fill 'name' on existing rows, then:")
        assertThat(sql).contains("-- ALTER TABLE app.widget ALTER COLUMN name SET NOT NULL;")

    }

    @Test
    fun `fixes a primary key mismatch using the actual constraint name when known`() {

        val table = TableDiff(
            "app.widget", TableStatus.MISMATCHED,
            primaryKeyMismatch = PrimaryKeyMismatch(listOf("id"), listOf("id", "version"), actualConstraintName = "widget_id_version_pkey"),
        )

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(table)))

        assertThat(sql).contains("ALTER TABLE app.widget DROP CONSTRAINT IF EXISTS widget_id_version_pkey;")
        assertThat(sql).contains("ALTER TABLE app.widget ADD PRIMARY KEY (id);")

    }

    @Test
    fun `falls back to the default Postgres pkey naming convention when the actual constraint name is unknown`() {

        val table = TableDiff(
            "app.widget", TableStatus.MISMATCHED,
            primaryKeyMismatch = PrimaryKeyMismatch(listOf("id"), listOf("id", "version")),
        )

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(table)))

        assertThat(sql).contains("ALTER TABLE app.widget DROP CONSTRAINT IF EXISTS widget_pkey;")

    }

    @Test
    fun `adds a missing foreign key with a Postgres-style default constraint name`() {

        val table = TableDiff(
            "app.child", TableStatus.MISMATCHED,
            missingForeignKeys = listOf(ExpectedForeignKeyDef("parent_id", "app.parent", "id")),
        )

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(table)))

        assertThat(sql).contains(
            "ALTER TABLE app.child ADD CONSTRAINT child_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES app.parent(id);"
        )

    }

    @Test
    fun `adds a missing composite foreign key with a Postgres-style default constraint name`() {

        val table = TableDiff(
            "app.child_history", TableStatus.MISMATCHED,
            missingCompositeForeignKeys = listOf(
                ExpectedCompositeForeignKeyDef(listOf("parent_id", "parent_version"), "app.parent_history", listOf("id", "version"))
            ),
        )

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(table)))

        assertThat(sql).contains(
            "ALTER TABLE app.child_history ADD CONSTRAINT child_history_parent_id_fkey " +
                "FOREIGN KEY (parent_id, parent_version) REFERENCES app.parent_history(id, version);"
        )

    }

    @Test
    fun `creates a missing index, unique when expected`() {

        val table = TableDiff(
            "app.widget", TableStatus.MISMATCHED,
            missingIndexes = listOf(ExpectedIndexDef("widget_name_idx", listOf("name"), unique = true)),
        )

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(table)))

        assertThat(sql).contains("CREATE UNIQUE INDEX widget_name_idx ON app.widget (name);")

    }

    @Test
    fun `flags an entirely missing table without attempting to synthesize its DDL`() {

        val table = TableDiff("app.widget", TableStatus.MISSING)

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(table)))

        assertThat(sql).contains("app.widget")
        assertThat(sql).contains("sqlCreateScriptsDir")
        assertThat(sql).doesNotContain("CREATE TABLE")

    }

    @Test
    fun `comments out drops for warnings-only drift instead of applying them directly`() {

        val extraTable = TableDiff("app.leftover", TableStatus.EXTRA)
        val extraColumn = TableDiff("app.widget", TableStatus.MISMATCHED, extraColumns = listOf("legacy_flag"))
        val extraIndex = TableDiff("app.widget2", TableStatus.MISMATCHED, extraIndexes = listOf("widget2_extra_idx"))
        val extraForeignKey = TableDiff("app.widget3", TableStatus.MISMATCHED, extraForeignKeys = listOf("other_id"))

        val sql = SchemaFixSqlGenerator.generate(SchemaDiffReport(listOf(extraTable, extraColumn, extraIndex, extraForeignKey)))

        assertThat(sql).contains("-- DROP TABLE app.leftover;")
        assertThat(sql).contains("-- ALTER TABLE app.widget DROP COLUMN legacy_flag;")
        assertThat(sql).contains("-- DROP INDEX widget2_extra_idx;")
        assertThat(sql).contains("extra foreign key on column 'other_id'")

        // None of the warning-driven fixes should ever be emitted as live, runnable statements.
        assertThat(sql).doesNotContain("\nDROP TABLE")
        assertThat(sql).doesNotContain("\nALTER TABLE app.widget DROP COLUMN")
        assertThat(sql).doesNotContain("\nDROP INDEX")

    }

}
