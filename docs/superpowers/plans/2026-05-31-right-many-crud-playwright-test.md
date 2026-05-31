# RightMany CRUD Playwright Journey Test — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Playwright CRUD journey test for `RightManyEntity` covering create (with left-entity chip), FK-guarded delete error path, edit (chip pre-population + removal), cancel-delete, and confirm-delete.

**Architecture:** Four new page objects (blotter, create page, edit page, view page) mirror the generated Angular routes. `AbstractPlaywrightTest` is updated to declare and instantiate them. A single `@Test fun \`crud journey\`` exercises all five scenarios in sequence.

**Tech Stack:** Kotlin, JUnit 5, Playwright, Spring Boot Testcontainers, Elasticsearch Testcontainer, ag-Grid, Angular Material

**User Verification:** NO

---

## File Map

| Action | File |
|--------|------|
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyBlotterPage.kt` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyCreatePage.kt` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyViewPage.kt` |
| Modify | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt` |

---

## Task 1: Create the four page objects

**Goal:** Four new page-object classes that encapsulate all Playwright interactions for the RightMany UI pages.

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyBlotterPage.kt`
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyCreatePage.kt`
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt`
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyViewPage.kt`

**Acceptance Criteria:**
- [ ] All four files compile (`./gradlew :maia-showcase:app:compileTestKotlin` passes)
- [ ] `RightManyBlotterPage` handles both the FK-check error dialog and the success-path delete dialog as separate methods
- [ ] `RightManyCreatePage` and `RightManyEditPage` use the JS-click pattern for chip selection (avoids orphaned CDK tooltips)

**Verify:** `./gradlew :maia-showcase:app:compileTestKotlin` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Create `RightManyBlotterPage.kt`**

The blotter's Add button navigates to a full page (not a dialog), so `clickAddButton()` does not wait for `mat-dialog-container`. The delete flow is two dialogs: FK check (auto-closes when no references) then delete confirm. `waitForDeleteDialog()` uses text-based filtering to distinguish the delete dialog from the FK dialog.

```kotlin
// maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyBlotterPage.kt
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class RightManyBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/right-many-blotter",
    "right_many_blotter"
) {


    fun clickAddButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()

    }


    fun clickEditButtonForFirstRow() {

        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"someString\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )

        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 0")
        val editCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='edit']").first()
        editCell.waitFor()
        editCell.scrollIntoViewIfNeeded()
        editCell.click()

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

    }


    fun assertFkCheckDialogShowsError() {

        page.locator("mat-dialog-container").waitFor()
        page.waitForFunction(
            "() => {" +
            "  const container = document.querySelector('mat-dialog-container');" +
            "  if (!container) return false;" +
            "  return container.innerText.includes('Foreign key references');" +
            "}"
        )

    }


    fun dismissFkCheckDialog() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cancel")).click()
        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )

    }


    fun waitForDeleteDialog() {

        page.locator("mat-dialog-container").filter(
            Locator.FilterOptions().setHasText("Do you want to delete this record?")
        ).waitFor()

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


}
```

- [ ] **Step 2: Create `RightManyCreatePage.kt`**

The create page uses the `input[placeholder='Search Left Entities...']` autocomplete. The JS-click pattern (`option.evaluate("el => el.click()")`) avoids the CDK tooltip issue documented in the existing `LeftManyBlotterPage.searchAndSelectRightEntity()`.

```kotlin
// maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyCreatePage.kt
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class RightManyCreatePage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/right-many/create",
    "right_many_create"
) {


    fun fillForm(
        someInt: String = "42",
        someString: String = "testright",
    ) {

        page.locator("input[name='someInt']").fill(someInt)
        page.locator("input[name='someString']").fill(someString)

    }


    fun searchAndSelectLeftEntity(searchTerm: String) {

        page.locator("input[placeholder='Search Left Entities...']").fill(searchTerm)
        val option = page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm))
        option.waitFor()
        option.evaluate("el => el.click()")
        page.locator("mat-chip-row").filter(Locator.FilterOptions().setHasText(searchTerm)).waitFor()

    }


    fun clickSubmitButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))

    }


}
```

- [ ] **Step 3: Create `RightManyEditPage.kt`**

