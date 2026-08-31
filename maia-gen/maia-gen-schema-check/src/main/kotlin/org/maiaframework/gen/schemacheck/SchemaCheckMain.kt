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
        // DriverManager only auto-registers drivers via ServiceLoader once per JVM, the first time
        // its class is initialized. When this code runs inside a Gradle worker with classloader
        // isolation, each work item gets its own classloader instance, and DriverManager rejects
        // drivers it can't associate with the *current* caller's classloader. Explicitly loading
        // the driver class here (using this class's own classloader) re-registers it every time,
        // so it's always visible to the DriverManager.getConnection() call below.
        Class.forName("org.postgresql.Driver")
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

    if (args.fixSqlOutputFile != null) {
        args.fixSqlOutputFile.writeText(SchemaFixSqlGenerator.generate(report))
    }

    return if (report.hasErrors) 1 else 0

}
