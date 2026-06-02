package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class UserGroupMembershipBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/user-group-membership-blotter",
    "user_group_membership_blotter"
) {


    fun clickAddButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
    }


    fun clickEditButtonForFirstRow() {

        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"userDisplayName\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )

        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 0")
        val editCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='edit']").first()
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
