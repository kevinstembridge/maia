package org.maiaframework.showcase.many_to_many

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user

class LeftFetchForEditRightEntitiesTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var leftDao: LeftDao


    @Autowired
    private lateinit var rightDao: RightDao


    @Autowired
    private lateinit var manyToManyJoinDao: LeftToRightManyToManyJoinDao


    private val leftEntity = LeftEntityTestBuilder().build()
    private val rightEntity1 = RightEntityTestBuilder(someString = "alpha").build()
    private val rightEntity2 = RightEntityTestBuilder(someString = "beta").build()


    @BeforeEach
    fun setUp() {

        manyToManyJoinDao.deleteAll()
        leftDao.deleteAll()
        rightDao.deleteAll()
        leftDao.insert(leftEntity)
        rightDao.bulkInsert(listOf(rightEntity1, rightEntity2))
        manyToManyJoinDao.bulkInsert(listOf(
            LeftToRightManyToManyJoinEntityTestBuilder(leftId = leftEntity.id, rightId = rightEntity1.id).build(),
            LeftToRightManyToManyJoinEntityTestBuilder(leftId = leftEntity.id, rightId = rightEntity2.id).build()
        ))

    }


    @Test
    fun `fetchForEdit returns rightEntities sorted by someString`() {

        assertThat(
            mockMvc.get().uri("/api/left/fetch_for_edit/${leftEntity.id}")
                .with(user("nigel").roles("ADMIN"))
                .exchange()
        ).hasStatus(HttpStatus.OK)
         .bodyJson()
         .isEqualTo("""
            {
                "createdTimestampUtc": "${leftEntity.createdTimestampUtc}",
                "id": "${leftEntity.id}",
                "rightEntities": [
                    {"id": "${rightEntity1.id}", "name": "${rightEntity1.someString}"},
                    {"id": "${rightEntity2.id}", "name": "${rightEntity2.someString}"}
                ],
                "someInt": ${leftEntity.someInt},
                "someString": "${leftEntity.someString}"
            }
         """.trimIndent())

    }


    @Test
    fun `fetchForEdit returns empty rightEntities when none associated`() {

        manyToManyJoinDao.deleteAll()

        assertThat(
            mockMvc.get().uri("/api/left/fetch_for_edit/${leftEntity.id}")
                .with(user("nigel").roles("ADMIN"))
                .exchange()
        ).hasStatus(HttpStatus.OK)
         .bodyJson()
         .isEqualTo("""
            {
                "createdTimestampUtc": "${leftEntity.createdTimestampUtc}",
                "id": "${leftEntity.id}",
                "rightEntities": [],
                "someInt": ${leftEntity.someInt},
                "someString": "${leftEntity.someString}"
            }
         """.trimIndent())

    }

}
