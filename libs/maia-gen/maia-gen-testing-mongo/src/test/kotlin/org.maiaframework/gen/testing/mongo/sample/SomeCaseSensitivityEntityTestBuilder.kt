package org.maiaframework.gen.testing.mongo.sample

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.mongo.sample.case_sensitivity.SomeCaseSensitivityEntity
import org.maiaframework.testing.domain.Anys.anyString
import org.maiaframework.testing.domain.Anys.randomiseCase
import java.time.Instant

data class SomeCaseSensitivityEntityTestBuilder(
    val someCaseSensitiveString: String = randomiseCase(anyString()),
    val someCaseInsensitiveString: String = randomiseCase(anyString()),
    val createdTimestampUtc: Instant = Instant.now(),
    val id: DomainId = DomainId.newId()
) {


    fun build(): SomeCaseSensitivityEntity {

        return SomeCaseSensitivityEntity(
            this.someCaseInsensitiveString,
            this.someCaseSensitiveString,
            this.createdTimestampUtc,
            this.id
        )

    }


}
