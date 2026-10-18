package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class PropsDashboardPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/props-dashboard",
    "props_dashboard"
) {


    /**
     * Navigates to the dashboard by clicking its menu item, as a user would.
     */
    fun navigateViaMenu() {

        this.page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Example icon-button with a menu")).click()
        this.page.getByRole(AriaRole.MENUITEM, Page.GetByRoleOptions().setName("Props Dashboard")).click()
        assertOnPage()

    }


    fun card(propertyName: String): Locator = this.page.getByTestId("property-card-$propertyName")


    fun assertCardIsOverridden(propertyName: String, value: String) {

        assertThat(card(propertyName).locator(".badge-overridden")).isVisible()
        assertThat(card(propertyName).locator(".prop-value")).hasText(value)

    }


    fun assertCardIsAbsent(propertyName: String) {

        assertThat(card(propertyName)).hasCount(0)

    }


    fun clickAddOverride() {

        this.page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add override")).click()

    }


    fun clickEdit(propertyName: String) {

        card(propertyName).getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Edit $propertyName")).click()

    }


    fun clickRemove(propertyName: String) {

        card(propertyName).getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Remove $propertyName")).click()

    }


    fun fillOverrideForm(propertyName: String?, value: String, comment: String, reviewDate: String) {

        val dialog = dialog()
        propertyName?.let { dialog.getByLabel("Property name").fill(it) }
        dialog.getByLabel("Value").fill(value)
        dialog.getByLabel("Comment (optional)").fill(comment)
        dialog.getByLabel("Review date (optional)").fill(reviewDate)

    }


    fun assertPropertyNameIsReadOnly() {

        assertThat(dialog().getByLabel("Property name")).not().isEditable()

    }


    fun submitOverrideForm() {

        dialog().getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Submit")).click()
        assertThat(dialog()).not().isVisible()

    }


    fun assertSubmitIsDisabled() {

        assertThat(dialog().getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Submit"))).isDisabled()

    }


    fun cancelDialog() {

        dialog().getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Cancel")).click()
        assertThat(dialog()).not().isVisible()

    }


    fun confirmRemove(comment: String) {

        dialog().getByLabel("Comment (optional)").fill(comment)
        dialog().getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Remove").setExact(true)).click()
        assertThat(dialog()).not().isVisible()

    }


    private fun dialog() = this.page.getByRole(AriaRole.DIALOG)


}
