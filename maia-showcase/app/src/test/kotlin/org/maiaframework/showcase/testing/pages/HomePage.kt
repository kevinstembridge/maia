package org.maiaframework.showcase.testing.pages


import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper
import com.microsoft.playwright.Page


class HomePage(
    page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/",
    "home_page"
)
