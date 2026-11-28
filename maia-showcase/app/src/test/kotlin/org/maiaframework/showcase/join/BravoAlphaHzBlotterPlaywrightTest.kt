package org.maiaframework.showcase.join

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.springframework.beans.factory.annotation.Autowired


/**
 * Black-box test of the Client-side blotter over the Hazelcast-backed BravoAlphaHz and DeltaHz Searchable DTOs
 * (ADR-0007, ADR-0008): an Entity ref column shows the display name, and the browser filters and sorts on it.
 */
class BravoAlphaHzBlotterPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var alphaDao: AlphaDao


    @Autowired
    private lateinit var bravoDao: BravoDao


    @Autowired
    private lateinit var charlieDao: CharlieDao


    @Autowired
    private lateinit var deltaDao: DeltaDao


    @Autowired
    private lateinit var bravoAlphaHzRepo: BravoAlphaHzDtoHazelcastRepo


    @Autowired
    private lateinit var deltaHzRepo: DeltaHzDtoHazelcastRepo


    private val insertedDeltas = mutableListOf<DeltaEntity>()
    private val insertedCharlies = mutableListOf<CharlieEntity>()
    private val insertedBravos = mutableListOf<BravoEntity>()
    private val insertedAlphas = mutableListOf<AlphaEntity>()


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
    fun deleteSeededRows() {

        insertedDeltas.forEach { deltaDao.deleteByPrimaryKey(it.id) }
        insertedCharlies.forEach { charlieDao.deleteByPrimaryKey(it.id) }
        insertedBravos.forEach { bravoDao.deleteByPrimaryKey(it.id) }
        insertedAlphas.forEach { alphaDao.deleteByPrimaryKey(it.id) }
        insertedDeltas.clear()
        insertedCharlies.clear()
        insertedBravos.clear()
        insertedAlphas.clear()
        bravoAlphaHzRepo.populate()
        deltaHzRepo.populate()

    }


    @Test
    fun `hazelcast-backed blotter shows the alpha display text, not a uuid`() {

        seedBravoWithAlpha("alpha-display-text", "bravo-for-ref-column")
        bravoAlphaHzRepo.populate()

        `log in as admin user`()
        `navigate to the`(bravoAlphaHzBlotterPage)

        assertEquals("alpha-display-text", bravoAlphaHzBlotterPage.alphaCellTextForRowContaining("bravo-for-ref-column"))

    }


    @Test
    fun `hazelcast-backed blotter filters and sorts the alpha column on the display name`() {

        seedBravoWithAlpha("reftok-zebra", "bravo-ref-zebra")
        seedBravoWithAlpha("reftok-apple", "bravo-ref-apple")
        seedBravoWithAlpha("unrelated-mango", "bravo-ref-mango")
        bravoAlphaHzRepo.populate()

        `log in as admin user`()
        `navigate to the`(bravoAlphaHzBlotterPage)

        bravoAlphaHzBlotterPage.filterAlphaColumnBy("reftok")

        assertEquals(
            setOf("reftok-apple", "reftok-zebra"),
            bravoAlphaHzBlotterPage.alphaCellTexts(expectedCount = 2).toSet()
        )

        bravoAlphaHzBlotterPage.clickAlphaColumnHeaderToSort()
        assertEquals(listOf("reftok-apple", "reftok-zebra"), bravoAlphaHzBlotterPage.alphaCellTexts(expectedCount = 2))

        bravoAlphaHzBlotterPage.clickAlphaColumnHeaderToSort()
        assertEquals(listOf("reftok-zebra", "reftok-apple"), bravoAlphaHzBlotterPage.alphaCellTexts(expectedCount = 2))

    }


    @Test
    fun `nullable foreign key shows the alpha display text, or an empty cell when null`() {

        val charlie = insertCharlie()
        val alpha = AlphaEntityTestBuilder(someString = "delta-hz-alpha-display").build()
        alphaDao.insert(alpha)
        insertedAlphas.add(alpha)
        insertDelta(DeltaEntityTestBuilder(alphaId = alpha.id, charlieId = charlie.id, someString = "delta-hz-with-alpha").build())
        insertDelta(DeltaEntityTestBuilder(alphaId = null, charlieId = charlie.id, someString = "delta-hz-without-alpha").build())
        deltaHzRepo.populate()

        `log in as admin user`()
        `navigate to the`(deltaHzBlotterPage)

        assertEquals("delta-hz-alpha-display", deltaHzBlotterPage.cellTextForRowContaining("delta-hz-with-alpha", "alpha"))
        assertEquals("", deltaHzBlotterPage.cellTextForRowContaining("delta-hz-without-alpha", "alpha"))

    }


    private fun seedBravoWithAlpha(alphaText: String, bravoText: String): BravoEntity {

        val alpha = AlphaEntityTestBuilder(someString = alphaText).build()
        alphaDao.insert(alpha)
        insertedAlphas.add(alpha)
        val bravo = BravoEntityTestBuilder(alphaId = alpha.id, someString = bravoText).build()
        bravoDao.insert(bravo)
        insertedBravos.add(bravo)
        return bravo

    }


    private fun insertCharlie(): CharlieEntity {

        val bravo = seedBravoWithAlpha("charlie-alpha", "charlie-bravo")
        val charlie = CharlieEntityTestBuilder(bravoId = bravo.id).build()
        charlieDao.insert(charlie)
        insertedCharlies.add(charlie)
        return charlie

    }


    private fun insertDelta(delta: DeltaEntity) {

        deltaDao.insert(delta)
        insertedDeltas.add(delta)

    }


}
