# Bravo CRUD Playwright Test Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a full Playwright CRUD test for BravoEntity, backed by spec-generated UI with a typeahead dropdown for the AlphaEntity foreign key.

**Architecture:** Add `bravoDtoHtmlTableDef` + `bravoCrudDef` to the spec, regenerate Angular + web layers, create a Bravo blotter Angular page, add Alpha fixture support, create `BravoBlotterPage` page object, and wire up `BravoCrudPlaywrightTest` following the exact same pattern as `AllFieldTypesCrudPlaywrightTest`.

**Tech Stack:** Kotlin/Spring Boot, Angular 24, Angular Material (`mat-autocomplete`), Playwright, JUnit 5, AG Grid.

---

## File Map

| Action | Path |
|--------|------|
| Modify | `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt` |
| Generate (inspect) | `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/join/bravo-create-dialog.component.html` |
| Generate (inspect) | `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/join/bravo-edit-dialog.component.html` |
| Create | `maia-showcase/maia-showcase-ui/src/app/pages/bravo-blotter/bravo-blotter-page.ts` |
| Create | `maia-showcase/maia-showcase-ui/src/app/pages/bravo-blotter/bravo-blotter-page.html` |
| Modify | `maia-showcase/maia-showcase-ui/src/app/app.routes.ts` |
| Modify | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/fixtures/Fixtures.kt` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/BravoBlotterPage.kt` |
| Modify | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/join/BravoCrudPlaywrightTest.kt` |

---

## Task 1: Add Bravo spec entries

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt`

- [ ] **Step 1: Locate the insertion point**

Open `MaiaShowcaseSpec.kt`. Find `bravoSearchableDtoDef` (around line 913). The new entries go immediately after it, before `charlieSearchableDtoDef`.

- [ ] **Step 2: Add `bravoDtoHtmlTableDef` and `bravoCrudDef`**

Insert after the closing `}` of `bravoSearchableDtoDef`:

```kotlin
    val bravoDtoHtmlTableDef = dtoHtmlTable(bravoSearchableDtoDef) {
        columnFromDto(dtoFieldName = "tableStringFromAlpha", fieldPathInSourceData = "dtoStringFromAlpha")
        columnFromDto(dtoFieldName = "tableStringFromBravo", fieldPathInSourceData = "dtoStringFromBravo")
        columnFromDto(dtoFieldName = "createdTimestampUtc", fieldPathInSourceData = "createdTimestampUtc")
    }


    val bravoCrudDef = crudTableDef(bravoDtoHtmlTableDef, bravoEntityDef.entityCrudApiDef!!)
```

- [ ] **Step 3: Commit**

```bash
git add maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt
git commit -m "feat: add bravoDtoHtmlTableDef and bravoCrudDef to spec"
```

---

## Task 2: Regenerate web and Angular UI layers

**Files:**
- Regenerated: `maia-showcase/web/src/generated/` (Spring controllers + DTOs)
- Regenerated: `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/join/` (Angular components)

- [ ] **Step 1: Run web layer generation**

```bash
./gradlew :maia-showcase:web:maiaGeneration
```

Expected: `BUILD SUCCESSFUL`. New or updated files appear in `maia-showcase/web/src/generated/kotlin/main/`.

- [ ] **Step 2: Run Angular UI generation**

```bash
./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
```

Expected: `BUILD SUCCESSFUL`. New files appear in `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/join/`:
- `bravo-create-dialog.component.html`
- `bravo-create-dialog.component.ts`
- `bravo-edit-dialog.component.html`
- `bravo-edit-dialog.component.ts`
- `bravo-delete-dialog.component.html`
- `bravo-delete-dialog.component.ts`
- `bravo-crud-table.component.html`
- `bravo-crud-table.component.ts`
- Plus typeahead-related files (service, DTO)

- [ ] **Step 3: Inspect `bravo-create-dialog.component.html` — note the alphaId typeahead input**

Read the generated `bravo-create-dialog.component.html`. Find the input for the Alpha foreign key. Note:
1. The `name` attribute on the typeahead input — expect `name="alphaId"`, but confirm
2. Whether option text is `someString` (from the Alpha fixture)
3. Whether `mat-option` elements appear inside a `mat-autocomplete` panel

Example of the expected pattern (exact attributes may differ — use what you find):
```html
<input name="alphaId" formControlName="alphaId" [matAutocomplete]="alphaAuto" matInput ... />
<mat-autocomplete #alphaAuto="matAutocomplete">
    <mat-option [value]="option.id">{{ option.someString }}</mat-option>
</mat-autocomplete>
```

Record the actual `name` attribute value — you will use it in Task 5.

- [ ] **Step 4: Inspect `bravo-edit-dialog.component.html` — confirm alphaId is read-only or editable**

