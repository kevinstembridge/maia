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
import java.time.LocalDate
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
    fun `test filter text equals`() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "someStringFromRight" to mapOf(
                    "filterType" to "text",
                    "type" to "equals",
                    "filter" to "aSomeRightValue1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 3,
                rows = listOf(
                    leftEntity1 to rightEntity1,
                    leftEntity2 to rightEntity1,
                    leftEntity3 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )


    }


    @Test
    fun testFilter_text_notEqual() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            sortModel = listOf(
                mapOf(
                    "colId" to "someIntFromLeft",
                    "sort" to "asc"
                ),
                mapOf(
                    "colId" to "someIntFromRight",
                    "sort" to "asc"
                )
            ),
            filterModel = mapOf(
                "someStringFromLeft" to mapOf(
                    "filterType" to "text",
                    "type" to "notEqual",
                    "filter" to "aSomeLeftValue1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    leftEntity2 to rightEntity1,
                    leftEntity2 to rightEntity2,
                    leftEntity3 to rightEntity1
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_text_notEqual_2() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            sortModel = listOf(
                mapOf(
                    "colId" to "someIntFromLeft",
                    "sort" to "asc"
                ),
                mapOf(
                    "colId" to "someIntFromRight",
                    "sort" to "asc"
                )
            ),
            filterModel = mapOf(
                "someStringFromLeft" to mapOf(
                    "filterType" to "text",
                    "type" to "notEqual",
                    "filter" to "bSomeLeftValue1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    leftEntity1 to rightEntity1,
                    leftEntity1 to rightEntity2,
                    leftEntity3 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_text_contains() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            sortModel = listOf(
                mapOf(
                    "colId" to "someIntFromLeft",
                    "sort" to "asc"
                ),
                mapOf(
                    "colId" to "someIntFromRight",
                    "sort" to "asc"
                )
            ),
            filterModel = mapOf(
                "someStringFromLeft" to mapOf(
                    "filterType" to "text",
                    "type" to "contains",
                    "filter" to "SomeLeftValue"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 6,
                rows = listOf(
                    leftEntity1 to rightEntity1,
                    leftEntity1 to rightEntity2,
                    leftEntity2 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_text_notContains() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            sortModel = listOf(
                mapOf(
                    "colId" to "someIntFromRight",
                    "sort" to "asc"
                )
            ),
            filterModel = mapOf(
                "someStringFromLeft" to mapOf(
                    "filterType" to "text",
                    "type" to "notContains",
                    "filter" to "Value1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    leftEntity3 to rightEntity1,
                    leftEntity3 to rightEntity2,
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_text_startsWith() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            sortModel = listOf(
                mapOf(
                    "colId" to "someIntFromLeft",
                    "sort" to "asc"
                ),
                mapOf(
                    "colId" to "someIntFromRight",
                    "sort" to "asc"
                )
            ),
            filterModel = mapOf(
                "someStringFromLeft" to mapOf(
                    "filterType" to "text",
                    "type" to "startsWith",
                    "filter" to "bSomeLeft"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    leftEntity2 to rightEntity1,
                    leftEntity2 to rightEntity2,
                    leftEntity3 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_text_endsWith() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            sortModel = listOf(
                mapOf(
                    "colId" to "someIntFromLeft",
                    "sort" to "asc"
                ),
                mapOf(
                    "colId" to "someIntFromRight",
                    "sort" to "asc"
                )
            ),
            filterModel = mapOf(
                "someStringFromLeft" to mapOf(
                    "filterType" to "text",
                    "type" to "endsWith",
                    "filter" to "Value1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    leftEntity1 to rightEntity1,
                    leftEntity1 to rightEntity2,
                    leftEntity2 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_date_equals() {

        val today = LocalDate.now().toString()

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "createdTimestampUtc" to mapOf(
                    "filterType" to "date",
                    "type" to "equals",
                    "dateFrom" to today,
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    leftEntity2 to rightEntity1,
                    leftEntity2 to rightEntity2,
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    @Disabled
    fun testFilter_date_equals_with_multiple_conditions() {

        val today = LocalDate.now().toString()
        val tomorrow = LocalDate.now().plusDays(1).toString()

        val actualResult = submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "createdTimestampUtc" to mapOf(
                    "operator" to "OR",
                    "condition1" to mapOf(
                        "filterType" to "date",
                        "type" to "equals",
                        "dateFrom" to today,
                        "dateTo" to null
                    ),
                        "condition2" to mapOf(
                        "filterType" to "date",
                        "type" to "equals",
                        "dateFrom" to tomorrow,
                        "dateTo" to null
                    )
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        )

        val expectedResult = expectedResult(
            totalCount = 3,
            rows = listOf(
//                Pair(bravoEntity2, leftEntity1),
//                Pair(bravoEntity3, leftEntity2),
//                Pair(bravoEntity4, leftEntity2)
            ),
            firstResultIndex = 1,
            lastResultIndex = 3,
            offset = 0,
            limit = 3
        )

        actualResult.bodyJson().isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_date_greaterThan() {

        val today = LocalDate.now().toString()

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "createdTimestampUtc" to mapOf(
                    "filterType" to "date",
                    "type" to "greaterThan",
                    "dateFrom" to today,
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    leftEntity3 to rightEntity1,
                    leftEntity3 to rightEntity2,
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )


    }


    @Test
    fun testFilter_date_lessThan() {

        val today = LocalDate.now().toString()

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "createdTimestampUtc" to mapOf(
                    "filterType" to "date",
                    "type" to "lessThan",
                    "dateFrom" to today,
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    leftEntity1 to rightEntity1,
                    leftEntity1 to rightEntity2,
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_date_notEqual() {

        val today = LocalDate.now().toString()

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "createdTimestampUtc" to mapOf(
                    "filterType" to "date",
                    "type" to "notEqual",
                    "dateFrom" to today,
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    leftEntity1 to rightEntity1,
                    leftEntity1 to rightEntity2,
                    leftEntity3 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_date_inRange() {

        val yesterday = LocalDate.now().minusDays(1).toString()
        val today = LocalDate.now().toString()

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "createdTimestampUtc" to mapOf(
                    "filterType" to "date",
                    "type" to "inRange",
                    "dateFrom" to yesterday,
                    "dateTo" to today
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    leftEntity1 to rightEntity1,
                    leftEntity1 to rightEntity2,
                    leftEntity2 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_number_equals() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "someIntFromLeft" to mapOf(
                    "filterType" to "number",
                    "type" to "equals",
                    "filter" to 2
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    leftEntity2 to rightEntity1,
                    leftEntity2 to rightEntity2,
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    @Disabled("Until we support Ag Grid queries")
    fun testFilter_number_equals_with_multiple_and_conditions() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "someStringFromLeft" to mapOf(
                    "operator" to "AND",
                    "condition1" to mapOf(
                        "filterType" to "number",
                        "type" to "equals",
                        "filter" to 1.5
                    ),
                        "condition2" to mapOf(
                        "filterType" to "number",
                        "type" to "equals",
                        "filter" to 2.5
                    )
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 0,
                rows = emptyList(),
                firstResultIndex = 1,
                lastResultIndex = 0,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    @Disabled("Until we support Ag Grid queries")
    fun testFilter_number_equals_with_multiple_or_conditions() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "amount" to mapOf(
                    "condition1" to mapOf(
                        "filterType" to "number",
                        "type" to "equals",
                        "filter" to 1.5
                    ),
                    "condition2" to mapOf(
                        "filterType" to "number",
                        "type" to "equals",
                        "filter" to 2.5
                    ),
                    "operator" to "OR"
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        )

//        val expectedResult = expectedResult(
//                totalCount = 2,
//                rows = listOf(
//                        personEntity1,
//                        personEntity2))
//
//        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_number_greaterThan() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "someIntFromLeft" to mapOf(
                    "filterType" to "number",
                    "type" to "greaterThan",
                    "filter" to 1
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    leftEntity2 to rightEntity1,
                    leftEntity2 to rightEntity2,
                    leftEntity3 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_number_greaterThanOrEqual() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "someIntFromLeft" to mapOf(
                    "filterType" to "number",
                    "type" to "greaterThanOrEqual",
                    "filter" to 1.5
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    leftEntity2 to rightEntity1,
                    leftEntity2 to rightEntity2,
                    leftEntity3 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

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


    @Test
    fun testFilter_number_lessThanOrEqual() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "someIntFromLeft" to mapOf(
                    "filterType" to "number",
                    "type" to "lessThanOrEqual",
                    "filter" to 2.5
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    leftEntity1 to rightEntity1,
                    leftEntity1 to rightEntity2,
                    leftEntity2 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_number_notEqual() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "someIntFromLeft" to mapOf(
                    "filterType" to "number",
                    "type" to "notEqual",
                    "filter" to 1
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    leftEntity2 to rightEntity1,
                    leftEntity2 to rightEntity2,
                    leftEntity3 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_number_inRange() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "someIntFromLeft" to mapOf(
                    "filterType" to "number",
                    "type" to "inRange",
                    "filter" to 1,
                    "filterTo" to 3
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "someStringFromLeft",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    leftEntity1 to rightEntity1,
                    leftEntity1 to rightEntity2,
                    leftEntity2 to rightEntity1,
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    @Disabled("Until we support Ag Grid queries")
    fun testFilter_multi_conditions_on_multi_columns() {

        val today = LocalDate.now().toString()
        val tomorrow = LocalDate.now().plusDays(1).toString()

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "amount" to mapOf(
                    "condition1" to mapOf(
                        "filterType" to "number",
                        "type" to "equals",
                        "filter" to 1.5 // => PersonEntity1
                    ),
                    "condition2" to mapOf(
                        "filterType" to "number",
                        "type" to "equals",
                        "filter" to 2.5 // => PersonEntity2
                    ),
                    "operator" to "OR"
                ),
                "createdTimestampUtc" to mapOf(
                    "condition1" to mapOf(
                        "filterType" to "date",
                        "type" to "equals",
                        "dateFrom" to today, // => PersonEntity2, UserEntity1
                        "dateTo" to null
                    ),
                    "condition2" to mapOf(
                        "filterType" to "date",
                        "type" to "equals",
                        "dateFrom" to tomorrow, // => UserEntity2
                        "dateTo" to null
                    ),
                    "operator" to "OR"
                )
            )
        )

    }


    @Test
    fun testFilter_on_multi_columns() {

        val today = LocalDate.now().toString()

        submitSearch(
            startRow = 0,
            endRow = 3,
            sortModel = listOf(
                mapOf(
                    "colId" to "someIntFromLeft",
                    "sort" to "asc"
                ),
                mapOf(
                    "colId" to "someIntFromRight",
                    "sort" to "asc"
                )
            ),
            filterModel = mapOf(
                "someIntFromLeft" to mapOf(
                    "filterType" to "number",
                    "type" to "equals",
                    "filter" to 2
                ),
                "createdTimestampUtc" to mapOf(
                    "filterType" to "date",
                    "type" to "equals",
                    "dateFrom" to today,
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    leftEntity2 to rightEntity1,
                    leftEntity2 to rightEntity2,
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    private fun submitSearch(
        path: String = "/api/left_searchable/search",
        startRow: Int,
        endRow: Int,
        sortModel: List<Map<String, String>> = emptyList(),
        filterModel: Map<String, Any?> = emptyMap()
    ): MvcTestResultAssert {

        val requestBody = asJson(
            mapOf(
                "startRow" to startRow,
                "endRow" to endRow,
                "sortModel" to sortModel,
                "filterModel" to filterModel
            )
        )

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


    private fun expectedResult(
        totalCount: Int,
        rows: List<Pair<LeftEntity, RightEntity>>,
        firstResultIndex: Int,
        lastResultIndex: Int,
        offset: Int,
        limit: Int
    ): String {

        val expectedResultData = rows.map { jsonFor(it) }

        return asJson(mapOf(
            "totalResultCount" to totalCount,
            "results" to expectedResultData,
            "firstResultIndex" to firstResultIndex,
            "lastResultIndex" to lastResultIndex,
            "limit" to limit,
            "offset" to offset
        ))

    }


    private fun jsonFor(pair: Pair<LeftEntity, RightEntity>): Map<String, Any?> {

        val leftEntity = pair.first
        val rightEntity = pair.second

        return mapOf(
            "id" to leftEntity.id.value,
            "someStringFromRight" to rightEntity.someString,
            "someStringFromLeft" to leftEntity.someString,
            "someIntFromRight" to rightEntity.someInt,
            "someIntFromLeft" to leftEntity.someInt,
            "createdTimestampUtc" to leftEntity.createdTimestampUtc.truncatedTo(ChronoUnit.MILLIS).toString()
        )

    }


}
