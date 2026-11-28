package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


/** The Blotter over the Hazelcast-backed DeltaHz Searchable DTO. */
class DeltaHzBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/delta-hz-blotter",
    "delta_hz_blotter"
) {


    /** The text of the [columnId] cell of the row whose cells contain [rowText]. */
    fun cellTextForRowContaining(rowText: String, columnId: String): String {

        val row = page.locator(".ag-row")
            .filter(Locator.FilterOptions().setHasText(rowText))
            .first()
        val cell = row.locator(".ag-cell[col-id='$columnId']")
        cell.waitFor()
        cell.scrollIntoViewIfNeeded()
        return cell.innerText().trim()

    }


}
