package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


/** The Blotter over the Elasticsearch-enabled BravoRefOnlyEs Searchable DTO. */
class BravoRefOnlyEsBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/bravo-ref-only-es-blotter",
    "bravo_ref_only_es_blotter"
) {


    /** The text of the `alpha` cell of the row whose cells contain [rowText]. */
    fun alphaCellTextForRowContaining(rowText: String): String {

        val row = page.locator(".ag-row:not(.ag-row-loading)")
            .filter(Locator.FilterOptions().setHasText(rowText))
            .first()
        val cell = row.locator(".ag-cell[col-id='alpha']")
        cell.waitFor()
        cell.scrollIntoViewIfNeeded()
        return cell.innerText().trim()

    }


}
