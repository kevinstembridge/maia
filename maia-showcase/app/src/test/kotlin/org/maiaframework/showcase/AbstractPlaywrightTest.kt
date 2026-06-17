package org.maiaframework.showcase

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.ConsoleMessage
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Tracing
import com.microsoft.playwright.options.AriaRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.showcase.testing.pages.LoginPage
import org.maiaframework.showcase.testing.fixtures.UserFixture
import org.maiaframework.showcase.testing.pages.AllFieldTypesBlotterPage
import org.maiaframework.showcase.testing.pages.AllFieldTypesCreatePage
import org.maiaframework.showcase.testing.pages.AllFieldTypesEditPage
import org.maiaframework.showcase.testing.pages.AllFieldTypesViewPage
import org.maiaframework.showcase.testing.pages.BravoBlotterPage
import org.maiaframework.showcase.testing.pages.BravoCreatePage
import org.maiaframework.showcase.testing.pages.BravoEditPage
import org.maiaframework.showcase.testing.pages.BravoViewPage
import org.maiaframework.showcase.testing.pages.LeftManyBlotterPage
import org.maiaframework.showcase.testing.pages.LeftManyCreatePage
import org.maiaframework.showcase.testing.pages.LeftManyEditPage
import org.maiaframework.showcase.testing.pages.LeftManyViewPage
import org.maiaframework.showcase.testing.pages.RightManyBlotterPage
import org.maiaframework.showcase.testing.pages.RightManyCreatePage
import org.maiaframework.showcase.testing.pages.RightManyEditPage
import org.maiaframework.showcase.testing.pages.RightManyHistoryBlotterPage
import org.maiaframework.showcase.testing.pages.RightManyViewPage
import org.maiaframework.showcase.testing.pages.SimpleBlotterPage
import org.maiaframework.showcase.testing.pages.SimpleCreatePage
import org.maiaframework.showcase.testing.pages.SimpleEditPage
import org.maiaframework.showcase.testing.pages.SimpleViewPage
import org.maiaframework.showcase.testing.pages.UserGroupMembershipBlotterPage
import org.maiaframework.showcase.testing.pages.UserGroupMembershipCreatePage
import org.maiaframework.showcase.testing.pages.UserGroupMembershipEditPage
import org.maiaframework.showcase.testing.pages.UserGroupMembershipViewPage
import org.maiaframework.showcase.testing.pages.CompositePkBlotterPage
import org.maiaframework.showcase.testing.pages.CompositePkCreatePage
import org.maiaframework.showcase.testing.pages.CompositePkEditPage
import org.maiaframework.showcase.testing.pages.CompositePkViewPage
import org.maiaframework.showcase.testing.pages.UsersBlotterPage
import org.maiaframework.showcase.testing.pages.UsersCreatePage
import org.maiaframework.showcase.testing.pages.UsersEditPage
import org.maiaframework.showcase.testing.pages.UsersViewPage
import org.maiaframework.showcase.testing.pages.SomeVersionedBlotterPage
import org.maiaframework.showcase.testing.pages.SomeVersionedCreatePage
import org.maiaframework.showcase.testing.pages.SomeVersionedEditPage
import org.maiaframework.showcase.testing.pages.SomeVersionedViewPage
import org.maiaframework.showcase.testing.pages.HomePage
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import org.springframework.util.backoff.ExponentialBackOff
import java.nio.file.Paths


abstract class AbstractPlaywrightTest : AbstractBlackBoxTest() {


    private lateinit var playwright: Playwright


    protected lateinit var page: Page


    private lateinit var browserContext: BrowserContext


    private lateinit var urlHelper: UrlHelper


    protected lateinit var loginPage: LoginPage


    protected lateinit var homePage: HomePage


    protected lateinit var allFieldTypesBlotterPage: AllFieldTypesBlotterPage


    protected lateinit var allFieldTypesCreatePage: AllFieldTypesCreatePage


    protected lateinit var allFieldTypesEditPage: AllFieldTypesEditPage


    protected lateinit var allFieldTypesViewPage: AllFieldTypesViewPage


    protected lateinit var someVersionedBlotterPage: SomeVersionedBlotterPage


