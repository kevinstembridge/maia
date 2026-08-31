package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.testing.postgresql.SingletonPostgresqlContainer
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

class SchemaCheckMainTest {

    companion object {

        private lateinit var connection: Connection
        private lateinit var jdbcUrl: String
        private lateinit var username: String
        private lateinit var password: String

        @JvmStatic
        @BeforeAll
        fun setUp() {
            val container = SingletonPostgresqlContainer.instance.also { it.start() }
            jdbcUrl = container.jdbcUrl
            username = container.username
            password = container.password
            connection = DriverManager.getConnection(jdbcUrl, username, password)
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            connection.close()
        }

    }

    // AbstractSpec.modelDef only implements ModelDefProvider (single ModelDef); the CLI's
    // ApplicationModelDefInstantiator requires ApplicationModelDefProvider, implemented by
    // ApplicationSpec, which aggregates one or more ModelDefs. Both are needed together.
    class MatchingModelSpec : AbstractSpec(AppKey("SchemaCheckMainTest")) {
        val widget = entity("com.example", "Widget") {
            field("name", FieldTypes.string) { lengthConstraint(max = 50) }
        }
    }

    class MatchingApplicationSpec : ApplicationSpec("com.example") {
        private val modelSpec = MatchingModelSpec()
        override val modelDefs: List<ModelDef> = listOf(modelSpec.modelDef)
    }

    @Test
    fun `returns 0 when the database matches the spec`() {

        connection.createStatement().use { statement ->
            statement.execute("create schema if not exists schemacheckmaintest")
            // Column types match real ExpectedSchemaExtractor output for a plain entity:
            // id is uuid (FieldTypes.domainId), name is text (FieldTypes.string never maps to
            // varchar — see ExpectedSchemaExtractorTest), created_timestamp is
            // timestamp(3) with time zone (FieldTypes.instant). lastModifiedTimestamp/version
            // are optional per-entity fields, absent unless explicitly configured.
            statement.execute(
                """
                create table if not exists schemacheckmaintest.widget (
                    id uuid not null,
                    name text not null,
                    created_timestamp timestamp(3) with time zone not null,
                    primary key (id)
                )
                """.trimIndent()
            )
        }

        val exitCode = runSchemaCheck(
            SchemaCheckArgs(
                applicationSpecClassName = MatchingApplicationSpec::class.java.name,
                jdbcUrl = jdbcUrl,
                username = username,
                password = password,
            )
        )

        assertThat(exitCode).isEqualTo(0)

        connection.createStatement().use { it.execute("drop schema schemacheckmaintest cascade") }

    }

    @Test
    fun `returns 1 when the database is missing a table the spec expects`() {

        val exitCode = runSchemaCheck(
            SchemaCheckArgs(
                applicationSpecClassName = MatchingApplicationSpec::class.java.name,
                jdbcUrl = jdbcUrl,
                username = username,
                password = password,
            )
        )

        assertThat(exitCode).isEqualTo(1)

    }

    @Test
    fun `writes a fix SQL file when fixSqlOutputFile is set`(@TempDir tempDir: File) {

        val fixSqlFile = tempDir.resolve("fix.sql")

        val exitCode = runSchemaCheck(
            SchemaCheckArgs(
                applicationSpecClassName = MatchingApplicationSpec::class.java.name,
                jdbcUrl = jdbcUrl,
                username = username,
                password = password,
                fixSqlOutputFile = fixSqlFile,
            )
        )

        assertThat(exitCode).isEqualTo(1)
        assertThat(fixSqlFile).exists()
        assertThat(fixSqlFile.readText()).contains("schemacheckmaintest.widget")

    }

    @Test
    fun `returns 2 when the spec class cannot be instantiated`() {

        val exitCode = runSchemaCheck(
            SchemaCheckArgs(
                applicationSpecClassName = "com.example.DoesNotExist",
                jdbcUrl = jdbcUrl,
                username = username,
                password = password,
            )
        )

        assertThat(exitCode).isEqualTo(2)

    }

    @Test
    fun `returns 2 when the database connection fails`() {

        val exitCode = runSchemaCheck(
            SchemaCheckArgs(
                applicationSpecClassName = MatchingApplicationSpec::class.java.name,
                jdbcUrl = "jdbc:postgresql://localhost:1/nonexistent",
                username = username,
                password = password,
            )
        )

        assertThat(exitCode).isEqualTo(2)

    }

}
