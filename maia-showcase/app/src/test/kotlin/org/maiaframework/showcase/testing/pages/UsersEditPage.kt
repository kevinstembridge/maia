package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import org.maiaframework.webtesting.PageExtensions.inputForField
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper
import java.util.regex.Pattern


class UsersEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/user/edit",
    "user_edit"
) {


    fun fillEditForm(
        firstName: String = "EditedFirst",
        additionalAuthorities: List<String> = emptyList(),
    ) {
        // Wait for fetchForEdit() to populate the form — lastName is required so it will be non-empty
        // once patchValue() has run. The form template has no spinner, so we poll the DOM directly.
        page.waitForFunction("() => (document.querySelector('input[name=\"lastName\"], input[name$=\".lastName\"]')?.value ?? '') !== ''")
        page.inputForField("firstName").waitFor()
        page.inputForField("firstName").fill(firstName)
        val existingLastName = page.inputForField("lastName").inputValue()
        page.inputForField("lastName").fill(existingLastName)
        page.inputForField("lastName").press("Tab")
        additionalAuthorities.forEach { selectAuthority(it) }
        page.mouse().move(0.0, 0.0)
        Thread.sleep(1000)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))
    }


    private fun selectAuthority(authority: String) {
        page.locator("mat-select[formcontrolname='authorities']").click()
        page.locator("mat-option").filter(Locator.FilterOptions().setHasText(Pattern.compile("^${authority}$"))).click()
        page.mouse().move(0.0, 0.0)
        page.evaluate("document.querySelector('.cdk-overlay-transparent-backdrop')?.click()")
        page.locator("mat-option").first().waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )
    }


}
