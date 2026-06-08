package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class RightManyEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/right-many/edit",
    "right_many_edit"
) {


    fun `assert a LeftEntity is visible with name`(entityName: String) {

        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .waitFor()

    }


    fun `remove the LeftEntity named`(entityName: String) {

        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .locator("button[type='button']")
            .click()
        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .waitFor(Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN))

    }


    fun `enter form input`(
        someString: String = "testright_edited",
    ) {

        page.locator("input[name='someString']").fill(someString)

    }


    fun `click the Submit button`() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))

    }


}
