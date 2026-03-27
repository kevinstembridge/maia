package org.maiaframework.showcase.user

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.auth.Authority
import org.maiaframework.showcase.testing.fixtures.UserFixture

class UsersCrudPlaywrightTest : AbstractPlaywrightTest() {


    private lateinit var sysAdminUser: UserFixture


    private lateinit var anotherUser: UserFixture


    @BeforeAll
    fun setUp() {

        // SYS__ADMIN required to create/edit users; initAdminUserFixture() uses WRITE authority
        sysAdminUser = fixtures.aUser(
            loginMailVerified = true,
            { it.copy(authorities = listOf(Authority.SYS__ADMIN)) }
        )

        anotherUser = fixtures.aUser(
            loginMailVerified = true,
            { it.copy(authorities = listOf(Authority.READ)) }
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

            // Table loads and displays data for both users
            assertTableContainsValue(sysAdminUser.displayName)
            assertTableContainsValue(Authority.SYS__ADMIN.name)
            assertTableContainsValue(anotherUser.displayName)
            assertTableContainsValue(Authority.READ.name)

            // Add: fill the form and submit — backend rejects because encryptedPassword is notCreatableByUser
            clickAddButton()
            fillCreateForm("NewFirst", "NewLast", Authority.WRITE.name)
            clickSubmitButton()
            assertDialogShowsError()
            clickCancelButton()
            assertCreateDialogClosed()

            // Edit: open dialog for anotherUser, add WRITE alongside READ, submit
            clickEditButtonForRow(anotherUser.displayName)
            fillEditForm(firstName = "EditedFirst", additionalAuthorities = listOf(Authority.WRITE.name))
            clickSubmitButton()
            assertEditDialogClosed()
            assertTableContainsValue("EditedFirst")
            assertTableContainsValue("${Authority.READ.name}, ${Authority.WRITE.name}")

        }

    }


}
