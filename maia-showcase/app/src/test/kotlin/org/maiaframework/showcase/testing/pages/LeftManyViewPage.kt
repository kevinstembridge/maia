package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class LeftManyViewPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/left-many/view",
    "left_many_view"
) {


    fun clickEditButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Edit")).click()
    }


}
