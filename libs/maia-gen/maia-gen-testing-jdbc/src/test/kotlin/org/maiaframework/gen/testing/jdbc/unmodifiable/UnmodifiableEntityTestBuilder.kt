package org.maiaframework.gen.testing.jdbc.unmodifiable

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.jdbc.sample.UnmodifiableEntity
import org.maiaframework.testing.domain.Anys.anyInt
import java.time.Instant

data class UnmodifiableEntityTestBuilder(
    val createdTimestampUtc: Instant = Instant.now(),
    val id: DomainId = DomainId.newId(),
    val someUniqueInt: Int = anyInt()
) {


    fun build(): UnmodifiableEntity {

        return UnmodifiableEntity(
            this.createdTimestampUtc,
            this.id,
            this.someUniqueInt
        )

    }


}
