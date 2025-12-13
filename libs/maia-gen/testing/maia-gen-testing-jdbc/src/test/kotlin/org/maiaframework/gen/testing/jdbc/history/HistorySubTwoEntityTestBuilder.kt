package org.maiaframework.gen.testing.jdbc.history

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.sample.history.HistorySubOneEntity
import org.maiaframework.gen.testing.sample.history.HistorySubTwoEntity
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyIntOfLength
import java.time.Instant

data class HistorySubTwoEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestampUtc: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestampUtc: Instant = anyInstant(),
    val someInt: Int = anyIntOfLength(5),
    val version: Long = 1L
) {


    fun build(): HistorySubTwoEntity {

        return HistorySubTwoEntity(
            this.createdById,
            this.createdTimestampUtc,
            this.id,
            this.lastModifiedById,
            this.lastModifiedTimestampUtc,
            this.someInt,
            this.version
        )

    }


}
