package org.maiaframework.showcase.join

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.elasticsearch.index.IndexNameFactory
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.testing.EsTestIndex
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser


/**
 * Black-box test (ADR-0007) of refreshing the Entity ref on a Blotter whose only link to Alpha is the direct
 * `alpha` ref column (no path through Alpha), so the refresh dependency must come from the ref field itself.
 */
class BravoRefOnlyEsBlotterPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var elasticsearchClient: ElasticsearchClient


    @Autowired
    private lateinit var indexNameFactory: IndexNameFactory


    @Autowired
    private lateinit var alphaDao: AlphaDao


    @Autowired
    private lateinit var alphaCrudService: AlphaCrudService


    @Autowired
    private lateinit var bravoDao: BravoDao


    @Autowired
    private lateinit var indexService: BravoRefOnlyEsV1IndexService


    @Autowired
    private lateinit var indexControl: BravoRefOnlyEsEsIndexControl_v0001


    private val esIndex by lazy {
        EsTestIndex(elasticsearchClient, indexNameFactory, BravoRefOnlyEsEsIndexMeta_v0001.indexBaseNameAndVersion) {
            indexControl.createIndex()
        }
    }


    @Autowired
    private lateinit var bravoCrudService: BravoCrudService


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
    fun cleanUp() {

        insertedBravos.forEach { bravoDao.deleteByPrimaryKey(it.id) }
        indexService.refreshByIds(insertedBravos.map { it.id })
        insertedAlphas.forEach { alphaDao.deleteByPrimaryKey(it.id) }
        insertedBravos.clear()
        insertedAlphas.clear()

    }


    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `editing the referenced alpha refreshes the ref shown in the blotter`() {

        val alpha = seedAlpha("alpha-before-edit")
        seedBravo(alpha, "bravo-ref-only-row")

        `log in as admin user`()
        `show the ref only blotter filtered to`("bravo-ref-only-row")
        assertEquals("alpha-before-edit", bravoRefOnlyEsBlotterPage.alphaCellTextForRowContaining("bravo-ref-only-row"))

        alphaCrudService.updateSomeString(AlphaUpdate_someStringRequestDto(id_raw = alpha.id, someString_raw = "alpha-after-edit"))
        esIndex.refresh()

        `show the ref only blotter filtered to`("bravo-ref-only-row")
        assertEquals("alpha-after-edit", bravoRefOnlyEsBlotterPage.alphaCellTextForRowContaining("bravo-ref-only-row"))

    }


    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `changing the bravo's alpha shows the new alpha in the ref cell`() {

        val bravo = seedBravo(seedAlpha("alpha-original"), "bravo-switching-alpha")
        val replacement = seedAlpha("alpha-replacement")

        `log in as admin user`()
        `show the ref only blotter filtered to`("bravo-switching-alpha")
        assertEquals("alpha-original", bravoRefOnlyEsBlotterPage.alphaCellTextForRowContaining("bravo-switching-alpha"))

        bravoCrudService.updateAlpha(BravoUpdate_alphaRequestDto(alpha_raw = replacement.id, id_raw = bravo.id))
        esIndex.refresh()

        `show the ref only blotter filtered to`("bravo-switching-alpha")
        assertEquals("alpha-replacement", bravoRefOnlyEsBlotterPage.alphaCellTextForRowContaining("bravo-switching-alpha"))

    }


    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `editing an alpha field that is not part of the ref leaves the cell and the indexed document untouched`() {

        val alpha = seedAlpha("alpha-stable-text")
        val bravo = seedBravo(alpha, "bravo-unrelated-edit")
        val versionBefore = esIndex.documentVersion(bravo.id)

        `log in as admin user`()
        `show the ref only blotter filtered to`("bravo-unrelated-edit")
        assertEquals("alpha-stable-text", bravoRefOnlyEsBlotterPage.alphaCellTextForRowContaining("bravo-unrelated-edit"))

        alphaCrudService.updateSomeInt(AlphaUpdate_someIntRequestDto(id_raw = alpha.id, someInt_raw = alpha.someInt + 1))
        esIndex.refresh()

        `show the ref only blotter filtered to`("bravo-unrelated-edit")
        assertEquals("alpha-stable-text", bravoRefOnlyEsBlotterPage.alphaCellTextForRowContaining("bravo-unrelated-edit"))
        assertEquals(versionBefore, esIndex.documentVersion(bravo.id), "the document must not be re-indexed")

    }


    /**
     * The ticket asks that deleting the Alpha leaves no stale ref, but a Bravo that references an Alpha blocks its
     * deletion through the foreign key, so that state is unreachable. The realistic case is deleting the referencing
     * Bravo: its row must leave the Blotter.
     */
    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `deleting the bravo removes its row from the blotter`() {

        val bravo = seedBravo(seedAlpha("alpha-of-deleted-bravo"), "bravo-to-delete")

        `log in as admin user`()
        `show the ref only blotter filtered to`("bravo-to-delete")
        bravoRefOnlyEsBlotterPage.assertRowContaining("bravo-to-delete")

        bravoCrudService.delete(bravo.id)
        esIndex.refresh()

        `show the ref only blotter filtered to`("bravo-to-delete")
        bravoRefOnlyEsBlotterPage.assertNoRowContaining("bravo-to-delete")

    }


    private fun `show the ref only blotter filtered to`(bravoText: String) {

        `navigate to the`(bravoRefOnlyEsBlotterPage)
        bravoRefOnlyEsBlotterPage.filterBravoColumnBy(bravoText)

    }


    private fun seedAlpha(someString: String): AlphaEntity {

        val alpha = AlphaEntityTestBuilder(someString = someString).build()
        alphaDao.insert(alpha)
        insertedAlphas.add(alpha)
        return alpha

    }


    private fun seedBravo(alpha: AlphaEntity, someString: String): BravoEntity {

        val bravo = BravoEntityTestBuilder(alphaId = alpha.id, someString = someString).build()
        bravoDao.insert(bravo)
        insertedBravos.add(bravo)
        esIndex.ensureExists()
        indexService.refreshByIds(listOf(bravo.id))
        esIndex.refresh()
        return bravo

    }


}
