package org.maiaframework.showcase.unmodifiable

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys.anyInt
import java.time.Instant


data class UnmodifiableEntityTestBuilder(
    val createdTimestamp: Instant = Instant.now(),
    val id: DomainId = DomainId.newId(),
    val someUniqueInt: Int = anyInt()
) {


    fun build(): UnmodifiableEntity {

        return UnmodifiableEntity(
            this.createdTimestamp,
            this.id,
            this.someUniqueInt
        )

    }


}
