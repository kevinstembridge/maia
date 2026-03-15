package org.maiaframework.showcase.hierarchy

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyAlphaNumeric
import org.maiaframework.testing.domain.Anys.anyInstant
import java.time.Instant


data class ParentOneEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestampUtc: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestampUtc: Instant = anyInstant(),
    val someString: String = anyAlphaNumeric(),
    val someUniqueString: String = anyAlphaNumeric(),
) {


    fun build(): ParentOneEntity {

        return ParentOneEntity(
            this.createdById,
            this.createdTimestampUtc,
            this.id,
            this.lastModifiedById,
            this.lastModifiedTimestampUtc,
            this.someString,
            this.someUniqueString
        )

    }


}
