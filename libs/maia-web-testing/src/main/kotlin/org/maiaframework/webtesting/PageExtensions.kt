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
     * Reactive forms render `name="field"`; signal forms render `name="<form>.field"`.
     */
    fun Page.inputForField(fieldName: String): Locator {

        return locator("input[name='$fieldName'], input[name\$='.$fieldName']")

    }


    fun Page.getAutocompleteOptionWithText(text: String): Locator {

        return locator("mat-option:has-text('$text')")

    }

}
