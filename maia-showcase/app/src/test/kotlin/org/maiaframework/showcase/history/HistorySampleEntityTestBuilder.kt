package org.maiaframework.showcase.history

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyAlphaNumeric
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyIntOfLength
import java.time.Instant


data class HistorySampleEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestamp: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestamp: Instant = anyInstant(),
    val someInt: Int = anyIntOfLength(5),
    val someString: String = anyAlphaNumeric(5),
    val version: Long = 1L
) {


    fun build(): HistorySampleEntity {

        return HistorySampleEntity(
            this.createdById,
            this.createdTimestamp,
            this.id,
            this.lastModifiedById,
            this.lastModifiedTimestamp,
            this.someInt,
            this.someString,
            this.version
        )

    }


}
