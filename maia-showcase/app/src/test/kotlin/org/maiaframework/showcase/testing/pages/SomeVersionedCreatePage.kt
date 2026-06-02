package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class SomeVersionedCreatePage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/some-versioned/create",
    "some_versioned_create"
) {


    fun fillCreateForm(
        someString: String = "hello",
        someInt: String = "1"
    ) {
        page.locator("input[name='someString']").fill(someString)
        page.locator("input[name='someInt']").fill(someInt)
        Thread.sleep(1000)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
    }


}
