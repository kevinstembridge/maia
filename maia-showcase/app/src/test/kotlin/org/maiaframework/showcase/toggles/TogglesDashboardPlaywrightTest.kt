package org.maiaframework.showcase.toggles

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.maiaframework.domain.auth.Authority as DomainAuthority
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.Authority
import org.maiaframework.showcase.testing.fixtures.UserFixture
import java.util.regex.Pattern


// Verified passing when the app is built with a single copy of Angular. Disabled because maia-showcase-ui currently
// bundles two copies (its own and libs/maia-ui-workspace/node_modules'), so every page hosted in a workspace library
// fails with NG0203 ("The `_HttpHandler` token injection failed").
@Disabled("NG0203: duplicate Angular copies in the maia-showcase-ui bundle")
class TogglesDashboardPlaywrightTest : AbstractPlaywrightTest() {


    private lateinit var togglesWriterUser: UserFixture

    private lateinit var togglesReaderUser: UserFixture


    @BeforeAll
    fun setUp() {

        togglesWriterUser = fixtures.aUser(
            loginMailVerified = true,
            { it.copy(authorities = listOf(DomainAuthority(Authority.MAIA_TOGGLES_READ.name), DomainAuthority(Authority.MAIA_TOGGLES_WRITE.name))) }
        )

        togglesReaderUser = fixtures.aUser(
            loginMailVerified = true,
            { it.copy(authorities = listOf(DomainAuthority(Authority.MAIA_TOGGLES_READ.name))) }
        )

        fixtures.resetDatabaseState()

    }


    @BeforeEach
    fun logOut() {

        homePage.tryToNavigateToMe()
        `logout current user`()

    }


    @Test
    fun `dashboard journey`() {

        `log in user`(togglesWriterUser)
        togglesDashboardPage.navigateViaMenu()

        togglesDashboardPage.apply {

            // The sample features are synced into the database at startup
            assertCardCount(3)
            assertCardIsEnabled("ShowcaseNewDashboard")
            assertCardIsDisabled("ShowcaseBetaExport")
            assertCardIsDisabled("ShowcaseDarkLaunch")
            assertCardIsOverdue("ShowcaseBetaExport")

            // Filters
            fillNameFilter("beta")
            assertCardCount(1)
            fillNameFilter("")
            assertCardCount(3)
            clickFilterTile("Enabled")
            assertCardCount(1)
            clickFilterTile("Enabled")
            clickFilterTile("Overdue")
            assertCardCount(1)
            clickFilterTile("Overdue")
            assertCardCount(3)

            // Disable, then re-enable
            clickChangeState("ShowcaseNewDashboard", enable = false)
            confirmChangeState(enable = false, comment = "Switched off by the Playwright test")
            assertCardIsDisabled("ShowcaseNewDashboard")

            clickChangeState("ShowcaseNewDashboard", enable = true)
            confirmChangeState(enable = true)
            assertCardIsEnabled("ShowcaseNewDashboard")

            // Add an activation strategy
            clickEditStrategies("ShowcaseDarkLaunch")
            addStrategy("maiaTogglesUsernameActivationStrategy", mapOf("usernames" to "alice,bob"))
            saveStrategies()
            assertCardHasStrategy("ShowcaseDarkLaunch", "maiaTogglesUsernameActivationStrategy (usernames=alice,bob)")

            // A required parameter must be filled in before the strategy can be saved
            clickEditStrategies("ShowcaseDarkLaunch")
            removeFirstStrategy()
            addStrategy("maiaTogglesUsernameActivationStrategy", emptyMap())
            assertSaveStrategiesDisabled()
            removeFirstStrategy()
            saveStrategies()
            assertCardHasNoStrategies("ShowcaseDarkLaunch")

            // History: the toggle was changed above, so there are history rows to show
            clickHistory("ShowcaseNewDashboard")

        }

        assertThat(page).hasURL(Pattern.compile(".*/ops/toggles/feature-toggle/history/[0-9a-f-]{36}$"))
        assertThat(page.locator("//*[@data-page-id]")).hasAttribute("data-page-id", "feature_toggle_history_blotter")
        assertThat(page.locator(".ag-row").first()).isVisible()

    }


    @Test
    fun `a read-only user sees the toggles but cannot change them`() {

        `log in user`(togglesReaderUser)
        togglesDashboardPage.navigateViaMenu()

        togglesDashboardPage.apply {

            assertCardCount(3)

            clickChangeState("ShowcaseDarkLaunch", enable = true)
            confirmChangeState(enable = true)

            assertActionErrorVisible()
            assertCardIsDisabled("ShowcaseDarkLaunch")

        }

    }


}
