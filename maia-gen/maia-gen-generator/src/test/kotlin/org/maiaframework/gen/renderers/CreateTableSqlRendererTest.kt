package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File

class CreateTableSqlRendererTest {

    @Test
    fun `renders columns, types, nullability, PK, FK and index for a plain entity hierarchy`(@TempDir tempDir: File) {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val parent = entity("com.example", "Parent") {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }

            val child = entity("com.example", "Child") {
                field("title", FieldTypes.string) { lengthConstraint(max = 100); nullable() }
                field("count", FieldTypes.int)
                foreignKey("parentId", parent)
                index { withFieldAscending("count") }
            }

        }

        val renderer = CreateTableSqlRenderer(spec.modelDef.rootEntityHierarchies, "create_tables.sql")
        renderer.renderToDir(tempDir)

        val sql = tempDir.resolve("create_tables.sql").readText()

        assertThat(sql).contains("CREATE TABLE test.parent (")
        // FieldTypes.string always maps to JdbcCompatibleType.text (never .varchar), so a
        // lengthConstraint currently has no effect on the emitted column type or size suffix.
        // This is current, real behavior being characterized here, not an assumption.
        assertThat(sql).contains("name text NOT NULL")
        assertThat(sql).contains("CREATE TABLE test.child (")
        assertThat(sql).contains("title text NULL")
        assertThat(sql).contains("count integer NOT NULL")
        // Surrogate PKs are `uuid`, and a foreign key field name gets an extra "_id" suffix
        // appended on top of its own camelCase-to-snake_case conversion when the referenced
        // entity has a surrogate primary key (see ForeignKeyFieldDefBuilder.tableColumnName).
        assertThat(sql).contains("parent_id_id uuid NOT NULL REFERENCES test.parent(id)")
        assertThat(sql).contains("PRIMARY KEY(id)")
        assertThat(sql).containsPattern("CREATE INDEX \\w+ ON test\\.child\\(count\\);")

    }

    @Test
    fun `renders a single merged table with a type_discriminator column for a subclass hierarchy`(@TempDir tempDir: File) {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val vehicle = entity("com.example", "Vehicle") {
                typeDiscriminator("VEHICLE")
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }

            val car = entity("com.example", "Car") {
                superclass(vehicle)
                typeDiscriminator("CAR")
                field("doors", FieldTypes.int)
            }

        }

        val renderer = CreateTableSqlRenderer(spec.modelDef.rootEntityHierarchies, "create_tables.sql")
        renderer.renderToDir(tempDir)

        val sql = tempDir.resolve("create_tables.sql").readText()

        assertThat(sql).contains("CREATE TABLE test.vehicle (")
        assertThat(sql).doesNotContain("CREATE TABLE test.car (")
        assertThat(sql).contains("type_discriminator text not null")
        assertThat(sql).contains("name text NOT NULL")
        // Subclass-only fields are merged into the shared table and become nullable,
        // since they only apply to rows of that subclass's type_discriminator.
        assertThat(sql).contains("doors integer NULL")

    }

    @Test
    fun `renders a separate history table for a recordVersionHistory entity`(@TempDir tempDir: File) {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val base = entity("com.example", "Base", recordVersionHistory = true) {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }

        }

        val renderer = CreateTableSqlRenderer(spec.modelDef.rootEntityHierarchies, "create_tables.sql")
        renderer.renderToDir(tempDir)

        val sql = tempDir.resolve("create_tables.sql").readText()

        assertThat(sql).contains("CREATE TABLE test.base (")
        assertThat(sql).contains("CREATE TABLE test.base_history (")

    }

}
