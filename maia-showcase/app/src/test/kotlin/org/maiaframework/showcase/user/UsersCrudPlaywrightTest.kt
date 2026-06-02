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
            { it.copy(authorities = listOf(Authority.WRITE)) }
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

        // Table loads and displays data for both users
        usersBlotterPage.apply {
            assertTableContainsValue(sysAdminUser.displayName)
            assertTableContainsValue(Authority.WRITE.name)
            assertTableContainsValue(anotherUser.displayName)
            assertTableContainsValue(Authority.READ.name)
        }

        // Add: fill the form and submit — backend rejects because encryptedPassword is notCreatableByUser
        usersBlotterPage.clickAddButton()

        usersCreatePage.apply {
            assertOnPage()
            fillCreateForm("NewFirst", "NewLast", Authority.WRITE.name)
            clickSubmitButton()
            assertShowsError()
            clickCancelButton()
        }

        // Edit: navigate to edit page for anotherUser, add WRITE alongside READ, submit
        usersBlotterPage.clickEditButtonForRow(anotherUser.displayName)

        usersEditPage.apply {
            assertOnPage()
            fillEditForm(firstName = "EditedFirst", additionalAuthorities = listOf(Authority.WRITE.name))
            clickSubmitButton()
        }

        usersViewPage.assertOnPage()

        `navigate to the`(usersBlotterPage)

        usersBlotterPage.apply {
            assertTableContainsValue("EditedFirst")
            assertTableContainsValue("${Authority.READ.name}, ${Authority.WRITE.name}")
        }

    }


}
