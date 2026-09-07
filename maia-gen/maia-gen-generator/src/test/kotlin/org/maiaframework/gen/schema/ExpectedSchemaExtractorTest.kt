package org.maiaframework.gen.schema

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.gen.schema.expected.ExpectedColumnDef
import org.maiaframework.gen.schema.expected.ExpectedCompositeForeignKeyDef
import org.maiaframework.gen.schema.expected.ExpectedForeignKeyDef
import org.maiaframework.gen.schema.expected.ExpectedSchemaExtractor
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ReferencedEntity
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.FieldTypes

class ExpectedSchemaExtractorTest {


    @Test
    fun `extracts columns, types, nullability and PK for a plain entity`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val widget = entity("com.example", "Widget") {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
                field("description", FieldTypes.string) { lengthConstraint(max = 200); nullable() }
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)
        val widgetTable = tables.single { it.schemaAndTableName == "test.widget" }

        assertThat(widgetTable.primaryKeyColumnNames()).containsExactly(TableColumnName("id"))
        assertThat(widgetTable.columns).contains(
            ExpectedColumnDef(TableColumnName("id"), "uuid", nullable = false, isPrimaryKey = true),
            // FieldTypes.string always maps to JdbcCompatibleType.text (never .varchar), so a
            // lengthConstraint currently has no effect on the emitted column type.
            ExpectedColumnDef(TableColumnName("name"), "text", nullable = false, isPrimaryKey = false),
            ExpectedColumnDef(TableColumnName("description"), "text", nullable = true, isPrimaryKey = false),
        )

    }


    @Test
    fun `extracts a foreign key column`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val parent = entity("com.example", "Parent") {}
            val child = entity("com.example", "Child") {
                foreignKey("parent", parent)
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)
        val childTable = tables.single { it.schemaAndTableName == "test.child" }

        assertThat(childTable.foreignKeys).containsExactly(
            ExpectedForeignKeyDef(TableColumnName("parent_id"), "test.parent", TableColumnName("id"))
        )

    }


    @Test
    fun `extracts an index`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val widget = entity("com.example", "Widget") {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
                index { withFieldAscending("name") }
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)
        val widgetTable = tables.single { it.schemaAndTableName == "test.widget" }

        assertThat(widgetTable.indexes).hasSize(1)
        assertThat(widgetTable.indexes.single().columns).containsExactly(TableColumnName("name"))
        assertThat(widgetTable.indexes.single().unique).isFalse()

    }


    @Test
    fun `extracts a separate table for a history entity`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val widget = entity("com.example", "Widget", recordVersionHistory = true) {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)

        assertThat(tables.map { it.schemaAndTableName }).contains("test.widget", "test.widget_history")

    }


    @Test
    fun `extracts a separate table for a many-to-many join entity`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val left = entity("com.example", "Left", nameFieldForPkAndNameDto = "name") {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }
            val right = entity("com.example", "Right", nameFieldForPkAndNameDto = "name") {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }
            // Field names deliberately omit a trailing "Id" — simpleManyToManyEntity ultimately
            // calls the same foreignKey(fieldName, entityDef) builder that appends "_id" itself
            // when the target has a surrogate PK, matching the real convention used in
            // maia-showcase/spec (e.g. ReferencedEntity("userGroup", ...)).
            val join = simpleManyToManyEntity(
                "com.example", "LeftRight",
                leftEntity = ReferencedEntity(fieldName = "left", displayName = "Left", entityDef = left),
                rightEntity = ReferencedEntity(fieldName = "right", displayName = "Right", entityDef = right),
            )
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)

        assertThat(tables.map { it.schemaAndTableName }).contains("test.left_right")

    }


    @Test
    fun `extracts an effective_range column for an entity with effective timestamps`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val widget = entity("com.example", "Widget") {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
                withEffectiveTimestamps()
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)
        val widgetTable = tables.single { it.schemaAndTableName == "test.widget" }

        assertThat(widgetTable.columns).contains(
            ExpectedColumnDef(TableColumnName.effectiveRange, "tstzrange", nullable = false, isPrimaryKey = false)
        )

    }


    @Test
    fun `extracts an exclusion constraint index for a single-effective-record entity`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val widget = entity("com.example", "Widget") {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
                index { withFieldAscending("name") }
                withEffectiveTimestamps(hasSingleEffectiveRecord = true)
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)
        val widgetTable = tables.single { it.schemaAndTableName == "test.widget" }

        assertThat(widgetTable.indexes).hasSize(2)
        val baseIndex = widgetTable.indexes.single { !it.name.endsWith("_excl") }
        val exclusionIndex = widgetTable.indexes.single { it.name == "${baseIndex.name}_excl" }

        assertThat(exclusionIndex.columns).containsExactly(*baseIndex.columns.toTypedArray(), TableColumnName.effectiveRange)
        assertThat(exclusionIndex.unique).isFalse()

    }


    @Test
    fun `extracts a composite foreign key when a history entity FKs another history entity`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val parent = entity("com.example", "Parent", recordVersionHistory = true) {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }
            val child = entity("com.example", "Child", recordVersionHistory = true) {
                foreignKey("parent", parent)
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)
        val childHistoryTable = tables.single { it.schemaAndTableName == "test.child_history" }

        assertThat(childHistoryTable.foreignKeys).isEmpty()
        assertThat(childHistoryTable.columns.map { it.name }).contains(TableColumnName("parent_version"))
        assertThat(childHistoryTable.compositeForeignKeys).containsExactly(
            ExpectedCompositeForeignKeyDef(
                columnNames = listOf(TableColumnName("parent_id"), TableColumnName("parent_version")),
                referencedSchemaAndTable = "test.parent_history",
                referencedColumns = listOf(TableColumnName("id"), TableColumnName("version")),
            )
        )

        val childTable = tables.single { it.schemaAndTableName == "test.child" }
        assertThat(childTable.foreignKeys).containsExactly(
            ExpectedForeignKeyDef(TableColumnName("parent_id"), "test.parent", TableColumnName("id"))
        )

    }


}
