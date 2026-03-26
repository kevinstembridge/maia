# CompositePrimaryKeyEntity CRUD Playwright Test — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a full-stack CRUD Playwright test for `CompositePrimaryKeyEntity` (create → edit → cancel delete → confirm delete with version assertions) and add menu items for Composite PK and Some Versioned to the main nav.

**Architecture:** Extend the spec DSL to make `someModifiableString` user-editable, add CRUD APIs + searchable DTO + blotter table, regenerate all layers, add Angular page/route/menu items, a Kotlin page object, and a JUnit Playwright test.

**Tech Stack:** Kotlin DSL (spec), Kotlin/Spring Boot (backend), Angular 19 (frontend), AG Grid (blotter), Playwright (browser automation), JUnit 5, Testcontainers (PostgreSQL).

---

## File Map

| Action | File |
|--------|------|
| Modify | `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt` |
| Generate (auto) | `maia-showcase/*/src/generated/…/composite_pk/CompositePrimaryKey*.kt` |
| Generate (auto) | `maia-showcase/maia-showcase-ui/src/generated/typescript/…/composite_pk/composite-primary-key-*.ts/html` |
| Create | `maia-showcase/maia-showcase-ui/src/app/pages/composite-pk/composite-pk-blotter-page.ts` |
| Create | `maia-showcase/maia-showcase-ui/src/app/pages/composite-pk/composite-pk-blotter-page.html` |
| Modify | `maia-showcase/maia-showcase-ui/src/app/app.routes.ts` |
| Modify | `maia-showcase/maia-showcase-ui/src/app/app.html` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/CompositePkBlotterPage.kt` |
| Modify | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/composite_pk/CompositePkCrudPlaywrightTest.kt` |

---

## Task 1: Update the spec DSL

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt`

- [ ] **Step 1: Replace the `compositePrimaryKeyEntityDef` block and add searchable/table/crud defs**

Find the existing block (around line 1164):

```kotlin
val compositePrimaryKeyEntityDef = entity(
    "org.maiaframework.showcase.composite_pk",
    "CompositePrimaryKey",
    deletable = Deletable.TRUE,
    allowDeleteAll = AllowDeleteAll.TRUE,
    versioned = true,
    recordVersionHistory = true
) {
    cacheable {  }
    field("someString", FieldTypes.string) {
        primaryKey()
        lengthConstraint(max = 100)
    }
    field("someInt", FieldTypes.int) {
        primaryKey()
    }
    field("someModifiableString", FieldTypes.string) {
        lengthConstraint(max = 100)
        modifiableBySystem()
    }

}
```

Replace with:

```kotlin
val compositePrimaryKeyEntityDef = entity(
    "org.maiaframework.showcase.composite_pk",
    "CompositePrimaryKey",
    deletable = Deletable.TRUE,
    allowDeleteAll = AllowDeleteAll.TRUE,
    versioned = true,
    recordVersionHistory = true
) {
    cacheable {  }
    field("someString", FieldTypes.string) {
        fieldDisplayName("Some String")
        primaryKey()
        lengthConstraint(max = 100)
    }
    field("someInt", FieldTypes.int) {
        fieldDisplayName("Some Int")
        primaryKey()
    }
    field("someModifiableString", FieldTypes.string) {
        fieldDisplayName("Some Modifiable String")
        lengthConstraint(max = 100)
        editableByUser()
    }
    crud {
        apis(defaultAuthority = partySpec.writeAuthority) {
            create()
            update()
            delete()
        }
    }
}


val compositePrimaryKeySearchableDtoDef = searchableEntityDef(
    "org.maiaframework.showcase.composite_pk",
    "CompositePrimaryKey",
    entityDef = compositePrimaryKeyEntityDef,
    withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
    withGeneratedDto = WithGeneratedDto.TRUE
) {
    field("someString")
    field("someInt")
    field("someModifiableString")
    field("version")
    field("createdTimestampUtc")
}


val compositePrimaryKeyDtoHtmlTableDef = dtoHtmlTable(
    compositePrimaryKeySearchableDtoDef,
    withAddButton = true,
    withGeneratedDto = WithGeneratedDto.TRUE,
    withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
    withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE,
) {
    columnFromDto("someString")
    columnFromDto("someInt")
    columnFromDto("someModifiableString")
    columnFromDto("version")
    columnFromDto("createdTimestampUtc")
    editActionColumn()
    deleteActionColumn()
}


