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

        homePage.tryToNavigateToMe()
        Thread.sleep(300)
        val logoutLink = page.getByText("Logout")

        if (logoutLink.isVisible) {
            logoutLink.click()
            Thread.sleep(300)
        }

    }


    @Test
    fun `user with edit permission can navigate to the all field types page`() {

        `log in as admin user`()
        `navigate to the`(allFieldTypesBlotterPage)

    }


    @Test
    fun `user can create an all field types record`() {

        `log in as admin user`()
        `navigate to the`(allFieldTypesBlotterPage)
        allFieldTypesBlotterPage.clickAddButton()
        allFieldTypesBlotterPage.fillCreateForm()
        allFieldTypesBlotterPage.clickSubmitButton()
        allFieldTypesBlotterPage.assertCreateDialogClosed()

    }


}
