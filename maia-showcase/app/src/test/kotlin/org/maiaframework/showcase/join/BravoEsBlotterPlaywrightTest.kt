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


/**
 * Black-box test of the Blotter over the Elasticsearch-enabled BravoEs Searchable DTO (ADR-0007): a foreign-key
 * column whose foreign Entity has an Entity ref shows the ref's display name, not the uuid.
 */
class BravoEsBlotterPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var elasticsearchClient: ElasticsearchClient


    @Autowired
    private lateinit var indexNameFactory: IndexNameFactory


    @Autowired
    private lateinit var alphaDao: AlphaDao


    @Autowired
    private lateinit var bravoDao: BravoDao


    @Autowired
    private lateinit var bravoEsIndexService: BravoEsV1IndexService


    @Autowired
    private lateinit var bravoEsIndexControl: BravoEsEsIndexControl_v0001


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
        bravoEsIndexService.refreshByIds(insertedBravos.map { it.id })
        insertedBravos.clear()

    }


    @Test
    fun `es-backed blotter shows the alpha display text, not a uuid`() {

        val alpha = AlphaEntityTestBuilder(someString = "alpha-display-text").build()
        alphaDao.insert(alpha)
        val bravo = BravoEntityTestBuilder(alphaId = alpha.id, someString = "bravo-for-ref-column").build()
        bravoDao.insert(bravo)
        insertedBravos.add(bravo)

        indexIntoElasticsearch(bravo)

        `log in as admin user`()
        `navigate to the`(bravoEsBlotterPage)

        assertEquals("alpha-display-text", bravoEsBlotterPage.alphaCellTextForRowContaining("bravo-for-ref-column"))

    }


    private fun indexIntoElasticsearch(bravo: BravoEntity) {

        val indexName = indexNameFactory.indexNameFrom(BravoEsEsIndexMeta_v0001.indexBaseNameAndVersion).value

        if (!elasticsearchClient.indices().exists { r -> r.index(indexName) }.value()) {
            bravoEsIndexControl.createIndex()
        }

        bravoEsIndexService.refreshByIds(listOf(bravo.id))
        elasticsearchClient.indices().refresh { r -> r.index(indexName) }

    }


}
