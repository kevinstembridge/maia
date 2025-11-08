package org.maiaframework.domain

import java.time.Instant

abstract class AbstractHistoryEntity protected constructor(
    val changeType: ChangeType,
    createdTimestampUtc: Instant,
    val entityId: DomainId,
    id: DomainId,
    v: Long
) : AbstractVersionedEntity(
    createdTimestampUtc,
    id,
    v
)
