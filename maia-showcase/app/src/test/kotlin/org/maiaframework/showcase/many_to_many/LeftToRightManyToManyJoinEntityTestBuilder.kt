package org.maiaframework.showcase.many_to_many

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys.anyInstant
import java.time.Instant


data class LeftToRightManyToManyJoinEntityTestBuilder(
    val createdTimestampUtc: Instant = anyInstant(),
    val effectiveFrom: Instant = anyInstant(),
    val effectiveTo: Instant? = null,
    val id: DomainId = DomainId.newId(),
    val lastModifiedTimestampUtc: Instant = anyInstant(),
    val leftId: DomainId,
    val rightId: DomainId
) {


    fun build(): LeftToRightManyToManyJoinEntity {

        return LeftToRightManyToManyJoinEntity(
            this.createdTimestampUtc,
            this.effectiveFrom,
            this.effectiveTo,
            this.id,
            this.lastModifiedTimestampUtc,
            this.leftId,
            this.rightId
        )

    }


}
