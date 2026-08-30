package org.maiaframework.showcase.hierarchy

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyAlphaNumeric
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyInt
import java.time.Instant


data class ChildOneEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestamp: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestamp: Instant = anyInstant(),
    val someInt: Int = anyInt(),
    val someString: String = anyAlphaNumeric(),
    val someUniqueString: String = anyAlphaNumeric(),
) {


    fun build(): ChildOneEntity {

        return ChildOneEntity(
            this.createdById,
            this.createdTimestamp,
            this.id,
            this.lastModifiedById,
            this.lastModifiedTimestamp,
            this.someInt,
            this.someString,
            this.someUniqueString
        )

    }


}
