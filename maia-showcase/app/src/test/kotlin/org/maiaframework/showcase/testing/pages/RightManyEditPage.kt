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


    fun `set the effectiveFrom for the LeftEntity named`(entityName: String, date: String, time: String) {

        val joinEntry = page.locator(".join-entry").filter(Locator.FilterOptions().setHasText(entityName))
        joinEntry.locator("input.join-effective-from-date").fill(date)
        joinEntry.locator("input.join-effective-from-time").fill(time)
        joinEntry.locator("input.join-effective-from-time").press("Tab")

    }


    fun `click the Add button for Left entities`() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add Left Entities"))
            .click()

    }


    fun `select a Left entity in the mini form`(searchTerm: String) {

        val input = page.locator("input[placeholder='Search Left Entities...']")
        input.waitFor()
        input.fill(searchTerm)
        val option = page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm))
        option.waitFor()
        option.click()

    }


    fun `click to confirm adding the Left entity`() {

        page.locator(".join-mini-form button[type='button']").filter(Locator.FilterOptions().setHasText("Add"))
            .click()

    }


    fun `remove the LeftEntity named`(entityName: String) {

        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .locator("button.join-remove-button")
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
