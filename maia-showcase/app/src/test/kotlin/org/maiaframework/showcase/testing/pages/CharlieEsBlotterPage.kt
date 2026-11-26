package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


/** The Blotter over the Elasticsearch-enabled CharlieEs Searchable DTO. */
class CharlieEsBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/charlie-es-blotter",
    "charlie_es_blotter"
) {


    /** The text of the [columnId] cell of the row whose cells contain [rowText]. */
    fun cellTextForRowContaining(rowText: String, columnId: String): String {

        val row = page.locator(".ag-row:not(.ag-row-loading)")
            .filter(Locator.FilterOptions().setHasText(rowText))
            .first()
        val cell = row.locator(".ag-cell[col-id='$columnId']")
        cell.waitFor()
        cell.scrollIntoViewIfNeeded()
        return cell.innerText().trim()

    }


}
