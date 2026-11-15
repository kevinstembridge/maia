package org.maiaframework.showcase.cached_decimal

import com.hazelcast.core.HazelcastInstance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant


class CachedDecimalHazelcastTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var hazelcastInstance: HazelcastInstance


    @Test
    fun `BigDecimal fields round trip through Hazelcast with exact value and scale`() {

        val entity = CachedDecimalEntity(
            Instant.now(),
            DomainId.newId(),
            BigDecimal("123456789.12345678"),
            BigDecimal("-0.000"),
            BigDecimal("1.50"),
        )

        val actual = roundTrip(entity)

        assertThat(actual.someDecimal).isEqualTo(BigDecimal("123456789.12345678"))
        assertThat(actual.someDecimal.scale()).isEqualTo(8)
        assertThat(actual.someDecimalScaled).isEqualTo(BigDecimal("1.50"))
        assertThat(actual.someDecimalScaled.scale()).isEqualTo(2)
        assertThat(actual.someDecimalNullable).isEqualTo(BigDecimal("-0.000"))
        assertThat(actual.someDecimalNullable!!.scale()).isEqualTo(3)

    }


    @Test
    fun `null BigDecimal round trips through Hazelcast`() {

        val entity = CachedDecimalEntity(
            Instant.now(),
            DomainId.newId(),
            BigDecimal("1.0"),
            null,
            BigDecimal("2.00"),
        )

        assertThat(roundTrip(entity).someDecimalNullable).isNull()

    }


    private fun roundTrip(entity: CachedDecimalEntity): CachedDecimalEntity {

        val map = this.hazelcastInstance.getMap<String, CachedDecimalEntity>("cached-decimal-test")
        map.put(entity.id.value, entity)
        return map.get(entity.id.value)!!

    }


}
