package org.maiaframework.showcase.join

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.springframework.beans.factory.annotation.Autowired


class BravoCrudPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var elasticsearchClient: ElasticsearchClient

    @Autowired
    private lateinit var alphaTypeaheadEsIndex: AlphaTypeaheadEsIndex

    @Autowired
    private lateinit var alphaTypeaheadEsIndexControl: AlphaTypeaheadEsIndexControl_v0001


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()
        val alphaFixture = fixtures.anAlpha(someString = "alpha-fixture")
        fixtures.resetDatabaseState()

        // Create the Elasticsearch index and index the alpha fixture so the typeahead returns results
        alphaTypeaheadEsIndexControl.createIndex()
        elasticsearchClient.index { r ->
            r.index(alphaTypeaheadEsIndex.indexName().asString)
                .id(alphaFixture.id.value)
                .document(AlphaTypeaheadV1EsDoc(id = alphaFixture.id, someString = alphaFixture.someString))
        }
        elasticsearchClient.indices().refresh { r ->
            r.index(alphaTypeaheadEsIndex.indexName().asString)
        }

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(bravoBlotterPage)
        bravoBlotterPage.apply {
            clickAddButton()
            fillCreateForm()
            clickSubmitButton()
            assertCreateDialogClosed()

            clickEditButtonForFirstRow()
            fillEditForm()
            clickSubmitButton()
            assertEditDialogClosed()
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
