package org.maiaframework.domain

import java.time.Instant

@Deprecated("The functionality provided by this class is now rendered directly into the generated DAOs")
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
