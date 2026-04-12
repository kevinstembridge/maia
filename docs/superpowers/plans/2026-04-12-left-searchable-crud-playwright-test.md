# LeftSearchable CRUD Playwright Test Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add full CRUD (create/update/delete) to `leftSearchableDtoHtmlTableDef`, create the Angular blotter page, and write a Playwright test exercising the full journey.

**Architecture:** Upgrade `leftSearchableDtoHtmlTableDef` in-place to a CRUD table by adding CRUD APIs to `leftEntityDef`, a new `leftCrudDef`, an Angular blotter page, and a Kotlin page object + Playwright test. Regenerate all affected code layers between spec and UI.

**Tech Stack:** Kotlin/Spring Boot (spec, domain, dao, service, web), Angular 19 (UI), Playwright (testing), Gradle

**User Verification:** NO

---

## File Map

| Action | File |
|--------|------|
| Modify | `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt` |
| Generate | `maia-showcase/domain/src/generated/…` (LeftCreateRequestDto, LeftUpdateRequestDto, LeftEntityUpdater, etc.) |
| Generate | `maia-showcase/dao/src/generated/…` |
| Generate | `maia-showcase/repo/src/generated/…` |
| Generate | `maia-showcase/service/src/generated/…` |
| Generate | `maia-showcase/web/src/generated/…` (LeftCrudEndpoint, LeftSearchableTableDtoSearchEndpoint) |
| Generate | `maia-showcase/maia-showcase-ui/src/generated/…` (LeftSearchableCrudTableComponent + dialogs) |
| Create | `maia-showcase/maia-showcase-ui/src/app/pages/left-searchable-blotter/left-searchable-blotter-page.ts` |
| Create | `maia-showcase/maia-showcase-ui/src/app/pages/left-searchable-blotter/left-searchable-blotter-page.html` |
| Modify | `maia-showcase/maia-showcase-ui/src/app/app.routes.ts` |
| Modify | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt` |
| Modify | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/fixtures/Fixtures.kt` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftSearchableBlotterPage.kt` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftSearchableCrudPlaywrightTest.kt` |

---

## Task 1: Spec changes — add CRUD to leftEntityDef + upgrade table def

**Goal:** Update the spec so `leftEntityDef` has CRUD APIs and `leftSearchableDtoHtmlTableDef` is a full CRUD table.

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt:1075-1086` (leftEntityDef)
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt:1136-1142` (leftSearchableDtoHtmlTableDef)

**Acceptance Criteria:**
- [ ] `leftEntityDef` has `fieldDisplayName`, `editableByUser()`, and `crud { apis { create() update() delete() } }`
- [ ] `leftSearchableDtoHtmlTableDef` has `withAddButton = true`, `someStringFromLeft` column, `editActionColumn()`, `deleteActionColumn()`
- [ ] `leftCrudDef` declared immediately after the table def
- [ ] `./gradlew :maia-showcase:spec:build` passes

**Verify:** `./gradlew :maia-showcase:spec:build` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Replace `leftEntityDef`**

In `MaiaShowcaseSpec.kt`, replace the current `leftEntityDef` block (lines 1075–1086):

```kotlin
val leftEntityDef = entity(
    "org.maiaframework.showcase.many_to_many",
    "Left",
    deletable = Deletable.TRUE,
    allowDeleteAll = AllowDeleteAll.TRUE,
    nameFieldForPkAndNameDto = "someString",
) {
    field("someInt", FieldTypes.int) {
        fieldDisplayName("Some Int")
        editableByUser()
    }
    field("someString", FieldTypes.string) {
        fieldDisplayName("Some String")
        lengthConstraint(max = 100)
        editableByUser()
    }
    crud {
        apis {
            create()
            update()
            delete()
        }
    }
}
```

- [ ] **Step 2: Replace `leftSearchableDtoHtmlTableDef` and add `leftCrudDef`**

Replace the current `leftSearchableDtoHtmlTableDef` block (lines 1136–1142) with:

