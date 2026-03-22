package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class AllFieldTypesBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/all_field_types",
    "all_field_types_blotter"
) {


    fun clickAddButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
        page.locator("mat-dialog-container").waitFor()
    }


    fun fillCreateForm(
        someInt: String = "42",
        someIntModifiable: String = "100",
        someIntType: String = "1",
        someIntTypeProvided: String = "2",
        someLongType: String = "1000",
        someLongTypeProvided: String = "2000",
        someLocalDateModifiable: String = "2026-01-15",
        someInstantDate: String = "01/15/2026",
        someInstantTime: String = "10:00",
        someInstantModifiableDate: String = "01/15/2026",
        someInstantModifiableTime: String = "10:00",
        somePeriodModifiable: String = "P1Y",
        someString: String = "teststring_create",
        someStringModifiable: String = "testmodifiable",
        someStringType: String = "teststringtype",
        someProvidedStringType: String = "testprovided",
        someEnum: String = "OK",
    ) {

        // Click boolean checkboxes so FormControl values are proper booleans (not empty strings)
        page.locator("mat-checkbox[formcontrolname='someBoolean']").click()
        page.locator("mat-checkbox[formcontrolname='someBooleanNullable']").click()
        page.locator("mat-checkbox[formcontrolname='someBooleanType']").click()
        page.locator("mat-checkbox[formcontrolname='someBooleanTypeNullable']").click()
        page.locator("mat-checkbox[formcontrolname='someBooleanTypeProvided']").click()
        page.locator("mat-checkbox[formcontrolname='someBooleanTypeProvidedNullable']").click()

        page.locator("input[name='someInt']").fill(someInt)
        page.locator("input[name='someIntModifiable']").fill(someIntModifiable)
        page.locator("input[name='someIntType']").fill(someIntType)
        page.locator("input[name='someIntTypeProvided']").fill(someIntTypeProvided)
        page.locator("input[name='someLongType']").fill(someLongType)
        page.locator("input[name='someLongTypeProvided']").fill(someLongTypeProvided)
        page.locator("input[name='someLocalDateModifiable']").fill(someLocalDateModifiable)
        page.locator("input[name='someInstant']").fill(someInstantDate)
        page.locator("input[name='someInstantTime']").fill(someInstantTime)
        page.locator("input[name='someInstantModifiable']").fill(someInstantModifiableDate)
        page.locator("input[name='someInstantModifiableTime']").fill(someInstantModifiableTime)
        page.locator("input[name='somePeriodModifiable']").fill(somePeriodModifiable)
        page.locator("input[name='someString']").fill(someString)
        page.locator("input[name='someStringModifiable']").fill(someStringModifiable)
        page.locator("input[name='someStringType']").fill(someStringType)
        page.locator("input[name='someProvidedStringType']").fill(someProvidedStringType)
        page.locator("mat-select[formControlName='someEnum']").click()
        page.getByRole(AriaRole.OPTION, Page.GetByRoleOptions().setName(someEnum)).click()
        // Wait for async validators (debounced ~300ms)
        Thread.sleep(1000)

    }


    fun clickSubmitButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()

    }


    fun assertCreateDialogClosed() {

        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )

    }


}
