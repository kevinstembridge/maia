package org.maiaframework.gen.testing.jdbc.join

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.sample.join.BravoAgGridEntity
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyInt
import org.maiaframework.testing.domain.Anys.anyString
import java.time.Instant


data class BravoAgGridEntityTestBuilder(
    val alphaId: DomainId = DomainId.newId(),
    val createdTimestampUtc: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val someInt: Int = anyInt(),
    val someString: String = anyString()
) {


    fun build(): BravoAgGridEntity {

        return BravoAgGridEntity(
            this.alphaId,
            this.createdTimestampUtc,
            this.id,
            this.someInt,
            this.someString
        )

    }


}
