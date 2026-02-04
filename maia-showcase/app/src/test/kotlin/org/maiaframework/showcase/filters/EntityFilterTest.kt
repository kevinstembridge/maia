package org.maiaframework.showcase.filters

import org.maiaframework.testing.domain.Anys.anyString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.simple.SimpleDao
import org.maiaframework.showcase.simple.SimpleEntity
import org.maiaframework.showcase.simple.SimpleEntityFilters
import org.springframework.beans.factory.annotation.Autowired


class EntityFilterTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var simpleDao: SimpleDao


    @Test
    fun `should huh`() {

        val entity1 = SimpleEntity.newInstance(anyString())
        val entity2 = SimpleEntity.newInstance(anyString())
        val entity3 = SimpleEntity.newInstance(anyString())

        simpleDao.insert(entity1)
        simpleDao.insert(entity2)
        simpleDao.insert(entity3)

        val ids = listOf(entity1.id, entity3.id)

        val filter = SimpleEntityFilters().id.`in`(ids)

        val found = this.simpleDao.findAllBy(filter)

        assertThat(found.map { it.id }).isEqualTo(ids)

    }


    @Test
    fun `test a filter with an OR statement`() {

        val entity1 = SimpleEntity.newInstance(anyString())
        val entity2 = SimpleEntity.newInstance(anyString())
        val entity3 = SimpleEntity.newInstance(anyString())

        simpleDao.insert(entity1)
        simpleDao.insert(entity2)
        simpleDao.insert(entity3)

        val verySimpleEntityFilters = SimpleEntityFilters()
        val filter = verySimpleEntityFilters.or(
            verySimpleEntityFilters.someString eq entity2.someString,
            verySimpleEntityFilters.someString eq entity3.someString
        )

        val found = this.simpleDao.findAllBy(filter)

        assertThat(found.map { it.id }).isEqualTo(listOf(entity2.id, entity3.id))

    }


}
