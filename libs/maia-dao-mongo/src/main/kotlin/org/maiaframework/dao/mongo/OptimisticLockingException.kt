package org.maiaframework.dao.mongo

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.types.CollectionName
import org.bson.types.ObjectId

class OptimisticLockingException(
    val collectionName: CollectionName,
    val id: DomainId,
    val staleVersion: Long
): RuntimeException("OPTIMISTIC_LOCKING: collection=$collectionName, id=$id, staleVersion=$staleVersion")
