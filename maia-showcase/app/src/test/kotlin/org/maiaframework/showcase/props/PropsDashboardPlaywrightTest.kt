package org.maiaframework.showcase.props

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.domain.auth.Authority as DomainAuthority
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.Authority
import org.maiaframework.showcase.testing.fixtures.UserFixture


class PropsDashboardPlaywrightTest : AbstractPlaywrightTest() {


    private lateinit var propsWriterUser: UserFixture


    @BeforeAll
    fun setUp() {

        propsWriterUser = fixtures.aUser(
            loginMailVerified = true,
            { it.copy(authorities = listOf(DomainAuthority(Authority.MAIA_PROPS_READ.name), DomainAuthority(Authority.MAIA_PROPS_WRITE.name))) }
        )

        fixtures.resetDatabaseState()

    }


    @BeforeEach
    fun logOut() {

        homePage.tryToNavigateToMe()
        `logout current user`()

    }


    @Test
    fun `override journey`() {

        `log in user`(propsWriterUser)
        propsDashboardPage.navigateViaMenu()

        propsDashboardPage.apply {

            val propertyName = "playwright.test.property"

            // A property needs a name and a value before it can be submitted
            clickAddOverride()
            assertSubmitIsDisabled()
            cancelDialog()

            clickAddOverride()
            fillOverrideForm(propertyName, value = "first", comment = "Added by the Playwright test", reviewDate = "01/15/2030")
            submitOverrideForm()
            assertCardIsOverridden(propertyName, "first")

            // The name of an existing override can't be changed
            clickEdit(propertyName)
            assertPropertyNameIsReadOnly()
            fillOverrideForm(propertyName = null, value = "second", comment = "Edited by the Playwright test", reviewDate = "01/16/2030")
            submitOverrideForm()
            assertCardIsOverridden(propertyName, "second")

            clickRemove(propertyName)
            confirmRemove("Removed by the Playwright test")
            assertCardIsAbsent(propertyName)

        }

    }


}
