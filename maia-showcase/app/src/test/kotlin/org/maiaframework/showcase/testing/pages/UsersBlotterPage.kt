package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class UsersBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/users",
    "users_blotter"
) {


    fun clickAddButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
        page.locator("mat-dialog-container").waitFor()
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
    }


    fun clickCancelButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cancel")).click()
    }


    fun assertCreateDialogClosed() {
        assertEditDialogClosed()
    }


    fun assertEditDialogClosed() {
        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )
    }


    fun clickEditButtonForFirstRow() {
        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"displayName\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val editCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='edit']").first()
        editCell.waitFor()
        editCell.scrollIntoViewIfNeeded()
        editCell.click()
        page.locator("mat-dialog-container").waitFor()
    }


    fun fillEditForm(
        firstName: String = "EditedFirst"
    ) {
        // Wait for the loading spinner to disappear and the form to be ready
        page.locator("mat-spinner").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )
        // Wait for firstName input to be visible
        page.locator("input[name='firstName']").waitFor()
        page.locator("input[name='firstName']").fill(firstName)
        // Touch lastName to trigger Angular validation on required field (updateOn: 'change')
        val existingLastName = page.locator("input[name='lastName']").inputValue()
        page.locator("input[name='lastName']").fill(existingLastName)
        // Press Tab to blur lastName and trigger change event, ensuring Angular processes the value
        page.locator("input[name='lastName']").press("Tab")
        // Wait for async validators (debounced ~300ms)
        Thread.sleep(1000)
    }


    fun assertTableContainsValue(value: String) {
        page.waitForFunction(
            "() => {" +
            "  if (document.querySelector('.ag-row-loading')) return false;" +
            "  const vp = document.querySelector('.ag-center-cols-viewport');" +
            "  if (vp) vp.scrollLeft = 0;" +
            "  return Array.from(document.querySelectorAll('.ag-cell'))" +
            "    .some(c => c.innerText && c.innerText.includes('$value'));" +
            "}"
        )
    }


}
