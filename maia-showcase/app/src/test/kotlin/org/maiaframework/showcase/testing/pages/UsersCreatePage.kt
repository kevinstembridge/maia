package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class UsersCreatePage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/user/create",
    "user_create"
) {


    fun fillCreateForm(
        firstName: String,
        lastName: String,
        vararg authorities: String
    ) {
        authorities.forEach { selectAuthority(it) }
        page.locator("input[name='firstName']").fill(firstName)
        page.locator("input[name='lastName']").fill(lastName)
        page.locator("input[name='lastName']").press("Tab")
        page.mouse().move(0.0, 0.0)
        Thread.sleep(1000)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))
    }


    fun assertShowsError() {
        page.locator(".alert").waitFor()
    }


    fun clickCancelButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cancel")).click()
    }


    private fun selectAuthority(authority: String) {
        page.locator("mat-select[formcontrolname='authorities']").click()
        page.locator("mat-option").filter(Locator.FilterOptions().setHasText(authority)).click()
        page.mouse().move(0.0, 0.0)
        page.evaluate("document.querySelector('.cdk-overlay-transparent-backdrop')?.click()")
        page.locator("mat-option").first().waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )
    }


}
