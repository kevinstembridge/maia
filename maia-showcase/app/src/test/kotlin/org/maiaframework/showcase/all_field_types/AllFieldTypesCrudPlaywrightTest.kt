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
        allFieldTypesBlotterPage.fillCreateForm()
        allFieldTypesBlotterPage.clickSubmitButton()
        allFieldTypesBlotterPage.assertCreateDialogClosed()

        allFieldTypesBlotterPage.clickEditButtonForFirstRow()
        allFieldTypesBlotterPage.fillEditForm()
        allFieldTypesBlotterPage.clickSubmitButton()
        allFieldTypesBlotterPage.assertEditDialogClosed()
        allFieldTypesBlotterPage.assertTableContainsValue("testmodifiable_edited")

        // Cancel path
        allFieldTypesBlotterPage.clickDeleteButtonForFirstRow()
        allFieldTypesBlotterPage.clickCancelButton()
        allFieldTypesBlotterPage.assertDeleteDialogClosed()
        allFieldTypesBlotterPage.assertTableContainsValue("testmodifiable_edited")

        // Confirm delete path
        allFieldTypesBlotterPage.clickDeleteButtonForFirstRow()
        allFieldTypesBlotterPage.clickYesButton()
        allFieldTypesBlotterPage.assertDeleteDialogClosed()
        allFieldTypesBlotterPage.assertTableDoesNotContainValue("testmodifiable_edited")

    }


}
