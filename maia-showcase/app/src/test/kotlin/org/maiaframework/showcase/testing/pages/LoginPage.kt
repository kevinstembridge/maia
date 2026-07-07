package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class LoginPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/login",
    "login_page"
) {


    private val emailAddressInput = this.page.locator("input[name=emailAddress]")


    private val passwordInput = this.page.locator("input[name=password]")


    private val passwordVisibilityToggle = this.page.locator("mat-form-field:has(input[name=password]) button")


    fun submitForm(
        emailAddress: EmailAddress,
        password: String
    ) {

        this.emailAddressInput.clear()
        this.emailAddressInput.fill(emailAddress.value)

        this.passwordInput.clear()
        this.passwordInput.fill(password)

        this.passwordInput.press("Enter")

    }


    fun togglePasswordVisibility() {

        this.passwordVisibilityToggle.click()

    }


    fun assertPasswordFieldType(expectedType: String) {

        assertThat(this.passwordInput).hasAttribute("type", expectedType)

    }


}
