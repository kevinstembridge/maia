package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import org.maiaframework.webtesting.PageExtensions.inputForField
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class UserGroupMembershipEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/user-group-membership/edit",
    "user_group_membership_edit"
) {


    fun fillEditForm(userGroupId: String, userId: String) {
        page.locator("mat-spinner").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )
        page.inputForField("userGroup").waitFor()
        page.inputForField("userGroup").fill(userGroupId)
        page.inputForField("user").fill(userId)
        page.inputForField("user").press("Tab")
        page.mouse().move(0.0, 0.0)
        Thread.sleep(1000)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))
    }


}
