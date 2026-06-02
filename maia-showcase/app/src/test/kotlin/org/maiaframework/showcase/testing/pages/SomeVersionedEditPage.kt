package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class SomeVersionedEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/some-versioned/edit",
    "some_versioned_edit"
) {


    fun fillEditForm(
        someString: String = "hello_edited",
        someInt: String = "1"
    ) {
        page.waitForFunction("() => (document.querySelector('input[name=\"someString\"]')?.value ?? '') !== ''")
        page.locator("input[name='someString']").fill(someString)
        page.locator("input[name='someInt']").fill(someInt)
        Thread.sleep(1000)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
    }


}
