package org.maiaframework.showcase.cached_big_decimal

import com.hazelcast.core.HazelcastInstance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant


class CachedBigDecimalHazelcastTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var hazelcastInstance: HazelcastInstance


    @Test
    fun `BigDecimal fields round trip through Hazelcast with exact value and scale`() {

        val entity = CachedBigDecimalEntity(
            Instant.now(),
            DomainId.newId(),
            BigDecimal("123456789.12345678"),
            BigDecimal("-0.000"),
            BigDecimal("1.50"),
        )

        val actual = roundTrip(entity)

        assertThat(actual.someBigDecimal).isEqualTo(BigDecimal("123456789.12345678"))
        assertThat(actual.someBigDecimal.scale()).isEqualTo(8)
        assertThat(actual.someBigDecimalScaled).isEqualTo(BigDecimal("1.50"))
        assertThat(actual.someBigDecimalScaled.scale()).isEqualTo(2)
        assertThat(actual.someBigDecimalNullable).isEqualTo(BigDecimal("-0.000"))
        assertThat(actual.someBigDecimalNullable!!.scale()).isEqualTo(3)

    }


    @Test
    fun `null BigDecimal round trips through Hazelcast`() {

        val entity = CachedBigDecimalEntity(
            Instant.now(),
            DomainId.newId(),
            BigDecimal("1.0"),
            null,
            BigDecimal("2.00"),
        )

        assertThat(roundTrip(entity).someBigDecimalNullable).isNull()

    }


    private fun roundTrip(entity: CachedBigDecimalEntity): CachedBigDecimalEntity {

        val map = this.hazelcastInstance.getMap<String, CachedBigDecimalEntity>("cached-big-decimal-test")
        map.put(entity.id.value, entity)
        return map.get(entity.id.value)!!

    }


}
