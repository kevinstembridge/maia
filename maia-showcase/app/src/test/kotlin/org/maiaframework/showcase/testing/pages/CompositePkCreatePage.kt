package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class CompositePkCreatePage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/composite-primary-key/create",
    "composite_primary_key_create"
) {


    fun fillCreateForm(
        someString: String = "abc",
        someInt: String = "1",
        someModifiableString: String = "initial"
    ) {
        page.locator("input[name='someString']").fill(someString)
        page.locator("input[name='someInt']").fill(someInt)
        page.locator("input[name='someModifiableString']").fill(someModifiableString)
        Thread.sleep(1000)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
    }


}
