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
    private lateinit var leftDao: LeftManyDao


    @Autowired
    private lateinit var rightDao: RightManyDao


    @Autowired
    private lateinit var manyToManyJoinDao: LeftToRightManyToManyJoinDao


    @Autowired
    private lateinit var simpleJoinDao: LeftToRightSimpleJoinDao


    private lateinit var leftEntity: LeftManyEntity


    private lateinit var rightEntity1: RightManyEntity


    private lateinit var rightEntity2: RightManyEntity


    private lateinit var join1: LeftToRightManyToManyJoinEntity


    private lateinit var join2: LeftToRightManyToManyJoinEntity


    @BeforeEach
    fun setUp() {

        leftEntity = LeftManyEntityTestBuilder().build()
        rightEntity1 = RightManyEntityTestBuilder(someString = "alpha").build()
        rightEntity2 = RightManyEntityTestBuilder(someString = "beta").build()
        join1 = LeftToRightManyToManyJoinEntityTestBuilder(leftId = leftEntity.id, rightId = rightEntity1.id).build()
        join2 = LeftToRightManyToManyJoinEntityTestBuilder(leftId = leftEntity.id, rightId = rightEntity2.id).build()

        jdbcOps.update("delete from maia.left_to_right_simple_join_history")
        manyToManyJoinDao.deleteAll()
        simpleJoinDao.deleteAll()
        leftDao.deleteAll()
        rightDao.deleteAll()
        leftDao.insert(leftEntity)
        rightDao.bulkInsert(listOf(rightEntity1, rightEntity2))
        manyToManyJoinDao.bulkInsert(listOf(join1, join2))

    }


    @Test
    fun `fetchForEdit returns rightEntities sorted by someString`() {

        assertThat(
            mockMvc.get().uri("/api/left-many/fetch-for-edit/${leftEntity.id}")
                .with(user("nigel").roles("ADMIN"))
                .exchange()
        ).hasStatus(HttpStatus.OK)
         .bodyJson()
         .isLenientlyEqualTo("""
            {
                "createdTimestampUtc": "${leftEntity.createdTimestampUtc}",
                "id": "${leftEntity.id}",
                "rightEntities": [
                    {"id": "${join1.id}", "entityId": "${rightEntity1.id}", "name": "${rightEntity1.someString}"},
                    {"id": "${join2.id}", "entityId": "${rightEntity2.id}", "name": "${rightEntity2.someString}"}
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
            mockMvc.get().uri("/api/left-many/fetch-for-edit/${leftEntity.id}")
                .with(user("nigel").roles("ADMIN"))
                .exchange()
        ).hasStatus(HttpStatus.OK)
         .bodyJson()
         .isEqualTo("""
            {
                "createdTimestampUtc": "${leftEntity.createdTimestampUtc}",
                "id": "${leftEntity.id}",
                "rightEntities": [],
                "rightLeftToRightSimpleJoinEntities": [],
                "someInt": ${leftEntity.someInt},
                "someString": "${leftEntity.someString}",
                "version": ${leftEntity.version}
            }
         """.trimIndent())

    }

}
