package org.maiaframework.gen.testing.jdbc.suuper

import org.maiaframework.gen.testing.jdbc.AbstractJdbcTest
import org.maiaframework.gen.testing.jdbc.sample.suuper.SubOneDao
import org.maiaframework.gen.testing.jdbc.sample.suuper.SubOneEntityMeta
import org.maiaframework.gen.testing.jdbc.sample.suuper.SubTwoDao
import org.maiaframework.jdbc.EntityNotFoundException
import org.maiaframework.testing.domain.Anys.anyString
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class SuperDaoTest : AbstractJdbcTest() {


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


}
