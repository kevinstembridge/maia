package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class UserGroupMembershipViewPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/user-group-membership/view",
    "user_group_membership_view"
)
