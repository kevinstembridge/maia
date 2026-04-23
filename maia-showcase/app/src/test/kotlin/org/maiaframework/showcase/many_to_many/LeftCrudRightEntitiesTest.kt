package org.maiaframework.showcase.many_to_many

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user

class LeftCrudRightEntitiesTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var leftDao: LeftDao


    @Autowired
    private lateinit var rightDao: RightDao


    @Autowired
    private lateinit var manyToManyJoinDao: LeftToRightManyToManyJoinDao


    private val rightEntity1 = RightEntityTestBuilder(someString = "right1").build()
    private val rightEntity2 = RightEntityTestBuilder(someString = "right2").build()
    private val rightEntity3 = RightEntityTestBuilder(someString = "right3").build()


    @BeforeEach
    fun setUp() {

        manyToManyJoinDao.deleteAll()
        leftDao.deleteAll()
        rightDao.deleteAll()
        rightDao.bulkInsert(listOf(rightEntity1, rightEntity2, rightEntity3))

    }


    @Test
    fun `create with rightEntityIds creates join records`() {

        assertThat_POST(
            "/api/left/create",
            """
            {
                "someInt": 1,
                "someString": "test",
                "rightEntityIds": ["${rightEntity1.id}", "${rightEntity2.id}"]
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

        assertThat_POST(
            "/api/left/create",
            """{"someInt": 1, "someString": "test"}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftEntities = leftDao.findAllAsSequence().toList()
        assertThat(leftEntities).hasSize(1)

        val joins = manyToManyJoinDao.findByLeft(leftEntities.first().id)
        assertThat(joins).isEmpty()

    }


    @Test
    fun `update replaces rightEntityIds`() {

        // Create with right1 and right2
        assertThat_POST(
            "/api/left/create",
            """{"someInt": 1, "someString": "test", "rightEntityIds": ["${rightEntity1.id}", "${rightEntity2.id}"]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id

        // Update to right2 and right3 only
        val csrfCookie = `fetch CSRF cookie`()
        assertThat(
            mockMvc.put().uri("/api/left/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"id": "$leftId", "someInt": 2, "someString": "test2", "rightEntityIds": ["${rightEntity2.id}", "${rightEntity3.id}"]}""")
                .header("X-XSRF-TOKEN", csrfCookie.value)
                .with(user("nigel").roles("ADMIN"))
                .cookie(csrfCookie)
                .exchange()
        ).hasStatus(HttpStatus.OK)

        val joins = manyToManyJoinDao.findByLeft(leftId)
        assertThat(joins).hasSize(2)
        assertThat(joins.map { it.right }).containsExactlyInAnyOrder(rightEntity2.id, rightEntity3.id)

    }


    @Test
    fun `update with empty rightEntityIds removes all join records`() {

        // Create with right1
        assertThat_POST(
            "/api/left/create",
            """{"someInt": 1, "someString": "test", "rightEntityIds": ["${rightEntity1.id}"]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id

        val csrfCookie = `fetch CSRF cookie`()
        assertThat(
            mockMvc.put().uri("/api/left/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"id": "$leftId", "someInt": 1, "someString": "test", "rightEntityIds": []}""")
                .header("X-XSRF-TOKEN", csrfCookie.value)
                .with(user("nigel").roles("ADMIN"))
                .cookie(csrfCookie)
                .exchange()
        ).hasStatus(HttpStatus.OK)

        val joins = manyToManyJoinDao.findByLeft(leftId)
        assertThat(joins).isEmpty()

    }


}