    protected lateinit var someVersionedCreatePage: SomeVersionedCreatePage


    protected lateinit var someVersionedEditPage: SomeVersionedEditPage


    protected lateinit var someVersionedViewPage: SomeVersionedViewPage


    protected lateinit var compositePkBlotterPage: CompositePkBlotterPage


    protected lateinit var compositePkCreatePage: CompositePkCreatePage


    protected lateinit var compositePkEditPage: CompositePkEditPage


    protected lateinit var compositePkViewPage: CompositePkViewPage


    protected lateinit var usersBlotterPage: UsersBlotterPage


    protected lateinit var usersCreatePage: UsersCreatePage


    protected lateinit var usersEditPage: UsersEditPage


    protected lateinit var usersViewPage: UsersViewPage


    protected lateinit var bravoBlotterPage: BravoBlotterPage


    protected lateinit var bravoCreatePage: BravoCreatePage


    protected lateinit var bravoEditPage: BravoEditPage


    protected lateinit var bravoViewPage: BravoViewPage


    protected lateinit var leftManyBlotterPage: LeftManyBlotterPage


    protected lateinit var leftManyCreatePage: LeftManyCreatePage


    protected lateinit var leftManyEditPage: LeftManyEditPage


    protected lateinit var leftManyViewPage: LeftManyViewPage


    protected lateinit var rightManyBlotterPage: RightManyBlotterPage


    protected lateinit var rightManyCreatePage: RightManyCreatePage


    protected lateinit var rightManyEditPage: RightManyEditPage


    protected lateinit var rightManyHistoryBlotterPage: RightManyHistoryBlotterPage


    protected lateinit var rightManyViewPage: RightManyViewPage


    protected lateinit var simpleBlotterPage: SimpleBlotterPage


    protected lateinit var simpleCreatePage: SimpleCreatePage


    protected lateinit var simpleViewPage: SimpleViewPage


    protected lateinit var simpleEditPage: SimpleEditPage


    protected lateinit var userGroupMembershipBlotterPage: UserGroupMembershipBlotterPage


    protected lateinit var userGroupMembershipCreatePage: UserGroupMembershipCreatePage


    protected lateinit var userGroupMembershipEditPage: UserGroupMembershipEditPage


    protected lateinit var userGroupMembershipViewPage: UserGroupMembershipViewPage


    protected val retryTemplate = RetryTemplate(RetryPolicy.builder()
        .includes(AssertionError::class.java, Exception::class.java)
        .backOff(ExponentialBackOff().apply { maxAttempts = 4L })
        .build())


    protected lateinit var adminUser: UserFixture


    @Autowired
    private lateinit var env: Environment


    @BeforeAll
    fun initPlaywrightPage() {

        playwright = Playwright.create()
        val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(false))
        browserContext = browser.newContext()
        browserContext.tracing().start(Tracing.StartOptions().setSnapshots(true).setScreenshots(true))
        page = browserContext.newPage()
        page.onConsoleMessage { msg: ConsoleMessage -> println(msg.text()) }
        urlHelper = UrlHelper(env)

