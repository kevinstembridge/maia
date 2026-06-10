# RightMany CRUD Playwright Test - Implement TODOs - Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement all TODOs in `RightManyCrudPlaywrightTest`, including making existing many-to-many join effectiveFrom/To dates editable and renaming a misleadingly-named blotter column.

**Architecture:** (1) Generic generator change to `AbstractCrudReactiveFormHtmlRenderer` makes existing join entries' effectiveFrom/To editable via mat-datepicker/timepicker bound with `[(ngModel)]` (no backend change - update() already deletes & recreates joins from the submitted array). (2) Spec-level rename of `rightEntities` -> `leftEntities` for the RightMany side only (data already correct, just mislabeled). (3) New page-object methods + full rewrite of the Playwright test covering create -> multiple edits -> delete, with view/blotter/history assertions.

**Tech Stack:** Kotlin code generator (maia-gen), Angular 20 + Angular Material (generated UI), Playwright/JUnit5 (Kotlin) e2e tests, Spring Boot/Postgres backend.

**User Verification:** NO - no user verification required (test-only / generator change, verified via automated test run).

---

## Task 1: Make existing join effectiveFrom/To editable in the generator

**Goal:** Replace the static effectiveFrom/effectiveTo `<span>` display for existing many-to-many join entries with editable date/time pickers, and give the remove button a stable selector class.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt:33-108` (`renderManyToManyTimestampedFields()`)
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyEditPage.kt` (`removeJoinEntry`)
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt` (`` `remove the LeftEntity named` ``)
- Regenerate: `maia-showcase/maia-showcase-ui/src/generated/...` (via gradle)

**Acceptance Criteria:**
- [ ] Each `.join-entry` row renders 4 mat-form-fields (effective-from date, effective-from time, effective-to date, effective-to time) bound to `join.effectiveFrom` / `join.effectiveTo` via two-way `ngModel` with `standalone: true`.
- [ ] The remove button has class `join-remove-button` and is the only `button[type='button']` matched by that class within `.join-entry`.
- [ ] `LeftManyCrudPlaywrightTest` (existing, passing test that uses `removeJoinEntry`) still passes after regeneration.

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.LeftManyCrudPlaywrightTest"` -> BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Replace the join-entry template in the renderer**

In `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt`, replace lines 37-49 (the `<div class="join-entries">...</div>` block) with:

```kotlin
            append("""
                |        <div class="join-entries">
                |            @for (join of ${field.joinsFieldName}; track join.entityId) {
                |                <div class="join-entry">
                |                    <span>{{ join.entityName }}</span>
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective From Date</mat-label>
                |                        <input matInput class="join-effective-from-date" [matDatepicker]="effectiveFromDatePicker"
                |                            [(ngModel)]="join.effectiveFrom" [ngModelOptions]="{standalone: true}" />
                |                        <mat-datepicker-toggle matIconSuffix [for]="effectiveFromDatePicker"></mat-datepicker-toggle>
                |                        <mat-datepicker #effectiveFromDatePicker></mat-datepicker>
                |                    </mat-form-field>
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective From Time</mat-label>
                |                        <input matInput class="join-effective-from-time" [matTimepicker]="effectiveFromTimePicker"
                |                            [(ngModel)]="join.effectiveFrom" [ngModelOptions]="{standalone: true}" />
                |                        <mat-timepicker #effectiveFromTimePicker></mat-timepicker>
                |                        <mat-timepicker-toggle matSuffix [for]="effectiveFromTimePicker"></mat-timepicker-toggle>
                |                    </mat-form-field>
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective To Date</mat-label>
                |                        <input matInput class="join-effective-to-date" [matDatepicker]="effectiveToDatePicker"
                |                            [(ngModel)]="join.effectiveTo" [ngModelOptions]="{standalone: true}" />
                |                        <mat-datepicker-toggle matIconSuffix [for]="effectiveToDatePicker"></mat-datepicker-toggle>
                |                        <mat-datepicker #effectiveToDatePicker></mat-datepicker>
                |                    </mat-form-field>
                |                    <mat-form-field appearance="outline">
                |                        <mat-label>Effective To Time</mat-label>
                |                        <input matInput class="join-effective-to-time" [matTimepicker]="effectiveToTimePicker"
                |                            [(ngModel)]="join.effectiveTo" [ngModelOptions]="{standalone: true}" />
                |                        <mat-timepicker #effectiveToTimePicker></mat-timepicker>
                |                        <mat-timepicker-toggle matSuffix [for]="effectiveToTimePicker"></mat-timepicker-toggle>
                |                    </mat-form-field>
                |                    <button mat-icon-button type="button" class="join-remove-button" (click)="${field.removeMethodName}(${'$'}index)">
                |                        <mat-icon>delete</mat-icon>
                |                    </button>
                |                </div>
                |            }
                |        </div>
```

