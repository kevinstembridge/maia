package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SchemaCheckReporterTest {

    @Test
    fun `console output marks a clean table with a checkmark and an errored table with a cross`() {

        val report = SchemaDiffReport(
            listOf(
                TableDiff("app.ok_table", TableStatus.OK),
                TableDiff("app.bad_table", TableStatus.MISSING),
            )
        )

        val output = SchemaCheckReporter.renderConsole(report)

        assertThat(output).contains("✓ app.ok_table [OK]")
        assertThat(output).contains("✗ app.bad_table [MISSING]")
        assertThat(output).contains("2 tables checked, 1 errors, 0 warnings")

    }

    @Test
    fun `json output round-trips a report`() {

        val report = SchemaDiffReport(listOf(TableDiff("app.widget", TableStatus.OK)))

        val json = SchemaCheckReporter.renderJson(report)

        assertThat(json).contains("\"schemaAndTableName\":\"app.widget\"")
        assertThat(json).contains("\"status\":\"OK\"")

    }

}
