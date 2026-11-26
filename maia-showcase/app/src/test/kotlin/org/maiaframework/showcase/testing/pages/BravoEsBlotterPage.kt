package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


/** The Blotter over the Elasticsearch-enabled BravoEs Searchable DTO. */
class BravoEsBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/bravo-es-blotter",
    "bravo_es_blotter"
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


    /** Types [text] into the floating filter of the `alpha` column. */
    fun filterAlphaColumnBy(text: String) {

        val input = page.locator(".ag-floating-filter[col-id='alpha'] input").first()
        input.waitFor()
        input.fill(text)

    }


    /** Clicks the `alpha` column header once, cycling its sort: none, ascending, descending. */
    fun clickAlphaColumnHeaderToSort() {

        page.locator(".ag-header-cell-sortable[col-id='alpha'] .ag-header-cell-label").click()

    }


    /** The `alpha` cell texts of the rows currently shown, in display order, once exactly [expectedCount] are shown. */
    fun alphaCellTexts(expectedCount: Int): List<String> {

        val cells = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='alpha']")
        val deadline = System.currentTimeMillis() + 15_000
        while (System.currentTimeMillis() < deadline &&
            !(cells.count() == expectedCount && cells.allInnerTexts().none { it.isBlank() })) {
            page.waitForTimeout(100.0)
        }
        return page.locator(".ag-row:not(.ag-row-loading)")
            .all()
            .sortedBy { it.getAttribute("row-index").toInt() }
            .map { it.locator(".ag-cell[col-id='alpha']").innerText().trim() }

    }


}
