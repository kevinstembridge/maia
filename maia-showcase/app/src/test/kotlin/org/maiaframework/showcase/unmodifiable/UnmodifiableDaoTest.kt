package org.maiaframework.showcase.unmodifiable

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.UnmodifiableDao
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

class UnmodifiableDaoTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var unmodifiableDao: UnmodifiableDao


    @Test
    fun testUpsertBySomeUniqueInt() {

        val entityTestBuilder = UnmodifiableEntityTestBuilder()
        val unmodifiableEntityOriginal = entityTestBuilder.build()

        val actualOriginalId = this.unmodifiableDao.upsertBySomeUniqueInt(unmodifiableEntityOriginal)
        assertThat(actualOriginalId).isEqualTo(unmodifiableEntityOriginal.id)

        val updatedEntity = entityTestBuilder.copy(
            createdTimestampUtc = Instant.now(),
        ).build()

        val actualUpdatedId = this.unmodifiableDao.upsertBySomeUniqueInt(updatedEntity)
        assertThat(actualUpdatedId).isEqualTo(unmodifiableEntityOriginal.id)

    }


}
