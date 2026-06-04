package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class RightManyCreatePage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/right-many/create",
    "right_many_create"
) {


    fun fillForm(
        someInt: String = "42",
        someString: String = "testright",
    ) {

        page.locator("input[name='someInt']").fill(someInt)
        page.locator("input[name='someString']").fill(someString)

    }


    fun clickAddLeftEntityButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add Left Entities"))
            .click()

    }


    fun searchAndSelectLeftEntityInMiniForm(searchTerm: String) {

        val input = page.locator("input[placeholder='Search Left Entities...']")
        input.waitFor()
        input.fill(searchTerm)
        val option = page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm))
        option.waitFor()
        input.press("ArrowDown")
        input.press("Enter")

    }


    fun clickConfirmAddInMiniForm() {

        page.locator(".join-mini-form button[type='button']").filter(Locator.FilterOptions().setHasText("Add"))
            .click()

    }


    fun clickSubmitButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))

    }


}
