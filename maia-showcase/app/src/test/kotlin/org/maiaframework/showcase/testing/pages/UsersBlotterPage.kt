package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class UsersBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/users",
    "users_blotter"
) {


    fun clickAddButton() {

        `click the button named`("Add")
        page.locator("mat-dialog-container").waitFor()

    }


    fun clickSubmitButton() {

        `click the button named`("Submit")

    }


    fun clickCancelButton() {

        `click the button named`("Cancel")

    }

    private fun `click the button named`(buttonName: String) {

        `get the button named`(buttonName).click()

    }

    private fun `get the button named`(buttonName: String): Locator {

        return page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(buttonName))
            ?: throw RuntimeException("Could not find button named '$buttonName'")

    }


    fun assertCreateDialogClosed() {
        assertEditDialogClosed()
    }


    fun assertEditDialogClosed() {

        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )

    }


    fun clickEditButtonForFirstRow() {

        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"displayName\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )

        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val editCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='edit']").first()
        editCell.waitFor()
        editCell.scrollIntoViewIfNeeded()
        editCell.click()
        page.locator("mat-dialog-container").waitFor()

    }


    fun clickEditButtonForRow(displayName: String) {

        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"displayName\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )

        val rowIndex = page.evaluate(
            """() => {
                const rows = document.querySelectorAll('.ag-row:not(.ag-row-loading)');
                for (const row of rows) {
                    const cell = row.querySelector('.ag-cell[col-id="displayName"]');
                    if (cell && cell.innerText.includes('$displayName')) {
                        return row.getAttribute('row-index');
                    }
                }
                return null;
            }"""
        ) as String?

        requireNotNull(rowIndex) { "Could not find row for displayName: $displayName" }
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val editCell = page.locator(".ag-row[row-index='$rowIndex'] .ag-cell[col-id='edit']")
        editCell.waitFor()
        editCell.scrollIntoViewIfNeeded()
        editCell.click()
        page.locator("mat-dialog-container").waitFor()

    }


    fun fillCreateForm(
        firstName: String,
        lastName: String,
        vararg authorities: String
    ) {

        authorities.forEach { selectAuthority(it) }
        page.locator("input[name='firstName']").fill(firstName)
        page.locator("input[name='lastName']").fill(lastName)
        page.locator("input[name='lastName']").press("Tab")
        page.mouse().move(0.0, 0.0)
        Thread.sleep(1000)

    }


    fun assertDialogShowsError() {

        page.locator("mat-dialog-container .alert").waitFor()

    }


    fun selectAuthority(authority: String) {

        page.locator("mat-dialog-container mat-select[formcontrolname='authorities']").click()
        page.locator("mat-option").filter(Locator.FilterOptions().setHasText(authority)).click()
        page.keyboard().press("Escape")

    }


    fun fillEditForm(
        firstName: String = "EditedFirst",
        additionalAuthorities: List<String> = emptyList(),
    ) {
        // Wait for the loading spinner to disappear and the form to be ready
        page.locator("mat-spinner").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )
        // Wait for firstName input to be visible
        page.locator("input[name='firstName']").waitFor()
        page.locator("input[name='firstName']").fill(firstName)
        // Touch lastName to trigger Angular validation on required field (updateOn: 'change')
        val existingLastName = page.locator("input[name='lastName']").inputValue()
        page.locator("input[name='lastName']").fill(existingLastName)
        // Press Tab to blur lastName and trigger change event, ensuring Angular processes the value
        page.locator("input[name='lastName']").press("Tab")
        additionalAuthorities.forEach { selectAuthority(it) }
        // Wait for async validators (debounced ~300ms)
        page.mouse().move(0.0, 0.0)
        Thread.sleep(1000)
    }


    fun assertTableContainsValue(value: String) {
        page.waitForFunction(
            "() => {" +
            "  if (document.querySelector('.ag-row-loading')) return false;" +
            "  const vp = document.querySelector('.ag-center-cols-viewport');" +
            "  if (vp) vp.scrollLeft = 0;" +
            "  return Array.from(document.querySelectorAll('.ag-cell'))" +
            "    .some(c => c.innerText && c.innerText.includes('$value'));" +
            "}"
        )
    }


}
