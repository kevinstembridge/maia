package org.maiaframework.dao.mongo.migration

import java.time.Duration
import java.time.Instant

class MaiaGenMongoMigrationLockNotAvailableException(
    val lockCreatedTimestamp: Instant,
    val hostname: String,
    val processName: String
) : Exception(
    "Unable to obtain MaiaGen migration lock. Existing lock held by process ["
        + processName
        + "] on host ["
        + hostname
        + "] since "
        + lockCreatedTimestamp
        + " (" + Duration.between(lockCreatedTimestamp, Instant.now()) + ")"
)
