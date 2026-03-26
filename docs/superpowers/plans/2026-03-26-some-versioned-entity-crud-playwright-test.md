# SomeVersionedEntity CRUD Playwright Test — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a full-stack CRUD Playwright test for `SomeVersionedEntity` that creates, edits, cancels a delete, and confirms a delete — with `version` field assertions after each mutation.

**Architecture:** Extend the spec DSL to add CRUD APIs + searchable DTO + blotter table for `SomeVersionedEntity`, regenerate all layers, add an Angular blotter page, a Kotlin page-object, and a JUnit Playwright test that drives the browser through the full CRUD journey.

**Tech Stack:** Kotlin DSL (spec), Kotlin/Spring Boot (backend), Angular 19 (frontend), AG Grid (blotter), Playwright (browser automation), JUnit 5, Testcontainers (PostgreSQL).

---

## File Map

| Action | File |
|--------|------|
| Modify | `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt` |
| Generate (auto) | `maia-showcase/domain/src/generated/…/versioned/SomeVersionedEntity*.kt` |
| Generate (auto) | `maia-showcase/dao/src/generated/…/versioned/SomeVersionedDao*.kt` |
| Generate (auto) | `maia-showcase/repo/src/generated/…/versioned/SomeVersionedRepo*.kt` |
| Generate (auto) | `maia-showcase/service/src/generated/…/versioned/SomeVersionedCrudService.kt` |
| Generate (auto) | `maia-showcase/web/src/generated/…/versioned/SomeVersioned*.kt` |
| Generate (auto) | `maia-showcase/maia-showcase-ui/src/generated/typescript/…/versioned/some-versioned-*.ts/html` |
| Create | `maia-showcase/maia-showcase-ui/src/app/pages/some-versioned/some-versioned-blotter-page.ts` |
| Create | `maia-showcase/maia-showcase-ui/src/app/pages/some-versioned/some-versioned-blotter-page.html` |
| Modify | `maia-showcase/maia-showcase-ui/src/app/app.routes.ts` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/SomeVersionedBlotterPage.kt` |
| Modify | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/versioned/SomeVersionedCrudPlaywrightTest.kt` |

---

## Task 1: Update the spec DSL

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt`

- [ ] **Step 1: Add `deletable`, `crud`, searchable DTO, blotter table, and CRUD def to `someVersionedEntityDef`**

Find the existing `someVersionedEntityDef` block (around line 738):

```kotlin
val someVersionedEntityDef = entity(
    "org.maiaframework.showcase.versioned",
    "SomeVersioned",
    versioned = true
) {
    field("someString", FieldTypes.string) {
        editableByUser()
        lengthConstraint(max = 100)
    }
    field("someInt", FieldTypes.int) {
        editableByUser()
    }
    index {
        unique()
        withFieldAscending("someInt")
    }
}
```

Replace it with the full wired-up definition:

```kotlin
val someVersionedEntityDef = entity(
    "org.maiaframework.showcase.versioned",
    "SomeVersioned",
    versioned = true,
    deletable = Deletable.TRUE
) {
    field("someString", FieldTypes.string) {
        editableByUser()
        lengthConstraint(max = 100)
    }
    field("someInt", FieldTypes.int) {
        editableByUser()
    }
    index {
        unique()
        withFieldAscending("someInt")
    }
    crud {
        apis(defaultAuthority = partySpec.writeAuthority) {
            create()
            update()
            delete()
        }
    }
}


val someVersionedSearchableDtoDef = searchableEntityDef(
    "org.maiaframework.showcase.versioned",
    "SomeVersioned",
    entityDef = someVersionedEntityDef,
    withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
    withGeneratedDto = WithGeneratedDto.TRUE
) {
    field("someString")
    field("someInt")
    field("version")
    field("id")
    field("createdTimestampUtc")
}


val someVersionedDtoHtmlTableDef = dtoHtmlTable(
    someVersionedSearchableDtoDef,
    withAddButton = true,
    withGeneratedDto = WithGeneratedDto.TRUE,
    withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
    withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE,
) {
    columnFromDto("someString")
    columnFromDto("someInt")
    columnFromDto("version")
    columnFromDto("id")
    columnFromDto("createdTimestampUtc")
    editActionColumn()
    deleteActionColumn()
}


