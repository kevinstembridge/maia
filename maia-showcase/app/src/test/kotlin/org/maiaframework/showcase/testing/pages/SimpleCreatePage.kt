package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class SimpleCreatePage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/simple/create",
    "simple_create"
) {


    fun fillCreateForm(someString: String = "teststring") {
        page.locator("input[name='someString']").fill(someString)
        Thread.sleep(1000)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
    }


}
