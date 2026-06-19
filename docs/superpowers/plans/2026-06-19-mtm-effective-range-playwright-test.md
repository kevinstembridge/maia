# ManyToManyEffectiveRangeCrudPlaywrightTest Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Write a Playwright CRUD test for the system-managed effective-timestamps many-to-many join entity, gated behind a prerequisite generator fix that suppresses date picker inputs for SYSTEM-managed joins.

**Architecture:** Fix the HTML and TS component renderers to skip date-picker fields for SYSTEM-managed timestamped joins, regenerate the showcase UI, add scoped page-object methods, then write the test class following the same pattern as `LeftManyCrudPlaywrightTest`.

**Tech Stack:** Kotlin, JUnit 5, Playwright, Spring Boot test slice, Elasticsearch, Angular (generated code only)

**User Verification:** NO

---

## File Map

| Action | File |
|---|---|
| Modify | `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ManyToManyTimestampedFieldDef.kt` |
| Modify | `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt` |
| Modify | `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt` |
| Regenerated (do not edit) | `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.html` |
| Regenerated (do not edit) | `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.ts` |
| Regenerated (do not edit) | `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-edit-form.html` |
| Regenerated (do not edit) | `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-edit-form.ts` |
| Modify | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyCreatePage.kt` |
| Modify | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyEditPage.kt` |
| Create | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/ManyToManyEffectiveRangeCrudPlaywrightTest.kt` |

---

### Task 0: Fix generator — suppress date-picker fields for SYSTEM-managed timestamped joins

**Goal:** Make the generator skip date-picker UI for any timestamped MtM join whose `managedBy == SYSTEM`.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ManyToManyTimestampedFieldDef.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt`

**Acceptance Criteria:**
- [x] `ManyToManyTimestampedFieldDef` exposes `isManagedBySystem: Boolean`
- [x] HTML renderer skips the four date/time picker fields in the join entries list and mini-form when `isManagedBySystem`
- [x] TS component renderer skips `effectiveFromControlName`/`effectiveToControlName` class fields when `isManagedBySystem`
- [x] TS component renderer emits `effectiveFrom: null, effectiveTo: null` in `confirmAdd` when `isManagedBySystem`
- [x] `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [x] **Step 1: Add `isManagedBySystem` to `ManyToManyTimestampedFieldDef`**

  File: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ManyToManyTimestampedFieldDef.kt`

  Add an import and a new property. The existing imports are at lines 1–10; add after line 8 (`import org.maiaframework.gen.spec.definition.TypeaheadDef`):

  ```kotlin
  import org.maiaframework.gen.spec.EffectiveRangeManagedBy
  ```

  Then add the property after `private val nameSuffix` (currently line 25):

  ```kotlin
  val isManagedBySystem: Boolean =
      manyToManyEntityDef.entityDef.effectiveRangeDef?.managedBy == EffectiveRangeManagedBy.SYSTEM
  ```

