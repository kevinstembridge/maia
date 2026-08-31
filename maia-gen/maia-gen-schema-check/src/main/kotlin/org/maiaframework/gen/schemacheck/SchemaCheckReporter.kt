package org.maiaframework.gen.schemacheck

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

object SchemaCheckReporter {

    private val jsonMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()

    fun renderConsole(report: SchemaDiffReport): String {

        val lines = mutableListOf<String>()

        report.tables.sortedBy { it.schemaAndTableName }.forEach { table ->
            val marker = if (table.hasErrors) "✗" else "✓"
            lines.add("$marker ${table.schemaAndTableName} [${table.status}]")

            table.missingColumns.forEach { lines.add("    ERROR: missing column '${it.name}' (${it.postgresType}, nullable=${it.nullable})") }
            table.mismatchedColumns.forEach {
                lines.add("    ERROR: column '${it.name}' expected ${it.expectedType}/nullable=${it.expectedNullable}, actual ${it.actualType}/nullable=${it.actualNullable}")
            }
            table.primaryKeyMismatch?.let { lines.add("    ERROR: primary key expected ${it.expected}, actual ${it.actual}") }
            table.missingForeignKeys.forEach { lines.add("    ERROR: missing foreign key '${it.columnName}' -> ${it.referencedSchemaAndTable}(${it.referencedColumn})") }
            table.missingIndexes.forEach { lines.add("    ERROR: missing index '${it.name}' on ${it.columns}") }

            table.extraColumns.forEach { lines.add("    WARNING: extra column '$it'") }
            table.extraForeignKeys.forEach { lines.add("    WARNING: extra foreign key on column '$it'") }
            table.extraIndexes.forEach { lines.add("    WARNING: extra index '$it'") }
        }

        val errorCount = report.tables.sumOf {
            it.missingColumns.size + it.mismatchedColumns.size + (if (it.primaryKeyMismatch != null) 1 else 0) +
                    it.missingForeignKeys.size + it.missingIndexes.size + (if (it.status == TableStatus.MISSING) 1 else 0)
        }
        val warningCount = report.tables.sumOf { it.extraColumns.size + it.extraForeignKeys.size + it.extraIndexes.size + (if (it.status == TableStatus.EXTRA) 1 else 0) }

        lines.add("")
        lines.add("${report.tables.size} tables checked, $errorCount errors, $warningCount warnings")

        return lines.joinToString("\n")

    }

    fun renderJson(report: SchemaDiffReport): String {

        return jsonMapper.writeValueAsString(report)

    }

}
