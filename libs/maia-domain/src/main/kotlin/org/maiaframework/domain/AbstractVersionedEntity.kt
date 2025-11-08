package org.maiaframework.domain

import java.time.Instant

abstract class AbstractVersionedEntity protected constructor(
    createdTimestampUtc: Instant,
    id: DomainId,
    val version: Long
) : AbstractEntity(
    createdTimestampUtc,
    id
) {

    val idAndVersion = IdAndVersion(id, version)

}
