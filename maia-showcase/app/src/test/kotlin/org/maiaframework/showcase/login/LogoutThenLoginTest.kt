package org.maiaframework.showcase.login

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.testing.fixtures.UserFixture

class LogoutThenLoginTest : AbstractPlaywrightTest() {


    private lateinit var user: UserFixture


    @BeforeAll
    fun setUp() {

        user = fixtures.aUser()
        fixtures.resetDatabaseState()

    }


    @Test
    fun `user can log back in immediately after logging out without a full page reload`() {

        `log in user`(user)
        `logout current user`()

        // Follow the in-app "Login" link (client-side route change, no full page reload,
        // so APP_INITIALIZER's CSRF fetch does not run again here).
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Login")).click()

        loginPage.submitForm(
            user.emailAddressEntity.emailAddress,
            user.rawPassword
        )

        homePage.assertOnPage()

    }


}
