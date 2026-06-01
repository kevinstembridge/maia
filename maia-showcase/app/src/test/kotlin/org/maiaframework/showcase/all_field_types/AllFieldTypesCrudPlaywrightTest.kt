package org.maiaframework.showcase.all_field_types

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest


class AllFieldTypesCrudPlaywrightTest : AbstractPlaywrightTest() {


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
        `navigate to the`(allFieldTypesBlotterPage)

        allFieldTypesBlotterPage.clickAddButton()

        allFieldTypesCreatePage.apply {
            assertOnPage()
            fillCreateForm()
            clickSubmitButton()
        }

        allFieldTypesViewPage.apply {
            assertOnPage()
            clickEditButton()
        }

        allFieldTypesEditPage.apply {
            assertOnPage()
            fillEditForm()
            clickSubmitButton()
        }

        allFieldTypesViewPage.assertOnPage()

        `navigate to the`(allFieldTypesBlotterPage)

        allFieldTypesBlotterPage.apply {
            assertTableContainsValue("testmodifiable_edited")

            // Cancel path
            clickDeleteButtonForFirstRow()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("testmodifiable_edited")

            // Confirm delete path
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("testmodifiable_edited")
        }

    }


}
