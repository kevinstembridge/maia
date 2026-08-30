package org.maiaframework.showcase.history

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyAlphaNumeric
import org.maiaframework.testing.domain.Anys.anyInstant
import java.time.Instant


data class HistorySubOneEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestamp: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestamp: Instant = anyInstant(),
    val someString: String = anyAlphaNumeric(5),
    val version: Long = 1L
) {


    fun build(): HistorySubOneEntity {

        return HistorySubOneEntity(
            this.createdById,
            this.createdTimestamp,
            this.id,
            this.lastModifiedById,
            this.lastModifiedTimestamp,
            this.someString,
            this.version
        )

    }


}
