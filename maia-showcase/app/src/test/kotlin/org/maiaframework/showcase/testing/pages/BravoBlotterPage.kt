package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class BravoBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/bravo",
    "bravo_blotter"
) {


    fun clickAddButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
        page.locator("mat-dialog-container").waitFor()
    }


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


    fun assertCreateDialogClosed() {
        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )
    }


    fun clickEditButtonForFirstRow() {
        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"dtoStringFromBravo\"]'); " +
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
        someString: String = "testbravo_edited",
    ) {
        page.locator("input[name='someString']").fill(someString)
    }


    fun assertEditDialogClosed() {
        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )
    }


    fun assertTableContainsValue(value: String) {
        page.waitForFunction(
            "() => {" +
            "  const vp = document.querySelector('.ag-center-cols-viewport');" +
            "  if (!vp) return false;" +
            "  vp.scrollLeft = 0;" +
            "  return Array.from(document.querySelectorAll('.ag-cell'))" +
            "    .some(c => c.innerText && c.innerText.includes('$value'));" +
            "}"
        )
    }


    fun clickDeleteButtonForFirstRow() {
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 0")
        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"dtoStringFromBravo\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val deleteCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='delete']").first()
        deleteCell.waitFor()
        deleteCell.scrollIntoViewIfNeeded()
        deleteCell.click()
        page.locator("mat-dialog-container").waitFor()
    }


    fun clickYesButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Yes")).click()
    }


    fun clickCancelButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cancel")).click()
    }


    fun assertDeleteDialogClosed() {
        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )
    }


    fun assertTableDoesNotContainValue(value: String) {
        page.waitForFunction(
            "(value) => {" +
            "  if (document.querySelector('.ag-overlay-no-rows-center')) return true;" +
            "  if (document.querySelector('.ag-row-loading')) return false;" +
            "  return !Array.from(document.querySelectorAll('.ag-cell'))" +
            "    .some(c => c.innerText && c.innerText.includes(value));" +
            "}",
            value
        )
    }


    private fun selectAlpha(searchTerm: String) {
        page.locator("input[formcontrolname='alpha']").fill(searchTerm)
        Thread.sleep(500)
        page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm)).waitFor()
        page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm)).click()
    }


}
