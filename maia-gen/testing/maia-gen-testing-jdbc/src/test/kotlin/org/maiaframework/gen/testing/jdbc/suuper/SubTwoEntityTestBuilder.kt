package org.maiaframework.gen.testing.jdbc.suuper

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.sample.suuper.SubTwoEntity
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyAlphaNumeric
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyInt
import java.time.Instant

data class SubTwoEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestampUtc: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestampUtc: Instant = anyInstant(),
    val someInt: Int = anyInt(),
    val someUniqueString: String = anyAlphaNumeric(),
) {


    fun build(): SubTwoEntity {

        return SubTwoEntity(
            this.createdById,
            this.createdTimestampUtc,
            this.id,
            this.lastModifiedById,
            this.lastModifiedTimestampUtc,
            this.someInt,
            this.someUniqueString
        )

    }


}
