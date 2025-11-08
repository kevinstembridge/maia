package org.maiaframework.gen.testing.jdbc.searchable

import org.maiaframework.gen.testing.jdbc.AbstractJdbcTest
import org.maiaframework.gen.testing.jdbc.join.AlphaEntityTestBuilder
import org.maiaframework.gen.testing.jdbc.join.BravoEntityTestBuilder
import org.maiaframework.gen.testing.jdbc.sample.join.AlphaDao
import org.maiaframework.gen.testing.jdbc.sample.join.AlphaEntity
import org.maiaframework.gen.testing.jdbc.sample.join.BravoDao
import org.maiaframework.gen.testing.jdbc.sample.join.BravoEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SearchableDtoTest : AbstractJdbcTest() {


    @Autowired
    private lateinit var alphaDao: AlphaDao

    @Autowired
    private lateinit var bravoDao: BravoDao

    private val timestamp1 = Instant.now().truncatedTo(ChronoUnit.MILLIS).minusSeconds(24 * 60 * 60)
    private val timestamp2 = Instant.now().truncatedTo(ChronoUnit.MILLIS)
    private val timestamp3 = Instant.now().truncatedTo(ChronoUnit.MILLIS).plusSeconds(24 * 60 * 60)

    private val someInt1 = 1
    private val someInt2 = 2

    private val alphaEntity1 = AlphaEntityTestBuilder(someInt = someInt1, someString = "someAlphaValue1").build()
    private val alphaEntity2 = AlphaEntityTestBuilder(someInt = someInt2, someString = "someAlphaValue2").build()

    private val bravoEntity1 = BravoEntityTestBuilder(createdTimestampUtc = timestamp1, alphaId = alphaEntity1.id, someString = "aSomeValue1").build()
    private val bravoEntity2 = BravoEntityTestBuilder(createdTimestampUtc = timestamp2, alphaId = alphaEntity1.id, someString = "aSomeValue2").build()
    private val bravoEntity3 = BravoEntityTestBuilder(createdTimestampUtc = timestamp2, alphaId = alphaEntity2.id, someString = "bSomeValue3").build()
    private val bravoEntity4 = BravoEntityTestBuilder(createdTimestampUtc = timestamp3, alphaId = alphaEntity2.id, someString = "bSomeValue4").build()

    @BeforeEach
    fun beforeEach() {

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

        this.mockMvc.perform(post("/api/bravo/search").content(requestBody))
            .andExpect(status().isForbidden)

    }


    @Test
    fun `test filter text equals`() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "fieldType" to "text",
                    "filterType" to "equals",
                    "filter" to "aSomeValue1"
                )
            )
        ).andExpect {
            content {
                json(
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
        }


    }


    @Test
    fun `test filter text equals on parent field`() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromAlpha",
                    "fieldType" to "text",
                    "filterType" to "equals",
                    "filter" to "someAlphaValue1"
                )
            )
        ).andExpect {
            content {
                json(
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
        }


    }


    @Test
    fun `test filter text equals on 2 parent fields`() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromAlpha",
                    "fieldType" to "text",
                    "filterType" to "equals",
                    "filter" to "someAlphaValue1"
                ),
                mapOf(
                    "fieldPath" to "dtoIntFromAlpha",
                    "fieldType" to "number",
                    "filterType" to "equals",
                    "filter" to someInt1
                ),
            )
        ).andExpect {
            content {
                json(
                    expectedResult(
                        totalCount = 2,
                        rows = listOf(
                            Pair(bravoEntity1,alphaEntity1),
                            Pair(bravoEntity2, alphaEntity1)
                        ),
                        firstResultIndex = 1,
                        lastResultIndex = 2,
                        offset = 0,
                        limit = 3
                    )
                )
            }
        }

    }


    @Test
    fun `test filter text equals with sort on parent field`() {

        submitSearch(
            startRow = 0,
            endRow = 1,
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoIntFromAlpha",
                    "sortDirection" to "desc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

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
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "fieldType" to "text",
                    "filterType" to "notEqual",
                    "filter" to "aSomeValue1"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    fun testFilter_text_notEqual_2() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromAlpha",
                    "fieldType" to "text",
                    "filterType" to "notEqual",
                    "filter" to "someAlphaValue1"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    fun testFilter_text_contains() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromAlpha",
                    "fieldType" to "text",
                    "filterType" to "contains",
                    "filter" to "AlphaValue1"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    fun testFilter_text_notContains() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromAlpha",
                    "fieldType" to "text",
                    "filterType" to "notContains",
                    "filter" to "Value1"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    fun testFilter_text_startsWith() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "fieldType" to "text",
                    "filterType" to "startsWith",
                    "filter" to "aSomeValue"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    fun testFilter_text_endsWith() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromAlpha",
                    "fieldType" to "text",
                    "filterType" to "endsWith",
                    "filter" to "Value1"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    fun testFilter_date_equals() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "createdTimestampUtc",
                    "fieldType" to "date",
                    "filterType" to "equals",
                    "dateFrom" to today,
                    "dateTo" to null
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    @Disabled
    fun testFilter_date_equals_with_multiple_conditions() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())
        val tomorrow = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now().plusDays(1))

        val actualResult = submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "createdTimestampUtc",
                    "condition1" to mapOf(
                        "fieldType" to "date",
                        "filterType" to "equals",
                        "dateFrom" to today,
                        "dateTo" to null
                    ),
                    "condition2" to mapOf(
                        "fieldType" to "date",
                        "filterType" to "equals",
                        "dateFrom" to tomorrow,
                        "dateTo" to null
                    ),
                    "operator" to "OR"
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        )

        val expectedResult = expectedResult(
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

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_date_greaterThan() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "createdTimestampUtc",
                    "fieldType" to "date",
                    "filterType" to "greaterThan",
                    "dateFrom" to today,
                    "dateTo" to null
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }


    }


    @Test
    fun testFilter_date_lessThan() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "createdTimestampUtc",
                    "fieldType" to "date",
                    "filterType" to "lessThan",
                    "dateFrom" to today,
                    "dateTo" to null
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

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
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "createdTimestampUtc",
                    "fieldType" to "date",
                    "filterType" to "notEqual",
                    "dateFrom" to today,
                    "dateTo" to null
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(expectedResult)
            }
        }

    }


    @Test
    fun testFilter_date_inRange() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())
        val tomorrow = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now().plusDays(1))

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "createdTimestampUtc",
                    "fieldType" to "date",
                    "filterType" to "inRange",
                    "dateFrom" to today,
                    "dateTo" to tomorrow
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    fun testFilter_number_equals() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoIntFromAlpha",
                    "fieldType" to "number",
                    "filterType" to "equals",
                    "filter" to 2
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

