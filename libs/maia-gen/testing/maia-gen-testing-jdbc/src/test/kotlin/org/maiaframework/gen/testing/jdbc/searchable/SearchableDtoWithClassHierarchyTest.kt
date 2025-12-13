package org.maiaframework.gen.testing.jdbc.searchable

import org.maiaframework.json.JsonFacade
import org.maiaframework.gen.testing.jdbc.AbstractJdbcTest
import org.maiaframework.gen.testing.jdbc.party.OrgEntityTestBuilder
import org.maiaframework.gen.testing.jdbc.party.PersonEntityTestBuilder
import org.maiaframework.gen.testing.jdbc.party.UserEntityTestBuilder
import org.maiaframework.gen.testing.jdbc.sample.party.PartyDao
import org.maiaframework.gen.testing.jdbc.sample.party.PartyEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post
import java.time.temporal.ChronoUnit

class SearchableDtoWithClassHierarchyTest : AbstractJdbcTest() {


    @Autowired
    private lateinit var partyDao: PartyDao


    private val orgEntity1 = OrgEntityTestBuilder(name = "Some Mega Corp").build()
    private val orgEntity2 = OrgEntityTestBuilder(name = "The Corner Shop").build()

    private val personEntity1 = PersonEntityTestBuilder().build()
    private val personEntity2 = PersonEntityTestBuilder().build()

    private val userEntity1 = UserEntityTestBuilder().build()
    private val userEntity2 = UserEntityTestBuilder().build()


    @BeforeEach
    fun beforeEach() {

        deleteParties()
        this.partyDao.insert(orgEntity1)
        this.partyDao.insert(orgEntity2)
        this.partyDao.insert(personEntity1)
        this.partyDao.insert(personEntity2)
        this.partyDao.insert(userEntity1)
        this.partyDao.insert(userEntity2)

    }


    @Test
    fun `test Org search with no filter terms`() {

        submitSearch(
            path = "/api/org/search",
            startRow = 0,
            endRow = 3,
            filterModel = listOf()
        ).andExpect {
            content {
                json(
                    expectedResult(
                        totalCount = 2,
                        rows = listOf(
                            orgEntity1,
                            orgEntity2
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
    fun `test Person search with no filter terms`() {

        submitSearch(
            path = "/api/person/search",
            startRow = 0,
            endRow = 10,
            filterModel = listOf()
        ).andExpect {
            content {
                json(
                    expectedResult(
                        totalCount = 5,
                        rows = listOf(
                            defaultUser,
                            personEntity1,
                            personEntity2,
                            userEntity1,
                            userEntity2
                        ),
                        firstResultIndex = 1,
                        lastResultIndex = 5,
                        offset = 0,
                        limit = 10
                    )
                )
            }
        }

    }


    @Test
    fun `test User search with no filter terms`() {

        submitSearch(
            path = "/api/user/search",
            startRow = 0,
            endRow = 10,
            filterModel = listOf()
        ).andExpect {
            content {
                json(
                    expectedResult(
                        totalCount = 3,
                        rows = listOf(
                            defaultUser,
                            userEntity1,
                            userEntity2
                        ),
                        firstResultIndex = 1,
                        lastResultIndex = 3,
                        offset = 0,
                        limit = 10
                    )
                )
            }
        }

    }


    private fun submitSearch(
        path: String = "/api/org/search",
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
            characterEncoding = "UTF-8"
            with(user("nigel").roles("ADMIN"))
        }.andDo {
            print()
        }

    }


    private fun expectedResult(
        totalCount: Int,
        rows: List<PartyEntity>,
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


    private fun jsonFor(partyEntity: PartyEntity): Map<String, Any?> {

        return mapOf(
            "id" to partyEntity.id.value,
            "createdTimestampUtc" to partyEntity.createdTimestampUtc.truncatedTo(ChronoUnit.MILLIS).toString()
        )

    }


}
