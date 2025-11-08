package org.maiaframework.webtesting

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.assertj.core.api.Assertions
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

abstract class AbstractPage(
    private val page: Page,
    private val urlHelper: UrlHelper,
    protected val pageUrl: String,
    private val pageTestId: String
) {


    fun navigateToMe() {

        tryToNavigateToMe()
        Thread.sleep(100)
        assertOnPage()

    }


    fun tryToNavigateToMe() {

        navigateToUrl(this.pageUrl)

    }


    protected fun navigateToUrl(
            url: String,
            pathParams: List<String> = emptyList(),
            queryParams: MultiValueMap<String, Any> = LinkedMultiValueMap()
    ) {

        this.page.navigate(urlHelper.url(url, pathParams, queryParams))

    }


    fun `assert text content is visible`(content: String) {

        assertThat(this.page.getByText(content, Page.GetByTextOptions().setExact(true))).isVisible()

    }


    fun assertOnPage() {

        val element = this.page.locator("//*[@data-page-id]")
        assertThat(element).hasAttribute("data-page-id", this.pageTestId)

    }


    fun assertNotOnPage() {

        val element = this.page.locator("//*[@data-page-id]")
        assertThat(element).not().hasAttribute("data-page-id", this.pageTestId)

    }


    fun assertLinkWithText(text: String, hrefValue: String, exact: Boolean = true): Locator {

        val options = Page.GetByRoleOptions().setName(text).setExact(exact)
        val locator = this.page.getByRole(AriaRole.LINK, options)
        val locatorAssertions = assertThat(locator)
        locatorAssertions.hasAttribute("href", hrefValue)
        locatorAssertions.isVisible()
        return locator

    }


    fun locateByTestId(testId: String): Locator? {

        return this.page.getByTestId(testId)

    }


    fun locateButtonByName(name: String): Locator {

        val locator = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(name))
        Assertions.assertThat(locator).`as`("Button named: $name").isNotNull
        return locator

    }

    fun locateByHeading(heading: String): Locator {

        val locator = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(heading))
        Assertions.assertThat(locator).`as`(heading).isNotNull
        return locator

    }


    fun locateButtonByName(name: Regex): Locator {

        val locator = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(name.pattern))
        Assertions.assertThat(locator).`as`("Button named: $name").isNotNull
        return locator

    }


}
