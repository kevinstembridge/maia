package org.maiaframework.showcase.join

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyInt
import org.maiaframework.testing.domain.Anys.anyString
import java.time.Instant


data class AlphaEntityTestBuilder(
    val createdTimestamp: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val someInt: Int = anyInt(),
    val someString: String = anyString()
) {


    fun build(): AlphaEntity {

        return AlphaEntity(
            this.createdTimestamp,
            this.id,
            this.someInt,
            this.someString
        )

    }


}
