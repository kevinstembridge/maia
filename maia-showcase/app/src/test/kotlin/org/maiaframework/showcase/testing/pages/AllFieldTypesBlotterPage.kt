package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class AllFieldTypesBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/all-field-types-blotter",
    "all_field_types_blotter"
) {


    fun clickAddButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
    }


    fun clickEditButtonForFirstRow() {
        // Scroll to 0 so the edit column (leftmost) stays in the virtual DOM.
        // AllFieldTypes has 30+ columns — scrolling right removes leftmost columns via AG Grid
        // column virtualization. SomeVersioned's few columns all fit, so scroll=99999 works there.
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 0")
        // Wait for a cell with actual content: AG Grid keeps row DOM nodes after reapplyFilters()
        // but sets rowNode.data = undefined immediately. Waiting for someInt (visible at scroll=0)
        // confirms the datasource response has arrived and data is truly loaded.
        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"someInt\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )
        // Click the edit cell — row data is guaranteed to be loaded (rowNode.data is defined)
        val editCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='edit']").first()
        editCell.waitFor()
        editCell.scrollIntoViewIfNeeded()
        editCell.click()
    }


    fun assertTableContainsValue(value: String) {
        // someStringModifiable is at ~2750px; scroll to 1800 so it falls in the 1244px viewport.
        // The scroll and cell check are combined in a single waitForFunction so that the scroll is
        // re-applied on every poll. This handles the race where AG Grid's infinite row model reset
        // (triggered by reapplyFilters()) may reset the horizontal scroll back to 0 between the
        // original evaluate() call and the cell becoming visible.
        page.waitForFunction(
            "() => {" +
            "  const vp = document.querySelector('.ag-center-cols-viewport');" +
            "  if (!vp) return false;" +
            "  vp.scrollLeft = 1800;" +
            "  return Array.from(document.querySelectorAll('.ag-cell'))" +
            "    .some(c => c.innerText && c.innerText.includes('$value'));" +
            "}"
        )
    }


    fun clickDeleteButtonForFirstRow() {
        // Scroll back to 0 first so the someInt column (far left) is in the virtual DOM
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


}
