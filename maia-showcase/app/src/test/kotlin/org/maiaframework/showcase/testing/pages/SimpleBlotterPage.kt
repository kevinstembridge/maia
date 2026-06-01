package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class SimpleBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/simple-blotter",
    "simple_blotter"
) {


    fun clickAddButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
    }


    fun clickViewButtonForFirstRow() {
        // Simple blotter has few columns (view, edit, someString, createdTimestampUtc, delete)
        // — all fit in the viewport so no horizontal scrolling needed.
        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"someString\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )
        val viewCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='view']").first()
        viewCell.waitFor()
        viewCell.click()
    }


}
