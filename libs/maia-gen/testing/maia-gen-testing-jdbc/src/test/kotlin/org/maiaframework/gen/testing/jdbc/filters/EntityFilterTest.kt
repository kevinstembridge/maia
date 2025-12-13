package org.maiaframework.gen.testing.jdbc.filters

import org.maiaframework.gen.testing.jdbc.AbstractJdbcTest
import org.maiaframework.gen.testing.sample.simple.VerySimpleDao
import org.maiaframework.gen.testing.sample.simple.VerySimpleEntity
import org.maiaframework.gen.testing.sample.simple.VerySimpleEntityFilters
import org.maiaframework.testing.domain.Anys.anyString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


class EntityFilterTest : AbstractJdbcTest() {


    @Autowired
    private lateinit var verySimpleDao: VerySimpleDao


    @Test
    fun `should huh`() {

        val entity1 = VerySimpleEntity.newInstance(anyString())
        val entity2 = VerySimpleEntity.newInstance(anyString())
        val entity3 = VerySimpleEntity.newInstance(anyString())

        verySimpleDao.insert(entity1)
        verySimpleDao.insert(entity2)
        verySimpleDao.insert(entity3)

        val ids = listOf(entity1.id, entity3.id)

        val filter = VerySimpleEntityFilters().id.`in`(ids)

        val found = this.verySimpleDao.findAllBy(filter)

        assertThat(found.map { it.id }).isEqualTo(ids)

    }


    @Test
    fun `test a filter with an OR statement`() {

        val entity1 = VerySimpleEntity.newInstance(anyString())
        val entity2 = VerySimpleEntity.newInstance(anyString())
        val entity3 = VerySimpleEntity.newInstance(anyString())

        verySimpleDao.insert(entity1)
        verySimpleDao.insert(entity2)
        verySimpleDao.insert(entity3)

        val verySimpleEntityFilters = VerySimpleEntityFilters()
        val filter = verySimpleEntityFilters.or(
            verySimpleEntityFilters.someString eq entity2.someString,
            verySimpleEntityFilters.someString eq entity3.someString
        )

        val found = this.verySimpleDao.findAllBy(filter)

        assertThat(found.map { it.id }).isEqualTo(listOf(entity2.id, entity3.id))

    }


}
