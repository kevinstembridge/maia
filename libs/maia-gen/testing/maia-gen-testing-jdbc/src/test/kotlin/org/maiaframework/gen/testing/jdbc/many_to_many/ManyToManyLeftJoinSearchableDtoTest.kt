package org.maiaframework.gen.testing.jdbc.many_to_many

import org.maiaframework.gen.testing.jdbc.AbstractJdbcTest
import org.maiaframework.gen.testing.jdbc.sample.many_to_many.LeftDao
import org.maiaframework.gen.testing.jdbc.sample.many_to_many.LeftEntity
import org.maiaframework.gen.testing.jdbc.sample.many_to_many.ManyToManyJoinDao
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
import java.time.temporal.ChronoUnit

class ManyToManyLeftJoinSearchableDtoTest : AbstractJdbcTest() {


    @Autowired
    private lateinit var leftDao: LeftDao

    @Autowired
    private lateinit var manyToManyJoinDao: ManyToManyJoinDao

    private val timestamp1 = Instant.now().truncatedTo(ChronoUnit.MILLIS).minusSeconds(24 * 60 * 60)
    private val timestamp2 = Instant.now().truncatedTo(ChronoUnit.MILLIS)
    private val timestamp3 = Instant.now().truncatedTo(ChronoUnit.MILLIS).plusSeconds(24 * 60 * 60)

    private val someInt1 = 1
    private val someInt2 = 2
    private val someInt3 = 3

    private val leftEntity1 = LeftEntityTestBuilder(someInt = someInt1, someString = "aSomeLeftValue1", createdTimestampUtc = timestamp1).build()
    private val leftEntity2 = LeftEntityTestBuilder(someInt = someInt2, someString = "bSomeLeftValue1", createdTimestampUtc = timestamp2).build()
    private val leftEntity3 = LeftEntityTestBuilder(someInt = someInt3, someString = "bSomeLeftValue2", createdTimestampUtc = timestamp3).build()




    @BeforeEach
    fun beforeEach() {

        this.manyToManyJoinDao.deleteAll()
        this.leftDao.deleteAll()

        this.leftDao.bulkInsert(listOf(leftEntity1, leftEntity2, leftEntity3))

    }


    @Test
    @Disabled
    fun should_return_403_for_unauthorised_user() {

        val requestBody = asJson(mapOf<String, String>())

        this.mockMvc.perform(post("/api/person_summary/aggrid_datasource").content(requestBody))
            .andExpect(status().isForbidden)

    }


    @Test
    fun `test filter text equals`() {

        submitSearch(
            startRow = 0,
            endRow = 3,
            filterModel = mapOf(
                "someStringFromLeft" to mapOf(
                    "filterType" to "text",
                    "type" to "equals",
                    "filter" to "aSomeLeftValue1"
                )
            )
        ).andExpect {
            content {
                json(
                    expectedResult(
                        totalCount = 1,
                        rows = listOf(leftEntity1),
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
        path: String = "/api/left_not_mapped_to_right/search",
        startRow: Int,
        endRow: Int,
        sortModel: List<Map<String, String>> = emptyList(),
        filterModel: Map<String, Any?> = emptyMap()
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
        rows: List<LeftEntity>,
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


    private fun jsonFor(leftEntity: LeftEntity): Map<String, Any?> {

        return mapOf(
            "id" to leftEntity.id.value,
            "someStringFromLeft" to leftEntity.someString,
            "someIntFromLeft" to leftEntity.someInt,
            "createdTimestampUtc" to leftEntity.createdTimestampUtc.truncatedTo(ChronoUnit.MILLIS).toString()
        )

    }


}
