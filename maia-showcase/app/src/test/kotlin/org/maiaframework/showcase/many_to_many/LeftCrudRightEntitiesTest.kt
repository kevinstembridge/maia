package org.maiaframework.showcase.many_to_many

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert

class LeftCrudRightEntitiesTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var leftDao: LeftManyDao


    @Autowired
    private lateinit var rightDao: RightManyDao


    @Autowired
    private lateinit var manyToManyJoinDao: LeftToRightManyToManyJoinDao


    private val rightEntity1 = RightManyEntityTestBuilder(someString = "right1").build()
    private val rightEntity2 = RightManyEntityTestBuilder(someString = "right2").build()
    private val rightEntity3 = RightManyEntityTestBuilder(someString = "right3").build()


    private fun post(path: String, body: String): MvcTestResultAssert {
        val csrfCookie = `fetch CSRF cookie`()
        return assertThat(
            mockMvc.post().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("X-XSRF-TOKEN", csrfCookie.value)
                .with(user("nigel").authorities(SimpleGrantedAuthority("WRITE")))
                .cookie(csrfCookie)
                .exchange()
        )
    }

    private fun put(path: String, body: String): MvcTestResultAssert {
        val csrfCookie = `fetch CSRF cookie`()
        return assertThat(
            mockMvc.put().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("X-XSRF-TOKEN", csrfCookie.value)
                .with(user("nigel").authorities(SimpleGrantedAuthority("WRITE")))
                .cookie(csrfCookie)
                .exchange()
        )
    }


    @BeforeEach
    fun setUp() {

        manyToManyJoinDao.deleteAll()
        leftDao.deleteAll()
        rightDao.deleteAll()
        rightDao.bulkInsert(listOf(rightEntity1, rightEntity2, rightEntity3))

    }


    @Test
    fun `create with rightEntities creates join records`() {

        post(
            "/api/left-many/create",
            """
            {
                "someInt": 1,
                "someString": "test",
                "rightEntities": [
                    {"rightEntityId": "${rightEntity1.id}"},
                    {"rightEntityId": "${rightEntity2.id}"}
                ]
            }""".trimIndent()
        ).hasStatus(201)

        val leftEntities = leftDao.findAllAsSequence().toList()
        assertThat(leftEntities).hasSize(1)

        val joins = manyToManyJoinDao.findByLeft(leftEntities.first().id)
        assertThat(joins).hasSize(2)
        assertThat(joins.map { it.right }).containsExactlyInAnyOrder(rightEntity1.id, rightEntity2.id)

    }


    @Test
    fun `create with no rightEntityIds creates no join records`() {

        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test"}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftEntities = leftDao.findAllAsSequence().toList()
        assertThat(leftEntities).hasSize(1)

        val joins = manyToManyJoinDao.findByLeft(leftEntities.first().id)
        assertThat(joins).isEmpty()

    }


    @Test
    fun `update replaces rightEntities`() {

        // Create with right1 and right2
        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test", "rightEntities": [{"rightEntityId": "${rightEntity1.id}"}, {"rightEntityId": "${rightEntity2.id}"}]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id

        // Update to right2 and right3 only
        put(
            "/api/left-many/update",
            """{"id": "$leftId", "someInt": 2, "someString": "test2", "rightEntities": [{"rightEntityId": "${rightEntity2.id}"}, {"rightEntityId": "${rightEntity3.id}"}]}"""
        ).hasStatus(HttpStatus.OK)

        val joins = manyToManyJoinDao.findByLeft(leftId)
        assertThat(joins).hasSize(2)
        assertThat(joins.map { it.right }).containsExactlyInAnyOrder(rightEntity2.id, rightEntity3.id)

    }


    @Test
    fun `update with empty rightEntities removes all join records`() {

        // Create with right1
        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test", "rightEntities": [{"rightEntityId": "${rightEntity1.id}"}]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id

        put(
            "/api/left-many/update",
            """{"id": "$leftId", "someInt": 1, "someString": "test", "rightEntities": []}"""
        ).hasStatus(HttpStatus.OK)

        val joins = manyToManyJoinDao.findByLeft(leftId)
        assertThat(joins).isEmpty()

    }


}