Read `bravo-edit-dialog.component.html`. If `alphaId` is editable in the edit dialog, note its `name` attribute. If it is read-only (disabled), the `fillEditForm()` method needs no `selectAlpha()` call — only `someString` is editable.

- [ ] **Step 5: Commit generated files**

```bash
git add maia-showcase/web/src/generated/ maia-showcase/maia-showcase-ui/src/generated/
git commit -m "chore: regenerate web and Angular UI layers for bravoCrudDef"
```

---

## Task 3: Create Bravo blotter Angular page and register route

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/bravo-blotter/bravo-blotter-page.ts`
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/bravo-blotter/bravo-blotter-page.html`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`

Reference: `maia-showcase/maia-showcase-ui/src/app/pages/all-field-types-blotter/all-field-types-blotter-page.ts`

- [ ] **Step 1: Create `bravo-blotter-page.ts`**

```typescript
import {Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    BravoCrudTableComponent
} from '@app/gen-components/org/maiaframework/showcase/join/bravo-crud-table.component';

@Component({
    imports: [
        PageLayoutComponent,
        BravoCrudTableComponent
    ],
    selector: 'app-bravo-blotter-page',
    templateUrl: './bravo-blotter-page.html',
})
export class BravoBlotterPage {

}
```

Note: the import path `bravo-crud-table.component` and class name `BravoCrudTableComponent` should match what was actually generated in Task 2. Verify the exact filename and class name in the generated file before writing this import.

- [ ] **Step 2: Create `bravo-blotter-page.html`**

```html
<app-page-layout pageTitle="Bravo" dataPageId="bravo_blotter">
    <app-bravo-crud-table></app-bravo-crud-table>
</app-page-layout>
```

Note: the selector `app-bravo-crud-table` should match the `selector` field in `bravo-crud-table.component.ts`. Verify before writing.

- [ ] **Step 3: Add route to `app.routes.ts`**

In `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`, add a new entry to the `routes` array (e.g., after the `all_field_types` route):

```typescript
    {
        path: 'bravo',
        loadComponent: () =>
            import('@app/pages/bravo-blotter/bravo-blotter-page').then(
                (m) => m.BravoBlotterPage,
            ),
    },
```

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/pages/bravo-blotter/
git add maia-showcase/maia-showcase-ui/src/app/app.routes.ts
git commit -m "feat: add Bravo blotter Angular page and route"
```

---

## Task 4: Add `anAlpha()` to Fixtures

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/fixtures/Fixtures.kt`

Reference: the existing `aUser()` method and `resetDatabaseState()` in the same file.

- [ ] **Step 1: Add AlphaDao constructor parameter and alphaFixtures list**

In `Fixtures.kt`, add to the constructor parameter list and class body:

```kotlin
// Constructor parameter (add alongside existing DAO parameters):
private val alphaDao: AlphaDao,

// Class-level list (add alongside the existing mutableListOf fields):
private val alphaFixtures = mutableListOf<AlphaEntity>()
```

- [ ] **Step 2: Add the `anAlpha()` method**

Add after `aUser()`:

```kotlin
    fun anAlpha(
        someString: String = anyString(),
        someInt: Int = anyInt(),
    ): AlphaEntity {

        val entity = AlphaEntityTestBuilder(someString = someString, someInt = someInt).build()
        this.alphaFixtures.add(entity)
        return entity

    }
```

- [ ] **Step 3: Insert alphaFixtures in `resetDatabaseState()`**

Add alpha truncation and re-insertion to `resetDatabaseState()`. Alpha must be truncated with cascade (handles orphaned Bravo rows from previous test runs) and re-inserted after the cascade. Add these lines at the end of `resetDatabaseState()`, after the existing insert blocks:

```kotlin
        this.jdbcOps.update("truncate ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")

        if (this.alphaFixtures.isNotEmpty()) {
            logger.info("Inserting ${this.alphaFixtures.size} alpha fixtures")
            this.alphaFixtures.forEach { alphaDao.insert(it) }
        }
```

- [ ] **Step 4: Add imports**

Add to the import block at the top of `Fixtures.kt`:

```kotlin
import org.maiaframework.showcase.join.AlphaDao
import org.maiaframework.showcase.join.AlphaEntity
import org.maiaframework.showcase.join.AlphaEntityMeta
import org.maiaframework.showcase.join.AlphaEntityTestBuilder
import org.maiaframework.testing.domain.Anys.anyInt
import org.maiaframework.testing.domain.Anys.anyString
```

- [ ] **Step 5: Compile to verify**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL` with no errors.

