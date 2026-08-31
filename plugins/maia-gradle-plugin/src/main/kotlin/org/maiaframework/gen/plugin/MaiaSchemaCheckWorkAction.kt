package org.maiaframework.gen.plugin

import org.gradle.workers.WorkAction
import org.maiaframework.gen.schemacheck.SchemaCheckArgs
import org.maiaframework.gen.schemacheck.runSchemaCheck

abstract class MaiaSchemaCheckWorkAction : WorkAction<MaiaSchemaCheckWorkParameters> {

    override fun execute() {

        val password = parameters.password.orNull ?: System.getenv("MAIA_SCHEMA_CHECK_DB_PASSWORD")
            ?: throw IllegalStateException("No schemaCheckPassword configured and no MAIA_SCHEMA_CHECK_DB_PASSWORD environment variable set")

        val exitCode = runSchemaCheck(
            SchemaCheckArgs(
                applicationSpecClassName = parameters.applicationSpecClassName.get(),
                jdbcUrl = parameters.jdbcUrl.get(),
                username = parameters.username.get(),
                password = password,
                format = parameters.outputFormat.getOrElse("text"),
                outputFile = parameters.outputFile.orNull?.asFile,
            )
        )

        check(exitCode != 2) { "Schema check failed to run — see output above." }

        if (exitCode == 1 && parameters.ignoreErrors.getOrElse(false) == false) {
            throw RuntimeException("Schema check found errors — see report above.")
        }

    }

}
