package org.maiaframework.showcase.suuper

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.jdbc.EntityNotFoundException
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.testing.domain.Anys.anyString
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID


class SuperDaoTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var subOneDao: SubOneDao


    @Autowired
    private lateinit var subTwoDao: SubTwoDao


    @BeforeEach
    fun beforeEach() {

        this.subOneDao.deleteAll()
        this.subTwoDao.deleteAll()

    }


    @Test
    fun testFindOneBySomeUniqueString() {

        //GIVEN
        val someString = anyString()
        val subOneEntity = SubOneEntityTestBuilder(someUniqueString = someString).build()
        this.subOneDao.insert(subOneEntity)

        val subTwoEntity = SubTwoEntityTestBuilder(someUniqueString = someString).build()
        this.subTwoDao.insert(subTwoEntity)

        //WHEN
        val actualSubOne = this.subOneDao.findOneBySomeUniqueString(someString)
        val actualSubTwo = this.subTwoDao.findOneBySomeUniqueString(someString)

        assertThat(this.subOneDao.existsBySomeUniqueString(someString)).isTrue()
        assertThat(this.subTwoDao.existsBySomeUniqueString(someString)).isTrue()

        //THEN
        assertThat(actualSubOne.id).isEqualTo(subOneEntity.id)
        assertThat(actualSubTwo.id).isEqualTo(subTwoEntity.id)

        assertThatThrownBy {
            this.subOneDao.findOneBySomeUniqueString(UUID.randomUUID().toString())
        }.isInstanceOf(EntityNotFoundException::class.java)
            .hasMessageContaining(SubOneEntityMeta.TABLE_NAME.value)

    }


    @Test
    fun `test upsert by a unique field`() {

        val subOneEntity1 = SubOneEntityTestBuilder().build()
        this.subOneDao.insert(subOneEntity1)

        val subOneEntity2 = SubOneEntityTestBuilder(someUniqueString = subOneEntity1.someUniqueString).build()
        this.subOneDao.upsertBySomeUniqueString(subOneEntity2)

        val subOneEntity2Exists = this.subOneDao.existsBySomeUniqueString(subOneEntity2.someUniqueString)
        assertThat(subOneEntity2Exists).isTrue()

        val updatedSubOne = this.subOneDao.findOneBySomeUniqueString(subOneEntity2.someUniqueString)
        assertThat(updatedSubOne.id).isEqualTo(subOneEntity1.id)
        assertThat(updatedSubOne.someString).isEqualTo(subOneEntity2.someString)

    }


}
