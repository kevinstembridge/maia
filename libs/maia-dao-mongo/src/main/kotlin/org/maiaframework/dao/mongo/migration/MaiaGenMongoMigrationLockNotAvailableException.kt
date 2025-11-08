package org.maiaframework.dao.mongo.migration

import java.time.Duration
import java.time.Instant

class MaiaGenMongoMigrationLockNotAvailableException(
    val lockCreatedTimestampUtc: Instant,
    val hostname: String,
    val processName: String
) : Exception(
    "Unable to obtain MaiaGen migration lock. Existing lock held by process ["
        + processName
        + "] on host ["
        + hostname
        + "] since "
        + lockCreatedTimestampUtc
        + " (" + Duration.between(lockCreatedTimestampUtc, Instant.now()) + ")"
)
