package org.maiaframework.webtesting

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

object PageExtensions {


    fun Page.getButtonByText(buttonText: String): Locator {

        return getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setExact(true).setName(buttonText))

    }


    fun Page.getAutocompleteOptionWithText(text: String): Locator {

        return locator("mat-option:has-text('$text')")

    }

}