- [x] **Step 2: Update `renderManyToManyTimestampedFields` in `AbstractCrudReactiveFormHtmlRenderer`**

  File: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt`

  In `renderManyToManyTimestampedFields()`, wrap the four mat-form-field blocks **in the join entries list** (currently lines 43–69) in a Kotlin `if` guard, and wrap the four mat-form-field blocks **in the mini-form** (currently lines 98–125) in the same guard.

  Replace the entire `renderManyToManyTimestampedFields()` method body with:

  ```kotlin
  protected open fun renderManyToManyTimestampedFields() {

      timestampedFields.forEach { field ->

          append("""
              |        <div class="join-entries">
              |            @for (join of ${field.joinsFieldName}; track join.entityId) {
              |                <div class="join-entry">
              |                    <span>{{ join.entityName }}</span>
              |""".trimMargin())

          if (!field.isManagedBySystem) {
              append("""
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
                  |""".trimMargin())
          }

          append("""
              |                    <button mat-icon-button type="button" class="join-remove-button" (click)="${field.removeMethodName}(${'$'}index)">
              |                        <mat-icon>delete</mat-icon>
              |                    </button>
              |                </div>
              |            }
              |        </div>
              |        <button mat-stroked-button type="button" (click)="${field.showFormSignalName}.set(true)">
              |            <mat-icon>add</mat-icon> Add ${field.labelText}
              |        </button>
              |        @if (${field.showFormSignalName}()) {
              |            <div class="join-mini-form">
              |                <mat-form-field appearance="outline">
              |                    <mat-label>${field.labelText}</mat-label>
              |                    <input
              |                        matInput
              |                        [formControl]="${field.addEntityControlName}"
              |                        [matAutocomplete]="${field.autocompleteRefName}"
              |                        placeholder="${field.searchPlaceholder}"
              |                    />
              |                    <mat-autocomplete #${field.autocompleteRefName}="matAutocomplete" [displayWith]="${field.displayWithMethodName}">
              |                        @if (${field.filteredIsLoadingFieldName}()) {
              |                            <mat-option disabled>Loading...</mat-option>
              |                        }
              |                        @for (option of ${field.filteredFieldName}; track option.${field.esDocIdFieldName}) {
              |                            <mat-option [value]="option">{{ option.${field.searchTermFieldName} }}</mat-option>
              |                        }
              |                    </mat-autocomplete>
              |                </mat-form-field>
              |""".trimMargin())

          if (!field.isManagedBySystem) {
              append("""
                  |                <mat-form-field appearance="outline">
                  |                    <mat-label>Effective From Date</mat-label>
                  |                    <input matInput [matDatepicker]="effectiveFromPicker${field.fieldName}"
                  |                        [formControl]="${field.effectiveFromControlName}" />
                  |                    <mat-datepicker-toggle matIconSuffix [for]="effectiveFromPicker${field.fieldName}"></mat-datepicker-toggle>
                  |                    <mat-datepicker #effectiveFromPicker${field.fieldName}></mat-datepicker>
                  |                </mat-form-field>
                  |                <mat-form-field appearance="outline">
                  |                    <mat-label>Effective From Time</mat-label>
                  |                    <input matInput [matTimepicker]="effectiveFromTimepicker${field.fieldName}"
                  |                        [formControl]="${field.effectiveFromControlName}" />
                  |                    <mat-timepicker #effectiveFromTimepicker${field.fieldName}></mat-timepicker>
                  |                    <mat-timepicker-toggle matSuffix [for]="effectiveFromTimepicker${field.fieldName}"></mat-timepicker-toggle>
                  |                </mat-form-field>
                  |                <mat-form-field appearance="outline">
                  |                    <mat-label>Effective To Date</mat-label>
                  |                    <input matInput [matDatepicker]="effectiveToPicker${field.fieldName}"
                  |                        [formControl]="${field.effectiveToControlName}" />
                  |                    <mat-datepicker-toggle matIconSuffix [for]="effectiveToPicker${field.fieldName}"></mat-datepicker-toggle>
                  |                    <mat-datepicker #effectiveToPicker${field.fieldName}></mat-datepicker>
                  |                </mat-form-field>
                  |                <mat-form-field appearance="outline">
                  |                    <mat-label>Effective To Time</mat-label>
                  |                    <input matInput [matTimepicker]="effectiveToTimepicker${field.fieldName}"
                  |                        [formControl]="${field.effectiveToControlName}" />
                  |                    <mat-timepicker #effectiveToTimepicker${field.fieldName}></mat-timepicker>
                  |                    <mat-timepicker-toggle matSuffix [for]="effectiveToTimepicker${field.fieldName}"></mat-timepicker-toggle>
                  |                </mat-form-field>
                  |""".trimMargin())
          }

          append("""
              |                <button mat-flat-button type="button" (click)="${field.confirmMethodName}()">Add</button>
              |                <button mat-flat-button type="button" (click)="${field.cancelMethodName}()">Cancel</button>
              |            </div>
              |        }
              |""".trimMargin())

      }

  }
  ```

- [x] **Step 3: Update class-field rendering in `AngularReactiveFormComponentRenderer`**

  File: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt`

  Find the block that appends `effectiveFromControlName` and `effectiveToControlName` class fields (currently lines 428–431). Wrap it in `if (!field.isManagedBySystem)`:

  Replace:
  ```kotlin
              |    ${field.effectiveFromControlName} = new FormControl<Date | null>(null);
              |
              |
              |    ${field.effectiveToControlName} = new FormControl<Date | null>(null);
  ```

  With:
  ```kotlin
  """.trimMargin())

          if (!field.isManagedBySystem) {
              append("""
                  |
                  |
                  |    ${field.effectiveFromControlName} = new FormControl<Date | null>(null);
                  |
                  |
                  |    ${field.effectiveToControlName} = new FormControl<Date | null>(null);
                  |""".trimMargin())
          }

          append("""
  ```

  That is, split the existing append() call at that point. The complete block around lines 410–438 should look like:

  ```kotlin
          append("""
              |
              |
              |    ${field.joinsFieldName}: {
              |        id: string | null;
              |        entityId: string;
              |        entityName: string;
              |        effectiveFrom: Date | null;
              |        effectiveTo: Date | null;
              |    }[] = [];
              |
              |
              |    ${field.showFormSignalName} = signal(false);
              |
              |
              |    ${field.addEntityControlName} = new FormControl<${field.esDocClassName} | null>(null);
              |""".trimMargin())

          if (!field.isManagedBySystem) {
              append("""
                  |
                  |
                  |    ${field.effectiveFromControlName} = new FormControl<Date | null>(null);
                  |
                  |
                  |    ${field.effectiveToControlName} = new FormControl<Date | null>(null);
                  |""".trimMargin())
          }

          append("""
              |
              |
              |    ${field.filteredFieldName}: ${field.esDocClassName}[] = [];
              |
              |
              |    ${field.filteredIsLoadingFieldName} = signal(false);
              |""".trimMargin())
  ```

