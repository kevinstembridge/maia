package org.maiaframework.showcase.many_to_many

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyInt
import org.maiaframework.testing.domain.Anys.anyString
import java.time.Instant


data class RightManyEntityTestBuilder(
    val createdTimestamp: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val someInt: Int = anyInt(),
    val someString: String = anyString(),
    val version: Long = 1L
) {


    fun build(): RightManyEntity {

        return RightManyEntity(
            this.createdTimestamp,
            this.id,
            this.someInt,
            this.someString,
            this.version
        )

    }


}
