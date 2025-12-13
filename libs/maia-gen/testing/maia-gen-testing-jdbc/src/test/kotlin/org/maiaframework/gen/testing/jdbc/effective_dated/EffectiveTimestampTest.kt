package org.maiaframework.gen.testing.jdbc.effective_dated

import org.maiaframework.gen.testing.jdbc.AbstractJdbcTest
import org.maiaframework.gen.testing.jdbc.sample.EffectiveTimestampDao
import org.maiaframework.gen.testing.jdbc.sample.EffectiveTimestampEntity
import org.maiaframework.testing.domain.Anys.anyFutureInstantWithin
import org.maiaframework.testing.domain.Anys.anyPastInstantWithin
import org.maiaframework.testing.domain.Anys.anyString
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.IncorrectResultSizeDataAccessException
import java.time.Period
import java.time.temporal.ChronoUnit

class EffectiveTimestampTest: AbstractJdbcTest() {


    @Autowired
    private lateinit var effectiveTimestampDao: EffectiveTimestampDao


    @Test
    fun `should find a single effective record`() {

        val entity = `insert a sample entity that is effective now`()

        val actual = this.effectiveTimestampDao.findEffectiveBySomeString(entity.someString)!!

        assertEntity(actual, entity)

    }


    @Test
    fun `should find a record with no effectiveTo`() {

        val effectiveFrom = anyPastInstantWithin(Period.ofDays(5))
        val entity = EffectiveTimestampEntityTestBuilder(
            effectiveFrom = effectiveFrom,
            effectiveTo = null
        ).build()

        this.effectiveTimestampDao.insert(entity)

        val actual = this.effectiveTimestampDao.findEffectiveBySomeString(entity.someString)!!

        assertEntity(actual, entity)

    }


    @Test
    fun `should blow up if multiple effective records`() {

        val entity1 = `insert a sample entity that is effective now`()

        // Create another sample instance that is effective now and has the same someString value
        `insert a sample entity that is effective now`(entity1.someString)

        assertThatThrownBy {
            this.effectiveTimestampDao.findEffectiveBySomeString(entity1.someString)
        }.isInstanceOf(IncorrectResultSizeDataAccessException::class.java)
            .hasMessageContaining("Incorrect result size: expected 1, actual 2")

    }


    @Test
    fun `should not find a record with a future effective date`() {

        val entity = `insert a sample entity that is effective in the future`()

        val actual = this.effectiveTimestampDao.findEffectiveBySomeString(entity.someString)

        assertThat(actual).isNull()

    }


    @Test
    fun `should not find a record with a past effective date`() {

        val entity = `insert a sample entity that is effective in the past`()

        val actual = this.effectiveTimestampDao.findEffectiveBySomeString(entity.someString)

        assertThat(actual).isNull()

    }


    @Test
    fun `should not find a record with no effectiveFrom`() {

        val effectiveTo = anyFutureInstantWithin(Period.ofDays(5)).plus(1, ChronoUnit.DAYS)
        val entity = EffectiveTimestampEntityTestBuilder(
            effectiveFrom = null,
            effectiveTo = effectiveTo
        ).build()

        this.effectiveTimestampDao.insert(entity)

        val actual = this.effectiveTimestampDao.findEffectiveBySomeString(entity.someString)

        assertThat(actual).isNull()

    }


    private fun `insert a sample entity that is effective in the past`(): EffectiveTimestampEntity {

        val effectiveTo = anyPastInstantWithin(Period.ofDays(5))
        val effectiveFrom = effectiveTo.minus(1, ChronoUnit.DAYS)

        val entity = EffectiveTimestampEntityTestBuilder(
            effectiveFrom = effectiveFrom,
            effectiveTo = effectiveTo
        ).build()

        this.effectiveTimestampDao.insert(entity)

        return entity

    }


    private fun `insert a sample entity that is effective in the future`(): EffectiveTimestampEntity {

        val effectiveFrom = anyFutureInstantWithin(Period.ofDays(5)).plus(1, ChronoUnit.DAYS)
        val effectiveTo = effectiveFrom.plus(1, ChronoUnit.DAYS)

        val entity = EffectiveTimestampEntityTestBuilder(
            effectiveFrom = effectiveFrom,
            effectiveTo = effectiveTo
        ).build()

        this.effectiveTimestampDao.insert(entity)

        return entity

    }


    private fun `insert a sample entity that is effective now`(someString: String = anyString()): EffectiveTimestampEntity {

        val entity = EffectiveTimestampEntityTestBuilder(
            effectiveFrom = anyPastInstantWithin(Period.ofDays(5)).minus(1, ChronoUnit.DAYS),
            effectiveTo = anyFutureInstantWithin(Period.ofDays(5)).plus(1, ChronoUnit.DAYS),
            someString = someString
        ).build()

        this.effectiveTimestampDao.insert(entity)

        return entity

    }


    private fun assertEntity(
        actual: EffectiveTimestampEntity,
        entity: EffectiveTimestampEntity
    ) {

        assertThat(actual.id).isEqualTo(entity.id)
        assertThat(actual.effectiveFrom).isEqualTo(entity.effectiveFrom)
        assertThat(actual.effectiveTo).isEqualTo(entity.effectiveTo)
        assertThat(actual.someString).isEqualTo(entity.someString)
        assertThat(actual.someString).isEqualTo(entity.someString)

    }


}
