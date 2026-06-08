package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class RightManyHistoryBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/right-many/history",
    "right_many_history_blotter"
) {


    fun `navigate to the history page for entity`(entityId: String) {

        navigateToUrl(pageUrl, listOf(entityId))
        assertOnPage()

    }


    fun `assert the table contains a row with`(changeType: String, someInt: String, someString: String, version: String) {

        page.waitForFunction(
            "() => {" +
            "  const cellText = (row, colId) => {" +
            "    const c = row.querySelector('.ag-cell[col-id=\"' + colId + '\"]');" +
            "    return c ? c.innerText.trim() : null;" +
            "  };" +
            "  return Array.from(document.querySelectorAll('.ag-row:not(.ag-row-loading)')).some(row =>" +
            "    cellText(row, 'changeType') === '$changeType' &&" +
            "    cellText(row, 'someInt') === '$someInt' &&" +
            "    cellText(row, 'someString') === '$someString' &&" +
            "    cellText(row, 'version') === '$version'" +
            "  );" +
            "}"
        )

    }


}
