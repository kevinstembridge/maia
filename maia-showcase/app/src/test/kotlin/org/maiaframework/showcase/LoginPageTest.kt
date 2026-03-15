package org.maiaframework.showcase

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.testing.fixtures.UserFixture


class LoginPageTest : AbstractPlaywrightTest() {


    private lateinit var user: UserFixture


    @BeforeAll
    fun setUp() {

        user = fixtures.aUser()
        fixtures.resetDatabaseState()

    }


    @Test
    fun `user can navigate to the login page and submit the form`() {

        loginPage.navigateToMe()

        Thread.sleep(300)

        loginPage.submitForm(
            user.emailAddressEntity.emailAddress,
            user.rawPassword
        )

        homePage.assertOnPage()

    }


}
