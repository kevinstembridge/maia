package org.maiaframework.gen.testing.jdbc.many_to_many

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.jdbc.sample.many_to_many.ManyToManyJoinEntity
import org.maiaframework.testing.domain.Anys.anyInstant
import java.time.Instant


data class ManyToManyJoinEntityTestBuilder(
    val createdTimestampUtc: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val lastModifiedTimestampUtc: Instant = anyInstant(),
    val leftId: DomainId,
    val rightId: DomainId
) {


    fun build(): ManyToManyJoinEntity {

        return ManyToManyJoinEntity(
            this.createdTimestampUtc,
            this.id,
            this.lastModifiedTimestampUtc,
            this.leftId,
            this.rightId
        )

    }


}