The edit page URL requires an entity ID (`/right-many/edit/:id`), so `navigateToMe()` is never called on this page object — the test reaches it via the blotter's edit button. The `pageUrl` is set to a non-navigable stub; only `assertOnPage()` (which checks `data-page-id`) is used.

```kotlin
// maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class RightManyEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/right-many/edit",
    "right_many_edit"
) {


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


    fun fillForm(
        someString: String = "testright_edited",
    ) {

        page.locator("input[name='someString']").fill(someString)

    }


    fun clickSubmitButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))

    }


}
```

- [ ] **Step 4: Create `RightManyViewPage.kt`**

The view page is only used for `assertOnPage()` after create/edit — no custom interaction methods needed.

```kotlin
// maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyViewPage.kt
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class RightManyViewPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/right-many/view",
    "right_many_view"
) {


}
```

- [ ] **Step 5: Verify compilation**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add \
  maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyBlotterPage.kt \
  maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyCreatePage.kt \
  maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt \
  maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyViewPage.kt
git commit -m "feat: add RightMany page objects for Playwright tests"
```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyBlotterPage.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyCreatePage.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyViewPage.kt"], "verifyCommand": "./gradlew :maia-showcase:app:compileTestKotlin", "acceptanceCriteria": ["All four page object files compile", "RightManyBlotterPage handles FK error dialog and delete confirm dialog separately", "Chip selection uses JS-click pattern"], "requiresUserVerification": false}
```

---

## Task 2: Wire page objects into `AbstractPlaywrightTest` and write the journey test

**Goal:** `AbstractPlaywrightTest` declares and instantiates the four new page objects; `RightManyCrudPlaywrightTest` exercises the full CRUD journey.

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt`

**Acceptance Criteria:**
- [ ] `AbstractPlaywrightTest` compiles with the four new page objects declared and instantiated
- [ ] `RightManyCrudPlaywrightTest` compiles
- [ ] Test passes when run against a live app: `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest"`

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest"` → `BUILD SUCCESSFUL` (requires Docker running for Testcontainers)

**Steps:**

- [ ] **Step 1: Add imports to `AbstractPlaywrightTest.kt`**

Insert these four imports after the existing `LeftManyBlotterPage` import (line 23) and before `LoginPage` (line 24). Keep alphabetical order.

```kotlin
import org.maiaframework.showcase.testing.pages.RightManyBlotterPage
import org.maiaframework.showcase.testing.pages.RightManyCreatePage
import org.maiaframework.showcase.testing.pages.RightManyEditPage
import org.maiaframework.showcase.testing.pages.RightManyViewPage
```

- [ ] **Step 2: Declare the four page object fields in `AbstractPlaywrightTest`**

After the `protected lateinit var leftManyBlotterPage: LeftManyBlotterPage` declaration (around line 75), add:

```kotlin
    protected lateinit var rightManyBlotterPage: RightManyBlotterPage


    protected lateinit var rightManyCreatePage: RightManyCreatePage


    protected lateinit var rightManyEditPage: RightManyEditPage


    protected lateinit var rightManyViewPage: RightManyViewPage
```

- [ ] **Step 3: Instantiate in `initPlaywrightPage()`**

After `leftManyBlotterPage = LeftManyBlotterPage(page, urlHelper)` (around line 118), add:

```kotlin
        rightManyBlotterPage = RightManyBlotterPage(page, urlHelper)
        rightManyCreatePage = RightManyCreatePage(page, urlHelper)
        rightManyEditPage = RightManyEditPage(page, urlHelper)
        rightManyViewPage = RightManyViewPage(page, urlHelper)
```

- [ ] **Step 4: Verify AbstractPlaywrightTest compiles**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Create `RightManyCrudPlaywrightTest.kt`**

`leftAlpha` is declared at class level so `@BeforeAll` can reference it without re-instantiation. The ES upsert ensures the left-entity typeahead search finds "left-alpha" during the create/edit forms. `leftToRightManyToManyJoinDao.deleteAll()` is called before `rightManyDao.deleteAll()` and `leftManyDao.deleteAll()` to satisfy FK constraints.

The test mirrors `LeftManyCrudPlaywrightTest` structure: `@BeforeAll` for DB/ES setup, `@BeforeEach` for logout, single `@Test` journey.

