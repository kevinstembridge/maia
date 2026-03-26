package org.maiaframework.showcase.versioned

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest

class SomeVersionedCrudPlaywrightTest : AbstractPlaywrightTest() {


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
        `navigate to the`(someVersionedBlotterPage)
        someVersionedBlotterPage.apply {

            // Create
            clickAddButton()
            fillCreateForm(someString = "hello", someInt = "1")
            clickSubmitButton()
            assertCreateDialogClosed()
            assertVersionEquals(1L)

            // Edit
            clickEditButtonForFirstRow()
            fillEditForm(someString = "hello_edited", someInt = "1")
            clickSubmitButton()
            assertEditDialogClosed()
            assertTableContainsValue("hello_edited")
            assertVersionEquals(2L)

            // Cancel delete
            clickDeleteButtonForFirstRow()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("hello_edited")
            assertVersionEquals(2L)

            // Confirm delete
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("hello_edited")

        }

    }


}