val compositePrimaryKeyCrudDef = crudTableDef(compositePrimaryKeyDtoHtmlTableDef, compositePrimaryKeyEntityDef.entityCrudApiDef!!)
```

- [ ] **Step 2: Verify the spec module compiles**

```bash
./gradlew :maia-showcase:spec:build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt
git commit -m "feat: add CRUD spec for CompositePrimaryKeyEntity"
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

Expected: `BUILD SUCCESSFUL` — new files appear under each module's `src/generated/` directory including `maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/composite_pk/`.

- [ ] **Step 2: Verify the full build compiles (excluding tests)**

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
git commit -m "feat: regenerate CompositePrimaryKeyEntity CRUD layers"
```

---

## Task 3: Add the Angular blotter page, route, and menu items

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/composite-pk/composite-pk-blotter-page.ts`
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/composite-pk/composite-pk-blotter-page.html`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.html`

- [ ] **Step 1: Verify the generated component name**

```bash
ls maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/composite_pk/
```

Look for `composite-primary-key-crud-table.component.ts` — check its exported class name and selector. Expected: `CompositePrimaryKeyCrudTableComponent` / `app-composite-primary-key-crud-table`. Adjust the files below if different.

- [ ] **Step 2: Create the blotter page component**

`maia-showcase/maia-showcase-ui/src/app/pages/composite-pk/composite-pk-blotter-page.ts`:

```typescript
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    CompositePrimaryKeyCrudTableComponent
} from '@app/gen-components/org/maiaframework/showcase/composite_pk/composite-primary-key-crud-table.component';

@Component({
    selector: 'app-composite-pk-blotter-page',
    templateUrl: './composite-pk-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        CompositePrimaryKeyCrudTableComponent
    ]
})
export class CompositePkBlotterPage {}
```

- [ ] **Step 3: Create the blotter page HTML template**

`maia-showcase/maia-showcase-ui/src/app/pages/composite-pk/composite-pk-blotter-page.html`:

```html
<app-page-layout pageTitle="Composite PK" dataPageId="composite_pk_blotter">
    <app-composite-primary-key-crud-table></app-composite-primary-key-crud-table>
</app-page-layout>
```

- [ ] **Step 4: Add the route**

Replace the entire contents of `maia-showcase/maia-showcase-ui/src/app/app.routes.ts` with:

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
    {
        path: 'composite_pk',
        loadComponent: () =>
            import('@app/pages/composite-pk/composite-pk-blotter-page').then(
                (m) => m.CompositePkBlotterPage,
            ),
    },
];
```

- [ ] **Step 5: Add menu items to `app.html`**

Replace the entire contents of `maia-showcase/maia-showcase-ui/src/app/app.html` with:

```html
<mat-toolbar color="primary" role="banner">
    <button matIconButton [matMenuTriggerFor]="menu" aria-label="Example icon-button with a menu">
        <mat-icon>menu</mat-icon>
    </button>
    <mat-menu #menu="matMenu">
        @if (hasReadAuthority()) {
            <button mat-menu-item routerLink="/simple">
                <mat-icon>dialpad</mat-icon>
                <span>Simple</span>
            </button>
        }
        @if (hasReadAuthority()) {
            <button mat-menu-item routerLink="/all_field_types">
                <mat-icon>dialpad</mat-icon>
                <span>All Field Types</span>
            </button>
        }
        @if (hasReadAuthority()) {
            <button mat-menu-item routerLink="/some_versioned">
                <mat-icon>dialpad</mat-icon>
                <span>Some Versioned</span>
            </button>
        }
        @if (hasReadAuthority()) {
            <button mat-menu-item routerLink="/composite_pk">
                <mat-icon>dialpad</mat-icon>
                <span>Composite PK</span>
            </button>
        }
    </mat-menu>

    <span class="ml-2 text-xl font-medium md:ml-0">{{ title() }}</span>

    <span class="flex-1" aria-hidden="true"></span>

    @if (isLoggedIn()) {
        <button mat-button (click)="logout()">Logout</button>
    } @else {
        <a mat-button routerLink="/login">Login</a>
    }

</mat-toolbar>

<main>
    <router-outlet />