- [x] **Step 4: Update `render timestamped join methods` in `AngularReactiveFormComponentRenderer`**

  File: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt`

  In `render timestamped join methods()` (around lines 861–914), the `confirmMethodName` method currently always references `effectiveFromControlName` and `effectiveToControlName`. For `isManagedBySystem`, emit `null` literals instead and skip resetting those controls.

  Replace the entire `render timestamped join methods()` method with:

  ```kotlin
  private fun `render timestamped join methods`() {

      timestampedFields.forEach { field ->

          append("""
              |
              |
              |    ${field.confirmMethodName}(): void {
              |
              |        const entity = this.${field.addEntityControlName}.value;
              |        if (!entity) return;
              |        if (this.${field.joinsFieldName}.some(j => j.entityId === entity.${field.esDocIdFieldName})) return;
              |        this.${field.joinsFieldName}.push({
              |            id: null,
              |            entityId: entity.${field.esDocIdFieldName},
              |            entityName: entity.${field.searchTermFieldName},
              |""".trimMargin())

          if (field.isManagedBySystem) {
              append("""
                  |            effectiveFrom: null,
                  |            effectiveTo: null,
                  |""".trimMargin())
          } else {
              append("""
                  |            effectiveFrom: this.${field.effectiveFromControlName}.value,
                  |            effectiveTo: this.${field.effectiveToControlName}.value,
                  |""".trimMargin())
          }

          append("""
              |        });
              |        this.${field.addEntityControlName}.reset();
              |""".trimMargin())

          if (!field.isManagedBySystem) {
              append("""
                  |        this.${field.effectiveFromControlName}.reset();
                  |        this.${field.effectiveToControlName}.reset();
                  |""".trimMargin())
          }

          append("""
              |        this.${field.filteredFieldName} = [];
              |        this.${field.showFormSignalName}.set(false);
              |
              |    }
              |
              |
              |    ${field.removeMethodName}(index: number): void {
              |
              |        this.${field.joinsFieldName}.splice(index, 1);
              |
              |    }
              |
              |
              |    ${field.cancelMethodName}(): void {
              |
              |        this.${field.addEntityControlName}.reset();
              |""".trimMargin())

          if (!field.isManagedBySystem) {
              append("""
                  |        this.${field.effectiveFromControlName}.reset();
                  |        this.${field.effectiveToControlName}.reset();
                  |""".trimMargin())
          }

          append("""
              |        this.${field.filteredFieldName} = [];
              |        this.${field.showFormSignalName}.set(false);
              |
              |    }
              |
              |
              |    ${field.displayWithMethodName} = (entity: ${field.esDocClassName} | null): string => {
              |        return entity ? entity.${field.searchTermFieldName} : '';
              |    };
              |""".trimMargin())

      }

  }
  ```

- [x] **Step 5: Compile the generator**

  ```bash
  ./gradlew :maia-gen:maia-gen-generator:compileKotlin
  ```

  Expected: `BUILD SUCCESSFUL`

- [x] **Step 6: Commit**

  ```bash
  git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ManyToManyTimestampedFieldDef.kt
  git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt
  git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt
  git commit -m "fix: suppress date-picker fields in SYSTEM-managed timestamped MtM join forms"
  ```

```json:metadata
{"files": ["maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ManyToManyTimestampedFieldDef.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-generator:compileKotlin", "acceptanceCriteria": ["ManyToManyTimestampedFieldDef exposes isManagedBySystem: Boolean", "HTML renderer skips date/time picker fields when isManagedBySystem", "TS component renderer skips effectiveFrom/To controls when isManagedBySystem", "compileKotlin BUILD SUCCESSFUL"], "requiresUserVerification": false}
```

---

### Task 1: Regenerate showcase UI and verify

**Goal:** Run code generation and confirm that the `rightJoins` section of the create/edit forms no longer contains date-picker inputs while `rightEffectiveJoins` still does.

**Files:**
- Regenerated: `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.html`
- Regenerated: `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.ts`
- Regenerated: `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-edit-form.html`
- Regenerated: `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-edit-form.ts`

**Acceptance Criteria:**
- [x] `left-many-entity-create-form.html` contains no date-picker inputs before the "Add Right Entities" button (the `rightJoins` block)
- [x] `left-many-entity-create-form.html` still contains date-picker inputs before the "Add Right Effective Entities" button (the `rightEffectiveJoins` block)
- [x] `left-many-entity-create-form.ts` has no `addRightJoinEffectiveFromControl` or `addRightJoinEffectiveToControl` fields
- [x] `left-many-entity-create-form.ts` still has `addRightEffectiveJoinEffectiveFromControl` and `addRightEffectiveJoinEffectiveToControl` fields

**Verify:** Grep the regenerated HTML for the presence/absence of `effectiveFromDatePicker` in the two sections.

**Steps:**

- [x] **Step 1: Run code generation for showcase UI**

  ```bash
  ./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
  ```

  Expected: `BUILD SUCCESSFUL`

- [x] **Step 2: Verify `rightJoins` section has no date pickers**

  ```bash
  # Count occurrences of effectiveFromDatePicker — should be exactly 1 (in rightEffectiveJoins only)
  grep -c "effectiveFromDatePicker" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.html
  ```

  Expected: `1`

  ```bash
  # Verify the remaining occurrence is after "Add Right Effective Entities"
  grep -n "effectiveFromDatePicker\|Add Right" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.html
  ```

  The `effectiveFromDatePicker` line must appear after the `Add Right Effective Entities` line.

- [x] **Step 3: Verify `addRightJoinEffectiveFrom/ToControl` absent from create TS**

  ```bash
  grep "addRightJoinEffective" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.ts
  ```

  Expected: no output (the SYSTEM-managed controls are gone).

  ```bash
  grep "addRightEffectiveJoinEffective" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.ts
  ```

  Expected: `addRightEffectiveJoinEffectiveFromControl` and `addRightEffectiveJoinEffectiveToControl` still present.

- [x] **Step 4: Commit the regenerated files**

  ```bash
  git add maia-showcase/maia-showcase-ui/src/generated/
  git commit -m "regen: showcase UI after SYSTEM-managed join date-picker fix"
  ```

```json:metadata
{"files": ["maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.html", "maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.ts", "maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-edit-form.html", "maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-edit-form.ts"], "verifyCommand": "grep -c effectiveFromDatePicker maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.html", "acceptanceCriteria": ["rightJoins create-form HTML has no date-picker inputs", "rightEffectiveJoins create-form HTML still has date-picker inputs", "create-form TS has no addRightJoinEffectiveFrom/ToControl", "edit-form HTML and TS same pattern as create-form"], "requiresUserVerification": false}
```

---

### Task 2: Add scoped page-object methods

**Goal:** Add `clickAddRightJoinEntityButton`, `searchAndSelectRightJoinEntityInMiniForm`, `clickConfirmAddRightJoinInMiniForm` to `LeftManyCreatePage` and `assertRightJoinEntryVisible`, `removeRightJoinEntry` to `LeftManyEditPage`.

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyCreatePage.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyEditPage.kt`

**Acceptance Criteria:**
- [x] `LeftManyCreatePage` has three new methods with the names specified
- [x] `LeftManyEditPage` has two new methods that scope to `page.locator(".join-entries").first()`
- [x] `./gradlew :maia-showcase:app:compileTestKotlin` → BUILD SUCCESSFUL

**Verify:** `./gradlew :maia-showcase:app:compileTestKotlin` → BUILD SUCCESSFUL

**Steps:**

- [x] **Step 1: Add methods to `LeftManyCreatePage`**

  File: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyCreatePage.kt`

  Add after the existing `clickConfirmAddInMiniForm()` method (before `clickSubmitButton()`):

  ```kotlin
      fun clickAddRightJoinEntityButton() {

          page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add Right Entities"))
              .click()

      }


      fun searchAndSelectRightJoinEntityInMiniForm(searchTerm: String) {

          page.locator("input[placeholder='Search Right Entities...']").fill(searchTerm)
          val option = page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm))
          option.waitFor()
          option.evaluate("el => el.click()")

      }


      fun clickConfirmAddRightJoinInMiniForm() {

          page.locator(".join-mini-form button[type='button']").filter(Locator.FilterOptions().setHasText("Add"))
              .click()

      }
  ```

  Note: these are functionally identical to `clickAddRightEntityButton()`, `searchAndSelectRightEntityInMiniForm()`, and `clickConfirmAddInMiniForm()` — they exist as clearly-named aliases so `ManyToManyEffectiveRangeCrudPlaywrightTest` reads unambiguously.

- [x] **Step 2: Add methods to `LeftManyEditPage`**

  File: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyEditPage.kt`

  Add after the existing `removeJoinEntry()` method (before `fillEditForm()`):

  ```kotlin
      fun assertRightJoinEntryVisible(entityName: String) {

          page.locator(".join-entries").first()
              .locator(".join-entry")
              .filter(Locator.FilterOptions().setHasText(entityName))
              .waitFor()

      }


      fun removeRightJoinEntry(entityName: String) {

          val section = page.locator(".join-entries").first()
          section.locator(".join-entry")
              .filter(Locator.FilterOptions().setHasText(entityName))
              .locator("button.join-remove-button")
              .click()
          section.locator(".join-entry")
              .filter(Locator.FilterOptions().setHasText(entityName))
              .waitFor(Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN))

      }
  ```

  These scope to `page.locator(".join-entries").first()` because the edit form has two `.join-entries` divs — the first for `rightJoins` (system-managed) and the second for `rightEffectiveJoins`.

