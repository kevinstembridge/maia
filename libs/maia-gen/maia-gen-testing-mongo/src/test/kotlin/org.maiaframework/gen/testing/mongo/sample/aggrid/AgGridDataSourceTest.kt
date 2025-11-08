package mahana.gen.testing.mongo.sample.aggrid

import org.maiaframework.json.JsonFacade
import org.maiaframework.dao.mongo.MongoClientFacade
import org.maiaframework.domain.DomainId
import org.maiaframework.domain.types.CollectionName
import org.maiaframework.gen.AbstractIntegrationTest
import org.maiaframework.gen.testing.mongo.sample.SomeCaseSensitivityEntityTestBuilder
import org.maiaframework.gen.testing.mongo.sample.case_sensitivity.SomeCaseSensitivityDao
import org.maiaframework.gen.testing.mongo.sample.case_sensitivity.SomeCaseSensitivityEntity
import org.maiaframework.gen.testing.mongo.sample.contact.EmailAddress
import org.maiaframework.gen.testing.mongo.sample.org.OrganizationDao
import org.maiaframework.gen.testing.mongo.sample.org.OrganizationEntityTestBuilder
import org.maiaframework.gen.testing.mongo.sample.person.PersonDao
import org.maiaframework.gen.testing.mongo.sample.person.PersonEntity
import org.maiaframework.gen.testing.mongo.sample.person.PersonEntityTestBuilder
import org.maiaframework.gen.testing.mongo.sample.simple.VerySimpleDao
import org.maiaframework.gen.testing.mongo.sample.simple.VerySimpleEntity
import org.maiaframework.gen.testing.mongo.sample.user.UserDao
import org.maiaframework.gen.testing.mongo.sample.user.UserEntityTestBuilder
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
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
import java.util.Locale

