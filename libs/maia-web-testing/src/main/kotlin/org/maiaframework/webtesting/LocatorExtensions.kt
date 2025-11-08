package org.maiaframework.webtesting

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole

object LocatorExtensions {


    fun Locator.getButtonByText(buttonText: String): Locator {

        return getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setExact(true).setName(buttonText))

    }


    fun Locator.getLinkByText(text: String): Locator {

        return getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setExact(true).setName(text))

    }


    fun Locator.assertCheckboxIsNotVisible() {

        val checkboxLocator = this.getByRole(AriaRole.CHECKBOX)
        assertThat(checkboxLocator).not().isVisible()

    }


}