- [ ] **Step 6: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/fixtures/Fixtures.kt
git commit -m "feat: add anAlpha() fixture support"
```

---

## Task 5: Create `BravoBlotterPage`

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/BravoBlotterPage.kt`

Reference: `AllFieldTypesBlotterPage.kt` for the overall pattern. The route path is `bravo` and the `dataPageId` is `bravo_blotter` (set in Task 3).

The typeahead input `name` attribute was noted in Task 2, Step 3. This plan assumes `name="alphaId"` — substitute the actual value if it differs.

- [ ] **Step 1: Create `BravoBlotterPage.kt`**

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class BravoBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/bravo",
    "bravo_blotter"
) {


    fun clickAddButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
        page.locator("mat-dialog-container").waitFor()
    }


    fun fillCreateForm(
        alphaSearchTerm: String = "alpha-fixture",
        someInt: String = "42",
        someString: String = "testbravo",
    ) {
        selectAlpha(alphaSearchTerm)
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
            "() => { const c = document.querySelector('.ag-cell[col-id=\"dtoStringFromBravo\"]'); " +
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
        someString: String = "testbravo_edited",
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
            "() => { const c = document.querySelector('.ag-cell[col-id=\"dtoStringFromBravo\"]'); " +
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


    private fun selectAlpha(searchTerm: String) {
        // Fill the typeahead input — name="alphaId" follows the FK naming pattern used throughout
        // (e.g., charlie-create-dialog has name="bravoId"). Verify against generated HTML from Task 2.
        page.locator("input[name='alphaId']").fill(searchTerm)
        // Wait for the autocomplete panel to show an option containing the search term
        page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm)).waitFor()
        page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm)).click()
    }


}
```

- [ ] **Step 2: Compile to verify**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/BravoBlotterPage.kt
git commit -m "feat: add BravoBlotterPage Playwright page object"
```

---

## Task 6: Register `bravoBlotterPage` in `AbstractPlaywrightTest`

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`

- [ ] **Step 1: Add import**

Add to the import block:

```kotlin
import org.maiaframework.showcase.testing.pages.BravoBlotterPage
```

- [ ] **Step 2: Add the property declaration**

Add alongside the other `protected lateinit var` page object declarations (e.g., after `allFieldTypesBlotterPage`):

```kotlin
    protected lateinit var bravoBlotterPage: BravoBlotterPage
```

- [ ] **Step 3: Initialise in `initPlaywrightPage()`**

Add the initialisation alongside the existing page object initialisations in `initPlaywrightPage()`:

```kotlin
        bravoBlotterPage = BravoBlotterPage(page, urlHelper)
```

- [ ] **Step 4: Compile to verify**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt
git commit -m "feat: register bravoBlotterPage in AbstractPlaywrightTest"
```

---

## Task 7: Create `BravoCrudPlaywrightTest`

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/join/BravoCrudPlaywrightTest.kt`

Reference: `AllFieldTypesCrudPlaywrightTest.kt` for structure.

- [ ] **Step 1: Create `BravoCrudPlaywrightTest.kt`**

```kotlin
package org.maiaframework.showcase.join

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest


class BravoCrudPlaywrightTest : AbstractPlaywrightTest() {


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()
        fixtures.anAlpha(someString = "alpha-fixture")
        fixtures.resetDatabaseState()

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(bravoBlotterPage)
        bravoBlotterPage.apply {
            clickAddButton()
            fillCreateForm()
            clickSubmitButton()
            assertCreateDialogClosed()

            clickEditButtonForFirstRow()
            fillEditForm()
            clickSubmitButton()
            assertEditDialogClosed()
            assertTableContainsValue("testbravo_edited")

            // Cancel path
            clickDeleteButtonForFirstRow()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("testbravo_edited")

            // Confirm delete path
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("testbravo_edited")
        }

    }


}
```

- [ ] **Step 2: Compile to verify**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run the test**

Ensure Docker compose is running first (`docker compose -f maia-showcase/compose.yaml up -d`), then:

```bash
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.join.BravoCrudPlaywrightTest"
```

Expected: `BUILD SUCCESSFUL`, 1 test passing. The test navigates to `/bravo`, creates a Bravo with `someInt=42` and `someString=testbravo` using the alpha typeahead, edits `someString` to `testbravo_edited`, and deletes it.

If `selectAlpha()` fails (mat-option not visible), check:
1. The `name` attribute on the typeahead input in the generated `bravo-create-dialog.component.html` (noted in Task 2, Step 3) — update `input[name='alphaId']` if different
2. Whether the typeahead debounce requires a `Thread.sleep()` before waiting for options (add `Thread.sleep(500)` after `fill()` if needed)

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/join/BravoCrudPlaywrightTest.kt
git commit -m "feat: add BravoCrudPlaywrightTest"
```
