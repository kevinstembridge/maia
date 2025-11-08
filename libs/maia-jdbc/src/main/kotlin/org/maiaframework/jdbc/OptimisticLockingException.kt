package org.maiaframework.jdbc

import org.maiaframework.domain.DomainId

class OptimisticLockingException(
    tableName: TableName,
    id: DomainId,
    staleVersion: Long
): MaiaDataAccessException(
    "OPTIMISTIC_LOCKING: table=$tableName, id=$id, staleVersion=$staleVersion"
)
