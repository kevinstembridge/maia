package org.maiaframework.showcase.blotter

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest

class BlotterPaginationPlaywrightTest : AbstractPlaywrightTest() {


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        fixtures.resetDatabaseState()

        jdbcOps.update("truncate maia.simple cascade")

        // 120 rows: one full default page of 100 plus a second page of 20
        jdbcOps.update(
            """
            insert into maia.some_versioned (created_timestamp, id, some_int, some_string, version)
            select now(), gen_random_uuid(), i, 'row-' || i, 1 from generate_series(1, 120) as i
            """.trimIndent()
        )

        jdbcOps.update("insert into maia.simple (created_timestamp, id, some_string) values (now(), gen_random_uuid(), 'only-row')")

    }


    @AfterAll
    fun tearDown() {

        // resetDatabaseState() does not cover these tables, and other tests assume they are empty
        jdbcOps.update("truncate maia.some_versioned, maia.simple cascade")

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `paginated blotter shows one page at a time`() {

        `log in as admin user`()

        `navigate to the`(someVersionedBlotterPage)

        someVersionedBlotterPage.apply {
            assertPagingPanelIsVisible()
            assertPagingSummaryEquals("1 to 100 of 120")
            clickNextPage()
            assertPagingSummaryEquals("101 to 120 of 120")
        }

    }


    @Test
    fun `page size selector changes the number of rows per page`() {

        `log in as admin user`()

        `navigate to the`(someVersionedBlotterPage)

        someVersionedBlotterPage.apply {
            assertPagingSummaryEquals("1 to 100 of 120")
            selectPageSize(25)
            assertPagingSummaryEquals("1 to 25 of 120")
            clickNextPage()
            assertPagingSummaryEquals("26 to 50 of 120")
        }

    }


    @Test
    fun `infinite-scroll blotter has no paging panel`() {

        `log in as admin user`()

        `navigate to the`(simpleBlotterPage)

        simpleBlotterPage.assertNoPagingPanel()

    }


}
