package org.maiaframework.showcase.searchable

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.hierarchy.ChildOneEntityTestBuilder
import org.maiaframework.showcase.hierarchy.GrandparentDao
import org.maiaframework.showcase.hierarchy.GrandparentEntity
import org.maiaframework.showcase.hierarchy.GrandparentEntityMeta
import org.maiaframework.showcase.hierarchy.ParentOneEntityTestBuilder
import org.maiaframework.showcase.hierarchy.ParentTwoEntityTestBuilder
import org.maiaframework.showcase.party.PartyDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert
import java.time.temporal.ChronoUnit

class SearchableDtoWithClassHierarchyTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var partyDao: PartyDao


    @Autowired
    private lateinit var grandparentDao: GrandparentDao

    private val parentOneEntity1 = ParentOneEntityTestBuilder(someString = "Some Mega Corp").build()
    private val parentOneEntity2 = ParentOneEntityTestBuilder(someString = "The Corner Shop").build()

    private val parentTwoEntity1 = ParentTwoEntityTestBuilder().build()
    private val parentTwoEntity2 = ParentTwoEntityTestBuilder().build()

    private val childOneEntity1 = ChildOneEntityTestBuilder().build()
    private val childOneEntity2 = ChildOneEntityTestBuilder().build()


    @BeforeEach
    fun beforeEach() {

        resetDatabase()
        this.grandparentDao.insert(parentOneEntity1)
        this.grandparentDao.insert(parentOneEntity2)
        this.grandparentDao.insert(parentTwoEntity1)
        this.grandparentDao.insert(parentTwoEntity2)
        this.grandparentDao.insert(childOneEntity1)
        this.grandparentDao.insert(childOneEntity2)

    }

    private fun resetDatabase() {

        this.jdbcOps.update("delete from ${GrandparentEntityMeta.SCHEMA_AND_TABLE_NAME}")

    }


    @Test
    fun `test ParentTwo search with no filter terms`() {

        submitSearch(
                path = "/api/parent_two/search",
                startRow = 0,
                endRow = 3,
                filterModel = listOf()
        ).bodyJson().isLenientlyEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    parentTwoEntity1,
                    parentTwoEntity2
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 3
            )
        )

    }


    @Test
    fun `test ParentOne search with no filter terms`() {

        submitSearch(
                path = "/api/parent_one/search",
                startRow = 0,
                endRow = 10,
                filterModel = listOf()
        ).bodyJson().isLenientlyEqualTo(
            expectedResult(
                totalCount = 4,
                rows = listOf(
                    parentOneEntity1,
                    parentOneEntity2,
                    childOneEntity1,
                    childOneEntity2
                ),
                firstResultIndex = 1,
                lastResultIndex = 4,
                offset = 0,
                limit = 10
            )
        )

    }


    @Test
    fun `test Child search with no filter terms`() {

        submitSearch(
                path = "/api/child_one/search",
                startRow = 0,
                endRow = 10,
                filterModel = listOf()
        ).bodyJson().isLenientlyEqualTo(
            expectedResult(
                totalCount = 2,
                rows = listOf(
                    childOneEntity1,
                    childOneEntity2
                ),
                firstResultIndex = 1,
                lastResultIndex = 2,
                offset = 0,
                limit = 10
            )
        )

    }


    private fun submitSearch(
        path: String,
        startRow: Int,
        endRow: Int,
        sortModel: List<Map<String, String>> = emptyList(),
        filterModel: List<Map<String, Any?>> = emptyList()
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
        rows: List<GrandparentEntity>,
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


    private fun jsonFor(entity: GrandparentEntity): Map<String, Any?> {

        return mapOf(
            "id" to entity.id.value,
            "createdTimestampUtc" to entity.createdTimestampUtc.truncatedTo(ChronoUnit.MILLIS).toString()
        )

    }


}
