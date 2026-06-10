package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class LeftManyEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/left-many/edit",
    "left_many_edit"
) {


    fun assertJoinEntryVisible(entityName: String) {

        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .waitFor()

    }


    fun removeJoinEntry(entityName: String) {

        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .locator("button.join-remove-button")
            .click()
        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .waitFor(Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN))

    }


    fun fillEditForm(
        someString: String = "testleft_edited",
    ) {

        page.locator("input[name='someString']").fill(someString)

    }


    fun clickSubmitButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))

    }


}