- [x] **Step 3: Compile**

  ```bash
  ./gradlew :maia-showcase:app:compileTestKotlin
  ```

  Expected: `BUILD SUCCESSFUL`

- [x] **Step 4: Commit**

  ```bash
  git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyCreatePage.kt
  git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyEditPage.kt
  git commit -m "test: add scoped page-object methods for system-managed MtM join section"
  ```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyCreatePage.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyEditPage.kt"], "verifyCommand": "./gradlew :maia-showcase:app:compileTestKotlin", "acceptanceCriteria": ["LeftManyCreatePage has clickAddRightJoinEntityButton, searchAndSelectRightJoinEntityInMiniForm, clickConfirmAddRightJoinInMiniForm", "LeftManyEditPage has assertRightJoinEntryVisible and removeRightJoinEntry scoped to first .join-entries", "compileTestKotlin BUILD SUCCESSFUL"], "requiresUserVerification": false}
```

---

### Task 3: Write `ManyToManyEffectiveRangeCrudPlaywrightTest`

**Goal:** Create the Playwright CRUD test for the system-managed effective-timestamps MtM join entity.

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/ManyToManyEffectiveRangeCrudPlaywrightTest.kt`

**Acceptance Criteria:**
- [x] Test class compiles
- [x] `crud journey` test exercises: login → blotter → create with one right-gamma join → view → edit (assert join visible, remove join, edit fields) → view → blotter (assert, delete, assert gone)

