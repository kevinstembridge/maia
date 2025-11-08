package org.maiaframework.gen.testing.jdbc.effective_dated

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.jdbc.sample.EffectiveTimestampEntity
import org.maiaframework.testing.domain.Anys
import java.time.Instant
import java.time.Period
import java.time.temporal.ChronoUnit


data class EffectiveTimestampEntityTestBuilder(
    val createdTimestampUtc: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    val effectiveFrom: Instant? = Anys.anyPastInstantWithin(Period.ofDays(5)),
    val effectiveTo: Instant? = Anys.anyFutureInstantWithin(Period.ofDays(5)),
    val id: DomainId = DomainId.newId(),
    val someString: String = Anys.anyString()
) {


    fun build(): EffectiveTimestampEntity {

        return EffectiveTimestampEntity(
            this.createdTimestampUtc,
            this.effectiveFrom,
            this.effectiveTo,
            this.id,
            this.someString
        )

    }


}
