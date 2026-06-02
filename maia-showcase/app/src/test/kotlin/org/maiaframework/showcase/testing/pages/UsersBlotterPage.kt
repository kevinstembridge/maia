package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class UsersBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/user-blotter",
    "user_blotter"
) {


    fun clickAddButton() {
        `click the button named`("Add")
    }


    private fun `click the button named`(buttonName: String) {
        `get the button named`(buttonName).click()
    }


    private fun `get the button named`(buttonName: String): Locator {
        return page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(buttonName))
            ?: throw RuntimeException("Could not find button named '$buttonName'")
    }


    fun clickEditButtonForRow(displayName: String) {

        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"displayName\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )

        val rowIndex = page.evaluate(
            """() => {
                const rows = document.querySelectorAll('.ag-row:not(.ag-row-loading)');
                for (const row of rows) {
                    const cell = row.querySelector('.ag-cell[col-id="displayName"]');
                    if (cell && cell.innerText.includes('$displayName')) {
                        return row.getAttribute('row-index');
                    }
                }
                return null;
            }"""
        ) as String?

        requireNotNull(rowIndex) { "Could not find row for displayName: $displayName" }
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val editCell = page.locator(".ag-row[row-index='$rowIndex'] .ag-cell[col-id='edit']")
        editCell.waitFor()
        editCell.scrollIntoViewIfNeeded()
        editCell.click()

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
