package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
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


}
