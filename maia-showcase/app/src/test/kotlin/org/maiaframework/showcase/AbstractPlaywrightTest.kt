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
import org.maiaframework.showcase.auth.Authority
import org.maiaframework.showcase.testing.pages.LoginPage
import org.maiaframework.showcase.testing.fixtures.UserFixture
import org.maiaframework.showcase.testing.pages.AllFieldTypesBlotterPage
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


    protected val retryTemplate = RetryTemplate(RetryPolicy.builder()
        .includes(AssertionError::class.java, Exception::class.java)
        .backOff(ExponentialBackOff().apply { maxAttempts = 4L })
        .build())


    private lateinit var adminUser: UserFixture


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
        homePage = HomePage(page, urlHelper)
        loginPage = LoginPage(page, urlHelper)
        allFieldTypesBlotterPage = AllFieldTypesBlotterPage(page, urlHelper)

    }


    protected fun initAdminUserFixture() {
        adminUser = fixtures.aUser(
            loginMailVerified = true,
            { userEntityTestBuilder -> userEntityTestBuilder.copy(authorities = listOf(Authority.WRITE)) }
        )
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