```kotlin
val leftSearchableDtoHtmlTableDef = dtoHtmlTable(leftSearchableDtoDef, withAddButton = true) {
    columnFromDto("someStringFromLeft", "someString") { header("Some String From Left") }
    columnFromDto("someIntFromLeft", "someInt") { header("Some Int From Left") }
    columnFromDto("rightEntities") { header("Right Entities") }
    editActionColumn()
    deleteActionColumn()
}


val leftCrudDef = crudTableDef(leftSearchableDtoHtmlTableDef, leftEntityDef.entityCrudApiDef!!)
```

- [ ] **Step 3: Verify spec compiles**

```bash
./gradlew :maia-showcase:spec:build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt
git commit -m "feat(many-to-many): add CRUD APIs to leftEntityDef and upgrade leftSearchableDtoHtmlTableDef"
```

```json:metadata
{"files": ["maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt"], "verifyCommand": "./gradlew :maia-showcase:spec:build", "acceptanceCriteria": ["leftEntityDef has crud block with create/update/delete", "leftSearchableDtoHtmlTableDef has withAddButton and action columns", "leftCrudDef declared", "spec build passes"], "requiresUserVerification": false}
```

---

## Task 2: Regenerate Kotlin + TypeScript code

**Goal:** Regenerate all affected code layers so the new CRUD APIs, DAOs, endpoints, and Angular components exist.

**Files:**
- Generate: `maia-showcase/domain/src/generated/…`
- Generate: `maia-showcase/dao/src/generated/…`
- Generate: `maia-showcase/repo/src/generated/…`
- Generate: `maia-showcase/service/src/generated/…`
- Generate: `maia-showcase/web/src/generated/…`
- Generate: `maia-showcase/maia-showcase-ui/src/generated/…`

**Acceptance Criteria:**
- [ ] `LeftCreateRequestDto.kt` exists in domain generated
- [ ] `LeftCrudEndpoint.kt` exists in web generated
- [ ] `left-searchable-crud-table.component.ts` exists in UI generated
- [ ] `./gradlew :maia-showcase:web:build -x test` passes

**Verify:** `./gradlew :maia-showcase:web:build -x test` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Regenerate all Kotlin layers in dependency order**

```bash
./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:dao:maiaGeneration :maia-showcase:repo:maiaGeneration :maia-showcase:service:maiaGeneration :maia-showcase:web:maiaGeneration
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Regenerate TypeScript**

```bash
./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Verify new files exist**

```bash
ls maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftCreateRequestDto.kt
ls maia-showcase/web/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftCrudEndpoint.kt
ls maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many_to_many/left-searchable-crud-table.component.ts
```

Expected: all three files present (no "No such file" errors)

- [ ] **Step 4: Verify Kotlin backend compiles**

```bash
./gradlew :maia-showcase:web:build -x test
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit generated code**

```bash
git add maia-showcase/domain/src/generated maia-showcase/dao/src/generated maia-showcase/repo/src/generated maia-showcase/service/src/generated maia-showcase/web/src/generated maia-showcase/maia-showcase-ui/src/generated
git commit -m "feat(many-to-many): regenerate CRUD code for LeftEntity and LeftSearchable table"
```

```json:metadata
{"files": ["maia-showcase/domain/src/generated", "maia-showcase/dao/src/generated", "maia-showcase/repo/src/generated", "maia-showcase/service/src/generated", "maia-showcase/web/src/generated", "maia-showcase/maia-showcase-ui/src/generated"], "verifyCommand": "./gradlew :maia-showcase:web:build -x test", "acceptanceCriteria": ["LeftCreateRequestDto.kt generated", "LeftCrudEndpoint.kt generated", "left-searchable-crud-table.component.ts generated", "web build passes"], "requiresUserVerification": false}
```

---

## Task 3: Angular blotter page + route

**Goal:** Create the `LeftSearchableBlotterPage` Angular component and wire it to route `/left_searchable`.

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/left-searchable-blotter/left-searchable-blotter-page.ts`
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/left-searchable-blotter/left-searchable-blotter-page.html`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`

**Acceptance Criteria:**
- [ ] Page component imports `LeftSearchableCrudTableComponent`
- [ ] HTML uses `dataPageId="left_searchable_blotter"`
- [ ] Route `/left_searchable` added to `app.routes.ts`

