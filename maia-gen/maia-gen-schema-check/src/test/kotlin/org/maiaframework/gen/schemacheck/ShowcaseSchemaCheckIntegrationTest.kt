package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.maiaframework.gen.generator.ApplicationModelDefInstantiator
import org.maiaframework.gen.schema.ExpectedSchemaExtractor
import org.maiaframework.testing.postgresql.SingletonPostgresqlContainer
import java.sql.DriverManager

// SingletonPostgresqlContainer.instance is shared (and never stopped) across the whole JVM test
// run, so the two tests below share schema state. Order matters: the "matches" assertion must
// run before the "dropped column" test mutates the shared schema.
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ShowcaseSchemaCheckIntegrationTest {

    @Test
    @Order(1)
    fun `maia-showcase's real Flyway migrations match its real spec`() {

        val container = SingletonPostgresqlContainer.instance.also { it.start() }

        Flyway.configure()
            .dataSource(container.jdbcUrl, container.username, container.password)
            .locations("filesystem:" + System.getProperty("maiaShowcaseMigrationsDir"))
            .load()
            .migrate()

        val applicationModelDef = ApplicationModelDefInstantiator.instantiate("org.maiaframework.showcase.MaiaShowcaseApplicationSpec")
        val expectedTables = ExpectedSchemaExtractor().extract(applicationModelDef.rootEntityHierarchies)
        val schemas = expectedTables.map { it.schemaAndTableName.substringBefore(".") }.toSet()

        val report = DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use { connection ->
            val actualTables = PostgresSchemaIntrospector(connection).introspectSchemas(schemas)
            SchemaComparator().compare(expectedTables, actualTables)
        }

        if (report.hasErrors) {
            println(SchemaCheckReporter.renderConsole(report))
        }

        assertThat(report.hasErrors)
            .withFailMessage(
                "maia-showcase's migrations have drifted from its spec — this may be a genuine finding, not a test bug:\n%s",
                SchemaCheckReporter.renderConsole(report)
            )
            .isFalse()

    }

    @Test
    @Order(2)
    fun `a dropped column the spec expects is reported as an error`() {

        val container = SingletonPostgresqlContainer.instance.also { it.start() }

        Flyway.configure()
            .dataSource(container.jdbcUrl, container.username, container.password)
            .locations("filesystem:" + System.getProperty("maiaShowcaseMigrationsDir"))
            .load()
            .migrate()

        val applicationModelDef = ApplicationModelDefInstantiator.instantiate("org.maiaframework.showcase.MaiaShowcaseApplicationSpec")
        val expectedTables = ExpectedSchemaExtractor().extract(applicationModelDef.rootEntityHierarchies)

        // Pick any expected table/column pair and drop that column to simulate drift.
        val tableWithColumns = expectedTables.first { it.columns.isNotEmpty() }
        val columnToDrop = tableWithColumns.columns.first { it.name != "id" }

        // cascade: some showcase columns (e.g. maia.party.authorities) have dependent views
        // (v_party), which a plain DROP COLUMN would refuse.
        DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use { connection ->
            connection.createStatement().use {
                it.execute("alter table ${tableWithColumns.schemaAndTableName} drop column ${columnToDrop.name} cascade")
            }
        }

        val schemas = expectedTables.map { it.schemaAndTableName.substringBefore(".") }.toSet()

        val report = DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use { connection ->
            val actualTables = PostgresSchemaIntrospector(connection).introspectSchemas(schemas)
            SchemaComparator().compare(expectedTables, actualTables)
        }

        assertThat(report.hasErrors).isTrue()
        val diff = report.tables.single { it.schemaAndTableName == tableWithColumns.schemaAndTableName }
        assertThat(diff.missingColumns.map { it.name }).contains(columnToDrop.name)

    }

}
