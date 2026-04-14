package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class LeftSearchableBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/left_searchable",
    "left_searchable_blotter"
) {


    fun clickAddButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
        page.locator("mat-dialog-container").waitFor()

    }


    fun fillCreateForm(
        someInt: String = "42",
        someString: String = "testleft",
    ) {

        page.locator("input[name='someInt']").fill(someInt)
        page.locator("input[name='someString']").fill(someString)

    }


    fun clickSubmitButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))

    }


    fun assertCreateDialogClosed() {

        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )

    }


    fun clickEditButtonForFirstRow() {

        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"someString\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )

        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val editCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='edit']").first()
        editCell.waitFor()
        editCell.scrollIntoViewIfNeeded()
        editCell.click()
        page.locator("mat-dialog-container").waitFor()

    }


    fun fillEditForm(
        someString: String = "testleft_edited",
    ) {

        page.locator("input[name='someString']").fill(someString)

    }


    fun assertEditDialogClosed() {

        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )

    }


    fun assertTableContainsValue(value: String) {

        page.waitForFunction(
            "() => {" +
            "  const vp = document.querySelector('.ag-center-cols-viewport');" +
            "  if (!vp) return false;" +
            "  vp.scrollLeft = 0;" +
            "  return Array.from(document.querySelectorAll('.ag-cell'))" +
            "    .some(c => c.innerText && c.innerText.includes('$value'));" +
            "}"
        )

    }


    fun clickDeleteButtonForFirstRow() {

        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 0")

        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"someString\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )

        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val deleteCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='delete']").first()
        deleteCell.waitFor()
        deleteCell.scrollIntoViewIfNeeded()
        deleteCell.click()
        page.locator("mat-dialog-container").waitFor()

    }


    fun clickYesButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Yes")).click()

    }


    fun clickCancelButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cancel")).click()

    }


    fun assertDeleteDialogClosed() {

        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )

    }


    fun assertTableDoesNotContainValue(value: String) {

        page.waitForFunction(
            "(value) => {" +
            "  if (document.querySelector('.ag-overlay-no-rows-center')) return true;" +
            "  if (document.querySelector('.ag-row-loading')) return false;" +
            "  return !Array.from(document.querySelectorAll('.ag-cell'))" +
            "    .some(c => c.innerText && c.innerText.includes(value));" +
            "}",
            value
        )

    }


    fun searchAndSelectRightEntity(searchTerm: String) {

        page.locator("input[placeholder='Search Right Entities...']").fill(searchTerm)
        val option = page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm))
        option.waitFor()
        // Use JS click to avoid Playwright moving the mouse to the option's position:
        // Playwright's option.click() hovers over the element before clicking, which can
        // trigger a CDK tooltip. When addRightEntity() then removes the option from DOM
        // without a mouseleave, the tooltip overlay is orphaned and blocks future clicks.
        option.evaluate("el => el.click()")
        page.locator("mat-chip-row").filter(Locator.FilterOptions().setHasText(searchTerm)).waitFor()

    }


    fun assertChipVisible(chipLabel: String) {

        page.locator("mat-chip-row")
            .filter(Locator.FilterOptions().setHasText(chipLabel))
            .waitFor()

    }


    fun removeChip(chipLabel: String) {

        page.locator("mat-chip-row")
            .filter(Locator.FilterOptions().setHasText(chipLabel))
            .locator("button")
            .click()
        page.locator("mat-chip-row")
            .filter(Locator.FilterOptions().setHasText(chipLabel))
            .waitFor(Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN))

    }


}
