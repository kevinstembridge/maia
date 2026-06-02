package org.maiaframework.showcase.many_to_many

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.elasticsearch.EsDocHolder
import org.maiaframework.elasticsearch.index.EsIndexOps
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.auth.Authority
import org.maiaframework.showcase.testing.fixtures.UserFixture
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


    private val leftAlpha = LeftManyEntityTestBuilder(someString = "left-alpha").build()


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        fixtures.resetDatabaseState()
        rightManyDao.deleteAll()
        leftManyDao.bulkInsert(listOf(leftAlpha))
        esIndexOps.upsert(
            EsDocHolder(
                id = leftAlpha.id.toString(),
                doc = LeftManyTypeaheadV1EsDoc(id = leftAlpha.id, someString = leftAlpha.someString),
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

        // Create: navigate to create page, fill form with someInt + someString + left-entity chip
        rightManyBlotterPage.clickAddButton()

        rightManyCreatePage.apply {

            assertOnPage()
            fillForm(someInt = "42", someString = "testright")
            searchAndSelectLeftEntity("left-alpha")
            clickSubmitButton()

        }

        // After create, Angular navigates to the view page
        rightManyViewPage.assertOnPage()

        `navigate to the`(rightManyBlotterPage)
        rightManyBlotterPage.assertTableContainsValue("testright")

        rightManyBlotterPage.apply {

            // Attempt delete while the join record exists — FK check dialog shows error
            clickDeleteButtonForFirstRow()
            assertFkCheckDialogShowsError()
            dismissFkCheckDialog()

            // Edit: verify chip pre-populated, remove it, change someString
            clickEditButtonForFirstRow()

        }

        rightManyEditPage.apply {

            assertOnPage()
            assertChipVisible("left-alpha")
            removeChip("left-alpha")
            fillForm(someString = "testright_edited")
            clickSubmitButton()

        }

        // After edit, Angular navigates to the view page
        rightManyViewPage.assertOnPage()

        `navigate to the`(rightManyBlotterPage)
        rightManyBlotterPage.assertTableContainsValue("testright_edited")

        rightManyBlotterPage.apply {

            // Cancel delete: FK check passes (no join records), delete dialog appears, cancel
            clickDeleteButtonForFirstRow()
            waitForDeleteDialog()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("testright_edited")

            // Confirm delete
            clickDeleteButtonForFirstRow()
            waitForDeleteDialog()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("testright_edited")

        }

    }


}
