package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern


/** The Client-side blotter over the Hazelcast-backed BravoHz Searchable DTO. */
class BravoHzBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/bravo-hz-blotter",
    "bravo_hz_blotter"
) {


    private val fetchAllRequests = AtomicInteger()


    init {

        page.onRequest { request ->
            if (request.url().contains(FETCH_ALL_PATH)) {
                fetchAllRequests.incrementAndGet()
            }
        }

    }


    /** Forgets the fetch-all requests seen so far. */
    fun resetFetchAllRequestCount() {

        fetchAllRequests.set(0)

    }


    /** How many fetch-all requests the page has made since the last reset. */
    fun fetchAllRequestCount(): Int {

        return fetchAllRequests.get()

    }


    /** The `dtoStringFromBravo` cell texts of the rows currently shown, in display order, once exactly [expectedCount] are shown. */
    fun stringCellTexts(expectedCount: Int): List<String> {

        val cells = page.locator(".ag-row .ag-cell[col-id='dtoStringFromBravo']")
        assertThat(cells).hasCount(expectedCount)
        assertThat(cells).hasText(Array(expectedCount) { NON_BLANK })

        return page.locator(".ag-row")
            .all()
            .sortedBy { it.getAttribute("row-index").toInt() }
            .map { it.locator(".ag-cell[col-id='dtoStringFromBravo']").innerText().trim() }

    }


    /** Types [text] into the floating filter of the `dtoStringFromBravo` column. */
    fun filterStringColumnBy(text: String) {

        val input = page.locator(".ag-floating-filter[col-id='dtoStringFromBravo'] input").first()
        input.waitFor()
        input.fill(text)

    }


    /** Clicks the `dtoStringFromBravo` column header once, cycling its sort: none, ascending, descending. */
    fun clickStringColumnHeaderToSort() {

        page.locator(".ag-header-cell-sortable[col-id='dtoStringFromBravo'] .ag-header-cell-label").click()

    }


    /** Clicks the refresh button, which fetches the whole set again. */
    fun clickRefreshButton() {

        page.getByTestId("refresh-button").click()

    }


    private companion object {

        const val FETCH_ALL_PATH = "/api/bravo-hz-blotter/find-all"

        val NON_BLANK: Pattern = Pattern.compile("\\S+")

    }


}