@Test(enabled = false)
class AgGridDataSourceTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var mongoClientFacade: MongoClientFacade

    @Autowired
    private lateinit var personDao: PersonDao

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var organizationDao: OrganizationDao

    @Autowired
    private lateinit var someCaseSensitivityDao: SomeCaseSensitivityDao

    @Autowired
    private lateinit var verySimpleDao: VerySimpleDao

    @Autowired
    private lateinit var jsonFacade: JsonFacade

    private val timestamp1 = Instant.now().minusSeconds(24 * 60 * 60)
    private val timestamp2 = Instant.now()
    private val timestamp3 = Instant.now().plusSeconds(24 * 60 * 60)

    private val organizationEntity1 = OrganizationEntityTestBuilder(emailAddress = EmailAddress("info@thecompany1.com")).build()

    private val organizationEntity2 = OrganizationEntityTestBuilder(emailAddress = EmailAddress("info@thecompany2.com")).build()

    private val personEntity1 = PersonEntityTestBuilder(
            emailAddress = EmailAddress("person@thecompany1.com"),
            createdTimestampUtc = timestamp1,
            amount = 1.5)
            .build()

    private val personEntity2 = PersonEntityTestBuilder(
            emailAddress = EmailAddress("person@thecompany2.com"),
            createdTimestampUtc = timestamp2,
            amount = 2.5)
            .build()


    private val userEntity1 = UserEntityTestBuilder(
            emailAddress = EmailAddress("user1@thecompany1.com"),
            createdTimestampUtc = timestamp2,
            amount = 3.5)
            .build()

    private val userEntity2 = UserEntityTestBuilder(
            emailAddress = EmailAddress("user2@thecompany2.com"),
            createdTimestampUtc = timestamp3,
            amount = 4.5)
            .build()

    private val someCaseSensitivityEntity1 = SomeCaseSensitivityEntityTestBuilder().build()
    private val someCaseSensitivityEntity2 = SomeCaseSensitivityEntityTestBuilder().build()

    private val someVerySimpleEntity = VerySimpleEntity(Instant.now(), DomainId.newId(), "someValue")

    @BeforeClass
    fun beforeClass() {

        this.mongoClientFacade.deleteMany(CollectionName("party"), Document())
        this.mongoClientFacade.deleteMany(CollectionName("someCaseSensitivity"), Document())
        this.mongoClientFacade.deleteMany(this.verySimpleDao.collectionName, Document())

        // GIVEN some Users, Persons and Organizations in the database

        this.organizationDao.insert(organizationEntity1)
        this.organizationDao.insert(organizationEntity2)
        this.personDao.insert(personEntity1)
        this.personDao.insert(personEntity2)
        this.userDao.insert(userEntity1)
        this.userDao.insert(userEntity2)
        this.someCaseSensitivityDao.insert(someCaseSensitivityEntity1)
        this.verySimpleDao.insert(someVerySimpleEntity)

    }


    @Test(enabled = false)
    fun should_return_403_for_unauthorised_user() {

        val requestBody = json(mapOf())

        this.mockMvc.perform(post("/api/person_summary/aggrid_datasource").content(requestBody))
                .andExpect(status().isForbidden)

    }


    @Test(enabled = false)
    fun testFilter_text_equals() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "emailAddress" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "equals",
                                "filter" to "user1@thecompany1.com"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 1,
                rows = listOf(userEntity1),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_text_equals_case_sensitive_expect_no_match() {

        val actualResult = submitSearch(
                path = "/api/some_case_sensitivity/aggrid_datasource",
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "caseSensitiveString" to mapOf(
                            "fieldType" to "text",
                            "filterType" to "equals",
                            "filter" to someCaseSensitivityEntity1.caseSensitiveString.lowercase(Locale.getDefault())
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 0,
                rows = emptyList(),
                firstResultIndex = 1,
                lastResultIndex = 0,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_with_empty_filter_model() {

        val actualResult = submitSearch(
                path = "/api/very_simple/aggrid_datasource",
                startRow = 0,
                endRow = 3,
                filterModel = emptyMap()
        )

        val expectedResult = expectedResult_verySimple(
                totalCount = 1,
                rows = listOf(someVerySimpleEntity),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_text_equals_case_sensitive_expect_match() {

        val actualResult = submitSearch(
                path = "/api/some_case_sensitivity/aggrid_datasource",
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "caseSensitiveString" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "equals",
                                "filter" to someCaseSensitivityEntity1.caseSensitiveString
                        )
                )
        )

        val expectedResult = expectedResult_caseSensitivity(
                totalCount = 1,
                rows = listOf(someCaseSensitivityEntity1),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_text_equals_case_insensitive_expect_match() {

        val actualResult = submitSearch(
                path = "/api/some_case_sensitivity/aggrid_datasource",
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "caseInsensitiveString" to mapOf(
                            "fieldType" to "text",
                            "filterType" to "equals",
                            "filter" to someCaseSensitivityEntity1.caseInsensitiveString.lowercase(Locale.getDefault())
                        )
                )
        )

        val expectedResult = expectedResult_caseSensitivity(
                totalCount = 1,
                rows = listOf(someCaseSensitivityEntity1),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }

    // TODO test case-insensitive match for all the text filter types (notContains, etc)

    @Test(enabled = false)
    fun testFilter_text_notEqual() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "emailAddress" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "notEqual",
                                "filter" to "user1@thecompany1.com"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 3,
                rows = listOf(
                        personEntity1,
                        personEntity2,
                        userEntity2),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_text_contains() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "emailAddress" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "contains",
                                "filter" to "@thecompany1.com"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        personEntity1,
                        userEntity1),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_text_contains_case_insensitive_expect_match() {

        val actualResult = submitSearch(
                path = "/api/some_case_sensitivity/aggrid_datasource",
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "caseInsensitiveString" to mapOf(
                            "fieldType" to "text",
                            "filterType" to "contains",
                            "filter" to someCaseSensitivityEntity1.caseInsensitiveString.lowercase(Locale.getDefault())
                        )
                )
        )

        val expectedResult = expectedResult_caseSensitivity(
                totalCount = 1,
                rows = listOf(someCaseSensitivityEntity1),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_text_startsWith_case_insensitive_expect_match() {

        val actualResult = submitSearch(
                path = "/api/some_case_sensitivity/aggrid_datasource",
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "caseInsensitiveString" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "startsWith",
                            "filter" to someCaseSensitivityEntity1.caseInsensitiveString.lowercase(Locale.getDefault())
                                .substring(0, 3)
                        )
                )
        )

        val expectedResult = expectedResult_caseSensitivity(
                totalCount = 1,
                rows = listOf(someCaseSensitivityEntity1),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_text_endsWith_case_insensitive_expect_match() {

        val caseInsensitiveString = someCaseSensitivityEntity1.caseInsensitiveString
        val length = caseInsensitiveString.length
        val term = caseInsensitiveString.lowercase(Locale.getDefault()).substring(length - 3, length)

        val actualResult = submitSearch(
            path = "/api/some_case_sensitivity/aggrid_datasource",
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "caseInsensitiveString" to mapOf(
                    "fieldType" to "text",
                    "filterType" to "endsWith",
                    "filter" to term
                )
            )
        )

        val expectedResult = expectedResult_caseSensitivity(
            totalCount = 1,
            rows = listOf(someCaseSensitivityEntity1),
            firstResultIndex = 1,
            lastResultIndex = 1,
            offset = 0,
            limit = 3
        )

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_text_notContains() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "emailAddress" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "notContains",
                                "filter" to "@thecompany1.com"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        personEntity2,
                        userEntity2),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_text_startsWith() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "emailAddress" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "startsWith",
                                "filter" to "user"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        userEntity1,
                        userEntity2),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_text_endsWith() {

        val actualResult = submitSearch(
                startRow = 0,
                endRow = 3,
                filterModel = mapOf(
                        "emailAddress" to mapOf(
                                "fieldType" to "text",
                                "filterType" to "endsWith",
                                "filter" to "@thecompany1.com"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        personEntity1,
                        userEntity1),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        personEntity2,
                        userEntity1),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 3,
                rows = listOf(
                        personEntity2,
                        userEntity1,
                        userEntity2),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 3,
                rows = listOf(
                        personEntity2,
                        userEntity1,
                        userEntity2),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 1,
                rows = listOf(
                        personEntity1),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        personEntity1,
                        userEntity2),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 3,
                rows = listOf(
                        personEntity2,
                        userEntity1,
                        userEntity2),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 1,
                rows = listOf(
                        personEntity1),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
    fun testFilter_number_equals_with_multiple_and_conditions() {

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
                                "operator" to "AND"
                        )
                ),
                sortModel = listOf(
                        mapOf(
                                "colId" to "emailAddress",
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
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        personEntity1,
                        personEntity2),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 1,
                rows = listOf(
                        userEntity2),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        userEntity1,
                        userEntity2),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        personEntity1,
                        personEntity2),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        personEntity1,
                        personEntity2),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 3,
                rows = listOf(
                        personEntity2,
                        userEntity1,
                        userEntity2),
                firstResultIndex = 1,
                lastResultIndex = 3,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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
                                "colId" to "emailAddress",
                                "sort" to "asc"
                        )
                )
        )

        val expectedResult = expectedResult(
                totalCount = 2,
                rows = listOf(
                        personEntity1,
                        personEntity2),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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

        val expectedResult = expectedResult(
                totalCount = 1,
                rows = listOf(personEntity2),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    @Test(enabled = false)
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

        val expectedResult = expectedResult(
                totalCount = 1,
                rows = listOf(personEntity2),
                firstResultIndex = 1,
                lastResultIndex = 1,
                offset = 0,
                limit = 3)

        assertThat(actualResult).isEqualTo(expectedResult)

    }


    private fun submitSearch(
            path: String = "/api/person_summary/aggrid_datasource",
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
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(user("nigel").roles("ADMIN"))
                .content(requestBody))
                .andReturn()
                .response
                .contentAsString

        return jsonFacade.readObjectAsMap(responseJson)

    }


    private fun expectedResult(
            totalCount: Int,
            rows: List<PersonEntity>
    ): Map<String, Any?> {

        val expectedResultData = rows.map { jsonFor(it) }

        return mapOf(
                "totalCount" to totalCount,
                "rows" to expectedResultData
        )

    }


    private fun expectedResult(
            totalCount: Int,
            rows: List<PersonEntity>,
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


    private fun expectedResult_verySimple(
            totalCount: Int,
            rows: List<VerySimpleEntity>
    ): Map<String, Any?> {

        val expectedResultData = rows.map { jsonFor(it) }

        return mapOf(
                "totalCount" to totalCount,
                "rows" to expectedResultData
        )

    }


    private fun expectedResult_verySimple(
            totalCount: Int,
            rows: List<VerySimpleEntity>,
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


    private fun expectedResult_caseSensitivity(
            totalCount: Int,
            rows: List<SomeCaseSensitivityEntity>
    ): Map<String, Any?> {

        val expectedResultData = rows.map { jsonFor(it) }

        return mapOf(
                "totalCount" to totalCount,
                "rows" to expectedResultData
        )

    }


    private fun expectedResult_caseSensitivity(
            totalCount: Int,
            rows: List<SomeCaseSensitivityEntity>,
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


    private fun jsonFor(personEntity: PersonEntity): Map<String, Any?> {

        return mapOf(

                "emailAddress" to personEntity.emailAddress.value,
                "firstName" to personEntity.firstName?.value,
                "lastName" to personEntity.lastName.value,
                "id" to personEntity.id.value,
                "createdTimestampUtc" to personEntity.createdTimestampUtc.toString(),
                "amount" to personEntity.amount
        )

    }


    private fun jsonFor(entity: VerySimpleEntity): Map<String, Any?> {

        return mapOf(
                "someString" to entity.someString)

    }


    private fun jsonFor(personEntity: SomeCaseSensitivityEntity): Map<String, Any?> {

        return mapOf(

                "caseInsensitiveString" to personEntity.caseInsensitiveString,
                "caseSensitiveString" to personEntity.caseSensitiveString,
                "id" to personEntity.id.value,
                "createdTimestampUtc" to personEntity.createdTimestampUtc.toString()
        )

    }


}
