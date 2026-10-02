package org.maiaframework.jdbc

class BulkOptimisticLockingException(
    tableName: TableName,
    failures: List<Pair<Any, Long>>
) : MaiaDataAccessException(
    "OPTIMISTIC_LOCKING: table=$tableName, failures=$failures"
)
