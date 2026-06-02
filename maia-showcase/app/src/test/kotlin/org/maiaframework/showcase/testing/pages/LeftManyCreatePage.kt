package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class LeftManyCreatePage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/left-many/create",
    "left_many_create"
) {


    fun fillCreateForm(
        someInt: String = "42",
        someString: String = "testleft",
    ) {
        page.locator("input[name='someInt']").fill(someInt)
        page.locator("input[name='someString']").fill(someString)
    }


    fun searchAndSelectRightEntity(searchTerm: String) {
        page.locator("input[placeholder='Search Right Entities...']").fill(searchTerm)
        val option = page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm))
        option.waitFor()
        option.evaluate("el => el.click()")
        page.locator("mat-chip-row").filter(Locator.FilterOptions().setHasText(searchTerm)).waitFor()
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))
    }


}
