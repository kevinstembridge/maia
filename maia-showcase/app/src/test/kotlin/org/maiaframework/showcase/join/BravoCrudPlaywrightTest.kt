package org.maiaframework.showcase.join

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.elasticsearch.index.IndexNameFactory
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.springframework.beans.factory.annotation.Autowired
import org.junit.jupiter.api.Assertions.assertEquals


class BravoCrudPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var elasticsearchClient: ElasticsearchClient


    @Autowired
    private lateinit var indexNameFactory: IndexNameFactory


    @Autowired
    private lateinit var alphaDao: AlphaDao


    @Autowired
    private lateinit var bravoDao: BravoDao


    @Autowired
    private lateinit var alphaTypeaheadEsIndexControl: AlphaTypeaheadEsIndexControl_v0001


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        val alphaFixture = fixtures.anAlpha(someString = "alpha-fixture")
        fixtures.resetDatabaseState()

        // Create the Elasticsearch index and index the alpha fixture so the typeahead returns results
        val indexName = indexNameFactory.indexNameFrom(AlphaTypeaheadEsIndexMeta_v0001.indexBaseNameAndVersion).value

        if (!elasticsearchClient.indices().exists { r -> r.index(indexName) }.value()) {
            alphaTypeaheadEsIndexControl.createIndex()
        }

        elasticsearchClient.index { r ->
            r.index(indexName)
                .id(alphaFixture.id.value)
                .document(AlphaTypeaheadV1EsDoc(id = alphaFixture.id, someString = alphaFixture.someString))
        }

        elasticsearchClient.indices().refresh { r ->
            r.index(indexName)
        }

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `sql-backed blotter shows the alpha foreign key as a plain uuid`() {

        val alpha = AlphaEntityTestBuilder(someString = "alpha-for-uuid-column").build()
        alphaDao.insert(alpha)
        val bravo = BravoEntityTestBuilder(alphaId = alpha.id, someString = "bravo-for-uuid-column").build()
        bravoDao.insert(bravo)

        try {

            `log in as admin user`()
            `navigate to the`(bravoBlotterPage)

            // The uuid cell renderer shows the last 12 characters of the uuid
            assertEquals(alpha.id.value.takeLast(12), bravoBlotterPage.alphaCellTextForRowContaining("bravo-for-uuid-column"))

        } finally {
            bravoDao.deleteByPrimaryKey(bravo.id)
        }

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(bravoBlotterPage)

        bravoBlotterPage.clickAddButton()

        bravoCreatePage.apply {
            assertOnPage()
            fillCreateForm()
            clickSubmitButton()
        }

        bravoViewPage.apply {
            assertOnPage()
            clickEditButton()
        }

        bravoEditPage.apply {
            assertOnPage()
            fillEditForm()
            clickSubmitButton()
        }

        bravoViewPage.assertOnPage()

        `navigate to the`(bravoBlotterPage)

        bravoBlotterPage.apply {
            assertTableContainsValue("testbravo_edited")

            // Cancel path
            clickDeleteButtonForFirstRow()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("testbravo_edited")

            // Confirm delete path
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("testbravo_edited")
        }

    }


}
