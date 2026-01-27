package org.maiaframework.jdbc

class OptimisticLockingException(
    tableName: TableName,
    primaryKey: Any,
    staleVersion: Long
): MaiaDataAccessException(
    "OPTIMISTIC_LOCKING: table=$tableName, id=$primaryKey, staleVersion=$staleVersion"
)
