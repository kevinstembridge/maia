package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class SimpleViewPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/simple/view",
    "simple_view"
) {


    fun assertEditButtonIsVisible() {
        assertThat(editButton()).isVisible()
    }


    fun clickEditButton() {
        editButton().click()
    }


    fun assertShowsText(text: String) {
        assertThat(page.getByText(text, Page.GetByTextOptions().setExact(true))).isVisible()
    }


    private fun editButton() =
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Edit"))


}
