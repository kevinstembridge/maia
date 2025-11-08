package mahana.gen.testing.mongo.sample.history

import org.maiaframework.domain.DomainId
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyInt
import org.maiaframework.testing.domain.Anys.anyString
import java.time.Instant

data class HistorySampleEntityTestBuilder(
    val createdTimestampUtc: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val someInt: Int = anyInt(),
    val someString: String = anyString(),
    val version: Long = 1
) {


    fun build(): HistorySampleEntity {

        return HistorySampleEntity(
            this.createdTimestampUtc,
            this.id,
            this.someInt,
            this.someString,
            this.version
        )

    }


}
