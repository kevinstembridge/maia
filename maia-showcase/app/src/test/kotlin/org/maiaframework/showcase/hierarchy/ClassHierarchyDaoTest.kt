package org.maiaframework.showcase.hierarchy

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.jdbc.EntityNotFoundException
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.testing.domain.Anys.anyString
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID


class ClassHierarchyDaoTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var parentOneDao: ParentOneDao


    @Autowired
    private lateinit var parentTwoDao: ParentTwoDao


    @Autowired
    private lateinit var childOneDao: ChildOneDao


    @BeforeEach
    fun beforeEach() {

        this.parentOneDao.deleteAll()
        this.parentTwoDao.deleteAll()
        this.childOneDao.deleteAll()

    }


    @Test
    fun testFindOneBySomeUniqueString() {

        //GIVEN
        val someString = anyString()
        val subOneEntity = ParentOneEntityTestBuilder(someUniqueString = someString).build()
        this.parentOneDao.insert(subOneEntity)

        val subTwoEntity = ParentTwoEntityTestBuilder(someUniqueString = someString).build()
        this.parentTwoDao.insert(subTwoEntity)

        //WHEN
        val actualSubOne = this.parentOneDao.findOneBySomeUniqueString(someString)
        val actualSubTwo = this.parentTwoDao.findOneBySomeUniqueString(someString)

        assertThat(this.parentOneDao.existsBySomeUniqueString(someString)).isTrue()
        assertThat(this.parentTwoDao.existsBySomeUniqueString(someString)).isTrue()

        //THEN
        assertThat(actualSubOne.id).isEqualTo(subOneEntity.id)
        assertThat(actualSubTwo.id).isEqualTo(subTwoEntity.id)

        assertThatThrownBy {
            this.parentOneDao.findOneBySomeUniqueString(UUID.randomUUID().toString())
        }.isInstanceOf(EntityNotFoundException::class.java)
            .hasMessageContaining(ParentOneEntityMeta.TABLE_NAME.value)

    }


    @Test
    fun `test upsert by a unique field`() {

        val subOneEntity1 = ParentOneEntityTestBuilder().build()
        this.parentOneDao.insert(subOneEntity1)

        val subOneEntity2 = ParentOneEntityTestBuilder(someUniqueString = subOneEntity1.someUniqueString).build()
        this.parentOneDao.upsertBySomeUniqueString(subOneEntity2)

        val subOneEntity2Exists = this.parentOneDao.existsBySomeUniqueString(subOneEntity2.someUniqueString)
        assertThat(subOneEntity2Exists).isTrue()

        val updatedSubOne = this.parentOneDao.findOneBySomeUniqueString(subOneEntity2.someUniqueString)
        assertThat(updatedSubOne.id).isEqualTo(subOneEntity1.id)
        assertThat(updatedSubOne.someString).isEqualTo(subOneEntity2.someString)

    }


}
