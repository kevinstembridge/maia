package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class TogglesDashboardPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/toggles-dashboard",
    "toggles_dashboard"
) {


    /**
     * Navigates to the dashboard by clicking its menu item, as a user would.
     */
    fun navigateViaMenu() {

        this.page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Example icon-button with a menu")).click()
        this.page.getByRole(AriaRole.MENUITEM, Page.GetByRoleOptions().setName("Feature Toggles")).click()
        assertOnPage()

    }


    fun card(featureName: String): Locator = this.page.getByTestId("toggle-card-$featureName")


    fun assertCardCount(expected: Int) {

        assertThat(this.page.locator("[data-testid^='toggle-card-']")).hasCount(expected)

    }


    fun assertCardIsEnabled(featureName: String) {

        assertThat(card(featureName).locator(".badge-enabled")).isVisible()

    }


    fun assertCardIsDisabled(featureName: String) {

        assertThat(card(featureName).locator(".badge-disabled")).isVisible()

    }


    fun assertCardIsOverdue(featureName: String) {

        assertThat(card(featureName).locator(".badge-overdue")).isVisible()

    }


    fun assertCardHasStrategy(featureName: String, summary: String) {

        assertThat(card(featureName).locator(".strategy-chip")).hasText(summary)

    }


    fun assertCardHasNoStrategies(featureName: String) {

        assertThat(card(featureName).locator(".strategy-chip")).hasCount(0)

    }


    fun clickFilterTile(label: String) {

        this.page.locator("button.toggles-filter-tile", Page.LocatorOptions().setHasText(label)).click()

    }


    fun fillNameFilter(text: String) {

        this.page.getByLabel("Filter by name").fill(text)

    }


    fun clickChangeState(featureName: String, enable: Boolean) {

        card(featureName).getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("${if (enable) "Enable" else "Disable"} $featureName")).click()

    }


    fun confirmChangeState(enable: Boolean, comment: String? = null) {

        val dialog = this.page.getByRole(AriaRole.DIALOG)
        comment?.let { dialog.getByLabel("Comment (optional)").fill(it) }
        dialog.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName(if (enable) "Enable" else "Disable").setExact(true)).click()
        assertThat(dialog).not().isVisible()

    }


    fun clickEditStrategies(featureName: String) {

        card(featureName).getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Edit strategies for $featureName")).click()

    }


    fun clickHistory(featureName: String) {

        card(featureName).getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("History $featureName")).click()

    }


    fun addStrategy(strategyId: String, parameters: Map<String, String>) {

        val dialog = this.page.getByRole(AriaRole.DIALOG)
        dialog.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Add strategy")).click()

        val row = dialog.locator("[data-testid^='strategy-row-']").last()
        row.getByRole(AriaRole.COMBOBOX).click()
        this.page.getByRole(AriaRole.OPTION, Page.GetByRoleOptions().setName(strategyId).setExact(true)).click()

        parameters.forEach { (name, value) -> row.getByLabel(name).fill(value) }

    }


    fun removeFirstStrategy() {

        val dialog = this.page.getByRole(AriaRole.DIALOG)
        dialog.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Remove strategy")).first().click()

    }


    fun assertSaveStrategiesDisabled() {

        assertThat(this.page.getByRole(AriaRole.DIALOG).getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Save"))).isDisabled()

    }


    fun saveStrategies() {

        val dialog = this.page.getByRole(AriaRole.DIALOG)
        dialog.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Save")).click()
        assertThat(dialog).not().isVisible()

    }


    fun assertActionErrorVisible() {

        assertThat(this.page.getByTestId("toggles-action-error")).isVisible()

    }


}
