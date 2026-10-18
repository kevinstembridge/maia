package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.PageExtensions.inputForField
import org.maiaframework.webtesting.UrlHelper

class DialogFormPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/dialog-form",
    "dialog_form_page"
) {


    fun openFeedbackDialog() {
        page.locator("#openFeedbackDialog").click()
        assertThat(dialog()).isVisible()
    }


    fun fillFeedbackForm(comment: String, rating: String) {
        page.inputForField("comment").fill(comment)
        page.inputForField("rating").fill(rating)
    }


    fun clickSendButton() {
        dialog().getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Send")).click()
    }


    fun clickCancelButton() {
        dialog().getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Cancel")).click()
    }


    fun assertDialogIsOpen() {
        assertThat(dialog()).isVisible()
    }


    fun assertDialogIsClosed() {
        assertThat(dialog()).isHidden()
    }


    fun assertValidationErrorIsShown(message: String) {
        assertThat(dialog().locator("mat-error")).containsText(message)
    }


    fun assertFeedbackReceived(text: String) {
        assertThat(page.locator("#feedbackReceived")).hasText(text)
    }


    private fun dialog() = page.locator("mat-dialog-container")


}