val someVersionedCrudDef = crudTableDef(someVersionedDtoHtmlTableDef, someVersionedEntityDef.entityCrudApiDef!!)
```

- [ ] **Step 2: Verify the spec module compiles**

```bash
./gradlew :maia-showcase:spec:build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt
git commit -m "feat: add CRUD spec for SomeVersionedEntity"
```

---

## Task 2: Regenerate all layers

**Files:** All `src/generated/` directories in `domain`, `dao`, `repo`, `service`, `web`, and `maia-showcase-ui` modules.

- [ ] **Step 1: Run code generation for all maia-showcase modules**

```bash
./gradlew \
  :maia-showcase:domain:maiaGeneration \
  :maia-showcase:dao:maiaGeneration \
  :maia-showcase:repo:maiaGeneration \
  :maia-showcase:service:maiaGeneration \
  :maia-showcase:web:maiaGeneration \
  :maia-showcase:maia-showcase-ui:maiaGeneration
```

Expected: `BUILD SUCCESSFUL` — new files appear under each module's `src/generated/` directory including `maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/versioned/`.

- [ ] **Step 2: Verify the full build compiles (excluding slow Playwright tests)**

```bash
./gradlew :maia-showcase:app:build -x test
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit generated files**

```bash
git add maia-showcase/domain/src/generated \
        maia-showcase/dao/src/generated \
        maia-showcase/repo/src/generated \
        maia-showcase/service/src/generated \
        maia-showcase/web/src/generated \
        maia-showcase/maia-showcase-ui/src/generated
git commit -m "feat: regenerate SomeVersionedEntity CRUD layers"
```

---

## Task 3: Add the Angular blotter page and route

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/some-versioned/some-versioned-blotter-page.ts`
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/some-versioned/some-versioned-blotter-page.html`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`

- [ ] **Step 1: Create the blotter page component**

`maia-showcase/maia-showcase-ui/src/app/pages/some-versioned/some-versioned-blotter-page.ts`:

```typescript
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    SomeVersionedCrudTableComponent
} from '@app/gen-components/org/maiaframework/showcase/versioned/some-versioned-crud-table.component';

@Component({
    selector: 'app-some-versioned-blotter-page',
    templateUrl: './some-versioned-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        SomeVersionedCrudTableComponent
    ]
})
export class SomeVersionedBlotterPage {}
```

- [ ] **Step 2: Create the blotter page HTML template**

`maia-showcase/maia-showcase-ui/src/app/pages/some-versioned/some-versioned-blotter-page.html`:

```html
<app-page-layout pageTitle="Some Versioned" dataPageId="some_versioned_blotter">
    <app-some-versioned-crud-table></app-some-versioned-crud-table>
</app-page-layout>
```

- [ ] **Step 3: Add the route**

In `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`, add a route entry for `some_versioned` before the closing `]`:

```typescript
{
    path: 'some_versioned',
    loadComponent: () =>
        import('@app/pages/some-versioned/some-versioned-blotter-page').then(
            (m) => m.SomeVersionedBlotterPage,
        ),
},
```

The full file should look like:

```typescript
import {Routes} from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('@app/pages/home/home-page.component').then(
                (m) => m.HomePageComponent,
            ),
    },
    {
        path: 'all_field_types',
        loadComponent: () =>
            import('@app/pages/all-field-types-blotter/all-field-types-blotter-page').then(
                (m) => m.AllFieldTypesBlotterPage,
            ),
    },
    {
        path: 'simple',
        loadComponent: () =>
            import('@app/pages/simple/simple-page.component').then(
                (m) => m.SimplePageComponent,
            ),
    },
    {
        path: 'login',
        loadComponent: () =>
            import('@app/pages/login/login-page.component').then(
                (m) => m.LoginPageComponent,
            ),
    },
    {
        path: 'some_versioned',
        loadComponent: () =>
            import('@app/pages/some-versioned/some-versioned-blotter-page').then(
                (m) => m.SomeVersionedBlotterPage,
            ),
    },
];
```

Note: the duplicate `all_field_types` entry that was in the original file is intentionally removed.

- [ ] **Step 4: Verify the UI builds**

```bash
./gradlew :maia-showcase:maia-showcase-ui:build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/pages/some-versioned \
        maia-showcase/maia-showcase-ui/src/app/app.routes.ts
git commit -m "feat: add SomeVersioned blotter page and route"
```

---

## Task 4: Add the Playwright page object

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/SomeVersionedBlotterPage.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`

- [ ] **Step 1: Create the page object**

`maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/SomeVersionedBlotterPage.kt`:

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class SomeVersionedBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/some_versioned",
    "some_versioned_blotter"
) {


    fun clickAddButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
        page.locator("mat-dialog-container").waitFor()
    }


    fun fillCreateForm(
        someString: String = "hello",
        someInt: String = "1"
    ) {
        page.locator("input[name='someString']").fill(someString)
        page.locator("input[name='someInt']").fill(someInt)
        // Wait for async validators (debounced ~300ms)
        Thread.sleep(1000)
    }


    fun fillEditForm(
        someString: String = "hello_edited",
        someInt: String = "1"
    ) {
        page.locator("input[name='someString']").fill(someString)
        page.locator("input[name='someInt']").fill(someInt)
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


    fun assertEditDialogClosed() {
        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )
    }


    fun clickEditButtonForFirstRow() {
        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"someInt\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val editCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='edit']").first()
        editCell.waitFor()
        editCell.scrollIntoViewIfNeeded()
        editCell.click()
        page.locator("mat-dialog-container").waitFor()
    }


    fun clickDeleteButtonForFirstRow() {
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 0")
        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"someInt\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val deleteCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='delete']").first()
        deleteCell.waitFor()
        deleteCell.scrollIntoViewIfNeeded()
        deleteCell.click()
        page.locator("mat-dialog-container").waitFor()
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


    fun assertVersionEquals(expectedVersion: Long) {
        page.waitForFunction(
            "(v) => {" +
            "  const c = document.querySelector('.ag-cell[col-id=\"version\"]');" +
            "  return c && c.innerText && c.innerText.trim() === String(v);" +
            "}",
            expectedVersion
        )
    }


}
```

- [ ] **Step 2: Register the page object in `AbstractPlaywrightTest`**

In `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`:

Add the import:
```kotlin
import org.maiaframework.showcase.testing.pages.SomeVersionedBlotterPage
```

Add a field (after `allFieldTypesBlotterPage`):
```kotlin
protected lateinit var someVersionedBlotterPage: SomeVersionedBlotterPage
```

In `initPlaywrightPage()`, after the line that initialises `allFieldTypesBlotterPage`:
```kotlin
someVersionedBlotterPage = SomeVersionedBlotterPage(page, urlHelper)
```

- [ ] **Step 3: Verify the test module compiles**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/SomeVersionedBlotterPage.kt \
        maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt
git commit -m "feat: add SomeVersionedBlotterPage page object"
```

---

## Task 5: Write and run the Playwright test

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/versioned/SomeVersionedCrudPlaywrightTest.kt`

- [ ] **Step 1: Create the test**

`maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/versioned/SomeVersionedCrudPlaywrightTest.kt`:

```kotlin
package org.maiaframework.showcase.versioned

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest

class SomeVersionedCrudPlaywrightTest : AbstractPlaywrightTest() {


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        fixtures.resetDatabaseState()

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(someVersionedBlotterPage)
        someVersionedBlotterPage.apply {

            // Create
            clickAddButton()
            fillCreateForm(someString = "hello", someInt = "1")
            clickSubmitButton()
            assertCreateDialogClosed()
            assertVersionEquals(1L)

            // Edit
            clickEditButtonForFirstRow()
            fillEditForm(someString = "hello_edited", someInt = "1")
            clickSubmitButton()
            assertEditDialogClosed()
            assertTableContainsValue("hello_edited")
            assertVersionEquals(2L)

            // Cancel delete
            clickDeleteButtonForFirstRow()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("hello_edited")
            assertVersionEquals(2L)

            // Confirm delete
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("hello_edited")

        }

    }


}
```

- [ ] **Step 2: Start the PostgreSQL container (if not already running)**

```bash
docker compose -f maia-showcase/compose.yaml up -d
```

Expected: PostgreSQL available on port 5433.

- [ ] **Step 3: Run the Playwright test**

```bash
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.versioned.SomeVersionedCrudPlaywrightTest"
```

Expected: `BUILD SUCCESSFUL` — 1 test passing.

If the test fails, check `maia-showcase/app/playwright-trace.zip` (generated automatically) — open it at `https://trace.playwright.dev` for a visual replay.

- [ ] **Step 4: Run the full test suite to check for regressions**

```bash
./gradlew :maia-showcase:app:test
```

Expected: `BUILD SUCCESSFUL` — all tests pass.

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/versioned/SomeVersionedCrudPlaywrightTest.kt
git commit -m "feat: add SomeVersionedEntity CRUD Playwright test with version assertions"
```
