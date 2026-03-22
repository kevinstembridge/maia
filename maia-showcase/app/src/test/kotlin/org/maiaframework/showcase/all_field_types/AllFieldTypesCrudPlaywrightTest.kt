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
    fun `user can create an all field types record`() {

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

    }


}
