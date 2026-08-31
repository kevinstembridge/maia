package org.maiaframework.gen.schemacheck

import org.maiaframework.gen.generator.ApplicationModelDefInstantiator
import org.maiaframework.gen.schema.ExpectedSchemaExtractor
import java.sql.DriverManager
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    val exitCode = try {
        runSchemaCheck(SchemaCheckArgs.from(args))
    } catch (t: Throwable) {
        System.err.println("Schema check failed to run: ${t.message}")
        2
    }

    exitProcess(exitCode)

}

fun runSchemaCheck(args: SchemaCheckArgs): Int {

    val applicationModelDef = try {
        ApplicationModelDefInstantiator.instantiate(args.applicationSpecClassName)
    } catch (t: Throwable) {
        System.err.println("Could not instantiate spec class '${args.applicationSpecClassName}': ${t.message}")
        return 2
    }

    val expectedTables = ExpectedSchemaExtractor().extract(applicationModelDef.rootEntityHierarchies)
    val schemas = expectedTables.map { it.schemaAndTableName.substringBefore(".") }.toSet()

    val report = try {
        DriverManager.getConnection(args.jdbcUrl, args.username, args.password).use { connection ->
            val actualTables = PostgresSchemaIntrospector(connection).introspectSchemas(schemas)
            SchemaComparator().compare(expectedTables, actualTables)
        }
    } catch (t: Throwable) {
        System.err.println("Could not connect to database at '${args.jdbcUrl}': ${t.message}")
        return 2
    }

    val output = if (args.format == "json") SchemaCheckReporter.renderJson(report) else SchemaCheckReporter.renderConsole(report)

    if (args.outputFile != null) {
        args.outputFile.writeText(output)
    } else {
        println(output)
    }

    return if (report.hasErrors) 1 else 0

}
