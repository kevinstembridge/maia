package org.maiaframework.showcase.simple

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.auth.Authority
import org.maiaframework.showcase.testing.fixtures.UserFixture

class SimpleCrudPlaywrightTest : AbstractPlaywrightTest() {


    private lateinit var testUser: UserFixture


    @BeforeAll
    fun setUp() {

        testUser = fixtures.aUser(
            loginMailVerified = true,
            { it.copy(authorities = listOf(Authority.WRITE)) }
        )

        fixtures.resetDatabaseState()

        // Simple entities are not covered by resetDatabaseState(), truncate separately
        jdbcOps.update("truncate maia.simple cascade")

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `edit journey from view page`() {

        `log in user`(testUser)

        `navigate to the`(simpleBlotterPage)

        simpleBlotterPage.clickAddButton()

        simpleCreatePage.apply {
            assertOnPage()
            fillCreateForm(someString = "original-string")
            clickSubmitButton()
        }

        simpleViewPage.apply {
            assertOnPage()
            assertEditButtonIsVisible()
            clickEditButton()
        }

        simpleEditPage.apply {
            assertOnPage()
            fillEditForm(someString = "edited-string")
            clickSubmitButton()
        }

        simpleViewPage.apply {
            assertOnPage()
            assertShowsText("edited-string")
        }

    }


}
