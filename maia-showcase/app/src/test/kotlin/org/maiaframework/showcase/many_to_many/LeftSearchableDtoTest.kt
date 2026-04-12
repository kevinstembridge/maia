package org.maiaframework.showcase.many_to_many

import org.maiaframework.json.JsonFacade
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockCookie
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert
import java.time.Instant
import java.time.temporal.ChronoUnit

class LeftSearchableDtoTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var leftDao: LeftDao

    @Autowired
    private lateinit var rightDao: RightDao

    @Autowired
    private lateinit var manyToManyJoinDao: LeftToRightManyToManyJoinDao

    @Autowired
    private lateinit var jsonFacade: JsonFacade

    private val timestamp1 = Instant.now().truncatedTo(ChronoUnit.MILLIS).minusSeconds(24 * 60 * 60)
    private val timestamp2 = Instant.now().truncatedTo(ChronoUnit.MILLIS)
    private val timestamp3 = Instant.now().truncatedTo(ChronoUnit.MILLIS).plusSeconds(24 * 60 * 60)

    private val someInt1 = 1
    private val someInt2 = 2
    private val someInt3 = 3

    private val leftEntity1 = LeftEntityTestBuilder(someInt = someInt1, someString = "aSomeLeftValue1", createdTimestampUtc = timestamp1).build()
    private val leftEntity2 = LeftEntityTestBuilder(someInt = someInt2, someString = "bSomeLeftValue1", createdTimestampUtc = timestamp2).build()
    private val leftEntity3 = LeftEntityTestBuilder(someInt = someInt3, someString = "bSomeLeftValue2", createdTimestampUtc = timestamp3).build()

    private val rightEntity1 = RightEntityTestBuilder(someInt = someInt1, someString = "aSomeRightValue1").build()
    private val rightEntity2 = RightEntityTestBuilder(someInt = someInt2, someString = "aSomeRightValue2").build()
    private val rightEntity3 = RightEntityTestBuilder(someInt = someInt3, someString = "aSomeRightValue3").build()

    private val left1ToRight1 = LeftToRightManyToManyJoinEntityTestBuilder(leftId = leftEntity1.id, rightId = rightEntity1.id).build()
    private val left1ToRight2 = LeftToRightManyToManyJoinEntityTestBuilder(leftId = leftEntity1.id, rightId = rightEntity2.id).build()
    private val left2ToRight1 = LeftToRightManyToManyJoinEntityTestBuilder(leftId = leftEntity2.id, rightId = rightEntity1.id).build()
    private val left2ToRight2 = LeftToRightManyToManyJoinEntityTestBuilder(leftId = leftEntity2.id, rightId = rightEntity2.id).build()
    private val left2ToRight3 = LeftToRightManyToManyJoinEntityTestBuilder(leftId = leftEntity2.id, rightId = rightEntity3.id).build()
    private val left3ToRight1 = LeftToRightManyToManyJoinEntityTestBuilder(leftId = leftEntity3.id, rightId = rightEntity1.id).build()
    private val left3ToRight2 = LeftToRightManyToManyJoinEntityTestBuilder(leftId = leftEntity3.id, rightId = rightEntity2.id).build()



    @BeforeEach
    fun beforeEach() {

        this.manyToManyJoinDao.deleteAll()
        this.leftDao.deleteAll()
        this.rightDao.deleteAll()

        this.leftDao.bulkInsert(listOf(leftEntity1, leftEntity2, leftEntity3))
        this.rightDao.bulkInsert(listOf(rightEntity1, rightEntity2, rightEntity3))

        this.manyToManyJoinDao.bulkInsert(listOf(
            left1ToRight1,
            left1ToRight2,
            left2ToRight1,
            left2ToRight2,
            left2ToRight3,
            left3ToRight1,
            left3ToRight2
        ))

    }


    @Test
    @Disabled
    fun should_return_403_for_unauthorised_user() {

        val requestBody = asJson(mapOf<String, String>())

        assertThat(
            mockMvc.post().uri("/api/left_searchable/aggrid_datasource")
                .content(requestBody)
                .exchange()
        ).hasStatus(HttpStatus.FORBIDDEN)

    }


    @Test
    fun testFilter_number_lessThan() {

        submitSearch(
            requestBody = """
            {
                "startRow": 0,
                "endRow": 3,
                "filterModel": {
                    "someIntFromLeft": {
                        "filterType": "number",
                        "type": "lessThan",
                        "filter": 2
                    }
                },
                "sortModel": [
                    {
                        "colId": "someStringFromLeft",
                        "sort": "asc"
                    }
                ]
            }
            """.trimIndent()
        )
            .bodyJson()
            .isEqualTo("""
                {
                    "results": [
                        {
                            "createdTimestampUtc": "${leftEntity1.createdTimestampUtc}",
                            "id": "${leftEntity1.id}",
                            "rightEntities": [
                                {
                                    "id": "${rightEntity1.id}",
                                    "name": "${rightEntity1.someString}"
                                },
                                {
                                    "id": "${rightEntity2.id}",
                                    "name": "${rightEntity2.someString}"
                                }
                            ],
                            "someIntFromLeft": ${leftEntity1.someInt},
                            "someStringFromLeft": "${leftEntity1.someString}"
                        }
                    ],
                    "totalResultCount": 1,
                    "offset": 0,
                    "limit": 3,
                    "firstResultIndex": 1,
                    "lastResultIndex": 1
                }""".trimIndent())

    }


    private fun submitSearch(
        path: String = "/api/left_searchable/search",
        requestBody: String,
    ): MvcTestResultAssert {

        return assertThat(
            mockMvc.post().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .characterEncoding("UTF-8")
                .with(user("nigel").roles("ADMIN"))
                .cookie(MockCookie("XSRF-TOKEN", "test-csrf-token"))
                .header("X-XSRF-TOKEN", "test-csrf-token")
                .exchange()
        ).debug()

    }


}
