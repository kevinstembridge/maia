package org.maiaframework.showcase.many_to_many

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.elasticsearch.EsDocHolder
import org.maiaframework.elasticsearch.index.EsIndexOps
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.springframework.beans.factory.annotation.Autowired


class RightManyCrudPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var leftManyDao: LeftManyDao


    @Autowired
    private lateinit var rightManyDao: RightManyDao


    @Autowired
    private lateinit var esIndexOps: EsIndexOps


    @Autowired
    private lateinit var leftManyTypeaheadEsIndex: LeftManyTypeaheadEsIndex


    private val left1 = LeftManyEntityTestBuilder(someString = "left-1").build()


    private val left2 = LeftManyEntityTestBuilder(someString = "left-2").build()


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        fixtures.resetDatabaseState()
        rightManyDao.deleteAll()

        leftManyDao.bulkInsert(listOf(left1, left2))

        `upsert to ElasticSearch`(left1)
        `upsert to ElasticSearch`(left2)

    }


    private fun `upsert to ElasticSearch`(leftManyEntity: LeftManyEntity) {

        esIndexOps.upsert(
            EsDocHolder(
                id = leftManyEntity.id.toString(),
                doc = LeftManyTypeaheadV1EsDoc(id = leftManyEntity.id, someString = leftManyEntity.someString),
                indexName = leftManyTypeaheadEsIndex.indexName()
            )
        )

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(rightManyBlotterPage)

        rightManyBlotterPage.clickAddButton()

        rightManyCreatePage.apply {

            assertOnPage()
            `enter form input`(someInt = "42", someString = "testright")
            `click the Add button for Left entities`()
            `select a Left entity in the mini form`("left-1")
            `click to confirm adding the Left entity`()
            `click the Submit button`()

        }

        rightManyViewPage.assertOnPage()

        val rightManyEntityId = page.url().substringAfterLast("/")

        `navigate to the`(rightManyBlotterPage)

        rightManyBlotterPage.apply {

            `assert the table contains value`("testright")

            `click to delete the first row`()
            `assert the FK Check dialog shows an error`()
            `dismiss the FK Check dialog`()

            `click to edit the first row`()

        }

        rightManyEditPage.apply {

            assertOnPage()
            `assert a LeftEntity is visible with name`("left-1")
            `remove the LeftEntity named`("left-1")
            `enter form input`(someString = "testright_edited")
            `click the Submit button`()

        }

        rightManyViewPage.assertOnPage()

        `navigate to the`(rightManyBlotterPage)

        rightManyBlotterPage.apply {

            `assert the table contains value`("testright_edited")

            // Cancel delete: FK check passes (no join records), delete dialog appears, cancel
            `click to delete the first row`()
            `wait for the Delete dialog`()
            `click the Cancel button`()
            `assert the Delete dialog closed`()
            `assert the table contains value`("testright_edited")

            // Confirm delete
            `click to delete the first row`()
            `wait for the Delete dialog`()
            `click the Yes button`()
            `assert the Delete dialog closed`()
            `assert the table does not contain value`("testright_edited")

        }

        rightManyHistoryBlotterPage.apply {

            `navigate to the history page for entity`(rightManyEntityId)
            `assert the table contains a row with`(changeType = "CREATE", someInt = "42", someString = "testright", version = "1")
            `assert the table contains a row with`(changeType = "UPDATE", someInt = "42", someString = "testright_edited", version = "2")
            `assert the table contains a row with`(changeType = "DELETE", someInt = "42", someString = "testright_edited", version = "3")

        }

    }


}
