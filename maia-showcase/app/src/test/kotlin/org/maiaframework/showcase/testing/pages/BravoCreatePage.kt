package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class BravoCreatePage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/bravo/create",
    "bravo_create"
) {


    fun fillCreateForm(
        alphaSearchTerm: String = "alpha-fixture",
        someInt: String = "42",
        someString: String = "testbravo",
    ) {
        selectAlpha(alphaSearchTerm)
        page.locator("input[name='someInt']").fill(someInt)
        page.locator("input[name='someString']").fill(someString)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))
    }


    private fun selectAlpha(searchTerm: String) {
        page.locator("input[formcontrolname='alpha']").fill(searchTerm)
        Thread.sleep(500)
        page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm)).waitFor()
        page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm)).click()
    }


}
