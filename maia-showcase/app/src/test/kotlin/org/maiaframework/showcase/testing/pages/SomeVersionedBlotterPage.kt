package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class SomeVersionedBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/some-versioned-blotter",
    "some_versioned_blotter"
) {


    fun clickAddButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
    }


    fun clickEditButtonForFirstRow() {
        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"someInt\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 0")
        val editCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='edit']").first()
        editCell.waitFor()
        editCell.scrollIntoViewIfNeeded()
        editCell.click()
    }


    fun clickDeleteButtonForFirstRow() {
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 0")
        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"someInt\"]'); " +
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


    fun assertVersionEquals(expectedVersion: Long) {
        page.waitForFunction(
            "(v) => {" +
            "  const c = document.querySelector('.ag-cell[col-id=\"version\"]');" +
            "  return c && c.innerText && c.innerText.trim() === String(v);" +
            "}",
            expectedVersion.toInt()
        )
    }


    fun assertPagingPanelIsVisible() {
        page.locator(".ag-paging-panel").waitFor()
    }


    fun assertNoPagingPanel() {
        // Wait for rows so the grid has rendered before asserting absence
        page.locator(".ag-row").first().waitFor()
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator(".ag-paging-panel")).isHidden()
    }


    fun assertPagingSummaryEquals(expected: String) {
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator(".ag-paging-row-summary-panel"))
            .containsText(expected)
    }


    fun clickNextPage() {
        page.locator(".ag-paging-button[aria-label='Next Page']").click()
    }


    fun selectPageSize(pageSize: Int) {
        page.locator(".ag-paging-page-size .ag-picker-field-wrapper").click()
        page.locator(".ag-rich-select-list .ag-rich-select-row, .ag-list-item")
            .filter(Locator.FilterOptions().setHasText(pageSize.toString()))
            .first()
            .click()
    }


}
