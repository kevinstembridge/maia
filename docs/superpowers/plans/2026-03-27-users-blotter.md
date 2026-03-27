# Users Blotter Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Users blotter page to maia-showcase: an ag-grid table listing all users, with Add and Edit action buttons.

**Architecture:** Add `dtoHtmlTable` and `crudTableDef` definitions for User to the spec, run the generator to produce the Angular table components, then wire up a thin page component with a route and menu item. A Playwright test verifies the edit flow end-to-end.

**Tech Stack:** Kotlin spec DSL, Gradle code generator, Angular 19, ag-grid, Playwright (Java/Kotlin).

---

### Task 1: Extend the spec and regenerate UI components

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcasePartySpec.kt`

The generator will produce (do not create these manually):
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-table.component.ts`
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-table.component.html`
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-table.service.ts`
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-crud-table.component.ts`
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-crud-table.component.html`

- [ ] **Step 1: Add table definitions to the spec**

In `MaiaShowcasePartySpec.kt`, insert after `userSearchableDtoDef` (which ends around line 256):

```kotlin
    val userDtoHtmlTableDef = dtoHtmlTable(
        userSearchableDtoDef,
        withAddButton = true,
    ) {
        columnFromDto("displayName")
        columnFromDto("firstName")
        columnFromDto("lastName")
        columnFromDto("createdTimestampUtc")
        editActionColumn()
    }


    val userCrudDef = crudTableDef(userDtoHtmlTableDef, userEntityDef.entityCrudApiDef!!)
```

- [ ] **Step 2: Run the UI generator**

```bash
./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Verify the five generated files exist**

```bash
ls maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/
```

Expected output includes:
- `user-table.component.ts`
- `user-table.component.html`
- `user-table.service.ts`
- `user-crud-table.component.ts`
- `user-crud-table.component.html`

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcasePartySpec.kt
git add maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: add dtoHtmlTable and crudTableDef for User entity, regenerate UI"
```

---

### Task 2: Create the blotter page, route, and menu item

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/users-blotter/users-blotter-page.ts`
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/users-blotter/users-blotter-page.html`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.html`

- [ ] **Step 1: Create the page component**

Create `maia-showcase/maia-showcase-ui/src/app/pages/users-blotter/users-blotter-page.ts`:

```typescript
import {Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {UserCrudTableComponent} from '@app/gen-components/org/maiaframework/showcase/user/user-crud-table.component';

@Component({
    imports: [
        PageLayoutComponent,
        UserCrudTableComponent,
    ],
    selector: 'app-users-blotter-page',
    templateUrl: './users-blotter-page.html',
})
export class UsersBlotterPage {}
```

Create `maia-showcase/maia-showcase-ui/src/app/pages/users-blotter/users-blotter-page.html`:

```html
<app-page-layout pageTitle="Users" dataPageId="users_blotter">
    <app-user-crud-table></app-user-crud-table>
</app-page-layout>
```

- [ ] **Step 2: Add the route**

In `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`, add after the `composite_pk` route (before the closing `]`):

```typescript
    {
        path: 'users',
        loadComponent: () =>
            import('@app/pages/users-blotter/users-blotter-page').then(
                (m) => m.UsersBlotterPage,
            ),
    },
```

The full file after the change:

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
    {
        path: 'users',
        loadComponent: () =>
            import('@app/pages/users-blotter/users-blotter-page').then(
                (m) => m.UsersBlotterPage,
            ),
    },
];
```

- [ ] **Step 3: Add the menu item**

In `maia-showcase/maia-showcase-ui/src/app/app.html`, add inside `<mat-menu #menu="matMenu">` after the last `@if` block:

```html
        @if (hasReadAuthority()) {
            <button mat-menu-item routerLink="/users">
                <mat-icon>people</mat-icon>
                <span>Users</span>
            </button>
        }
```

- [ ] **Step 4: Build to verify compilation**

```bash
./gradlew :maia-showcase:maia-showcase-ui:build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/pages/users-blotter/
git add maia-showcase/maia-showcase-ui/src/app/app.routes.ts
git add maia-showcase/maia-showcase-ui/src/app/app.html
git commit -m "feat: add Users blotter page, route, and menu item"
```

---

