package org.maiaframework.showcase.composite_pk

import java.time.Instant
import java.util.UUID


data class CompositePrimaryKeyEntityTestBuilder(
    val createdTimestamp: Instant = Instant.now(),
    val someInt: Int = (1..1_000_000).random(),
    val someModifiableString: String = "modifiable-${UUID.randomUUID()}",
    val someString: String = "key-${UUID.randomUUID()}",
    val version: Long = 1L
) {


    fun build(): CompositePrimaryKeyEntity {

        return CompositePrimaryKeyEntity(
            this.createdTimestamp,
            this.someInt,
            this.someModifiableString,
            this.someString,
            this.version
        )

    }


}
