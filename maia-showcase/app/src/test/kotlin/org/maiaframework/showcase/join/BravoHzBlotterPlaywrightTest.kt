package org.maiaframework.showcase.join

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.springframework.beans.factory.annotation.Autowired


/**
 * Black-box test of the Client-side blotter over the Hazelcast-backed BravoHz Searchable DTO (ADR-0008): the page
 * fetches the whole set once, and filtering and sorting happen in the browser with no further requests.
 */
class BravoHzBlotterPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var alphaDao: AlphaDao


    @Autowired
    private lateinit var bravoDao: BravoDao


    @Autowired
    private lateinit var bravoHzRepo: BravoHzDtoHazelcastRepo


    private val insertedBravos = mutableListOf<BravoEntity>()


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()
        fixtures.resetDatabaseState()

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @AfterEach
    fun deleteSeededBravos() {

        insertedBravos.forEach { bravoDao.deleteByPrimaryKey(it.id) }
        insertedBravos.clear()
        bravoHzRepo.populate()

    }


    @Test
    fun `hazelcast-backed blotter loads the whole set once and filters and sorts in the browser`() {

        seedBravo("hztok-zebra")
        seedBravo("hztok-apple")
        seedBravo("unrelated-mango")
        bravoHzRepo.populate()

        `log in as admin user`()
        bravoHzBlotterPage.resetFetchAllRequestCount()
        `navigate to the`(bravoHzBlotterPage)

        assertEquals(
            setOf("hztok-zebra", "hztok-apple", "unrelated-mango"),
            bravoHzBlotterPage.stringCellTexts(expectedCount = 3).toSet()
        )

        bravoHzBlotterPage.filterStringColumnBy("hztok")

        assertEquals(
            setOf("hztok-apple", "hztok-zebra"),
            bravoHzBlotterPage.stringCellTexts(expectedCount = 2).toSet()
        )

        bravoHzBlotterPage.clickStringColumnHeaderToSort()
        assertEquals(listOf("hztok-apple", "hztok-zebra"), bravoHzBlotterPage.stringCellTexts(expectedCount = 2))

        bravoHzBlotterPage.clickStringColumnHeaderToSort()
        assertEquals(listOf("hztok-zebra", "hztok-apple"), bravoHzBlotterPage.stringCellTexts(expectedCount = 2))

        assertEquals(1, bravoHzBlotterPage.fetchAllRequestCount(), "filtering and sorting must not call the server")

    }


    @Test
    fun `refresh button fetches the set again and picks up a row added since the page loaded`() {

        seedBravo("hzrefresh-one")
        bravoHzRepo.populate()

        `log in as admin user`()
        `navigate to the`(bravoHzBlotterPage)
        assertEquals(listOf("hzrefresh-one"), bravoHzBlotterPage.stringCellTexts(expectedCount = 1))

        seedBravo("hzrefresh-two")
        bravoHzRepo.populate()

        // the page shows a snapshot as of the last fetch until it is refreshed
        assertEquals(listOf("hzrefresh-one"), bravoHzBlotterPage.stringCellTexts(expectedCount = 1))

        bravoHzBlotterPage.clickRefreshButton()

        assertEquals(
            setOf("hzrefresh-one", "hzrefresh-two"),
            bravoHzBlotterPage.stringCellTexts(expectedCount = 2).toSet()
        )

    }


    private fun seedBravo(bravoText: String) {

        val alpha = AlphaEntityTestBuilder(someString = "alpha-for-$bravoText").build()
        alphaDao.insert(alpha)
        val bravo = BravoEntityTestBuilder(alphaId = alpha.id, someString = bravoText).build()
        bravoDao.insert(bravo)
        insertedBravos.add(bravo)

    }


}
