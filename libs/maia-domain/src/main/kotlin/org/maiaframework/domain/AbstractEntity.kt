package org.maiaframework.domain

import java.time.Instant

@Deprecated("The functionality provided by this class is now rendered directly into the generated DAOs")
abstract class AbstractEntity protected constructor(
    val createdTimestampUtc: Instant,
    val id: DomainId
)
