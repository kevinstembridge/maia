package org.maiaframework.domain.gridfs

import org.maiaframework.domain.AbstractEntity
import org.maiaframework.domain.DomainId
import java.time.Instant

abstract class GridFsEntryEntity protected constructor(
    createdTimestampUtc: Instant,
    val deleted: Boolean,
    val gridFsId: DomainId,
    id: DomainId,
    val lastModifiedTimestampUtc: Instant
) : AbstractEntity(
    createdTimestampUtc,
    id
)