**Verify:** `cd maia-showcase/maia-showcase-ui && npx ng build --configuration production` → `Application bundle generation complete`

**Steps:**

- [ ] **Step 1: Create page component**

Create `maia-showcase/maia-showcase-ui/src/app/pages/left-searchable-blotter/left-searchable-blotter-page.ts`:

```typescript
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    LeftSearchableCrudTableComponent
} from '@app/gen-components/org/maiaframework/showcase/many_to_many/left-searchable-crud-table.component';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        LeftSearchableCrudTableComponent
    ],
    selector: 'app-left-searchable-blotter-page',
    templateUrl: './left-searchable-blotter-page.html',
})
export class LeftSearchableBlotterPage {

}
```

- [ ] **Step 2: Create page template**

Create `maia-showcase/maia-showcase-ui/src/app/pages/left-searchable-blotter/left-searchable-blotter-page.html`:

```html
<app-page-layout pageTitle="Left Searchable" dataPageId="left_searchable_blotter">
    <app-left-searchable-crud-table></app-left-searchable-crud-table>
</app-page-layout>
```

- [ ] **Step 3: Add route**

In `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`, add after the `users` route entry:

```typescript
{
    path: 'left_searchable',
    loadComponent: () =>
        import('@app/pages/left-searchable-blotter/left-searchable-blotter-page').then(
            (m) => m.LeftSearchableBlotterPage,
        ),
},
```

- [ ] **Step 4: Verify Angular build**

```bash
cd maia-showcase/maia-showcase-ui && npx ng build --configuration production
```

Expected: `Application bundle generation complete` (no TypeScript errors)

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/pages/left-searchable-blotter/ maia-showcase/maia-showcase-ui/src/app/app.routes.ts
git commit -m "feat(many-to-many): add LeftSearchable Angular blotter page and route"
```

```json:metadata
{"files": ["maia-showcase/maia-showcase-ui/src/app/pages/left-searchable-blotter/left-searchable-blotter-page.ts", "maia-showcase/maia-showcase-ui/src/app/pages/left-searchable-blotter/left-searchable-blotter-page.html", "maia-showcase/maia-showcase-ui/src/app/app.routes.ts"], "verifyCommand": "cd maia-showcase/maia-showcase-ui && npx ng build --configuration production", "acceptanceCriteria": ["page component created", "route /left_searchable added", "Angular build passes"], "requiresUserVerification": false}
```

---

## Task 4: Kotlin page object + AbstractPlaywrightTest

**Goal:** Create `LeftSearchableBlotterPage` page object and register it in `AbstractPlaywrightTest`.

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftSearchableBlotterPage.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`

**Acceptance Criteria:**
- [ ] `LeftSearchableBlotterPage` bound to `/left_searchable` + `left_searchable_blotter`
- [ ] All CRUD methods implemented (clickAddButton, fillCreateForm, clickSubmitButton, clickEditButtonForFirstRow, fillEditForm, clickDeleteButtonForFirstRow, clickYesButton, clickCancelButton, all asserts)
- [ ] `leftSearchableBlotterPage` field in `AbstractPlaywrightTest` declared and initialized

**Verify:** `./gradlew :maia-showcase:app:compileTestKotlin` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Create `LeftSearchableBlotterPage.kt`**

