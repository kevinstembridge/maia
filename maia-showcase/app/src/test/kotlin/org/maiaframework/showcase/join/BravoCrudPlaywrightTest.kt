package org.maiaframework.showcase.join

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.auth.Authority
import org.maiaframework.showcase.testing.fixtures.UserFixture
import org.springframework.beans.factory.annotation.Autowired


class BravoCrudPlaywrightTest : AbstractPlaywrightTest() {


    private lateinit var testUser: UserFixture


    @Autowired
    private lateinit var elasticsearchClient: ElasticsearchClient

    @Autowired
    private lateinit var alphaTypeaheadEsIndex: AlphaTypeaheadEsIndex

    @Autowired
    private lateinit var alphaTypeaheadEsIndexControl: AlphaTypeaheadEsIndexControl_v0001


    @BeforeAll
    fun setUp() {

        testUser = fixtures.aUser(
            loginMailVerified = true,
            { it.copy(authorities = listOf(Authority.WRITE)) }
        )
        val alphaFixture = fixtures.anAlpha(someString = "alpha-fixture")
        fixtures.resetDatabaseState()

        // Create the Elasticsearch index and index the alpha fixture so the typeahead returns results
        val indexName = alphaTypeaheadEsIndex.indexName().asString

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
    fun `crud journey`() {

        `log in user`(testUser)
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
