package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


/** The Client-side blotter over the Hazelcast-backed BravoHzCapped Searchable DTO, whose `maxRows` is 20. */
class BravoHzCappedBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/bravo-hz-capped-blotter",
    "bravo_hz_capped_blotter"
) {


    /** Asserts the page shows [message] as its load error. */
    fun assertLoadErrorContains(message: String) {

        assertThat(page.getByTestId("blotter-load-error")).containsText(message)

    }


    /** Asserts the page shows no load error. */
    fun assertNoLoadError() {

        assertThat(page.getByTestId("blotter-load-error")).hasCount(0)

    }


    /** The number of rows the grid is showing. */
    fun rowCount(): Int {

        return page.locator(".ag-center-cols-container .ag-row").count()

    }


}
