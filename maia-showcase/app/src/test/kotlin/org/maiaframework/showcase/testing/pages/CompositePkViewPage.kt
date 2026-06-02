package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class CompositePkViewPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/composite-primary-key/view",
    "composite_primary_key_view"
)
