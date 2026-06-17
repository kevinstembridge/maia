package org.maiaframework.showcase.user

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.Authority
import org.maiaframework.showcase.testing.fixtures.UserFixture

class UsersCrudPlaywrightTest : AbstractPlaywrightTest() {


    private lateinit var anotherUser: UserFixture


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        anotherUser = fixtures.aUser(
            loginMailVerified = true,
            { it.copy(authorities = listOf(org.maiaframework.domain.auth.Authority(Authority.READ.name))) }
        )

        fixtures.resetDatabaseState()

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `users crud journey`() {

        `log in as admin user`()
        `navigate to the`(usersBlotterPage)

        // Table loads and displays data for both users
        usersBlotterPage.apply {
            assertTableContainsValue(adminUser.displayName)
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