```kotlin
// maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt
package org.maiaframework.showcase.many_to_many

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.elasticsearch.EsDocHolder
import org.maiaframework.elasticsearch.index.EsIndexOps
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.springframework.beans.factory.annotation.Autowired


class RightManyCrudPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var leftManyDao: LeftManyDao


    @Autowired
    private lateinit var rightManyDao: RightManyDao


    @Autowired
    private lateinit var leftToRightManyToManyJoinDao: LeftToRightManyToManyJoinDao


    @Autowired
    private lateinit var esIndexOps: EsIndexOps


    @Autowired
    private lateinit var leftManyTypeaheadEsIndex: LeftManyTypeaheadEsIndex


    private val leftAlpha = LeftManyEntityTestBuilder(someString = "left-alpha").build()


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()
        fixtures.resetDatabaseState()
        leftToRightManyToManyJoinDao.deleteAll()
        rightManyDao.deleteAll()
        leftManyDao.deleteAll()
        leftManyDao.bulkInsert(listOf(leftAlpha))
        esIndexOps.upsert(
            EsDocHolder(
                id = leftAlpha.id.toString(),
                doc = LeftManyTypeaheadV1EsDoc(id = leftAlpha.id, someString = leftAlpha.someString),
                indexName = leftManyTypeaheadEsIndex.indexName()
            )
        )

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(rightManyBlotterPage)

        rightManyBlotterPage.apply {

            // Create: fill form with someInt + someString + one left-entity chip, submit
            clickAddButton()

        }

        rightManyCreatePage.apply {

            assertOnPage()
            fillForm(someInt = "42", someString = "testright")
            searchAndSelectLeftEntity("left-alpha")
            clickSubmitButton()

        }

        // After create, Angular navigates to the view page
        rightManyViewPage.assertOnPage()

        `navigate to the`(rightManyBlotterPage)
        rightManyBlotterPage.assertTableContainsValue("testright")

        rightManyBlotterPage.apply {

            // Attempt delete while the join record exists — FK check dialog shows error
            clickDeleteButtonForFirstRow()
            assertFkCheckDialogShowsError()
            dismissFkCheckDialog()

            // Edit: verify chip pre-populated, remove it, change someString
            clickEditButtonForFirstRow()

        }

        rightManyEditPage.apply {

            assertOnPage()
            assertChipVisible("left-alpha")
            removeChip("left-alpha")
            fillForm(someString = "testright_edited")
            clickSubmitButton()

        }

        // After edit, Angular navigates to the view page
        rightManyViewPage.assertOnPage()

        `navigate to the`(rightManyBlotterPage)
        rightManyBlotterPage.assertTableContainsValue("testright_edited")

        rightManyBlotterPage.apply {

            // Cancel delete: FK check passes (no join records), delete dialog appears, cancel
            clickDeleteButtonForFirstRow()
            waitForDeleteDialog()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("testright_edited")

            // Confirm delete
            clickDeleteButtonForFirstRow()
            waitForDeleteDialog()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("testright_edited")

        }

    }


}
```

- [ ] **Step 6: Verify compilation**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Run the test (requires Docker)**

Ensure Docker is running and the Elasticsearch + PostgreSQL containers can start.

```bash
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest"
```

Expected: `BUILD SUCCESSFUL` with 1 test passed. A `playwright-trace.zip` is written to the project root on completion.

If the test fails, check:
- `assertFkCheckDialogShowsError()` — the FK check API call may take longer than expected. If needed, increase the `waitForFunction` timeout by passing `Page.WaitForFunctionOptions().setTimeout(10000.0)` as a second argument.
- `waitForDeleteDialog()` — the FK dialog briefly opens and closes before the delete dialog appears. If this races, add a short `Thread.sleep(300)` before the `waitFor()` call.

- [ ] **Step 8: Commit**

```bash
git add \
  maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt \
  maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt
git commit -m "feat: add RightMany CRUD Playwright journey test"
```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt"], "verifyCommand": "./gradlew :maia-showcase:app:test --tests \"org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest\"", "acceptanceCriteria": ["AbstractPlaywrightTest compiles with 4 new page objects", "RightManyCrudPlaywrightTest compiles", "Journey test passes: 1 test, 0 failures"], "requiresUserVerification": false}
```