**Verify:** `./gradlew :maia-showcase:app:compileTestKotlin` → BUILD SUCCESSFUL

**Steps:**

- [x] **Step 1: Create the test class**

  File: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/ManyToManyEffectiveRangeCrudPlaywrightTest.kt`

  ```kotlin
  package org.maiaframework.showcase.many_to_many

  import org.junit.jupiter.api.BeforeAll
  import org.junit.jupiter.api.BeforeEach
  import org.junit.jupiter.api.Test
  import org.maiaframework.elasticsearch.EsDocHolder
  import org.maiaframework.elasticsearch.index.EsIndexOps
  import org.maiaframework.showcase.AbstractPlaywrightTest
  import org.springframework.beans.factory.annotation.Autowired


  class ManyToManyEffectiveRangeCrudPlaywrightTest : AbstractPlaywrightTest() {


      @Autowired
      private lateinit var rightManyDao: RightManyDao


      @Autowired
      private lateinit var esIndexOps: EsIndexOps


      @Autowired
      private lateinit var rightManyTypeaheadEsIndex: RightManyTypeaheadEsIndex


      private val rightGamma = RightManyEntityTestBuilder(someString = "right-gamma").build()
      private val rightDelta = RightManyEntityTestBuilder(someString = "right-delta").build()


      @BeforeAll
      fun setUp() {

          initAdminUserFixture()

          fixtures.resetDatabaseState()
          rightManyDao.deleteAll()
          rightManyDao.bulkInsert(listOf(rightGamma, rightDelta))
          listOf(rightGamma, rightDelta).forEach { entity ->
              esIndexOps.upsert(EsDocHolder(
                  id = entity.id.toString(),
                  doc = RightManyTypeaheadV1EsDoc(id = entity.id, someString = entity.someString),
                  indexName = rightManyTypeaheadEsIndex.indexName()
              ))
          }

      }


      @BeforeEach
      fun logOut() {

          `log out`()

      }


      @Test
      fun `crud journey`() {

          `log in as admin user`()
          `navigate to the`(leftManyBlotterPage)

          leftManyBlotterPage.clickAddButton()

          leftManyCreatePage.apply {
              assertOnPage()
              fillCreateForm()
              clickAddRightJoinEntityButton()
              searchAndSelectRightJoinEntityInMiniForm("right-gamma")
              clickConfirmAddRightJoinInMiniForm()
              clickSubmitButton()
          }

          leftManyViewPage.apply {
              assertOnPage()
              clickEditButton()
          }

          leftManyEditPage.apply {
              assertOnPage()
              assertRightJoinEntryVisible("right-gamma")
              removeRightJoinEntry("right-gamma")
              fillEditForm()
              clickSubmitButton()
          }

          leftManyViewPage.assertOnPage()

          `navigate to the`(leftManyBlotterPage)

          leftManyBlotterPage.apply {
              assertTableContainsValue("testleft_edited")
              clickDeleteButtonForFirstRow()
              clickYesButton()
              assertTableDoesNotContainValue("testleft_edited")
          }

      }


  }
  ```

  **Why `rightDelta` is declared but not used in the test:** It is declared as a fixture entity to be inserted into the DB and indexed. While the `crud journey` test only uses `right-gamma`, having `right-delta` available ensures the typeahead search returns multiple results — confirming the test correctly selects the right entity by name rather than just taking the first result.

- [x] **Step 2: Compile**

  ```bash
  ./gradlew :maia-showcase:app:compileTestKotlin
  ```

  Expected: `BUILD SUCCESSFUL`

- [x] **Step 3: Commit**

  ```bash
  git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/ManyToManyEffectiveRangeCrudPlaywrightTest.kt
  git commit -m "test: add ManyToManyEffectiveRangeCrudPlaywrightTest"
  ```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/ManyToManyEffectiveRangeCrudPlaywrightTest.kt"], "verifyCommand": "./gradlew :maia-showcase:app:compileTestKotlin", "acceptanceCriteria": ["ManyToManyEffectiveRangeCrudPlaywrightTest compiles", "crud journey test follows login -> blotter -> create with right-gamma join -> view -> edit -> view -> blotter delete flow"], "requiresUserVerification": false}
```
