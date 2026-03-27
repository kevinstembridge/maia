package org.maiaframework.showcase.user

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.auth.Authority
import org.maiaframework.showcase.testing.fixtures.UserFixture

class UsersCrudPlaywrightTest : AbstractPlaywrightTest() {


    private lateinit var sysAdminUser: UserFixture


    @BeforeAll
    fun setUp() {

        // SYS__ADMIN required to create/edit users; initAdminUserFixture() uses WRITE authority
        sysAdminUser = fixtures.aUser(
            loginMailVerified = true,
            { it.copy(authorities = listOf(Authority.SYS__ADMIN)) }
        )

        fixtures.resetDatabaseState()

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `users blotter journey`() {

        `log in user`(sysAdminUser)
        `navigate to the`(usersBlotterPage)

        // Table loads and displays data
        usersBlotterPage.assertTableContainsValue(sysAdminUser.displayName)

        // Add dialog opens and can be cancelled
        usersBlotterPage.clickAddButton()
        usersBlotterPage.clickCancelButton()
        usersBlotterPage.assertCreateDialogClosed()

        // Edit flow: open dialog, change firstName, submit
        usersBlotterPage.clickEditButtonForFirstRow()
        usersBlotterPage.fillEditForm(firstName = "EditedFirst")
        usersBlotterPage.clickSubmitButton()
        usersBlotterPage.assertEditDialogClosed()
        usersBlotterPage.assertTableContainsValue("EditedFirst")

    }


}
