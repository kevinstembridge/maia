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


/**
 * Black-box test of the Blotter over the Elasticsearch-enabled DeltaEs Searchable DTO (ADR-0007): a nullable
 * foreign key to an Entity with an Entity ref gives a nullable ref (empty cell when null), and a foreign key to
 * an Entity without an Entity ref stays a uuid.
 */
class DeltaEsBlotterPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var elasticsearchClient: ElasticsearchClient


    @Autowired
    private lateinit var indexNameFactory: IndexNameFactory


    @Autowired
    private lateinit var alphaDao: AlphaDao


    @Autowired
    private lateinit var bravoDao: BravoDao


    @Autowired
    private lateinit var charlieDao: CharlieDao


    @Autowired
    private lateinit var deltaDao: DeltaDao


    @Autowired
    private lateinit var deltaEsIndexService: DeltaEsV1IndexService


    @Autowired
    private lateinit var deltaEsIndexControl: DeltaEsEsIndexControl_v0001


    private val esIndex by lazy {
        EsTestIndex(elasticsearchClient, indexNameFactory, DeltaEsEsIndexMeta_v0001.indexBaseNameAndVersion) {
            deltaEsIndexControl.createIndex()
        }
    }


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
        deltaEsIndexService.refreshByIds(insertedDeltas.map { it.id })
        insertedCharlies.forEach { charlieDao.deleteByPrimaryKey(it.id) }
        insertedBravos.forEach { bravoDao.deleteByPrimaryKey(it.id) }
        insertedAlphas.forEach { alphaDao.deleteByPrimaryKey(it.id) }
        insertedDeltas.clear()
        insertedCharlies.clear()
        insertedBravos.clear()
        insertedAlphas.clear()

    }


    @Test
    fun `nullable foreign key shows the alpha display text, or an empty cell when null`() {

        val charlie = insertCharlie()
        val alpha = AlphaEntityTestBuilder(someString = "delta-alpha-display-text").build()
        alphaDao.insert(alpha)
        insertedAlphas.add(alpha)
        insertDelta(DeltaEntityTestBuilder(alphaId = alpha.id, charlieId = charlie.id, someString = "delta-with-alpha").build())
        insertDelta(DeltaEntityTestBuilder(alphaId = null, charlieId = charlie.id, someString = "delta-without-alpha").build())

        indexIntoElasticsearch()

        `log in as admin user`()
        `navigate to the`(deltaEsBlotterPage)

        assertEquals("delta-alpha-display-text", deltaEsBlotterPage.cellTextForRowContaining("delta-with-alpha", "alpha"))
        assertEquals("", deltaEsBlotterPage.cellTextForRowContaining("delta-without-alpha", "alpha"))

    }


    @Test
    fun `foreign key to an Entity without an Entity ref stays a uuid`() {

        val charlie = insertCharlie()
        insertDelta(DeltaEntityTestBuilder(charlieId = charlie.id, someString = "delta-for-uuid-column").build())

        indexIntoElasticsearch()

        `log in as admin user`()
        `navigate to the`(deltaEsBlotterPage)

        val cellText = deltaEsBlotterPage.cellTextForRowContaining("delta-for-uuid-column", "charlie")
        assertEquals(charlie.id.value.takeLast(12), cellText)

    }


    private fun insertCharlie(): CharlieEntity {

        val alpha = AlphaEntityTestBuilder().build()
        alphaDao.insert(alpha)
        insertedAlphas.add(alpha)
        val bravo = BravoEntityTestBuilder(alphaId = alpha.id).build()
        bravoDao.insert(bravo)
        insertedBravos.add(bravo)
        val charlie = CharlieEntity(bravo.id, java.time.Instant.now(), org.maiaframework.domain.DomainId.newId(), 1, "charlie")
        charlieDao.insert(charlie)
        insertedCharlies.add(charlie)
        return charlie

    }


    private fun insertDelta(delta: DeltaEntity) {

        deltaDao.insert(delta)
        insertedDeltas.add(delta)

    }


    private fun indexIntoElasticsearch() {

        esIndex.ensureExists()
        deltaEsIndexService.refreshByIds(insertedDeltas.map { it.id })
        esIndex.refresh()

    }


}
