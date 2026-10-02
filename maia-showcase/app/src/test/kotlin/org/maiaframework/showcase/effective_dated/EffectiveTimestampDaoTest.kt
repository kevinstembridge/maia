package org.maiaframework.showcase.effective_dated

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.temporal.ChronoUnit


class EffectiveTimestampDaoTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var effectiveTimestampDao: EffectiveTimestampDao


    @Test
    fun testBulkSetFields_updatesEffectiveRangeAndPlainField_inOneCall() {

        val entity1 = EffectiveTimestampEntityTestBuilder().build()
        val entity2 = EffectiveTimestampEntityTestBuilder().build()

        this.effectiveTimestampDao.insert(entity1)
        this.effectiveTimestampDao.insert(entity2)

        val newEffectiveFrom = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS)
        val newEffectiveTo = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS)

        val updater1 = EffectiveTimestampEntityUpdater.forPrimaryKey(entity1.id) {
            effectiveFrom(newEffectiveFrom)
            effectiveTo(newEffectiveTo)
        }
        val updater2 = EffectiveTimestampEntityUpdater.forPrimaryKey(entity2.id) {
            someString("updated2")
        }

        this.effectiveTimestampDao.bulkSetFields(listOf(updater1, updater2))

        val updatedEntity1 = this.effectiveTimestampDao.findByPrimaryKey(entity1.id)
        val updatedEntity2 = this.effectiveTimestampDao.findByPrimaryKey(entity2.id)

        assertThat(updatedEntity1.effectiveFrom).isEqualTo(newEffectiveFrom)
        assertThat(updatedEntity1.effectiveTo).isEqualTo(newEffectiveTo)
        assertThat(updatedEntity2.someString).isEqualTo("updated2")

    }


}
