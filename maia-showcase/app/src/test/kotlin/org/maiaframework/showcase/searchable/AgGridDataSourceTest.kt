package org.maiaframework.showcase.searchable

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.all_field_types.AllFieldTypesDao
import org.maiaframework.showcase.all_field_types.AllFieldTypesEntity
import org.maiaframework.showcase.all_field_types.AllFieldTypesEntityTestBuilder
import org.maiaframework.showcase.types.SomeIntType
import org.maiaframework.showcase.types.SomeLongType
import org.maiaframework.showcase.types.SomeStringType
import org.maiaframework.showcase.join.AlphaAgGridDao
import org.maiaframework.showcase.join.AlphaAgGridEntity
import org.maiaframework.showcase.join.AlphaAgGridEntityTestBuilder
import org.maiaframework.showcase.join.BravoAgGridDao
import org.maiaframework.showcase.join.BravoAgGridEntity
import org.maiaframework.showcase.join.BravoAgGridEntityTestBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class AgGridDataSourceTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var allFieldTypesDao: AllFieldTypesDao


    @Autowired
    private lateinit var alphaDao: AlphaAgGridDao


    @Autowired
    private lateinit var bravoDao: BravoAgGridDao


    private val timestamp1 = Instant.now().truncatedTo(ChronoUnit.MILLIS).minusSeconds(24 * 60 * 60)


    private val timestamp2 = Instant.now().truncatedTo(ChronoUnit.MILLIS)


    private val timestamp3 = Instant.now().truncatedTo(ChronoUnit.MILLIS).plusSeconds(24 * 60 * 60)


    private val someInt1 = 1


    private val someInt2 = 2


    private val alphaEntity1 = AlphaAgGridEntityTestBuilder(someInt = someInt1, someString = "someAlphaValue1").build()


    private val alphaEntity2 = AlphaAgGridEntityTestBuilder(someInt = someInt2, someString = "someAlphaValue2").build()


    private val bravoEntity1 = BravoAgGridEntityTestBuilder(
        createdTimestampUtc = timestamp1,
        alphaId = alphaEntity1.id,
        someString = "aSomeValue1"
    ).build()


    private val bravoEntity2 = BravoAgGridEntityTestBuilder(
        createdTimestampUtc = timestamp2,
        alphaId = alphaEntity1.id,
        someString = "aSomeValue2"
    ).build()


    private val bravoEntity3 = BravoAgGridEntityTestBuilder(
        createdTimestampUtc = timestamp2,
        alphaId = alphaEntity2.id,
        someString = "bSomeValue3"
    ).build()


    private val bravoEntity4 = BravoAgGridEntityTestBuilder(
        createdTimestampUtc = timestamp3,
        alphaId = alphaEntity2.id,
        someString = "bSomeValue4"
    ).build()


    @BeforeEach
    fun beforeEach() {

        this.allFieldTypesDao.deleteAll()
        this.bravoDao.deleteAll()
        this.alphaDao.deleteAll()

        this.alphaDao.insert(alphaEntity1)
        this.alphaDao.insert(alphaEntity2)
        this.bravoDao.insert(bravoEntity1)
        this.bravoDao.insert(bravoEntity2)
        this.bravoDao.insert(bravoEntity3)
        this.bravoDao.insert(bravoEntity4)

    }


    @Test
    @Disabled
    fun should_return_403_for_unauthorised_user() {

        val requestBody = asJson(mapOf<String, String>())

        assertThat(
            mockMvc.post().uri("/api/bravo_ag_grid/search")
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
                "dtoStringFromBravo" to mapOf(
                    "filterType" to "text",
                    "type" to "equals",
                    "filter" to "aSomeValue1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 1,
                rows = listOf(Pair(bravoEntity1, alphaEntity1)),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3
            )
        )


    }


    @Test
    fun `test filter text equals on parent field`() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "dtoStringFromAlpha" to mapOf(
                    "filterType" to "text",
                    "type" to "equals",
                    "filter" to "someAlphaValue1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity1, alphaEntity1),
                    Pair(bravoEntity2, alphaEntity1)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )


    }


    @Test
    fun `test filter text equals on 2 parent fields`() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "dtoStringFromAlpha" to mapOf(
                    "filterType" to "text",
                    "type" to "equals",
                    "filter" to "someAlphaValue1"
                ),
                "dtoIntFromAlpha" to mapOf(
                    "filterType" to "number",
                    "type" to "equals",
                    "filter" to someInt1
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity1, alphaEntity1),
                    Pair(bravoEntity2, alphaEntity1)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun `test filter text equals with sort on parent field`() {

        submitSearch(
            startRow = 0,
            endRow = 1,
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoIntFromAlpha",
                    "sort" to "desc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    Pair(bravoEntity3, alphaEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 1
            )
        )

    }


    @Test
    fun testFilter_with_empty_filter_model() {

//        val actualResult = submitSearch(
//                path = "/api/very_simple/aggrid_datasource",
//                startRow = 0,
//                endRow = 3,
//                filterModel = emptyMap()
//        )

//        val expectedResult = expectedResult(
//                totalCount = 1,
//                rows = listOf(foreignEntity1))
//
//        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_text_notEqual() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "dtoStringFromBravo" to mapOf(
                    "filterType" to "text",
                    "type" to "notEqual",
                    "filter" to "aSomeValue1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 3,
                rows = listOf(
                    Pair(bravoEntity2, alphaEntity1),
                    Pair(bravoEntity3, alphaEntity2),
                    Pair(bravoEntity4, alphaEntity2)
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
            filterModel = mapOf(
                "dtoStringFromAlpha" to mapOf(
                    "filterType" to "text",
                    "type" to "notEqual",
                    "filter" to "someAlphaValue1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity3, alphaEntity2),
                    Pair(bravoEntity4, alphaEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
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
            filterModel = mapOf(
                "dtoStringFromAlpha" to mapOf(
                    "filterType" to "text",
                    "type" to "contains",
                    "filter" to "AlphaValue1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity1, alphaEntity1),
                    Pair(bravoEntity2, alphaEntity1)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
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
            filterModel = mapOf(
                "dtoStringFromAlpha" to mapOf(
                    "filterType" to "text",
                    "type" to "notContains",
                    "filter" to "Value1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity3, alphaEntity2),
                    Pair(bravoEntity4, alphaEntity2)
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
            filterModel = mapOf(
                "dtoStringFromBravo" to mapOf(
                    "filterType" to "text",
                    "type" to "startsWith",
                    "filter" to "aSomeValue"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity1, alphaEntity1),
                    Pair(bravoEntity2, alphaEntity1),
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
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
            filterModel = mapOf(
                "dtoStringFromAlpha" to mapOf(
                    "filterType" to "text",
                    "type" to "endsWith",
                    "filter" to "Value1"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity1, alphaEntity1),
                    Pair(bravoEntity2, alphaEntity1)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_date_equals() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "createdTimestampUtc" to mapOf(
                    "filterType" to "date",
                    "type" to "equals",
                    "dateFrom" to today
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity2, alphaEntity1),
                    Pair(bravoEntity3, alphaEntity2),
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_date_equals_with_multiple_conditions() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())
        val tomorrow = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now().plusDays(1))

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "createdTimestampUtc" to mapOf(
                    "operator" to "OR",
                    "condition1" to mapOf(
                        "filterType" to "date",
                        "type" to "equals",
                        "dateFrom" to today
                    ),
                    "condition2" to mapOf(
                        "filterType" to "date",
                        "type" to "equals",
                        "dateFrom" to tomorrow
                    ),
                    "conditions" to listOf(
                        mapOf(
                            "filterType" to "date",
                            "type" to "equals",
                            "dateFrom" to today
                        ),
                        mapOf(
                            "filterType" to "date",
                            "type" to "equals",
                            "dateFrom" to tomorrow
                        ),
                    )
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 3,
                rows = listOf(
                    Pair(bravoEntity2, alphaEntity1),
                    Pair(bravoEntity3, alphaEntity2),
                    Pair(bravoEntity4, alphaEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_date_greaterThan() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "createdTimestampUtc" to mapOf(
                    "filterType" to "date",
                    "type" to "greaterThan",
                    "dateFrom" to today
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 1,
                rows = listOf(Pair(bravoEntity4, alphaEntity2)),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3
            )
        )


    }


    @Test
    fun testFilter_date_lessThan() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

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
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 1,
                rows = listOf(Pair(bravoEntity1, alphaEntity1)),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_date_notEqual() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        val expectedResult = expectedResult(
            totalCount = 2,
            rows = listOf(
                Pair(bravoEntity1, alphaEntity1),
                Pair(bravoEntity4, alphaEntity2)
            ),
            firstResultIndex = 1,
            lastResultIndex = 2,
            offset = 0,
            limit = 3
        )
        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "createdTimestampUtc" to mapOf(
                    "filterType" to "date",
                    "type" to "notEqual",
                    "dateFrom" to today,
                    "dateTo" to null
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_date_inRange() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())
        val tomorrow = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now().plusDays(1))

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "createdTimestampUtc" to mapOf(
                    "filterType" to "date",
                    "type" to "inRange",
                    "dateFrom" to today,
                    "dateTo" to tomorrow
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 3,
                rows = listOf(
                    Pair(bravoEntity2, alphaEntity1),
                    Pair(bravoEntity3, alphaEntity2),
                    Pair(bravoEntity4, alphaEntity2)
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
                "dtoIntFromAlpha" to mapOf(
                    "filterType" to "number",
                    "type" to "equals",
                    "filter" to 2
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity3, alphaEntity2),
                    Pair(bravoEntity4, alphaEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_number_equals_with_multiple_and_conditions() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "dtoIntFromAlpha" to mapOf(
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
                    ),
                    "conditions" to listOf(
                        mapOf(
                            "filterType" to "number",
                            "type" to "equals",
                            "filter" to 1.5
                        ),
                        mapOf(
                            "filterType" to "number",
                            "type" to "equals",
                            "filter" to 2.5
                        )
                    )
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
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
    fun testFilter_number_equals_with_multiple_or_conditions() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "dtoIntFromBravo" to mapOf(
                    "operator" to "OR",
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
                    "conditions" to listOf(
                        mapOf(
                            "filterType" to "number",
                            "type" to "equals",
                            "filter" to 1.5
                        ),
                        mapOf(
                            "filterType" to "number",
                            "type" to "equals",
                            "filter" to 2.5
                        )
                    )
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        )

    }


    @Test
    fun testFilter_number_greaterThan() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "dtoIntFromAlpha" to mapOf(
                    "filterType" to "number",
                    "type" to "greaterThan",
                    "filter" to 1
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity3, alphaEntity2),
                    Pair(bravoEntity4, alphaEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
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
                "dtoIntFromAlpha" to mapOf(
                    "filterType" to "number",
                    "type" to "greaterThanOrEqual",
                    "filter" to 1.5
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity3, alphaEntity2),
                    Pair(bravoEntity4, alphaEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_number_lessThan() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "dtoIntFromAlpha" to mapOf(
                    "filterType" to "number",
                    "type" to "lessThan",
                    "filter" to 2
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity1, alphaEntity1),
                    Pair(bravoEntity2, alphaEntity1)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_number_lessThanOrEqual() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "dtoIntFromAlpha" to mapOf(
                    "filterType" to "number",
                    "type" to "lessThanOrEqual",
                    "filter" to 2.5
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    Pair(bravoEntity1, alphaEntity1),
                    Pair(bravoEntity2, alphaEntity1),
                    Pair(bravoEntity3, alphaEntity2)
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
                "dtoIntFromAlpha" to mapOf(
                    "filterType" to "number",
                    "type" to "notEqual",
                    "filter" to 1
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    Pair(bravoEntity3, alphaEntity2),
                    Pair(bravoEntity4, alphaEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
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
                "dtoIntFromAlpha" to mapOf(
                    "filterType" to "number",
                    "type" to "inRange",
                    "filter" to 1,
                    "filterTo" to 3
                )
            ),
            sortModel = listOf(
                mapOf(
                    "colId" to "dtoStringFromBravo",
                    "sort" to "asc"
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    Pair(bravoEntity1, alphaEntity1),
                    Pair(bravoEntity2, alphaEntity1),
                    Pair(bravoEntity3, alphaEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun testFilter_multi_conditions_on_multi_columns() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())
        val tomorrow = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now().plusDays(1))

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "dtoIntFromBravo" to mapOf(
                    "operator" to "OR",
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
                    "conditions" to listOf(
                        mapOf(
                            "filterType" to "number",
                            "type" to "equals",
                            "filter" to 1.5 // => PersonEntity1
                        ),
                        mapOf(
                            "filterType" to "number",
                            "type" to "equals",
                            "filter" to 2.5 // => PersonEntity2
                        ),
                    )
                ),
                "createdTimestampUtc" to mapOf(
                    "operator" to "OR",
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
                    "conditions" to listOf(
                        mapOf(
                            "filterType" to "date",
                            "type" to "equals",
                            "dateFrom" to today, // => PersonEntity2, UserEntity1
                            "dateTo" to null
                        ),
                        mapOf(
                            "filterType" to "date",
                            "type" to "equals",
                            "dateFrom" to tomorrow, // => UserEntity2
                            "dateTo" to null
                        ),
                    )
                )
            )
        )

    }


    @Test
    fun testFilter_on_multi_columns() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "dtoIntFromAlpha" to mapOf(
                    "filterType" to "number",
                    "type" to "equals",
                    "filter" to 2 // => AlphaAgGridEntity2
                ),
                "createdTimestampUtc" to mapOf(
                    "filterType" to "date",
                    "type" to "equals",
                    "dateFrom" to today, // => BravoAgGridEntity2, BravoAgGridEntity3
                    "dateTo" to null
                )
            )
        ).bodyJson().isEqualTo(
            expectedResult(
                totalCount = 1,
                rows = listOf(Pair(bravoEntity3, alphaEntity2)),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun `test filter text contains on someListOfStrings`() {

        val entity1 = AllFieldTypesEntityTestBuilder(
            someListOfStrings = listOf("apple", "banana"),
        ).build()

        val entity2 = AllFieldTypesEntityTestBuilder(
            someListOfStrings = listOf("cherry", "date"),
        ).build()

        val entity3 = AllFieldTypesEntityTestBuilder(
            someListOfStrings = listOf("apple", "cherry"),
        ).build()

        this.allFieldTypesDao.insert(entity1)
        this.allFieldTypesDao.insert(entity2)
        this.allFieldTypesDao.insert(entity3)

        submitAllFieldTypesSearch(
            startRow = 0,
            endRow = 10,
            filterModel = mapOf(
                "someListOfStrings" to mapOf(
                    "filterType" to "text",
                    "type" to "contains",
                    "filter" to "apple"
                )
            )
        ).bodyJson().isLenientlyEqualTo(expectedAllFieldTypesResult(
            totalCount = 2,
            rows = listOf(entity1, entity3),
            firstResultIndex = 1,
            lastResultIndex = 2,
            0,
            10))

        submitAllFieldTypesSearch(
            startRow = 0,
            endRow = 10,
            filterModel = mapOf(
                "someListOfStrings" to mapOf(
                    "filterType" to "text",
                    "type" to "contains",
                    "filter" to "banana"
                )
            )
        ).bodyJson().isLenientlyEqualTo(expectedAllFieldTypesResult(
            totalCount = 1,
            rows = listOf(entity1),
            firstResultIndex = 1,
            1,
            0,
            10))

        submitAllFieldTypesSearch(
            startRow = 0,
            endRow = 10,
            filterModel = mapOf(
                "someListOfStrings" to mapOf(
                    "filterType" to "text",
                    "type" to "contains",
                    "filter" to "notExists"
                )
            )
        ).bodyJson().isLenientlyEqualTo(expectedAllFieldTypesResult(
            totalCount = 0,
            rows = listOf(),
            firstResultIndex = 1,
            lastResultIndex = 0,
            0,
            10
        ))

    }


    @Test
    fun `test filter text notContains on someListOfStrings`() {

        val entity1 = AllFieldTypesEntityTestBuilder(
            someListOfStrings = listOf("apple", "banana"),
            someString = "notContainsEntity1",
            someIntType = SomeIntType(201),
            someLongType = SomeLongType(20001L),
            someStringType = SomeStringType("notContainsStrType1"),
        ).build()

        val entity2 = AllFieldTypesEntityTestBuilder(
            someListOfStrings = listOf("cherry", "date"),
            someString = "notContainsEntity2",
            someIntType = SomeIntType(202),
            someLongType = SomeLongType(20002L),
            someStringType = SomeStringType("notContainsStrType2"),
        ).build()

        this.allFieldTypesDao.insert(entity1)
        this.allFieldTypesDao.insert(entity2)

        submitAllFieldTypesSearch(
            startRow = 0,
            endRow = 10,
            filterModel = mapOf(
                "someListOfStrings" to mapOf(
                    "filterType" to "text",
                    "type" to "notContains",
                    "filter" to "apple"
                )
            )
        ).bodyJson().isEqualTo(expectedAllFieldTypesResult(
            1,
            listOf(entity2),
            3,
            4,
            5,
            6
        ))

    }


    private fun submitAllFieldTypesSearch(
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

        return assertThat_POST("/api/all_field_types_table/search", requestBody)

    }


    private fun submitSearch(
        path: String = "/api/bravo_ag_grid/search",
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

        return assertThat_POST(path, requestBody)

    }


    private fun expectedResult(
        totalCount: Int,
        rows: List<Pair<BravoAgGridEntity, AlphaAgGridEntity>>,
        firstResultIndex: Int,
        lastResultIndex: Int,
        offset: Int,
        limit: Int
    ): String {

        val expectedResultData = rows.map { jsonFor(it) }

        return asJson(
            mapOf(
                "totalResultCount" to totalCount,
                "results" to expectedResultData,
                "firstResultIndex" to firstResultIndex,
                "lastResultIndex" to lastResultIndex,
                "limit" to limit,
                "offset" to offset
            )
        )

    }


    private fun expectedAllFieldTypesResult(
        totalCount: Int,
        rows: List<AllFieldTypesEntity>,
        firstResultIndex: Int,
        lastResultIndex: Int,
        offset: Int,
        limit: Int
    ): String {

        val expectedResultData = rows.map { jsonFor(it) }

        return asJson(
            mapOf(
                "totalResultCount" to totalCount,
                "results" to expectedResultData,
                "firstResultIndex" to firstResultIndex,
                "lastResultIndex" to lastResultIndex,
                "limit" to limit,
                "offset" to offset
            )
        )

    }


    private fun jsonFor(pair: Pair<BravoAgGridEntity, AlphaAgGridEntity>): Map<String, Any?> {

        val bravoEntity = pair.first
        val alphaEntity = pair.second

        return mapOf(
            "id" to bravoEntity.id.value,
            "dtoStringFromAlpha" to alphaEntity.someString,
            "dtoStringFromBravo" to bravoEntity.someString,
            "dtoIntFromAlpha" to alphaEntity.someInt,
            "dtoIntFromBravo" to bravoEntity.someInt,
            "createdTimestampUtc" to bravoEntity.createdTimestampUtc.truncatedTo(ChronoUnit.MILLIS).toString()
        )

    }


    private fun jsonFor(
        entity: AllFieldTypesEntity
    ): Map<String, Any?> {

        return mapOf(
            "id" to entity.id.value,
            "someString" to entity.someString,
            "someInt" to entity.someInt
        )

    }


}
