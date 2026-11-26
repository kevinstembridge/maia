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
 * Black-box test of the Blotter over the Elasticsearch-enabled CharlieEs Searchable DTO (ADR-0007): a path field
 * (`bravo.alpha`) whose terminal field is a foreign key to an Entity with an Entity ref shows the ref's display
 * name, while a path field ending in a non-foreign-key field (`bravo.someString`) is unchanged.
 */
class CharlieEsBlotterPlaywrightTest : AbstractPlaywrightTest() {


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
    private lateinit var charlieDao: CharlieDao


    @Autowired
    private lateinit var charlieEsIndexService: CharlieEsV1IndexService


    @Autowired
    private lateinit var charlieEsIndexControl: CharlieEsEsIndexControl_v0001


    private val esIndex by lazy {
        EsTestIndex(elasticsearchClient, indexNameFactory, CharlieEsEsIndexMeta_v0001.indexBaseNameAndVersion) {
            charlieEsIndexControl.createIndex()
        }
    }


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

        insertedCharlies.forEach { charlieDao.deleteByPrimaryKey(it.id) }
        charlieEsIndexService.refreshByIds(insertedCharlies.map { it.id })
        insertedBravos.forEach { bravoDao.deleteByPrimaryKey(it.id) }
        insertedAlphas.forEach { alphaDao.deleteByPrimaryKey(it.id) }
        insertedCharlies.clear()
        insertedBravos.clear()
        insertedAlphas.clear()

    }


    @Test
    fun `path field ending in a foreign key shows the ref display text, a path ending in a plain field is unchanged`() {

        seedCharlie(alphaText = "alpha-via-bravo-text", bravoText = "bravo-plain-text", charlieText = "charlie-row-one")

        `log in as admin user`()
        `navigate to the`(charlieEsBlotterPage)

        assertEquals("alpha-via-bravo-text", charlieEsBlotterPage.cellTextForRowContaining("charlie-row-one", "alphaViaBravo"))
        assertEquals("bravo-plain-text", charlieEsBlotterPage.cellTextForRowContaining("charlie-row-one", "dtoStringFromBravo"))

    }


    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `editing the terminal referenced Alpha refreshes the Blotter`() {

        val alpha = seedCharlie(alphaText = "alpha-before-edit", bravoText = "bravo-for-edit", charlieText = "charlie-row-two")

        alphaCrudService.updateSomeString(AlphaUpdate_someStringRequestDto(id_raw = alpha.id, someString_raw = "alpha-after-edit"))
        esIndex.refresh()

        `log in as admin user`()
        `navigate to the`(charlieEsBlotterPage)

        assertEquals("alpha-after-edit", charlieEsBlotterPage.cellTextForRowContaining("charlie-row-two", "alphaViaBravo"))

    }


    private fun seedCharlie(alphaText: String, bravoText: String, charlieText: String): AlphaEntity {

        val alpha = AlphaEntityTestBuilder(someString = alphaText).build()
        alphaDao.insert(alpha)
        insertedAlphas.add(alpha)
        val bravo = BravoEntityTestBuilder(alphaId = alpha.id, someString = bravoText).build()
        bravoDao.insert(bravo)
        insertedBravos.add(bravo)
        val charlie = CharlieEntityTestBuilder(bravoId = bravo.id, someString = charlieText).build()
        charlieDao.insert(charlie)
        insertedCharlies.add(charlie)

        indexIntoElasticsearch(charlie)

        return alpha

    }


    private fun indexIntoElasticsearch(charlie: CharlieEntity) {

        esIndex.ensureExists()
        charlieEsIndexService.refreshByIds(listOf(charlie.id))
        esIndex.refresh()

    }


}
