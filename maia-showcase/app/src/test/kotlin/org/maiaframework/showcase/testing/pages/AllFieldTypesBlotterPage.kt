package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class AllFieldTypesBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/all_field_types",
    "all_field_types_page"
) {



}
