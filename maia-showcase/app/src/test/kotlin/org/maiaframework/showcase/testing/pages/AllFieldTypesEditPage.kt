package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import org.maiaframework.webtesting.PageExtensions.inputForField
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class AllFieldTypesEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/all-field-types/edit",
    "all_field_types_edit"
) {


    fun fillEditForm(
        someIntModifiable: String = "200",
        someLocalDateModifiable: String = "2026-06-01",
        someInstantModifiableDate: String = "06/01/2026",
        someInstantModifiableTime: String = "12:00",
        somePeriodModifiable: String = "P2Y",
        someStringModifiable: String = "testmodifiable_edited",
    ) {
        // Wait for fetchForEdit() to patch the form before filling — patchValue() runs asynchronously
        // after ngOnInit() completes the HTTP call. Without this wait, patchValue() can overwrite
        // our fills (and reset id to '' which causes a silent backend error on submit).
        page.waitForFunction(
            "() => (document.querySelector('input[name=\"someStringModifiable\"], input[id=\"someStringModifiable\"]')?.value ?? '') !== ''"
        )
        page.inputForField("someIntModifiable").fill(someIntModifiable)
        page.inputForField("someLocalDateModifiable").fill(someLocalDateModifiable)
        page.inputForField("someInstantModifiable").fill(someInstantModifiableDate)
        page.inputForField("someInstantModifiableTime").fill(someInstantModifiableTime)
        page.inputForField("somePeriodModifiable").fill(somePeriodModifiable)
        page.inputForField("someStringModifiable").fill(someStringModifiable)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
    }


}