//        val expectedResult = expectedResult(
//                totalCount = 1,
//                rows = listOf(
//                        personEntity1))
//
//        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    @Disabled("Until we support Ag Grid queries")
    fun testFilter_number_equals_with_multiple_and_conditions() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoIntFromAlpha",
                    "condition1" to mapOf(
                        "fieldType" to "number",
                        "filterType" to "equals",
                        "filter" to 1.5
                    ),
                    "condition2" to mapOf(
                        "fieldType" to "number",
                        "filterType" to "equals",
                        "filter" to 2.5
                    ),
                    "operator" to "AND"
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    @Disabled("Until we support Ag Grid queries")
    fun testFilter_number_equals_with_multiple_or_conditions() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "amount",
                    "condition1" to mapOf(
                        "fieldType" to "number",
                        "filterType" to "equals",
                        "filter" to 1.5
                    ),
                    "condition2" to mapOf(
                        "fieldType" to "number",
                        "filterType" to "equals",
                        "filter" to 2.5
                    ),
                    "operator" to "OR"
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
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
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoIntFromAlpha",
                    "fieldType" to "number",
                    "filterType" to "greaterThan",
                    "filter" to 1
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

//        val expectedResult = expectedResult(
//                totalCount = 1,
//                rows = listOf(
//                        userEntity2))
//
//        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_number_greaterThanOrEqual() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoIntFromAlpha",
                    "fieldType" to "number",
                    "filterType" to "greaterThanOrEqual",
                    "filter" to 1.5
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    fun testFilter_number_lessThan() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoIntFromAlpha",
                    "fieldType" to "number",
                    "filterType" to "lessThan",
                    "filter" to 2
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    fun testFilter_number_lessThanOrEqual() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoIntFromAlpha",
                    "fieldType" to "number",
                    "filterType" to "lessThanOrEqual",
                    "filter" to 2.5
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    fun testFilter_number_notEqual() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoIntFromAlpha",
                    "fieldType" to "number",
                    "filterType" to "notEqual",
                    "filter" to 1
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    fun testFilter_number_inRange() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoIntFromAlpha",
                    "fieldType" to "number",
                    "filterType" to "inRange",
                    "filter" to 1,
                    "filterTo" to 3
                )
            ),
            sortModel = listOf(
                mapOf(
                    "fieldPath" to "dtoStringFromBravo",
                    "sortDirection" to "asc"
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    @Test
    @Disabled("Until we support Ag Grid queries")
    fun testFilter_multi_conditions_on_multi_columns() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())
        val tomorrow = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now().plusDays(1))

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "amount",
                    "condition1" to mapOf(
                        "fieldType" to "number",
                        "filterType" to "equals",
                        "filter" to 1.5 // => PersonEntity1
                    ),
                    "condition2" to mapOf(
                        "fieldType" to "number",
                        "filterType" to "equals",
                        "filter" to 2.5 // => PersonEntity2
                    ),
                    "operator" to "OR"
                ),
                mapOf(
                    "fieldPath" to "createdTimestampUtc",
                    "condition1" to mapOf(
                        "fieldType" to "date",
                        "filterType" to "equals",
                        "dateFrom" to today, // => PersonEntity2, UserEntity1
                        "dateTo" to null
                    ),
                    "condition2" to mapOf(
                        "fieldType" to "date",
                        "filterType" to "equals",
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

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = listOf(
                mapOf(
                    "fieldPath" to "dtoIntFromAlpha",
                    "fieldType" to "number",
                    "filterType" to "equals",
                    "filter" to 2 // => AlphaEntity2
                ),
                mapOf(
                    "fieldPath" to "createdTimestampUtc",
                    "fieldType" to "date",
                    "filterType" to "equals",
                    "dateFrom" to today, // => BravoEntity2, BravoEntity3
                    "dateTo" to null
                )
            )
        ).andExpect {
            content {
                json(
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
        }

    }


    private fun submitSearch(
        path: String = "/api/bravo/search",
        startRow: Int,
        endRow: Int,
        sortModel: List<Map<String, String>> = emptyList(),
        filterModel: List<Map<String, Any?>> = emptyList()
    ): ResultActionsDsl {

        val requestBody = asJson(
            mapOf(
                "startRow" to startRow,
                "endRow" to endRow,
                "sortModel" to sortModel,
                "filterModel" to filterModel
            )
        )

        return mockMvc.post(path) {
            content = requestBody
            contentType = MediaType.APPLICATION_JSON
            with(user("nigel").roles("ADMIN"))
        }.andDo {
            print()
        }

    }


    private fun expectedResult(
        totalCount: Int,
        rows: List<Pair<BravoEntity, AlphaEntity>>,
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


    private fun jsonFor(pair: Pair<BravoEntity, AlphaEntity>): Map<String, Any?> {

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


}
