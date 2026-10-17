package org.maiaframework.webtesting

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

object PageExtensions {


    fun Page.getButtonByText(buttonText: String): Locator {

        return getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setExact(true).setName(buttonText))

    }


    /**
     * Locates the text input for a form field, whichever Angular form system generated the page.
     * Reactive forms render `name="field"`. Signal forms own the `name` attribute, so they render `id="field"`.
     */
    fun Page.inputForField(fieldName: String): Locator {

        return locator("input[name='$fieldName'], input[id='$fieldName']")

    }


    /** Locates the Material checkbox for a form field, whichever Angular form system generated the page. */
    fun Page.checkboxForField(fieldName: String): Locator {

        return locator("mat-checkbox[formcontrolname='$fieldName'], mat-checkbox[id='$fieldName']")

    }


    /** Locates the Material select for a form field, whichever Angular form system generated the page. */
    fun Page.selectForField(fieldName: String): Locator {

        return locator("mat-select[formcontrolname='$fieldName'], mat-select[id='$fieldName']")

    }


    fun Page.getAutocompleteOptionWithText(text: String): Locator {

        return locator("mat-option:has-text('$text')")

    }

}
