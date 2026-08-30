package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.maiaframework.testing.postgresql.SingletonPostgresqlContainer
import java.sql.Connection
import java.sql.DriverManager

class PostgresSchemaIntrospectorTest {

    companion object {

        private lateinit var connection: Connection

        @JvmStatic
        @BeforeAll
        fun setUp() {

            val container = SingletonPostgresqlContainer.instance.also { it.start() }
            connection = DriverManager.getConnection(container.jdbcUrl, container.username, container.password)

            connection.createStatement().use { statement ->
                statement.execute("create schema if not exists introspector_test")
                statement.execute("create schema if not exists introspector_empty_test")

                statement.execute(
                    """
                    create table introspector_test.parent (
                        id bigint not null,
                        name varchar(50) not null,
                        primary key (id)
                    )
                    """.trimIndent()
                )
                // Column declaration order deliberately puts "count" before "title" (attnum(count) < attnum(title)),
                // while the composite index below is declared as (title, count) — the reverse of both attnum
                // order and alphabetical order. This disambiguates a correctly-ordered `array_position` result
                // from an accidental attnum-order or alphabetical-order result.
                statement.execute(
                    """
                    create table introspector_test.child (
                        id bigint not null,
                        count integer not null,
                        title varchar(100),
                        parent_id bigint not null references introspector_test.parent(id),
                        primary key (id)
                    )
                    """.trimIndent()
                )
                statement.execute("create unique index parent_name_idx on introspector_test.parent(name)")
                statement.execute("create index child_title_idx on introspector_test.child(title)")
                statement.execute("create index child_title_count_idx on introspector_test.child(title, count)")

                // A view has no place on the expected side (ExpectedSchemaExtractor only derives
                // tables from spec entities), so it must never be reported by the introspector —
                // otherwise it would surface as a permanent, spurious "extra table" diff.
                statement.execute("create view introspector_test.parent_view as select id, name from introspector_test.parent")
            }

        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            connection.createStatement().use { statement ->
                statement.execute("drop schema introspector_test cascade")
                statement.execute("drop schema introspector_empty_test cascade")
            }
            connection.close()
        }

    }

    @Test
    fun `introspects tables, columns, PK, FK and indexes`() {

        val tables = PostgresSchemaIntrospector(connection).introspectSchemas(setOf("introspector_test"))

        // containsExactlyInAnyOrder here already implicitly excludes the view created in setUp(),
        // but assert it explicitly too since that's the specific behavior under test.
        assertThat(tables.map { it.schemaAndTableName })
            .containsExactlyInAnyOrder("introspector_test.parent", "introspector_test.child")
        assertThat(tables.map { it.schemaAndTableName }).doesNotContain("introspector_test.parent_view")

        val parent = tables.single { it.schemaAndTableName == "introspector_test.parent" }
        assertThat(parent.primaryKeyColumns).containsExactly("id")
        assertThat(parent.columns).contains(
            ActualColumnDef("id", "bigint", nullable = false),
            ActualColumnDef("name", "varchar(50)", nullable = false),
        )
        assertThat(parent.foreignKeys).isEmpty()
        val parentNameIndex = parent.indexes.single { it.name == "parent_name_idx" }
        assertThat(parentNameIndex.columns).containsExactly("name")
        assertThat(parentNameIndex.unique).isTrue()

        val child = tables.single { it.schemaAndTableName == "introspector_test.child" }
        assertThat(child.primaryKeyColumns).containsExactly("id")
        assertThat(child.columns).contains(
            ActualColumnDef("count", "integer", nullable = false),
            ActualColumnDef("title", "varchar(100)", nullable = true),
        )
        assertThat(child.foreignKeys).containsExactly(
            ActualForeignKeyDef("parent_id", "introspector_test.parent", "id")
        )

        val singleColumnIndex = child.indexes.single { it.name == "child_title_idx" }
        assertThat(singleColumnIndex.columns).containsExactly("title")
        assertThat(singleColumnIndex.unique).isFalse()

        // The specific risk under test: array_position(ix.indkey::int2[], a.attnum) must preserve the
        // declared column order (title, count), not attnum order (count, title) or alphabetical order
        // (count, title) — both of which coincide with each other but differ from the declared order.
        val compositeIndex = child.indexes.single { it.name == "child_title_count_idx" }
        assertThat(compositeIndex.columns).containsExactly("title", "count")
        assertThat(compositeIndex.unique).isFalse()

    }

    @Test
    fun `returns an empty list for a schema with no tables`() {

        val tables = PostgresSchemaIntrospector(connection).introspectSchemas(setOf("introspector_empty_test"))

        assertThat(tables).isEmpty()

    }

    @Test
    fun `returns an empty list when no schemas are requested`() {

        val tables = PostgresSchemaIntrospector(connection).introspectSchemas(emptySet())

        assertThat(tables).isEmpty()

    }

}
