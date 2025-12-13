package org.maiaframework.gen.testing.jdbc.many_to_many

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.jdbc.sample.many_to_many.LeftEntity
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyInt
import org.maiaframework.testing.domain.Anys.anyString
import java.time.Instant

data class LeftEntityTestBuilder(
    val createdTimestampUtc: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val someInt: Int = anyInt(),
    val someString: String = anyString()
) {


    fun build(): LeftEntity {

        return LeftEntity(
            this.createdTimestampUtc,
            this.id,
            this.someInt,
            this.someString
        )

    }


}
