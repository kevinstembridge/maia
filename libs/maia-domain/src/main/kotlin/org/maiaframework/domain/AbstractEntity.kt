package org.maiaframework.domain

import java.time.Instant

abstract class AbstractEntity protected constructor(
    val createdTimestampUtc: Instant,
    val id: DomainId
)