### Task 3: Playwright test — page object and CRUD journey

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/UsersBlotterPage.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/user/UsersCrudPlaywrightTest.kt`

**Context:** `AbstractPlaywrightTest` extends `AbstractBlackBoxTest` (full Spring Boot context + Testcontainers Postgres + Playwright Chromium). Fixtures are created via `fixtures.aUser(...)` and survive `resetDatabaseState()` because that method re-inserts all registered fixtures after clearing the tables.

The User create endpoint requires an `encryptedPassword` field that the generated UI form does not expose (it is `notCreatableByUser()` in the spec). Creating users via the UI form will therefore fail at the backend. The test covers: page loads, Add dialog opens and cancels correctly, and the Edit flow works end-to-end.

The `displayName` column has `col-id="displayName"` in ag-grid — used to detect when the table has loaded data.

- [ ] **Step 1: Create the `UsersBlotterPage` page object**

Create `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/UsersBlotterPage.kt`:

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class UsersBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/users",
    "users_blotter"
) {


    fun clickAddButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
        page.locator("mat-dialog-container").waitFor()
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
    }


    fun clickCancelButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cancel")).click()
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
            "() => { const c = document.querySelector('.ag-cell[col-id=\"displayName\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )
        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val editCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='edit']").first()
        editCell.waitFor()
        editCell.scrollIntoViewIfNeeded()
        editCell.click()
        page.locator("mat-dialog-container").waitFor()
    }


    fun fillEditForm(
        firstName: String = "EditedFirst",
    ) {
        page.locator("input[name='firstName']").fill(firstName)
    }


    fun assertTableContainsValue(value: String) {
        page.waitForFunction(
            "() => {" +
            "  if (document.querySelector('.ag-row-loading')) return false;" +
            "  return Array.from(document.querySelectorAll('.ag-cell'))" +
            "    .some(c => c.innerText && c.innerText.includes('$value'));" +
            "}"
        )
    }


}
```

- [ ] **Step 2: Register `usersBlotterPage` in `AbstractPlaywrightTest`**

In `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`:

Add the import near the other blotter page imports:
```kotlin
import org.maiaframework.showcase.testing.pages.UsersBlotterPage
```

Add a field after `compositePkBlotterPage`:
```kotlin
    protected lateinit var usersBlotterPage: UsersBlotterPage
```

Initialise it in `initPlaywrightPage()`, after the `compositePkBlotterPage` line:
```kotlin
        usersBlotterPage = UsersBlotterPage(page, urlHelper)
```

- [ ] **Step 3: Create the test class**

Create `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/user/UsersCrudPlaywrightTest.kt`:

```kotlin
package org.maiaframework.showcase.user

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.auth.Authority
import org.maiaframework.showcase.testing.fixtures.UserFixture

class UsersCrudPlaywrightTest : AbstractPlaywrightTest() {


    private lateinit var sysAdminUser: UserFixture


    @BeforeAll
    fun setUp() {

        sysAdminUser = fixtures.aUser(
            loginMailVerified = true,
            { it.copy(authorities = listOf(Authority.SYS__ADMIN)) }
        )

        fixtures.resetDatabaseState()

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `users blotter journey`() {

        `log in user`(sysAdminUser)
        `navigate to the`(usersBlotterPage)

        // Table loads and displays data
        usersBlotterPage.assertTableContainsValue(sysAdminUser.displayName)

        // Add dialog opens and can be cancelled
        usersBlotterPage.clickAddButton()
        usersBlotterPage.clickCancelButton()
        usersBlotterPage.assertCreateDialogClosed()

        // Edit flow: open dialog, change firstName, submit
        usersBlotterPage.clickEditButtonForFirstRow()
        usersBlotterPage.fillEditForm(firstName = "EditedFirst")
        usersBlotterPage.clickSubmitButton()
        usersBlotterPage.assertEditDialogClosed()
        usersBlotterPage.assertTableContainsValue("EditedFirst")

    }


}
```

- [ ] **Step 4: Run the test**

```bash
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.user.UsersCrudPlaywrightTest"
```

Expected: `BUILD SUCCESSFUL`, 1 test passes.

If the edit fails: open the Playwright trace at `maia-showcase/app/playwright-trace.zip` in the Playwright Trace Viewer (`npx playwright show-trace playwright-trace.zip`) to inspect what happened.

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/UsersBlotterPage.kt
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/user/UsersCrudPlaywrightTest.kt
git commit -m "test: add Playwright CRUD journey test for Users blotter page"
```
