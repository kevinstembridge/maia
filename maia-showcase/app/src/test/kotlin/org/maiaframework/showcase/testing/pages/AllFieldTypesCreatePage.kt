package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import org.maiaframework.webtesting.PageExtensions.inputForField
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class AllFieldTypesCreatePage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/all-field-types/create",
    "all_field_types_create"
) {


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

        page.locator("mat-checkbox[formcontrolname='someBoolean']").click()
        page.locator("mat-checkbox[formcontrolname='someBooleanType']").click()
        page.locator("mat-checkbox[formcontrolname='someBooleanTypeProvided']").click()

        page.inputForField("someInt").fill(someInt)
        page.inputForField("someIntModifiable").fill(someIntModifiable)
        page.inputForField("someIntType").fill(someIntType)
        page.inputForField("someIntTypeProvided").fill(someIntTypeProvided)
        page.inputForField("someLongType").fill(someLongType)
        page.inputForField("someLongTypeProvided").fill(someLongTypeProvided)
        page.inputForField("someLocalDateModifiable").fill(someLocalDateModifiable)
        page.inputForField("someInstant").fill(someInstantDate)
        page.inputForField("someInstantTime").fill(someInstantTime)
        page.inputForField("someInstantModifiable").fill(someInstantModifiableDate)
        page.inputForField("someInstantModifiableTime").fill(someInstantModifiableTime)
        page.inputForField("somePeriodModifiable").fill(somePeriodModifiable)
        page.inputForField("someString").fill(someString)
        page.inputForField("someStringModifiable").fill(someStringModifiable)
        page.inputForField("someStringType").fill(someStringType)
        page.inputForField("someProvidedStringType").fill(someProvidedStringType)
        page.locator("mat-select[formControlName='someEnum']").click()
        page.getByRole(AriaRole.OPTION, Page.GetByRoleOptions().setName(someEnum)).click()
        page.mouse().move(0.0, 0.0)
        page.evaluate("document.querySelector('.cdk-overlay-transparent-backdrop')?.click()")
        page.locator("mat-option").first().waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )

    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))
    }


}
