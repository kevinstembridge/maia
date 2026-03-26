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
        compositePkBlotterPage.apply {

            // Create
            clickAddButton()
            fillCreateForm(someString = "abc", someInt = "1", someModifiableString = "initial")
            clickSubmitButton()
            assertCreateDialogClosed()
            assertVersionEquals(1L)

            // Edit
            clickEditButtonForFirstRow()
            fillEditForm(someModifiableString = "edited")
            clickSubmitButton()
            assertEditDialogClosed()
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
