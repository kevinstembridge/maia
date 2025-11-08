package org.maiaframework.jdbc

class MaiaSqlException(
    message: String,
    sql: String,
    params: SqlParams,
    cause: Throwable? = null
): MaiaDataAccessException(
        "$message\nparams=$params\nsql=$sql",
        cause
)
