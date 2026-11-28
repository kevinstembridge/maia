package org.maiaframework.showcase.join

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.common.BlotterTooLargeException
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.springframework.beans.factory.annotation.Autowired


/**
 * Black-box test of the `maxRows` guard as the user sees it (ADR-0008): when the set behind the Client-side blotter of
 * the Hazelcast-backed BravoHzCapped Searchable DTO exceeds its limit of 20, the page shows the explicit error message
 * rather than an empty grid.
 */
class BravoHzCappedBlotterPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var alphaDao: AlphaDao


    @Autowired
    private lateinit var bravoDao: BravoDao


    @Autowired
    private lateinit var cappedRepo: BravoHzCappedDtoHazelcastRepo


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
        cappedRepo.populate()

    }


    @Test
    fun `a set over the limit shows the error message instead of an empty grid`() {

        repeat(21) { seedBravo("capped-$it") }
        assertThrows(BlotterTooLargeException::class.java) { cappedRepo.populate() }

        `log in as admin user`()
        `navigate to the`(bravoHzCappedBlotterPage)

        bravoHzCappedBlotterPage.assertLoadErrorContains("BravoHzCappedDto")
        bravoHzCappedBlotterPage.assertLoadErrorContains("maxRows limit of 20")
        assertEquals(0, bravoHzCappedBlotterPage.rowCount())

    }


    @Test
    fun `a set within the limit shows its rows and no error`() {

        seedBravo("capped-only")
        cappedRepo.populate()

        `log in as admin user`()
        `navigate to the`(bravoHzCappedBlotterPage)

        bravoHzCappedBlotterPage.assertNoLoadError()

    }


    private fun seedBravo(bravoText: String) {

        val alpha = AlphaEntityTestBuilder(someString = "alpha-for-$bravoText").build()
        alphaDao.insert(alpha)
        val bravo = BravoEntityTestBuilder(alphaId = alpha.id, someString = bravoText).build()
        bravoDao.insert(bravo)
        insertedBravos.add(bravo)

    }


}
