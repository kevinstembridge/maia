package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class RightManyViewPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/right-many/view",
    "right_many_view"
) {


    fun `assert the view shows`(someInt: String, someString: String, version: String) {

        assertThat(detailValue("Some Int")).hasText(someInt)
        assertThat(detailValue("Some String")).hasText(someString)
        assertThat(detailValue("Version")).hasText(version)

    }


    private fun detailValue(label: String): Locator {

        return page.locator(".detail-row")
            .filter(Locator.FilterOptions().setHasText(label))
            .locator(".detail-value")

    }


}
