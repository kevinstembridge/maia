package org.maiaframework.gen.testing.mongo.sample

import org.maiaframework.json.JsonFacade
import org.maiaframework.gen.AbstractIntegrationTest
import org.maiaframework.dao.mongo.MongoClientFacade
import org.maiaframework.domain.DomainId
import org.maiaframework.gen.testing.mongo.sample.ForeignKeyChildDao
import org.maiaframework.gen.testing.mongo.sample.ForeignKeyChildEntity
import org.maiaframework.gen.testing.mongo.sample.ForeignKeyParentDao
import org.maiaframework.gen.testing.mongo.sample.ForeignKeyParentEntity
import org.maiaframework.testing.domain.Anys
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SearchableDtoTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var mongoClientFacade: MongoClientFacade

    @Autowired
    private lateinit var foreignKeyParentDao: ForeignKeyParentDao

    @Autowired
    private lateinit var foreignKeyChildDao: ForeignKeyChildDao

    @Autowired
    private lateinit var jsonFacade: JsonFacade

    private val timestamp1 = Instant.now().minusSeconds(24 * 60 * 60)
    private val timestamp2 = Instant.now()
    private val timestamp3 = Instant.now().plusSeconds(24 * 60 * 60)

    private val someInt1 = 1
    private val someInt2 = 2

    private val parentEntity1 = ForeignKeyParentEntity(Instant.now(), DomainId.newId(), someInt1, "someForeignValue1")
    private val parentEntity2 = ForeignKeyParentEntity(Instant.now(), DomainId.newId(), someInt2, "someForeignValue2")

    private val childEntity1 = ForeignKeyChildEntity(timestamp1, DomainId.newId(), parentEntity1.id, "aSomeValue1")
    private val childEntity2 = ForeignKeyChildEntity(timestamp2, DomainId.newId(), parentEntity1.id, "aSomeValue2")
    private val childEntity3 = ForeignKeyChildEntity(timestamp2, DomainId.newId(), parentEntity2.id, "bSomeValue3")
    private val childEntity4 = ForeignKeyChildEntity(timestamp3, DomainId.newId(), parentEntity2.id, "bSomeValue4")

    @BeforeClass
    fun beforeClass() {

        this.mongoClientFacade.deleteMany(this.foreignKeyParentDao.collectionName, Document())
        this.mongoClientFacade.deleteMany(this.foreignKeyChildDao.collectionName, Document())

        this.foreignKeyParentDao.insert(parentEntity1)
        this.foreignKeyParentDao.insert(parentEntity2)
        this.foreignKeyChildDao.insert(childEntity1)
        this.foreignKeyChildDao.insert(childEntity2)
        this.foreignKeyChildDao.insert(childEntity3)
        this.foreignKeyChildDao.insert(childEntity4)

    }


    @Test
    fun should_return_403_for_unauthorised_user() {

        val requestBody = json(mapOf())

        this.mockMvc.perform(post("/api/person_summary/aggrid_datasource").content(requestBody))
                .andExpect(status().isForbidden)

    }


    @Test
    fun `test filter text equals`() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "someString" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "equals",
                                "filter" to "aSomeValue1"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 1,
                rows = listOf(Pair(childEntity1, parentEntity1)),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun `test filter text equals on parent field`() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "someForeignString" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "equals",
                                "filter" to "someForeignValue1"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(Pair(childEntity1, parentEntity1), Pair(childEntity2, parentEntity1)),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun `test filter text equals on 2 parent fields`() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "someForeignString" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "equals",
                                "filter" to "someForeignValue1"
                        ),
                        "someForeignInt" to mapOf(
                                "fieldType" to "number",
                                "filterType" to "equals",
                                "filter" to someInt1
                        ),
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(Pair(childEntity1, parentEntity1), Pair(childEntity2, parentEntity1)),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun `test filter text equals with sort on parent field`() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 1,
                sortModel = listOf(
                        mapOf(
                                "colId" to "someForeignInt",
                                "sort" to "desc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 4,
                rows = listOf(Pair(childEntity3, parentEntity2)),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 1)

        assertThat(actualResult).isEqualTo(expectedResult)

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

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "someString" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "notEqual",
                                "filter" to "aSomeValue1"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 3,
                rows = listOf(
                        Pair(childEntity2, parentEntity1),
                        Pair(childEntity3, parentEntity2),
                        Pair(childEntity4, parentEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }

    
    @Test
    fun testFilter_text_notEqual_2() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "someForeignString" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "notEqual",
                                "filter" to "someForeignValue1"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        Pair(childEntity3, parentEntity2),
                        Pair(childEntity4, parentEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_text_contains() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "someForeignString" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "contains",
                                "filter" to "ForeignValue1"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        Pair(childEntity1, parentEntity1),
                        Pair(childEntity2, parentEntity1)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_text_notContains() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "someForeignString" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "notContains",
                                "filter" to "Value1"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        Pair(childEntity3, parentEntity2),
                        Pair(childEntity4, parentEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_text_startsWith() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "someString" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "startsWith",
                                "filter" to "aSomeValue"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        Pair(childEntity1, parentEntity1),
                        Pair(childEntity2, parentEntity1),
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_text_endsWith() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "someForeignString" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "endsWith",
                                "filter" to "Value1"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        Pair(childEntity1, parentEntity1),
                        Pair(childEntity2, parentEntity1)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_date_equals() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "createdTimestampUtc" to mapOf(
                                "fieldType" to "date",
                                "filterType" to "equals",
                                "dateFrom" to today,
                                "dateTo" to null
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        Pair(childEntity2, parentEntity1),
                        Pair(childEntity3, parentEntity2),
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_date_equals_with_multiple_conditions() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())
        val tomorrow = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now().plusDays(1))

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "createdTimestampUtc" to mapOf(
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
                                "colId" to "someString",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 3,
                rows = listOf(
                        Pair(childEntity2, parentEntity1),
                        Pair(childEntity3, parentEntity2),
                        Pair(childEntity4, parentEntity2)
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

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "createdTimestampUtc" to mapOf(
                                "fieldType" to "date",
                                "filterType" to "greaterThan",
                                "dateFrom" to today,
                                "dateTo" to null
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 3,
                rows = listOf(
                        Pair(childEntity2, parentEntity1),
                        Pair(childEntity3, parentEntity2),
                        Pair(childEntity4, parentEntity2),
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_date_lessThan() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "createdTimestampUtc" to mapOf(
                                "fieldType" to "date",
                                "filterType" to "lessThan",
                                "dateFrom" to today,
                                "dateTo" to null
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 1,
                rows = listOf(
                        Pair(childEntity1, parentEntity1),
                ),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_date_notEqual() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "createdTimestampUtc" to mapOf(
                                "fieldType" to "date",
                                "filterType" to "notEqual",
                                "dateFrom" to today,
                                "dateTo" to null
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        Pair(childEntity1, parentEntity1),
                        Pair(childEntity4, parentEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_date_inRange() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())
        val tomorrow = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now().plusDays(1))

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "createdTimestampUtc" to mapOf(
                                "fieldType" to "date",
                                "filterType" to "inRange",
                                "dateFrom" to today,
                                "dateTo" to tomorrow
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 3,
                rows = listOf(
                        Pair(childEntity2, parentEntity1),
                        Pair(childEntity3, parentEntity2),
                        Pair(childEntity4, parentEntity2)
                ),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_number_equals() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "amount" to mapOf(
                                "fieldType" to "number",
                                "filterType" to "equals",
                                "filter" to 1.5
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
                                "sort" to "asc"
                        )
                )
        )

//        val expectedResult = expectedResult(
//                totalCount = 1,
//                rows = listOf(
//                        personEntity1))
//
//        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_number_equals_with_multiple_and_conditions() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "someForeignInt" to mapOf(
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
                                "colId" to "someString",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 0,
                rows = emptyList(),
                firstResultIndex = 1,
                lastResultIndex = 0,
                offset = 0,
                limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_number_equals_with_multiple_or_conditions() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "amount" to mapOf(
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
                                "colId" to "someString",
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

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "amount" to mapOf(
                                "fieldType" to "number",
                                "filterType" to "greaterThan",
                                "filter" to 3.5
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
                                "sort" to "asc"
                        )
                )
        )

//        val expectedResult = expectedResult(
//                totalCount = 1,
//                rows = listOf(
//                        userEntity2))
//
//        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_number_greaterThanOrEqual() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "amount" to mapOf(
                                "fieldType" to "number",
                                "filterType" to "greaterThanOrEqual",
                                "filter" to 3.5
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
                                "sort" to "asc"
                        )
                )
        )

//        val expectedResult = expectedResult(
//                totalCount = 2,
//                rows = listOf(
//                        userEntity1,
//                        userEntity2))
//
//        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_number_lessThan() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "amount" to mapOf(
                                "fieldType" to "number",
                                "filterType" to "lessThan",
                                "filter" to 3
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
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
    fun testFilter_number_lessThanOrEqual() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "amount" to mapOf(
                                "fieldType" to "number",
                                "filterType" to "lessThanOrEqual",
                                "filter" to 2.5
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
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
    fun testFilter_number_notEqual() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "amount" to mapOf(
                                "fieldType" to "number",
                                "filterType" to "notEqual",
                                "filter" to 1.5
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
                                "sort" to "asc"
                        )
                )
        )

//        val expectedResult = expectedResult(
//                totalCount = 3,
//                rows = listOf(
//                        personEntity2,
//                        userEntity1,
//                        userEntity2))
//
//        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_number_inRange() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "amount" to mapOf(
                                "fieldType" to "number",
                                "filterType" to "inRange",
                                "filter" to 1,
                                "filterTo" to 3
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "someString",
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
    fun testFilter_multi_conditions_on_multi_columns() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())
        val tomorrow = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now().plusDays(1))

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "amount" to mapOf(
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
                        "createdTimestampUtc" to mapOf(
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

//        val expectedResult = expectedResult(
//                totalCount = 1,
//                rows = listOf(personEntity2))
//
//        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test
    fun testFilter_on_multi_columns() {

        val today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "amount" to mapOf(
                                "fieldType" to "number",
                                "filterType" to "equals",
                                "filter" to 2.5 // => PersonEntity2
                        ),
                        "createdTimestampUtc" to mapOf(
                                "fieldType" to "date",
                                "filterType" to "equals",
                                "dateFrom" to today, // => PersonEntity2, UserEntity1
                                "dateTo" to null
                        )
                )
        )

//        val expectedResult = expectedResult(
//                totalCount = 1,
//                rows = listOf(personEntity2))
//
//        assertThat(actualResult).isEqualTo(expectedResult)

    }


    private fun submitSearch(
            path: String = "/api/foreign_key_child_summary/search",
            startRow: Int,
            endRow: Int,
            sortModel: List<Map<String, String>> = emptyList(),
            filterModel: Map<String, Map<String, Any?>> = emptyMap()
    ): Map<String, Any?> {

        val requestBody = json(mapOf(
                "startRow" to startRow,
                "endRow" to endRow,
                "sortModel" to sortModel,
                "filterModel" to filterModel))

        val responseJson = mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user("nigel").roles("ADMIN"))
                .content(requestBody))
                .andReturn()
                .response
                .contentAsString

        return jsonFacade.readObjectAsMap(responseJson)

    }


    private fun expectedResult(
            totalCount: Int,
            rows: List<Pair<ForeignKeyChildEntity, ForeignKeyParentEntity>>,
            firstResultIndex: Int,
            lastResultIndex: Int,
            offset: Int,
            limit: Int
    ): Map<String, Any?> {

        val expectedResultData = rows.map { jsonFor(it) }

        return mapOf(
                "totalResultCount" to totalCount,
                "results" to expectedResultData,
                "firstResultIndex" to firstResultIndex,
                "lastResultIndex" to lastResultIndex,
                "limit" to limit,
                "offset" to offset
        )

    }


    private fun jsonFor(pair: Pair<ForeignKeyChildEntity, ForeignKeyParentEntity>): Map<String, Any?> {

        val foreignKeyChildEntity = pair.first
        val foreignKeyParentEntity = pair.second
        
        return mapOf(
                "id" to foreignKeyChildEntity.id.value,
                "someString" to foreignKeyChildEntity.someString,
                "parentId" to foreignKeyChildEntity.parentId.value,
                "someForeignString" to foreignKeyParentEntity.someString,
                "someForeignInt" to foreignKeyParentEntity.someInt,
                "createdTimestampUtc" to foreignKeyChildEntity.createdTimestampUtc.truncatedTo(ChronoUnit.MILLIS).toString()
        )

    }


}
