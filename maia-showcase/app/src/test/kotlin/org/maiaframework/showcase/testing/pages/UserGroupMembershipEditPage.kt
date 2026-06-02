package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
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
        page.locator("input[name='userGroup']").waitFor()
        page.locator("input[name='userGroup']").fill(userGroupId)
        page.locator("input[name='user']").fill(userId)
        page.locator("input[name='user']").press("Tab")
        page.mouse().move(0.0, 0.0)
        Thread.sleep(1000)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))
    }


}