        allFieldTypesBlotterPage = AllFieldTypesBlotterPage(page, urlHelper)
        allFieldTypesCreatePage = AllFieldTypesCreatePage(page, urlHelper)
        allFieldTypesEditPage = AllFieldTypesEditPage(page, urlHelper)
        allFieldTypesViewPage = AllFieldTypesViewPage(page, urlHelper)
        bravoBlotterPage = BravoBlotterPage(page, urlHelper)
        bravoCreatePage = BravoCreatePage(page, urlHelper)
        bravoEditPage = BravoEditPage(page, urlHelper)
        bravoViewPage = BravoViewPage(page, urlHelper)
        compositePkBlotterPage = CompositePkBlotterPage(page, urlHelper)
        compositePkCreatePage = CompositePkCreatePage(page, urlHelper)
        compositePkEditPage = CompositePkEditPage(page, urlHelper)
        compositePkViewPage = CompositePkViewPage(page, urlHelper)
        homePage = HomePage(page, urlHelper)
        leftManyBlotterPage = LeftManyBlotterPage(page, urlHelper)
        leftManyCreatePage = LeftManyCreatePage(page, urlHelper)
        leftManyEditPage = LeftManyEditPage(page, urlHelper)
        leftManyViewPage = LeftManyViewPage(page, urlHelper)
        rightManyBlotterPage = RightManyBlotterPage(page, urlHelper)
        rightManyCreatePage = RightManyCreatePage(page, urlHelper)
        rightManyEditPage = RightManyEditPage(page, urlHelper)
        rightManyHistoryBlotterPage = RightManyHistoryBlotterPage(page, urlHelper)
        rightManyViewPage = RightManyViewPage(page, urlHelper)
        loginPage = LoginPage(page, urlHelper)
        simpleBlotterPage = SimpleBlotterPage(page, urlHelper)
        simpleCreatePage = SimpleCreatePage(page, urlHelper)
        simpleEditPage = SimpleEditPage(page, urlHelper)
        simpleViewPage = SimpleViewPage(page, urlHelper)
        someVersionedBlotterPage = SomeVersionedBlotterPage(page, urlHelper)
        someVersionedCreatePage = SomeVersionedCreatePage(page, urlHelper)
        someVersionedEditPage = SomeVersionedEditPage(page, urlHelper)
        someVersionedViewPage = SomeVersionedViewPage(page, urlHelper)
        userGroupMembershipBlotterPage = UserGroupMembershipBlotterPage(page, urlHelper)
        userGroupMembershipCreatePage = UserGroupMembershipCreatePage(page, urlHelper)
        userGroupMembershipEditPage = UserGroupMembershipEditPage(page, urlHelper)
        userGroupMembershipViewPage = UserGroupMembershipViewPage(page, urlHelper)
        usersBlotterPage = UsersBlotterPage(page, urlHelper)
        usersCreatePage = UsersCreatePage(page, urlHelper)
        usersEditPage = UsersEditPage(page, urlHelper)
        usersViewPage = UsersViewPage(page, urlHelper)

    }


    protected fun initAdminUserFixture() {
        adminUser = fixtures.aUser(
            loginMailVerified = true,
            { userEntityTestBuilder -> userEntityTestBuilder.copy(authorities = listOf(
                org.maiaframework.domain.auth.Authority(
                    Authority.WRITE.name
                )
            )) }
        )
    }


    protected fun `log out`() {

        homePage.tryToNavigateToMe()
        Thread.sleep(300)
        val logoutLink = page.getByText("Logout")

        if (logoutLink.isVisible) {
            logoutLink.click()
            Thread.sleep(300)
        }

    }


    protected fun `log in as admin user`() {

        `log in user`(adminUser)

    }


    protected fun `log in user`(userFixture: UserFixture) {

        `log in user`(
            userFixture.emailAddressEntity.emailAddress,
            userFixture.rawPassword
        )

    }


    protected fun `log in user`(
        emailAddress: EmailAddress,
        rawPassword: String
    ) {

//        `fetch CSRF cookie`()

        loginPage.apply {

            navigateToMe()
            Thread.sleep(300)
            submitForm(
                emailAddress,
                rawPassword
            )

        }

        // We have to wait for the login to succeed before we try and do anything else
        homePage.assertOnPage()

    }


    protected fun `logout current user`() {

        val locateButtonByAriaLabel = locateButtonByAriaLabel("My profile")

        Thread.sleep(200)

        if (locateButtonByAriaLabel.isVisible) {
            locateButtonByAriaLabel.click()
            this.page.getByText("Logout").click()
        }

    }


    protected fun `navigate to the`(page: AbstractPage) {

        page.navigateToMe()

    }


    protected fun `assert that we're on the`(page: AbstractPage) {

        page.assertOnPage()

    }


    @AfterAll
    fun destroyPlaywright() {

        browserContext.tracing().stop(Tracing.StopOptions().setPath(Paths.get("playwright-trace.zip")))
        playwright.close()

    }


    protected fun locateButtonByAriaLabel(ariaLabel: String): Locator {

        val locator = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(ariaLabel))
        assertThat(locator).isNotNull
        return locator

    }


}
