package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class AllFieldTypesViewPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/all-field-types/view",
    "all_field_types_view"
) {


    fun clickEditButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Edit")).click()
    }


}
