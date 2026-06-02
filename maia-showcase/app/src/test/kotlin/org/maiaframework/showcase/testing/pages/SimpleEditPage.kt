package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class SimpleEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/simple/edit",
    "simple_edit"
) {


    fun fillEditForm(someString: String = "edited-string") {
        page.waitForFunction("() => (document.querySelector('input[name=\"someString\"]')?.value ?? '') !== ''")
        page.locator("input[name='someString']").fill(someString)
        // Wait for async validators (debounced ~300ms)
        Thread.sleep(1000)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
    }


}
