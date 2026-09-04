package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.gen.schema.expected.ExpectedColumnDef
import org.maiaframework.gen.schema.expected.ExpectedCompositeForeignKeyDef
import org.maiaframework.gen.schema.expected.ExpectedForeignKeyDef
import org.maiaframework.gen.schema.expected.ExpectedIndexDef
import org.maiaframework.gen.schema.expected.ExpectedTableDef

class SchemaComparatorTest {

    private val comparator = SchemaComparator()

    @Test
    fun `reports a missing table as an error`() {

        val expected = listOf(ExpectedTableDef("app.widget", emptyList(), listOf("id"), emptyList(), emptyList()))

        val report = comparator.compare(expected, actual = emptyList())

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().status).isEqualTo(TableStatus.MISSING)

    }

    @Test
    fun `reports an extra table as a warning, not an error`() {

        val actual = listOf(ActualTableDef("app.leftover", emptyList(), listOf("id"), emptyList(), emptyList()))

        val report = comparator.compare(expected = emptyList(), actual)

        assertThat(report.hasErrors).isFalse()
        assertThat(report.tables.single().status).isEqualTo(TableStatus.EXTRA)

    }

    @Test
    fun `reports a missing column as an error`() {

        val expected = listOf(
            ExpectedTableDef("app.widget", listOf(ExpectedColumnDef("name", "text", false)), listOf("id"), emptyList(), emptyList())
        )
        val actual = listOf(ActualTableDef("app.widget", emptyList(), listOf("id"), emptyList(), emptyList()))

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().missingColumns).containsExactly(ExpectedColumnDef("name", "text", false))

    }

    @Test
    fun `reports an extra column as a warning`() {

        val expected = listOf(ExpectedTableDef("app.widget", emptyList(), listOf("id"), emptyList(), emptyList()))
        val actual = listOf(
            ActualTableDef("app.widget", listOf(ActualColumnDef("leftover", "text", false)), listOf("id"), emptyList(), emptyList())
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isFalse()
        assertThat(report.tables.single().extraColumns).containsExactly("leftover")

    }

    @Test
    fun `reports a column type mismatch as an error`() {

        val expected = listOf(
            ExpectedTableDef("app.widget", listOf(ExpectedColumnDef("count", "integer", false)), listOf("id"), emptyList(), emptyList())
        )
        val actual = listOf(
            ActualTableDef("app.widget", listOf(ActualColumnDef("count", "bigint", false)), listOf("id"), emptyList(), emptyList())
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().mismatchedColumns).containsExactly(
            ColumnMismatch("count", "integer", "bigint", false, false)
        )

    }

    @Test
    fun `reports a nullable mismatch as an error`() {

        val expected = listOf(
            ExpectedTableDef("app.widget", listOf(ExpectedColumnDef("name", "text", false)), listOf("id"), emptyList(), emptyList())
        )
        val actual = listOf(
            ActualTableDef("app.widget", listOf(ActualColumnDef("name", "text", true)), listOf("id"), emptyList(), emptyList())
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().mismatchedColumns).containsExactly(
            ColumnMismatch("name", "text", "text", false, true)
        )

    }

    @Test
    fun `reports a primary key mismatch as an error`() {

        val expected = listOf(ExpectedTableDef("app.widget", emptyList(), listOf("id"), emptyList(), emptyList()))
        val actual = listOf(ActualTableDef("app.widget", emptyList(), listOf("id", "version"), emptyList(), emptyList()))

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().primaryKeyMismatch).isEqualTo(PrimaryKeyMismatch(listOf("id"), listOf("id", "version")))

    }

    @Test
    fun `reports a missing foreign key as an error and an extra one as a warning`() {

        val expected = listOf(
            ExpectedTableDef(
                "app.child", emptyList(), listOf("id"),
                listOf(ExpectedForeignKeyDef("parent_id", "app.parent", "id")), emptyList()
            )
        )
        val actual = listOf(
            ActualTableDef(
                "app.child", emptyList(), listOf("id"),
                listOf(ActualForeignKeyDef("other_id", "app.other", "id")), emptyList()
            )
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        val diff = report.tables.single()
        assertThat(diff.missingForeignKeys).containsExactly(ExpectedForeignKeyDef("parent_id", "app.parent", "id"))
        assertThat(diff.extraForeignKeys).containsExactly("other_id")

    }

    @Test
    fun `reports a missing index as an error and an extra one as a warning`() {

        val expected = listOf(
            ExpectedTableDef("app.widget", emptyList(), listOf("id"), emptyList(), listOf(ExpectedIndexDef("widget_name_idx", listOf("name"), false)))
        )
        val actual = listOf(
            ActualTableDef("app.widget", emptyList(), listOf("id"), emptyList(), listOf(ActualIndexDef("widget_extra_idx", listOf("other"), false)))
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        val diff = report.tables.single()
        assertThat(diff.missingIndexes).containsExactly(ExpectedIndexDef("widget_name_idx", listOf("name"), false))
        assertThat(diff.extraIndexes).containsExactly("widget_extra_idx")

    }

    @Test
    fun `does not flag a real composite foreign key's columns as extra`() {

        val expected = listOf(
            ExpectedTableDef(
                "app.child_history", emptyList(), listOf("id", "version"), emptyList(), emptyList(),
                compositeForeignKeys = listOf(
                    ExpectedCompositeForeignKeyDef(listOf("parent_id", "parent_version"), "app.parent_history", listOf("id", "version"))
                ),
            )
        )
        val actual = listOf(
            ActualTableDef(
                "app.child_history",
                emptyList(),
                listOf("id", "version"),
                foreignKeys = listOf(
                    ActualForeignKeyDef("parent_id", "app.parent_history", "id"),
                    ActualForeignKeyDef("parent_version", "app.parent_history", "version"),
                ),
                indexes = emptyList(),
            )
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isFalse()
        assertThat(report.tables.single().extraForeignKeys).isEmpty()
        assertThat(report.tables.single().missingCompositeForeignKeys).isEmpty()

    }

    @Test
    fun `reports a missing composite foreign key as an error`() {

        val expected = listOf(
            ExpectedTableDef(
                "app.child_history", emptyList(), listOf("id", "version"), emptyList(), emptyList(),
                compositeForeignKeys = listOf(
                    ExpectedCompositeForeignKeyDef(listOf("parent_id", "parent_version"), "app.parent_history", listOf("id", "version"))
                ),
            )
        )
        val actual = listOf(ActualTableDef("app.child_history", emptyList(), listOf("id", "version"), emptyList(), emptyList()))

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().missingCompositeForeignKeys).hasSize(1)

    }

    @Test
    fun `reports a composite foreign key as missing when only one of its columns is present`() {

        val expected = listOf(
            ExpectedTableDef(
                "app.child_history", emptyList(), listOf("id", "version"), emptyList(), emptyList(),
                compositeForeignKeys = listOf(
                    ExpectedCompositeForeignKeyDef(listOf("parent_id", "parent_version"), "app.parent_history", listOf("id", "version"))
                ),
            )
        )
        val actual = listOf(
            ActualTableDef(
                "app.child_history",
                emptyList(),
                listOf("id", "version"),
                foreignKeys = listOf(
                    ActualForeignKeyDef("parent_id", "app.parent_history", "id"),
                ),
                indexes = emptyList(),
            )
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().missingCompositeForeignKeys).hasSize(1)
        assertThat(report.tables.single().extraForeignKeys).isEmpty()

    }

    @Test
    fun `a table matching exactly has no errors and OK status`() {

        val table = ExpectedTableDef("app.widget", listOf(ExpectedColumnDef("name", "text", false)), listOf("id"), emptyList(), emptyList())
        val actualTable = ActualTableDef("app.widget", listOf(ActualColumnDef("name", "text", false)), listOf("id"), emptyList(), emptyList())

        val report = comparator.compare(listOf(table), listOf(actualTable))

        assertThat(report.hasErrors).isFalse()
        assertThat(report.tables.single().status).isEqualTo(TableStatus.OK)

    }

}
