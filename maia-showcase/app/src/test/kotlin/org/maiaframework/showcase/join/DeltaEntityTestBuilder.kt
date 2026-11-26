package org.maiaframework.showcase.join

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyString
import java.time.Instant


data class DeltaEntityTestBuilder(
    val alphaId: DomainId? = null,
    val charlieId: DomainId = DomainId.newId(),
    val createdTimestamp: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val someString: String = anyString()
) {


    fun build(): DeltaEntity {

        return DeltaEntity(
            this.alphaId,
            this.charlieId,
            this.createdTimestamp,
            this.id,
            this.someString
        )

    }


}
