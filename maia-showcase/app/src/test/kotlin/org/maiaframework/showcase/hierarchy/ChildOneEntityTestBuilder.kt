package org.maiaframework.showcase.hierarchy

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyAlphaNumeric
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyInt
import java.time.Instant


data class ChildOneEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestampUtc: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestampUtc: Instant = anyInstant(),
    val someInt: Int = anyInt(),
    val someString: String = anyAlphaNumeric(),
    val someUniqueString: String = anyAlphaNumeric(),
) {


    fun build(): ChildOneEntity {

        return ChildOneEntity(
            this.createdById,
            this.createdTimestampUtc,
            this.id,
            this.lastModifiedById,
            this.lastModifiedTimestampUtc,
            this.someInt,
            this.someString,
            this.someUniqueString
        )

    }


}
