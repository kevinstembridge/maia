package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class BravoEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/bravo/edit",
    "bravo_edit"
) {


    fun fillEditForm(
        someString: String = "testbravo_edited",
    ) {
        page.waitForFunction("() => (document.querySelector('input[name=\"someString\"]')?.value ?? '') !== ''")
        page.locator("input[name='someString']").fill(someString)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))
    }


}