- [ ] **Step 2: Regenerate the showcase UI**

Run: `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration`

Confirm the regenerated `right-many-entity-edit-form.html` and `left-many-entity-edit-form.html` (under `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/`) contain the new `.join-entry` markup with `join-effective-from-date` / `join-effective-from-time` / `join-effective-to-date` / `join-effective-to-time` / `join-remove-button` classes.

- [ ] **Step 3: Update `LeftManyEditPage.removeJoinEntry`**

In `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyEditPage.kt`, change:

```kotlin
    fun removeJoinEntry(entityName: String) {

        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .locator("button[type='button']")
            .click()
```

to:

```kotlin
    fun removeJoinEntry(entityName: String) {

        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .locator("button.join-remove-button")
            .click()
```

- [ ] **Step 4: Update `RightManyEditPage.\`remove the LeftEntity named\``**

In `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt`, change:

```kotlin
    fun `remove the LeftEntity named`(entityName: String) {

        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .locator("button[type='button']")
            .click()
```

to:

```kotlin
    fun `remove the LeftEntity named`(entityName: String) {

        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .locator("button.join-remove-button")
            .click()
```

- [ ] **Step 5: Run the existing LeftMany Playwright test**

Run: `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.LeftManyCrudPlaywrightTest"`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt \
        maia-showcase/maia-showcase-ui/src/generated \
        maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyEditPage.kt \
        maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt
git commit -m "feat: make existing many-to-many join effectiveFrom/To dates editable"
```

---

## Task 2: Rename misleading "rightEntities" field to "leftEntities" on the RightMany side

**Goal:** `RightManyBlotterRowDto`/`RightManySearchableDto` field and the RightMany blotter column currently called `rightEntities`/"Right Entities" actually contain the joined **LeftMany** entities. Rename to `leftEntities`/"Left Entities".

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt:1413` and `:1427`
- Regenerate: `domain`, `dao`, `repo`, `service`, `web`, `maia-showcase-ui` (via gradle)

**Acceptance Criteria:**
- [ ] `rightManySearchableDtoDef` declares `manyToManyField("leftEntities", leftToRightManyToManyJoinEntityDef)`.
- [ ] `rightManyBlotterDef` declares `columnFromDto("leftEntities") { header("Left Entities") }`.
- [ ] `leftManySearchableDtoDef` / `leftManyBlotterDef` (lines ~1455/1469) are unchanged.
- [ ] Full project compiles after regeneration.

