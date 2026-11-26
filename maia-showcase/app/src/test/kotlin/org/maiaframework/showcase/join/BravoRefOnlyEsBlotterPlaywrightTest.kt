package org.maiaframework.showcase.join

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.elasticsearch.index.IndexNameFactory
import org.maiaframework.showcase.AbstractPlaywrightTest
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

        val alpha = AlphaEntityTestBuilder(someString = "alpha-before-edit").build()
        alphaDao.insert(alpha)
        insertedAlphas.add(alpha)
        val bravo = BravoEntityTestBuilder(alphaId = alpha.id, someString = "bravo-ref-only-row").build()
        bravoDao.insert(bravo)
        insertedBravos.add(bravo)
        ensureIndexExists()
        indexService.refreshByIds(listOf(bravo.id))
        refreshIndex()

        `log in as admin user`()
        `navigate to the`(bravoRefOnlyEsBlotterPage)
        assertEquals("alpha-before-edit", bravoRefOnlyEsBlotterPage.alphaCellTextForRowContaining("bravo-ref-only-row"))

        alphaCrudService.updateSomeString(AlphaUpdate_someStringRequestDto(id_raw = alpha.id, someString_raw = "alpha-after-edit"))
        refreshIndex()

        `navigate to the`(bravoRefOnlyEsBlotterPage)
        assertEquals("alpha-after-edit", bravoRefOnlyEsBlotterPage.alphaCellTextForRowContaining("bravo-ref-only-row"))

    }


    private fun indexName() = indexNameFactory.indexNameFrom(BravoRefOnlyEsEsIndexMeta_v0001.indexBaseNameAndVersion).value


    private fun ensureIndexExists() {

        if (!elasticsearchClient.indices().exists { r -> r.index(indexName()) }.value()) {
            indexControl.createIndex()
        }

    }


    private fun refreshIndex() {

        elasticsearchClient.indices().refresh { r -> r.index(indexName()) }

    }


}
