package org.maiaframework.gen.schemacheck

import java.io.File

data class SchemaCheckArgs(
    val applicationSpecClassName: String,
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val format: String = "text",
    val outputFile: File? = null,
) {

    companion object {

        fun from(args: Array<String>): SchemaCheckArgs {

            val argsMap = args.map { it.split("=", limit = 2) }.associate { it[0] to it[1] }

            return SchemaCheckArgs(
                applicationSpecClassName = argsMap["applicationSpecClassName"]
                    ?: throw IllegalArgumentException("Expecting an argument named applicationSpecClassName=<...>"),
                jdbcUrl = argsMap["jdbcUrl"]
                    ?: throw IllegalArgumentException("Expecting an argument named jdbcUrl=<...>"),
                username = argsMap["username"]
                    ?: throw IllegalArgumentException("Expecting an argument named username=<...>"),
                password = argsMap["password"] ?: System.getenv("MAIA_SCHEMA_CHECK_DB_PASSWORD")
                    ?: throw IllegalArgumentException("Expecting either a password=<...> argument or a MAIA_SCHEMA_CHECK_DB_PASSWORD environment variable"),
                format = argsMap["format"] ?: "text",
                outputFile = argsMap["outputFile"]?.let { File(it) },
            )

        }

    }

}
