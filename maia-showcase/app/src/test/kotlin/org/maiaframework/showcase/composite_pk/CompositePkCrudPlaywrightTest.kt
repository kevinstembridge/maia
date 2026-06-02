package org.maiaframework.showcase.composite_pk

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest

class CompositePkCrudPlaywrightTest : AbstractPlaywrightTest() {


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        fixtures.resetDatabaseState()

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(compositePkBlotterPage)

        // Create
        compositePkBlotterPage.clickAddButton()

        compositePkCreatePage.apply {
            assertOnPage()
            fillCreateForm(someString = "abc", someInt = "1", someModifiableString = "initial")
            clickSubmitButton()
        }

        compositePkViewPage.assertOnPage()

        `navigate to the`(compositePkBlotterPage)
        compositePkBlotterPage.assertVersionEquals(1L)

        // Edit
        compositePkBlotterPage.clickEditButtonForFirstRow()

        compositePkEditPage.apply {
            assertOnPage()
            fillEditForm(someModifiableString = "edited")
            clickSubmitButton()
        }

        compositePkViewPage.assertOnPage()

        `navigate to the`(compositePkBlotterPage)

        compositePkBlotterPage.apply {
            assertTableContainsValue("edited")
            assertVersionEquals(2L)

            // Cancel delete
            clickDeleteButtonForFirstRow()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("edited")
            assertVersionEquals(2L)

            // Confirm delete
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("edited")
        }

    }


}
