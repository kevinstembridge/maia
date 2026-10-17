package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.maiaframework.webtesting.PageExtensions.inputForField
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
        page.inputForField("someString").fill(someString)
        Thread.sleep(1000)
    }


    fun assertDuplicateErrorIsShown() {
        // The error is shown once the field has been touched
        page.inputForField("someString").press("Tab")
        assertThat(page.locator("mat-error")).containsText("already in use")
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
    }


}
