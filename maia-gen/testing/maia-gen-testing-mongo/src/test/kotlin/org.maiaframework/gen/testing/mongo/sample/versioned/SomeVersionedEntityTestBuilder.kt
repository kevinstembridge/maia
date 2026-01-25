package org.maiaframework.gen.testing.mongo.sample.versioned

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys.anyString
import java.time.Instant

data class SomeVersionedEntityTestBuilder(
    val createdTimestampUtc: Instant = Instant.now(),
    val id: DomainId = DomainId.newId(),
    val lastModifiedTimestampUtc: Instant = Instant.now(),
    val someInt: Int = 0,
    val someString: String = anyString(),
    val version: Long = 1
) {


    fun build(): SomeVersionedEntity {

        return SomeVersionedEntity(
            this.createdTimestampUtc,
            this.id,
            this.lastModifiedTimestampUtc,
            this.someInt,
            this.someString,
            this.version
        )

    }


}