</main>
```

- [ ] **Step 6: Verify the UI builds**

```bash
./gradlew :maia-showcase:maia-showcase-ui:build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/pages/composite-pk \
        maia-showcase/maia-showcase-ui/src/app/app.routes.ts \
        maia-showcase/maia-showcase-ui/src/app/app.html
git commit -m "feat: add CompositePK blotter page, route, and menu items"
```

---

## Task 4: Add the Playwright page object

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/CompositePkBlotterPage.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`

- [ ] **Step 1: Create the page object**

`maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/CompositePkBlotterPage.kt`:

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class CompositePkBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/composite_pk",
    "composite_pk_blotter"
) {


    fun clickAddButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
        page.locator("mat-dialog-container").waitFor()
    }


    fun fillCreateForm(
        someString: String = "abc",
        someInt: String = "1",
        someModifiableString: String = "initial"
    ) {
        page.locator("input[name='someString']").fill(someString)
        page.locator("input[name='someInt']").fill(someInt)
        page.locator("input[name='someModifiableString']").fill(someModifiableString)
        // Wait for async validators (debounced ~300ms)
        Thread.sleep(1000)
    }


    fun fillEditForm(
        someModifiableString: String = "edited"
    ) {
        page.locator("input[name='someModifiableString']").fill(someModifiableString)
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
            "() => { const c = document.querySelector('.ag-cell[col-id=\"someString\"]'); " +
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
            "() => { const c = document.querySelector('.ag-cell[col-id=\"someString\"]'); " +
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
            expectedVersion.toInt()
        )
    }


}
```

- [ ] **Step 2: Register the page object in `AbstractPlaywrightTest`**

In `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`, make three edits:

Add import (near the other page imports):
```kotlin
import org.maiaframework.showcase.testing.pages.CompositePkBlotterPage
```

Add field (after `someVersionedBlotterPage`):
```kotlin
protected lateinit var compositePkBlotterPage: CompositePkBlotterPage
```

In `initPlaywrightPage()`, after the line initialising `someVersionedBlotterPage`:
```kotlin
compositePkBlotterPage = CompositePkBlotterPage(page, urlHelper)
```

- [ ] **Step 3: Verify the test module compiles**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/CompositePkBlotterPage.kt \
        maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt
git commit -m "feat: add CompositePkBlotterPage page object"
```

---

## Task 5: Write and run the Playwright test

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/composite_pk/CompositePkCrudPlaywrightTest.kt`

- [ ] **Step 1: Create the test**

`maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/composite_pk/CompositePkCrudPlaywrightTest.kt`:

```kotlin
package org.maiaframework.showcase.composite_pk

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest

class CompositePkCrudPlaywrightTest : AbstractPlaywrightTest() {


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
        `navigate to the`(compositePkBlotterPage)
        compositePkBlotterPage.apply {

            // Create
            clickAddButton()
            fillCreateForm(someString = "abc", someInt = "1", someModifiableString = "initial")
            clickSubmitButton()
            assertCreateDialogClosed()
            assertVersionEquals(1L)

            // Edit
            clickEditButtonForFirstRow()
            fillEditForm(someModifiableString = "edited")
            clickSubmitButton()
            assertEditDialogClosed()
            assertTableContainsValue("edited")
            assertVersionEquals(2L)

            // Cancel delete
            clickDeleteButtonForFirstRow()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("edited")
            assertVersionEquals(2L)

            // Confirm delete
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("edited")

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
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.composite_pk.CompositePkCrudPlaywrightTest"
```

Expected: `BUILD SUCCESSFUL` — 1 test passing.

If the test fails, check `maia-showcase/app/playwright-trace.zip` — open it at `https://trace.playwright.dev` for a visual replay.

Diagnose form field names by reading:
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/composite_pk/composite-primary-key-create-dialog.component.html`
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/composite_pk/composite-primary-key-edit-dialog.component.html`

Diagnose AG Grid col-ids by reading:
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/composite_pk/composite-primary-key-table.component.ts`

Fix `CompositePkBlotterPage.kt` if field names or col-ids differ. Do NOT change test logic.

- [ ] **Step 4: Run the full test suite**

```bash
./gradlew :maia-showcase:app:test
```

Expected: `BUILD SUCCESSFUL` — all tests pass.

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/composite_pk/CompositePkCrudPlaywrightTest.kt
git commit -m "feat: add CompositePrimaryKeyEntity CRUD Playwright test with version assertions"
```
