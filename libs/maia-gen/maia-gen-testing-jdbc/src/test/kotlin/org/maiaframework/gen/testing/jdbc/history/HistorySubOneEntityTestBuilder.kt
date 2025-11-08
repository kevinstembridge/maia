package org.maiaframework.gen.testing.jdbc.history

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.jdbc.sample.history.HistorySampleEntity
import org.maiaframework.gen.testing.jdbc.sample.history.HistorySubOneEntity
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyAlphaNumeric
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyIntOfLength
import java.time.Instant

data class HistorySubOneEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestampUtc: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestampUtc: Instant = anyInstant(),
    val someString: String = anyAlphaNumeric(5),
    val version: Long = 1L
) {


    fun build(): HistorySubOneEntity {

        return HistorySubOneEntity(
            this.createdById,
            this.createdTimestampUtc,
            this.id,
            this.lastModifiedById,
            this.lastModifiedTimestampUtc,
            this.someString,
            this.version
        )

    }


}
