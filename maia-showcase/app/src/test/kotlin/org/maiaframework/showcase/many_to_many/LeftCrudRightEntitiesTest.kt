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
    private lateinit var leftRightComplexDao: LeftToRightComplexDao


    @Autowired
    private lateinit var leftToRightSimpleDao: LeftToRightSimpleDao


    private lateinit var rightEntity1: RightManyEntity


    private lateinit var rightEntity2: RightManyEntity


    private lateinit var rightEntity3: RightManyEntity


    @BeforeEach
    fun setUp() {

        rightEntity1 = RightManyEntityTestBuilder(someString = "right1").build()
        rightEntity2 = RightManyEntityTestBuilder(someString = "right2").build()
        rightEntity3 = RightManyEntityTestBuilder(someString = "right3").build()

        leftRightComplexDao.deleteAll()
        truncateTable(LeftToRightUserEffectiveEntityMeta.SCHEMA_AND_TABLE_NAME)
        truncateTable(LeftToRightSystemEffectiveEntityMeta.SCHEMA_AND_TABLE_NAME)
        truncateTable(LeftToRightSimpleEntityMeta.SCHEMA_AND_TABLE_NAME)
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
                    {"rightEntityId": "${rightEntity1.id}", "someInt": 10},
                    {"rightEntityId": "${rightEntity2.id}", "someInt": 20}
                ]
            }""".trimIndent()
        ).hasStatus(201)

        val leftEntities = leftDao.findAllAsSequence().toList()
        assertThat(leftEntities).hasSize(1)

        val joins = leftRightComplexDao.findByLeft(leftEntities.first().id)
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

        val joins = leftRightComplexDao.findByLeft(leftEntities.first().id)
        assertThat(joins).isEmpty()

    }


    @Test
    fun `update replaces rightEntities`() {

        // Create with right1 and right2
        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test", "rightEntities": [{"rightEntityId": "${rightEntity1.id}", "someInt": 10}, {"rightEntityId": "${rightEntity2.id}", "someInt": 20}]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id

        // Update to right2 and right3 only
        put(
            "/api/left-many/update",
            """{"id": "$leftId", "someInt": 2, "someString": "test2", "version": 1, "rightEntities": [{"rightEntityId": "${rightEntity2.id}", "someInt": 30}, {"rightEntityId": "${rightEntity3.id}", "someInt": 40}]}"""
        ).hasStatus(HttpStatus.OK)

        val joins = leftRightComplexDao.findByLeft(leftId).filter { it.effectiveTo == null }
        assertThat(joins).hasSize(2)
        assertThat(joins.map { it.right }).containsExactlyInAnyOrder(rightEntity2.id, rightEntity3.id)

    }


    @Test
    fun `update with empty rightEntities removes all join records`() {

        // Create with right1
        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test", "rightEntities": [{"rightEntityId": "${rightEntity1.id}", "someInt": 10}]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id

        put(
            "/api/left-many/update",
            """{"id": "$leftId", "someInt": 1, "someString": "test", "version": 1, "rightEntities": []}"""
        ).hasStatus(HttpStatus.OK)

        val joins = leftRightComplexDao.findEffectiveByLeft(leftId)
        assertThat(joins).isEmpty()

    }


    @Test
    fun `update with unchanged effectiveFrom and effectiveTo preserves join id and createdTimestampUtc`() {

        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test", "rightEntities": [{"rightEntityId": "${rightEntity1.id}", "someInt": 10}]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id
        val joinBefore = leftRightComplexDao.findByLeft(leftId).single()

        put(
            "/api/left-many/update",
            """{"id": "$leftId", "someInt": 2, "someString": "test2", "version": 1, "rightEntities": [{"id": "${joinBefore.id}", "rightEntityId": "${rightEntity1.id}", "someInt": 10}]}"""
        ).hasStatus(HttpStatus.OK)

        val joinAfter = leftRightComplexDao.findByLeft(leftId).single()
        assertThat(joinAfter.id).isEqualTo(joinBefore.id)
        assertThat(joinAfter.createdTimestampUtc).isEqualTo(joinBefore.createdTimestampUtc)
        assertThat(joinAfter.effectiveFrom).isEqualTo(joinBefore.effectiveFrom)
        assertThat(joinAfter.effectiveTo).isEqualTo(joinBefore.effectiveTo)

    }


    @Test
    fun `update with changed effectiveFrom does not update effectiveFrom for SYSTEM-managed join`() {

        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test", "rightEntities": [{"rightEntityId": "${rightEntity1.id}", "someInt": 10}]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id
        val joinBefore = leftRightComplexDao.findByLeft(leftId).single()

        put(
            "/api/left-many/update",
            """{"id": "$leftId", "someInt": 1, "someString": "test", "version": 1, "rightEntities": [{"id": "${joinBefore.id}", "rightEntityId": "${rightEntity1.id}", "someInt": 10, "effectiveFrom": "2026-01-01T00:00:00Z"}]}"""
        ).hasStatus(HttpStatus.OK)

        val joinAfter = leftRightComplexDao.findByLeft(leftId).single()
        assertThat(joinAfter.id).isEqualTo(joinBefore.id)
        assertThat(joinAfter.createdTimestampUtc).isEqualTo(joinBefore.createdTimestampUtc)
        assertThat(joinAfter.effectiveFrom).isEqualTo(joinBefore.effectiveFrom)

    }


    @Test
    fun `update with mixed submission deletes, updates and inserts correctly`() {

        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test", "rightEntities": [{"rightEntityId": "${rightEntity1.id}", "someInt": 10}, {"rightEntityId": "${rightEntity2.id}", "someInt": 20}]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id
        val joins = leftRightComplexDao.findByLeft(leftId)
        val join1 = joins.single { it.right == rightEntity1.id }
        // join2 (rightEntity2) will be omitted -> soft-deleted

        put(
            "/api/left-many/update",
            """{"id": "$leftId", "someInt": 1, "someString": "test", "version": 1, "rightEntities": [
                {"id": "${join1.id}", "rightEntityId": "${rightEntity1.id}", "someInt": 10, "effectiveFrom": "2026-01-01T00:00:00Z"},
                {"rightEntityId": "${rightEntity3.id}", "someInt": 30}
            ]}"""
        ).hasStatus(HttpStatus.OK)

        val joinsAfter = leftRightComplexDao.findByLeft(leftId).filter { it.effectiveTo == null }
        assertThat(joinsAfter.map { it.right }).containsExactlyInAnyOrder(rightEntity1.id, rightEntity3.id)

        val join1After = joinsAfter.single { it.right == rightEntity1.id }
        assertThat(join1After.id).isEqualTo(join1.id)
        assertThat(join1After.createdTimestampUtc).isEqualTo(join1.createdTimestampUtc)
        assertThat(join1After.effectiveFrom).isEqualTo(join1.effectiveFrom)

    }


    @Test
    fun `update with changed someInt closes old record and creates new record`() {

        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test", "rightEntities": [{"rightEntityId": "${rightEntity1.id}", "someInt": 10}]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id
        val joinBefore = leftRightComplexDao.findByLeft(leftId).single()

        put(
            "/api/left-many/update",
            """{"id": "$leftId", "someInt": 1, "someString": "test", "version": 1, "rightEntities": [{"id": "${joinBefore.id}", "rightEntityId": "${rightEntity1.id}", "someInt": 20}]}"""
        ).hasStatus(HttpStatus.OK)

        val allJoins = leftRightComplexDao.findByLeft(leftId)
        assertThat(allJoins).hasSize(2)

        val closedJoin = allJoins.single { it.id == joinBefore.id }
        assertThat(closedJoin.effectiveTo).isNotNull()

        val newJoin = allJoins.single { it.id != joinBefore.id }
        assertThat(newJoin.effectiveTo).isNull()
        assertThat(newJoin.someInt).isEqualTo(20)
        assertThat(newJoin.right).isEqualTo(rightEntity1.id)

    }


    @Test
    fun `update with unchanged someInt preserves join record`() {

        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test", "rightEntities": [{"rightEntityId": "${rightEntity1.id}", "someInt": 10}]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id
        val joinBefore = leftRightComplexDao.findByLeft(leftId).single()

        put(
            "/api/left-many/update",
            """{"id": "$leftId", "someInt": 1, "someString": "test", "version": 1, "rightEntities": [{"id": "${joinBefore.id}", "rightEntityId": "${rightEntity1.id}", "someInt": 10}]}"""
        ).hasStatus(HttpStatus.OK)

        val allJoins = leftRightComplexDao.findByLeft(leftId)
        assertThat(allJoins).hasSize(1)
        assertThat(allJoins.single().id).isEqualTo(joinBefore.id)
        assertThat(allJoins.single().effectiveTo).isNull()

    }


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
        ).debug()

    }


}