**Verify:** `./gradlew :maia-showcase:app:compileTestKotlin :maia-showcase:maia-showcase-ui:compileTestKotlin` -> BUILD SUCCESSFUL (Note: the UI module's "compile" is its TS build; if there is no such Kotlin task for maia-showcase-ui, use `./gradlew :maia-showcase:app:compileTestKotlin` plus `./gradlew :maia-showcase:maia-showcase-ui:build`)

**Steps:**

- [ ] **Step 1: Update the spec**

In `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt`, in `rightManySearchableDtoDef` (around line 1413), change:

```kotlin
        manyToManyField("rightEntities", leftToRightManyToManyJoinEntityDef)
```

to:

```kotlin
        manyToManyField("leftEntities", leftToRightManyToManyJoinEntityDef)
```

In `rightManyBlotterDef` (around line 1427), change:

```kotlin
        columnFromDto("rightEntities") { header("Right Entities") }
```

to:

```kotlin
        columnFromDto("leftEntities") { header("Left Entities") }
```

Do **not** change `leftManySearchableDtoDef` / `leftManyBlotterDef` (around lines 1455/1469) - those are correctly named already (they expose RightMany entities).

- [ ] **Step 2: Regenerate affected modules**

Run:
```bash
./gradlew :maia-showcase:domain:maiaGeneration \
          :maia-showcase:dao:maiaGeneration \
          :maia-showcase:repo:maiaGeneration \
          :maia-showcase:service:maiaGeneration \
          :maia-showcase:web:maiaGeneration \
          :maia-showcase:maia-showcase-ui:maiaGeneration
```

- [ ] **Step 3: Build and fix any remaining references**

Run: `./gradlew :maia-showcase:app:compileTestKotlin`

If this fails referencing `rightEntities` on `RightManyBlotterRowDto`/`RightManySearchableDto`/`RightManyDto` types, grep for stale references and update them:

```bash
grep -rn "RightManyBlotterRowDto\|RightManySearchableDto" maia-showcase --include=*.kt | grep -v /generated/ | grep -i rightEntities
```

(Expected: no hits, since no hand-written test references this field for the RightMany side - only the LeftMany side's `rightEntities`, which is unchanged.)

- [ ] **Step 4: Build the UI**

Run: `./gradlew :maia-showcase:maia-showcase-ui:build`
Expected: BUILD SUCCESSFUL. If the Angular build fails referencing `rightEntities` on `RightManyBlotterRowDto`, check `right-many-blotter.ts` was regenerated to use `leftEntities`/"Left Entities" (Step 2 should have handled this).

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt \
        maia-showcase/domain/src/generated maia-showcase/dao/src/generated \
        maia-showcase/repo/src/generated maia-showcase/service/src/generated \
        maia-showcase/web/src/generated maia-showcase/maia-showcase-ui/src/generated
git commit -m "fix: rename misleading rightEntities field to leftEntities on RightMany blotter/searchable DTO"
```

---

## Task 3: Add page-object methods for view-field assertions and join effectiveFrom editing

**Goal:** Add the page-object support needed by the rewritten test: reading detail-view field values, and editing an existing join's effectiveFrom date/time.

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyViewPage.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt`

**Acceptance Criteria:**
- [ ] `RightManyViewPage` has `` `assert the view shows`(someInt: String, someString: String, version: String) ``, asserting the `.detail-row` for "Some Int", "Some String", "Version" each have the matching `.detail-value`.
- [ ] `RightManyEditPage` has `` `set the effectiveFrom for the LeftEntity named`(entityName: String, date: String, time: String) `` that fills the new date/time inputs for that join entry.
- [ ] Both files compile.

**Verify:** `./gradlew :maia-showcase:app:compileTestKotlin` -> BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add view-field assertion to `RightManyViewPage`**

Replace the full contents of `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyViewPage.kt` with:

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
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


    fun `assert the view shows`(someInt: String, someString: String, version: String) {

        assertThat(detailValue("Some Int")).hasText(someInt)
        assertThat(detailValue("Some String")).hasText(someString)
        assertThat(detailValue("Version")).hasText(version)

    }


    private fun detailValue(label: String): Locator {

        return page.locator(".detail-row")
            .filter(Locator.FilterOptions().setHasText(label))
            .locator(".detail-value")

    }


}
```

- [ ] **Step 2: Add effectiveFrom-editing method to `RightManyEditPage`**

In `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt`, add this method after `` `assert a LeftEntity is visible with name` ``:

```kotlin
    fun `set the effectiveFrom for the LeftEntity named`(entityName: String, date: String, time: String) {

        val joinEntry = page.locator(".join-entry").filter(Locator.FilterOptions().setHasText(entityName))
        joinEntry.locator("input.join-effective-from-date").fill(date)
        joinEntry.locator("input.join-effective-from-time").fill(time)
        joinEntry.locator("input.join-effective-from-time").press("Tab")

    }
```

- [ ] **Step 3: Compile**

Run: `./gradlew :maia-showcase:app:compileTestKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyViewPage.kt \
        maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt
git commit -m "test: add view-field assertions and join effectiveFrom editing to RightMany page objects"
```

---

## Task 4: Rewrite `RightManyCrudPlaywrightTest` to implement the full journey

**Goal:** Implement the full create -> edit (x3) -> delete journey with view/blotter/history assertions, replacing all TODO comments.

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyBlotterPage.kt` (no change expected - `` `assert the table contains value` `` already works for chip cells, confirm during test run)

**Test journey and version numbering:**
1. Create: someInt=42, someString="testright", left-1 joined (no dates) -> version 1 (CREATE)
2. Edit 1: set left-1's effectiveFrom date/time, add left-2, someString -> "testright_edited" -> version 2 (UPDATE)
3. Edit 2: add left-3, remove left-1, someString -> "testright_edited2" -> version 3 (UPDATE)
4. Edit 3: remove all left entities (left-2, left-3), someString -> "testright_edited3" -> version 4 (UPDATE)
5. Delete -> version 5 (DELETE)

History is verified once at the end with all 5 rows (CREATE, 3x UPDATE, DELETE) - intermediate steps verify view + blotter only, to keep the test focused and avoid redundant slow history-page navigation.

**Acceptance Criteria:**
- [ ] `effectiveTimestampCrudService` constructor param removed (unused leftover).
- [ ] `LeftToRightManyToManyJoinDao` autowired and used to assert left-1's `effectiveFrom` is non-null after Edit 1.
- [ ] All TODO comments removed or replaced with implementation, except the documented history-blotter `leftEntities` follow-up (see below).
- [ ] Full journey implemented per the steps above.

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest"` -> BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Check `LeftToRightManyToManyJoinDao` API**

Run: `grep -n "fun " maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftToRightManyToManyJoinDao.kt`

Confirm a `findByRight(rightId: DomainId): List<LeftToRightManyToManyJoinEntity>` (or equivalent) method exists - it's referenced by the generated `RightManyCrudService.update()` (see `maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyCrudService.kt:97`: `this.leftToRightManyToManyJoinRepo.findByRight(id)`). Use the matching DAO method name found.

- [ ] **Step 2: Replace `RightManyCrudPlaywrightTest.kt`**

Replace the full contents of `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt` with:

```kotlin
package org.maiaframework.showcase.many_to_many

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.elasticsearch.EsDocHolder
import org.maiaframework.elasticsearch.index.EsIndexOps
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.domain.DomainId
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


    private val left1 = LeftManyEntityTestBuilder(someString = "left-1").build()


    private val left2 = LeftManyEntityTestBuilder(someString = "left-2").build()


    private val left3 = LeftManyEntityTestBuilder(someString = "left-3").build()


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        fixtures.resetDatabaseState()
        rightManyDao.deleteAll()

        leftManyDao.bulkInsert(listOf(left1, left2, left3))

        `upsert to ElasticSearch`(left1)
        `upsert to ElasticSearch`(left2)
        `upsert to ElasticSearch`(left3)

    }


    private fun `upsert to ElasticSearch`(leftManyEntity: LeftManyEntity) {

        esIndexOps.upsert(
            EsDocHolder(
                id = leftManyEntity.id.toString(),
                doc = LeftManyTypeaheadV1EsDoc(id = leftManyEntity.id, someString = leftManyEntity.someString),
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

        rightManyBlotterPage.clickAddButton()

        rightManyCreatePage.apply {

            assertOnPage()
            `enter form input`(someInt = "42", someString = "testright")

            `click the Add button for Left entities`()
            `select a Left entity in the mini form`("left-1")
            `click to confirm adding the Left entity`()

            `click the Submit button`()

        }

        rightManyViewPage.apply {

            assertOnPage()
            `assert the view shows`(someInt = "42", someString = "testright", version = "1")

        }

        val rightManyEntityId = page.url().substringAfterLast("/")

        `navigate to the`(rightManyBlotterPage)

        rightManyBlotterPage.apply {

            `assert the table contains value`("testright")

            `click to delete the first row`()
            `assert the FK Check dialog shows an error`()
            `dismiss the FK Check dialog`()

            `click to edit the first row`()

        }

        rightManyEditPage.apply {

            assertOnPage()
            `assert a LeftEntity is visible with name`("left-1")

            `set the effectiveFrom for the LeftEntity named`("left-1", "1/15/2026", "10:00 AM")

            `click the Add button for Left entities`()
            `select a Left entity in the mini form`("left-2")
            `click to confirm adding the Left entity`()

            `enter form input`(someString = "testright_edited")
            `click the Submit button`()

        }

        rightManyViewPage.apply {

            assertOnPage()
            `assert the view shows`(someInt = "42", someString = "testright_edited", version = "2")

        }

        val joinsAfterEdit1 = leftToRightManyToManyJoinDao.findByRight(DomainId.fromString(rightManyEntityId))
        val left1JoinAfterEdit1 = joinsAfterEdit1.first { it.left == left1.id }
        assertThat(left1JoinAfterEdit1.effectiveFrom).isNotNull()

        `navigate to the`(rightManyBlotterPage)

        rightManyBlotterPage.apply {

            `assert the table contains value`("testright_edited")
            `assert the table contains value`("left-1")
            `assert the table contains value`("left-2")

            `click to edit the first row`()

        }

        rightManyEditPage.apply {

            assertOnPage()
            `assert a LeftEntity is visible with name`("left-1")
            `assert a LeftEntity is visible with name`("left-2")

            `click the Add button for Left entities`()
            `select a Left entity in the mini form`("left-3")
            `click to confirm adding the Left entity`()

            `remove the LeftEntity named`("left-1")

            `enter form input`(someString = "testright_edited2")
            `click the Submit button`()

        }

        rightManyViewPage.apply {

            assertOnPage()
            `assert the view shows`(someInt = "42", someString = "testright_edited2", version = "3")

        }

        `navigate to the`(rightManyBlotterPage)

        rightManyBlotterPage.apply {

            `assert the table contains value`("testright_edited2")
            `assert the table contains value`("left-2")
            `assert the table contains value`("left-3")
            `assert the table does not contain value`("left-1")

            `click to edit the first row`()

        }

        rightManyEditPage.apply {

            assertOnPage()
            `assert a LeftEntity is visible with name`("left-2")
            `assert a LeftEntity is visible with name`("left-3")

            `remove the LeftEntity named`("left-2")
            `remove the LeftEntity named`("left-3")

            `enter form input`(someString = "testright_edited3")
            `click the Submit button`()

        }

        rightManyViewPage.apply {

            assertOnPage()
            `assert the view shows`(someInt = "42", someString = "testright_edited3", version = "4")

        }

        `navigate to the`(rightManyBlotterPage)

        rightManyBlotterPage.apply {

            `assert the table contains value`("testright_edited3")

            // Cancel delete: FK check passes (no join records), delete dialog appears, cancel
            `click to delete the first row`()
            `wait for the Delete dialog`()
            `click the Cancel button`()
            `assert the Delete dialog closed`()
            `assert the table contains value`("testright_edited3")

            // Confirm delete
            `click to delete the first row`()
            `wait for the Delete dialog`()
            `click the Yes button`()
            `assert the Delete dialog closed`()
            `assert the table does not contain value`("testright_edited3")

        }

        rightManyHistoryBlotterPage.apply {

            `navigate to the history page for entity`(rightManyEntityId)

            // TODO assert that the correct values are displayed in the leftEntities field of each row -
            // requires new generator support for effective-dated history on many-to-many join entities
            // (the join entity has withEffectiveTimestamps but no recordVersionHistory).
            `assert the table contains a row with`(changeType = "CREATE", someInt = "42", someString = "testright", version = "1")
            `assert the table contains a row with`(changeType = "UPDATE", someInt = "42", someString = "testright_edited", version = "2")
            `assert the table contains a row with`(changeType = "UPDATE", someInt = "42", someString = "testright_edited2", version = "3")
            `assert the table contains a row with`(changeType = "UPDATE", someInt = "42", someString = "testright_edited3", version = "4")
            `assert the table contains a row with`(changeType = "DELETE", someInt = "42", someString = "testright_edited3", version = "5")

        }

    }


}
```

- [ ] **Step 3: Run the test**

Run: `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest"`

- [ ] **Step 4: Iterate on failures**

Likely areas to debug if it fails:
- `` `set the effectiveFrom for the LeftEntity named` `` date/time format - try alternate formats (e.g. `"15 Jan 2026"`, `"10:00am"`) matching the locale configured for the app under test if `"1/15/2026"` / `"10:00 AM"` don't parse.
- Version numbers - if `RightManyEntityUpdater`/`recordVersionHistory` only increments version when a tracked field actually changes, confirm `someString` changes on every edit (it does, per the plan above) so each edit produces a new version.
- `DomainId.fromString` - confirm the actual factory method name on `DomainId` (`grep -n "fun " libs/maia-domain/src/main/kotlin/org/maiaframework/domain/DomainId.kt` or equivalent) and adjust if named differently (e.g. `DomainId.of(...)`).
- `leftToRightManyToManyJoinDao.findByRight` - confirm exact method name from Step 1 of this task and adjust if different.

Fix issues directly in `RightManyCrudPlaywrightTest.kt` (and page objects if a selector needs adjustment) and re-run until passing.

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt \
        maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages
git commit -m "test: implement full RightMany CRUD journey with join management and view/blotter/history assertions"
```

---

## Unresolved questions
- Exact date/time input format accepted by the generated mat-datepicker/mat-timepicker inputs in this app's configured locale - to be determined empirically in Task 4 Step 4.
- Exact `DomainId` and `LeftToRightManyToManyJoinDao` method names - to be confirmed in Task 4 Step 1 (signatures referenced in this plan are best-effort based on generated code conventions seen elsewhere in the codebase).
