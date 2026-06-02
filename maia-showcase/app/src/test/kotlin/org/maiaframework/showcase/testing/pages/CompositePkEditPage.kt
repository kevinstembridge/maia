package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class CompositePkEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/composite-primary-key/edit",
    "composite_primary_key_edit"
) {


    fun fillEditForm(
        someModifiableString: String = "edited"
    ) {
        page.waitForFunction("() => (document.querySelector('input[name=\"someModifiableString\"]')?.value ?? '') !== ''")
        page.locator("input[name='someModifiableString']").fill(someModifiableString)
        Thread.sleep(1000)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
    }


}