Create `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftSearchableBlotterPage.kt`:

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class LeftSearchableBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/left_searchable",
    "left_searchable_blotter"
) {


    fun clickAddButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
        page.locator("mat-dialog-container").waitFor()

    }


    fun fillCreateForm(
        someInt: String = "42",
        someString: String = "testleft",
    ) {

        page.locator("input[name='someInt']").fill(someInt)
        page.locator("input[name='someString']").fill(someString)

    }


    fun clickSubmitButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))

    }


    fun assertCreateDialogClosed() {

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


    fun fillEditForm(
        someString: String = "testleft_edited",
    ) {

        page.locator("input[name='someString']").fill(someString)

    }


    fun assertEditDialogClosed() {

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

- [ ] **Step 2: Update `AbstractPlaywrightTest`**

In `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`:

Add import at the top with the other page imports:
```kotlin
import org.maiaframework.showcase.testing.pages.LeftSearchableBlotterPage
```

Add field after `bravoBlotterPage`:
```kotlin
protected lateinit var leftSearchableBlotterPage: LeftSearchableBlotterPage
```

Add initialization in `initPlaywrightPage()` after `bravoBlotterPage = BravoBlotterPage(page, urlHelper)`:
```kotlin
leftSearchableBlotterPage = LeftSearchableBlotterPage(page, urlHelper)
```

- [ ] **Step 3: Verify compilation**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftSearchableBlotterPage.kt maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt
git commit -m "feat(many-to-many): add LeftSearchableBlotterPage page object and register in AbstractPlaywrightTest"
```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftSearchableBlotterPage.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt"], "verifyCommand": "./gradlew :maia-showcase:app:compileTestKotlin", "acceptanceCriteria": ["LeftSearchableBlotterPage created with all methods", "leftSearchableBlotterPage field in AbstractPlaywrightTest", "compileTestKotlin passes"], "requiresUserVerification": false}
```

---

## Task 5: Playwright test + Fixtures cleanup

**Goal:** Write `LeftSearchableCrudPlaywrightTest` and add left/join table truncation to `Fixtures.resetDatabaseState()` so each run starts clean.

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftSearchableCrudPlaywrightTest.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/fixtures/Fixtures.kt`

**Acceptance Criteria:**
- [ ] `Fixtures.resetDatabaseState()` truncates `maia.left_to_right_many_to_many_join` and `maia.left` (cascade)
- [ ] Test class has `@BeforeAll` (initAdminUserFixture + resetDatabaseState) and `@BeforeEach` (logOut)
- [ ] `crud journey` test covers create → assertContains → edit → assertContains → cancel-delete → assertContains → confirm-delete → assertDoesNotContain
- [ ] `./gradlew :maia-showcase:app:compileTestKotlin` passes

**Verify:** `./gradlew :maia-showcase:app:compileTestKotlin` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Add truncation to `Fixtures.resetDatabaseState()`**

In `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/fixtures/Fixtures.kt`, add the two imports at the top with other meta imports:

```kotlin
import org.maiaframework.showcase.many_to_many.LeftEntityMeta
import org.maiaframework.showcase.many_to_many.LeftToRightManyToManyJoinEntityMeta
```

In `resetDatabaseState()`, add after `truncateTable(AlphaEntityMeta.SCHEMA_AND_TABLE_NAME)`:

```kotlin
truncateTable(LeftToRightManyToManyJoinEntityMeta.SCHEMA_AND_TABLE_NAME)
truncateTable(LeftEntityMeta.SCHEMA_AND_TABLE_NAME)
```

- [ ] **Step 2: Create the test**

Create `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftSearchableCrudPlaywrightTest.kt`:

```kotlin
package org.maiaframework.showcase.many_to_many

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest


class LeftSearchableCrudPlaywrightTest : AbstractPlaywrightTest() {


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
        `navigate to the`(leftSearchableBlotterPage)
        leftSearchableBlotterPage.apply {
            clickAddButton()
            fillCreateForm()
            clickSubmitButton()
            assertCreateDialogClosed()
            assertTableContainsValue("testleft")

            clickEditButtonForFirstRow()
            fillEditForm()
            clickSubmitButton()
            assertEditDialogClosed()
            assertTableContainsValue("testleft_edited")

            // Cancel path
            clickDeleteButtonForFirstRow()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("testleft_edited")

            // Confirm delete path
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("testleft_edited")
        }

    }


}
```

- [ ] **Step 3: Verify compilation**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftSearchableCrudPlaywrightTest.kt maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/fixtures/Fixtures.kt
git commit -m "feat(many-to-many): add LeftSearchableCrudPlaywrightTest and truncate left tables in fixtures"
```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftSearchableCrudPlaywrightTest.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/fixtures/Fixtures.kt"], "verifyCommand": "./gradlew :maia-showcase:app:compileTestKotlin", "acceptanceCriteria": ["Fixtures truncates left and join tables", "test class compiles with full crud journey"], "requiresUserVerification": false}
```
