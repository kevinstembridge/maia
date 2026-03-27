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
    fun `users crud journey`() {

        `log in user`(sysAdminUser)
        `navigate to the`(usersBlotterPage)

        usersBlotterPage.apply {


            // Table loads and displays data
            assertTableContainsValue(sysAdminUser.displayName)
            assertTableContainsValue(Authority.SYS__ADMIN.name)

            // Add dialog opens and can be cancelled
            clickAddButton()
            clickCancelButton()
            assertCreateDialogClosed()

            // Edit flow: open dialog, change firstName, submit
            clickEditButtonForFirstRow()
            fillEditForm(firstName = "EditedFirst")
            clickSubmitButton()
            assertEditDialogClosed()
            assertTableContainsValue("EditedFirst")

        }

    }


}
