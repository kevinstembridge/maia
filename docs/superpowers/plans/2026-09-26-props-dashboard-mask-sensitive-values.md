# Props Dashboard Mask Sensitive Values Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Never send the plaintext value of a sensitive property (name contains password/secret/key/token/credential, case-insensitive) to the browser — mask it server-side in the property list, the history audit trail, and the edit form's pre-fill.

**Architecture:** Backend (`PropsSpec.kt` + `PropsManager.kt`) computes an `isSensitive` flag per property name and masks `effectiveValue`/`environmentValue`/history `propertyValue` to a fixed constant before they ever leave the server — the redundancy check keeps using raw values internally. Frontend surfaces the new `isSensitive` flag to avoid ever pre-filling the edit form with the masked placeholder, and shows a lock icon next to a masked value for clarity.

**Tech Stack:** Kotlin/Spring Boot backend (maia-props-service, maia-props-spec), Angular 21 frontend (maia-props), Gradle, Vitest.

**User Verification:** NO — no human sign-off was requested; standard automated test verification per task.

---

## Design reference

`docs/superpowers/specs/2026-09-26-props-dashboard-mask-sensitive-values-design.md`

## Working directories

- Backend: `/home/kevin/dev/code/maia` (Gradle project paths are `:libs:maia-props-parent:maia-props-spec`, `:libs:maia-props-parent:maia-props-service`, `:libs:maia-props-parent:maia-props-web`)
- Frontend: `/home/kevin/dev/code/maia/libs/maia-ui-workspace`

---

### Task 1: Backend — compute and apply `isSensitive` masking

**Goal:** `PropertyResponseDto` gains an `isSensitive` field; `PropsManager` masks `effectiveValue`/`environmentValue` (in both `getAllProperties`/`setProperty` paths) and history `propertyValue` for any property whose name matches a sensitive keyword, while `isRedundant` keeps comparing raw (unmasked) values.

**Files:**
- Modify: `libs/maia-props-parent/maia-props-spec/src/main/kotlin/org/maiaframework/props/spec/PropsSpec.kt`
- Modify: `libs/maia-props-parent/maia-props-service/src/main/kotlin/org/maiaframework/props/PropsManager.kt`
- Modify: `libs/maia-props-parent/maia-props-service/src/test/kotlin/org/maiaframework/props/PropsManagerTest.kt`

**Acceptance Criteria:**
- [ ] `PropertyResponseDto` (generated) has a new `isSensitive: Boolean` field
- [ ] A property name is "sensitive" if it contains, case-insensitively, `password`, `secret`, `key`, `token`, or `credential`
- [ ] For a sensitive property, `effectiveValue`/`environmentValue` are `"******"` when the raw value is non-null, and stay `null` when the raw value is `null` — in both the override-present and no-override branches of `toPropertyResponseDto`
- [ ] `isRedundant` is computed from the raw (unmasked) `override.propertyValue`/`environmentValue`, unaffected by masking
- [ ] `getPropertyHistory` masks every sensitive property's historical `propertyValue` to `"******"`
- [ ] A non-sensitive property is completely unaffected (existing tests keep passing)
- [ ] New tests cover: sensitive+overridden masked, sensitive+no-override masked, redundancy still correct when masked, non-sensitive unaffected, history masked for sensitive/unmasked for non-sensitive

**Verify:** `./gradlew :libs:maia-props-parent:maia-props-service:test` (from `/home/kevin/dev/code/maia`) → BUILD SUCCESSFUL, all tests pass (this also triggers `maiaGeneration` for `maia-props-domain` automatically as a build dependency)

**Steps:**

- [ ] **Step 1: Add the `isSensitive` field to the spec**

In `PropsSpec.kt`, find:

```kotlin
    val propertyDtoDef = simpleResponseDto("org.maiaframework.props", "Property") {
        field("propertyName", FieldTypes.string)
        field("effectiveValue", FieldTypes.string) {
            nullable()
        }
        field("isOverridden", FieldTypes.boolean)
        field("isRedundant", FieldTypes.boolean)
        field("environmentValue", FieldTypes.string) {
            nullable()
        }
```

and replace it with:

```kotlin
    val propertyDtoDef = simpleResponseDto("org.maiaframework.props", "Property") {
        field("propertyName", FieldTypes.string)
        field("effectiveValue", FieldTypes.string) {
            nullable()
        }
        field("isOverridden", FieldTypes.boolean)
        field("isRedundant", FieldTypes.boolean)
        field("isSensitive", FieldTypes.boolean)
        field("environmentValue", FieldTypes.string) {
            nullable()
        }
```

(Everything else in `PropsSpec.kt`, including `propertyHistoryItemDtoDef`, is unchanged — history masking doesn't need its own `isSensitive` field, see the design doc.)

- [ ] **Step 2: Add the sensitivity/masking helpers to `PropsManager`**

In `PropsManager.kt`, find:

```kotlin
class PropsManager(
    private val propsRepo: PropsRepo,
    private val environment: ConfigurableEnvironment
) {


    private val logger = getLogger<PropsManager>()


    fun getAllProperties(): List<PropertyResponseDto> {
```

and replace it with:

```kotlin
class PropsManager(
    private val propsRepo: PropsRepo,
    private val environment: ConfigurableEnvironment
) {


    companion object {
        private const val MASKED_VALUE = "******"
        private val SENSITIVE_NAME_FRAGMENTS = listOf("password", "secret", "key", "token", "credential")
    }


    private val logger = getLogger<PropsManager>()


    private fun isSensitivePropertyName(propertyName: String): Boolean =
        SENSITIVE_NAME_FRAGMENTS.any { propertyName.contains(it, ignoreCase = true) }


    private fun maskIfSensitive(value: String?, isSensitive: Boolean): String? =
        if (isSensitive && value != null) MASKED_VALUE else value


    fun getAllProperties(): List<PropertyResponseDto> {
```

- [ ] **Step 3: Apply masking in `toPropertyResponseDto`**

In the same file, find:

```kotlin
    private fun toPropertyResponseDto(
        propertyName: String,
        override: PropsEntity?,
        environmentSourceName: String?
    ): PropertyResponseDto {

        val environmentValue = `get property value from Spring Environment`(propertyName)

        return if (override != null) {
            PropertyResponseDto(
                comment = override.comment,
                effectiveValue = override.propertyValue,
                environmentValue = environmentValue,
                isOverridden = true,
                isRedundant = override.propertyValue == environmentValue,
                lastModifiedByUsername = override.lastModifiedByUsername,
                lastModifiedTimestamp = override.lastModifiedTimestamp,
                propertyName = propertyName,
                reviewDate = override.reviewDate,
                sourceName = "DB override",
            )
        } else {
            PropertyResponseDto(
                comment = null,
                effectiveValue = environmentValue,
                environmentValue = environmentValue,
                isOverridden = false,
                isRedundant = false,
                lastModifiedByUsername = null,
                lastModifiedTimestamp = null,
                propertyName = propertyName,
                reviewDate = null,
                sourceName = environmentSourceName,
            )
        }

    }
```

and replace it with:

```kotlin
    private fun toPropertyResponseDto(
        propertyName: String,
        override: PropsEntity?,
        environmentSourceName: String?
    ): PropertyResponseDto {

        val environmentValue = `get property value from Spring Environment`(propertyName)
        val isSensitive = isSensitivePropertyName(propertyName)

        return if (override != null) {
            PropertyResponseDto(
                comment = override.comment,
                effectiveValue = maskIfSensitive(override.propertyValue, isSensitive),
                environmentValue = maskIfSensitive(environmentValue, isSensitive),
                isOverridden = true,
                isRedundant = override.propertyValue == environmentValue,
                isSensitive = isSensitive,
                lastModifiedByUsername = override.lastModifiedByUsername,
                lastModifiedTimestamp = override.lastModifiedTimestamp,
                propertyName = propertyName,
                reviewDate = override.reviewDate,
                sourceName = "DB override",
            )
        } else {
            PropertyResponseDto(
                comment = null,
                effectiveValue = maskIfSensitive(environmentValue, isSensitive),
                environmentValue = maskIfSensitive(environmentValue, isSensitive),
                isOverridden = false,
                isRedundant = false,
                isSensitive = isSensitive,
                lastModifiedByUsername = null,
                lastModifiedTimestamp = null,
                propertyName = propertyName,
                reviewDate = null,
                sourceName = environmentSourceName,
            )
        }

    }
```

- [ ] **Step 4: Apply masking in `getPropertyHistory`**

In the same file, find:

```kotlin
    fun getPropertyHistory(propertyName: String): List<PropertyHistoryItemResponseDto> {

        return this.propsRepo.getPropertyHistory(propertyName).map {
            PropertyHistoryItemResponseDto(
                changeType = it.changeType,
                comment = it.comment,
                lastModifiedByUsername = it.lastModifiedByUsername,
                lastModifiedTimestamp = it.lastModifiedTimestamp,
                propertyName = it.propertyName,
                propertyValue = it.propertyValue,
                reviewDate = it.reviewDate,
                version = it.version,
            )
        }

    }
```

and replace it with:

```kotlin
    fun getPropertyHistory(propertyName: String): List<PropertyHistoryItemResponseDto> {

        val isSensitive = isSensitivePropertyName(propertyName)

        return this.propsRepo.getPropertyHistory(propertyName).map {
            PropertyHistoryItemResponseDto(
                changeType = it.changeType,
                comment = it.comment,
                lastModifiedByUsername = it.lastModifiedByUsername,
                lastModifiedTimestamp = it.lastModifiedTimestamp,
                propertyName = it.propertyName,
                propertyValue = if (isSensitive) MASKED_VALUE else it.propertyValue,
                reviewDate = it.reviewDate,
                version = it.version,
            )
        }

    }
```

- [ ] **Step 5: Extend the test environment and add masking tests**

In `PropsManagerTest.kt`, find:

```kotlin
    private val environment = StandardEnvironment().apply {
        propertySources.addFirst(MapPropertySource("test", mapOf("test.prop" to "abc")))
    }
```

and replace it with:

```kotlin
    private val environment = StandardEnvironment().apply {
        propertySources.addFirst(MapPropertySource("test", mapOf(
            "test.prop" to "abc",
            "test.secret" to "xyz",
        )))
    }
```

Then find:

```kotlin
    @Test
    fun `property without override has no review date`() {

        assertThat(propertyNamed("test.prop").reviewDate).isNull()

    }


    private fun propertyNamed(propertyName: String): PropertyResponseDto {
```

and replace it with:

```kotlin
    @Test
    fun `property without override has no review date`() {

        assertThat(propertyNamed("test.prop").reviewDate).isNull()

    }


    @Test
    fun `non-sensitive property is not flagged as sensitive`() {

        assertThat(propertyNamed("test.prop").isSensitive).isFalse()

    }


    @Test
    fun `sensitive property is flagged as sensitive`() {

        assertThat(propertyNamed("test.secret").isSensitive).isTrue()

    }


    @Test
    fun `sensitive property environment value is masked when no override exists`() {

        val property = propertyNamed("test.secret")

        assertThat(property.effectiveValue).isEqualTo("******")
        assertThat(property.environmentValue).isEqualTo("******")

    }


    @Test
    fun `sensitive property override value is masked`() {

        propsRepo.setPropertyOverride("test.secret", "hunter2", "user", null, null)

        val property = propertyNamed("test.secret")

        assertThat(property.effectiveValue).isEqualTo("******")
        assertThat(property.environmentValue).isEqualTo("******")

    }


    @Test
    fun `redundancy detection is unaffected by masking`() {

        propsRepo.setPropertyOverride("test.secret", "xyz", "user", null, null)

        assertThat(propertyNamed("test.secret").isRedundant).isTrue()

    }


    @Test
    fun `non-sensitive property value is not masked`() {

        assertThat(propertyNamed("test.prop").effectiveValue).isEqualTo("abc")

    }


    @Test
    fun `sensitive property history values are masked`() {

        propsManager.setProperty("test.secret", "hunter2", "user", null, null)

        val history = propsManager.getPropertyHistory("test.secret")

        assertThat(history.map { it.propertyValue }).containsOnly("******")

    }


    @Test
    fun `non-sensitive property history values are not masked`() {

        propsManager.setProperty("test.prop", "xyz", "user", null, null)

        val history = propsManager.getPropertyHistory("test.prop")

        assertThat(history.map { it.propertyValue }).containsOnly("xyz")

    }


    private fun propertyNamed(propertyName: String): PropertyResponseDto {
```

- [ ] **Step 6: Run the tests**

Run: `./gradlew :libs:maia-props-parent:maia-props-service:test` (from `/home/kevin/dev/code/maia`)
Expected: `BUILD SUCCESSFUL`, all tests pass

Also run: `./gradlew :libs:maia-props-parent:maia-props-web:compileKotlin` to confirm the hand-written web layer (which constructs no `PropertyResponseDto` itself, only consumes what `PropsManager` returns) still compiles against the new field.

- [ ] **Step 7: Commit**

```bash
git add libs/maia-props-parent/maia-props-spec/src/main/kotlin/org/maiaframework/props/spec/PropsSpec.kt libs/maia-props-parent/maia-props-service/src/main/kotlin/org/maiaframework/props/PropsManager.kt libs/maia-props-parent/maia-props-service/src/test/kotlin/org/maiaframework/props/PropsManagerTest.kt libs/maia-props-parent/maia-props-domain/src/generated
git commit -m "Mask sensitive property values server-side"
```

(The `libs/maia-props-domain/src/generated` path picks up the regenerated `PropertyResponseDto.kt` — generated sources are committed in this repo, matching the existing convention for all other generated files already tracked in git.)

---

### Task 2: Frontend — surface `isSensitive`, fix the edit-dialog pre-fill

**Goal:** The Angular model gains `isSensitive`; editing an already-sensitive property no longer pre-fills the masked placeholder into the value field, and the dialog explains why.

**Files:**
- Modify: `projects/maia-props/src/lib/props-dashboard/models/PropertyResponseDto.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/dialogs/edit-property-dialog/edit-property-dialog.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/dialogs/edit-property-dialog/edit-property-dialog.html`

**Acceptance Criteria:**
- [ ] `PropertyResponseDto.isSensitive: boolean` added
- [ ] `aProperty()` test fixture in `props-dashboard-filtering.spec.ts` supplies a default `isSensitive: false` (required so the interface change doesn't break compilation of existing fixtures)
- [ ] `onEdit(row)` passes `currentValue: null` (not `row.effectiveValue`) when `row.isSensitive` is true, and passes `isSensitive: row.isSensitive` to the dialog
- [ ] `onAddOverride()` passes `isSensitive: false`
- [ ] `EditPropertyDialogData` gains `isSensitive: boolean`
- [ ] The dialog shows a hint under the Value field — "Current value is hidden. Enter the full value to update it." — only when `data.isSensitive` is true AND the dialog is in edit mode (not `isAdding`)
- [ ] Project compiles, all tests pass

**Verify:** `npx ng build maia-props` (from `libs/maia-ui-workspace`) → build succeeds; `npx ng test maia-props` → all tests pass (33 existing + fixture update, no new test count change since `isSensitive: false` is just a new default field, not a new test)

**Steps:**

- [ ] **Step 1: Add the field to the model**

In `projects/maia-props/src/lib/props-dashboard/models/PropertyResponseDto.ts`, find:

```ts
export interface PropertyResponseDto {
    propertyName: string;
    effectiveValue: string | null;
    isOverridden: boolean;
    isRedundant: boolean;
    environmentValue: string | null;
    sourceName: string | null;
    lastModifiedByUsername: string | null;
    lastModifiedTimestamp: string | null;
    comment: string | null;
    reviewDate: string | null;
}
```

and replace it with:

```ts
export interface PropertyResponseDto {
    propertyName: string;
    effectiveValue: string | null;
    isOverridden: boolean;
    isRedundant: boolean;
    isSensitive: boolean;
    environmentValue: string | null;
    sourceName: string | null;
    lastModifiedByUsername: string | null;
    lastModifiedTimestamp: string | null;
    comment: string | null;
    reviewDate: string | null;
}
```

- [ ] **Step 2: Update the test fixture**

In `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts`, find:

```ts
function aProperty(overrides: Partial<PropertyResponseDto> = {}): PropertyResponseDto {
    return {
        propertyName: 'server.port',
        effectiveValue: '3200',
        isOverridden: false,
        isRedundant: false,
        environmentValue: '3200',
        sourceName: 'applicationConfig',
        lastModifiedByUsername: null,
        lastModifiedTimestamp: null,
        comment: null,
        reviewDate: null,
        ...overrides,
    };
}
```

and replace it with:

```ts
function aProperty(overrides: Partial<PropertyResponseDto> = {}): PropertyResponseDto {
    return {
        propertyName: 'server.port',
        effectiveValue: '3200',
        isOverridden: false,
        isRedundant: false,
        isSensitive: false,
        environmentValue: '3200',
        sourceName: 'applicationConfig',
        lastModifiedByUsername: null,
        lastModifiedTimestamp: null,
        comment: null,
        reviewDate: null,
        ...overrides,
    };
}
```

- [ ] **Step 3: Fix the page's edit/add wiring**

In `props-dashboard-page.ts`, find:

```ts
    onAddOverride() {

        this.openEditDialog({propertyName: null, currentValue: null, currentReviewDate: null});

    }


    onEdit(row: PropertyResponseDto) {

        this.openEditDialog({propertyName: row.propertyName, currentValue: row.effectiveValue, currentReviewDate: row.reviewDate});

    }
```

and replace it with:

```ts
    onAddOverride() {

        this.openEditDialog({propertyName: null, currentValue: null, currentReviewDate: null, isSensitive: false});

    }


    onEdit(row: PropertyResponseDto) {

        this.openEditDialog({
            propertyName: row.propertyName,
            currentValue: row.isSensitive ? null : row.effectiveValue,
            currentReviewDate: row.reviewDate,
            isSensitive: row.isSensitive,
        });

    }
```

- [ ] **Step 4: Add `isSensitive` to the dialog's data interface**

In `edit-property-dialog.ts`, find:

```ts
export interface EditPropertyDialogData {
    propertyName: string | null;
    currentValue: string | null;
    currentReviewDate: string | null;
}
```

and replace it with:

```ts
export interface EditPropertyDialogData {
    propertyName: string | null;
    currentValue: string | null;
    currentReviewDate: string | null;
    isSensitive: boolean;
}
```

- [ ] **Step 5: Add the hint to the dialog template**

In `edit-property-dialog.html`, find:

```html
        <mat-form-field>
            <mat-label>Value</mat-label>
            <input matInput name="propertyValue" formControlName="propertyValue">
        </mat-form-field>
```

and replace it with:

```html
        <mat-form-field>
            <mat-label>Value</mat-label>
            <input matInput name="propertyValue" formControlName="propertyValue">
            @if (data.isSensitive && !isAdding) {
                <mat-hint>Current value is hidden. Enter the full value to update it.</mat-hint>
            }
        </mat-form-field>
```

- [ ] **Step 6: Run build and tests**

Run: `npx ng build maia-props` (from `libs/maia-ui-workspace`)
Expected: `Built @maia/maia-props` with no TypeScript errors

Run: `npx ng test maia-props` (from `libs/maia-ui-workspace`)
Expected: `Test Files 1 passed (1)`, `Tests 33 passed (33)`

- [ ] **Step 7: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/models/PropertyResponseDto.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/dialogs/edit-property-dialog/edit-property-dialog.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/dialogs/edit-property-dialog/edit-property-dialog.html
git commit -m "Surface isSensitive on the frontend and fix the edit dialog's masked-value pre-fill"
```

---

### Task 3: Frontend — lock icon for masked values on the card

**Goal:** `PropsCard` shows a small lock icon next to a masked effective value, so it's visually clear the asterisks are a deliberate mask rather than a literal value.

**Files:**
- Modify: `projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.html`
- Modify: `projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.scss`

**Acceptance Criteria:**
- [ ] When `property().isSensitive` is true, a small `lock` `mat-icon` appears immediately before the effective value text
- [ ] The icon has an `aria-label` identifying it as a masked value indicator for that property
- [ ] No change to the non-sensitive rendering path
- [ ] Project compiles

**Verify:** `npx ng build maia-props` (from `libs/maia-ui-workspace`) → build succeeds; `npx ng test maia-props` → 33/33 pass

**Steps:**

- [ ] **Step 1: Wrap the value in a row with a conditional lock icon**

In `props-card.html`, find:

```html
            <div class="identity">
                <span class="prop-name">{{property().propertyName}}</span>
                <span class="prop-value">{{property().effectiveValue}}</span>
                @if (property().isOverridden) {
```

and replace it with:

```html
            <div class="identity">
                <span class="prop-name">{{property().propertyName}}</span>
                <span class="prop-value-row">
                    @if (property().isSensitive) {
                        <mat-icon class="sensitive-icon" [attr.aria-label]="'Masked value for ' + property().propertyName">lock</mat-icon>
                    }
                    <span class="prop-value">{{property().effectiveValue}}</span>
                </span>
                @if (property().isOverridden) {
```

(The `MatIconModule` import already exists in `props-card.ts` for the action buttons — no import changes needed.)

- [ ] **Step 2: Style the new row and icon**

In `props-card.scss`, find:

```scss
.prop-value {
    font-size: 0.8125rem;
    color: var(--mat-sys-on-surface-variant);
    overflow-wrap: anywhere;
}
```

and replace it with:

```scss
.prop-value-row {
    display: flex;
    align-items: center;
    gap: 0.25rem;
}

.prop-value {
    font-size: 0.8125rem;
    color: var(--mat-sys-on-surface-variant);
    overflow-wrap: anywhere;
}

.sensitive-icon {
    font-size: 1rem;
    width: 1rem;
    height: 1rem;
    flex-shrink: 0;
    color: var(--mat-sys-outline);
}
```

- [ ] **Step 3: Run build and tests**

Run: `npx ng build maia-props` (from `libs/maia-ui-workspace`)
Expected: `Built @maia/maia-props` with no TypeScript errors

Run: `npx ng test maia-props` (from `libs/maia-ui-workspace`)
Expected: `Test Files 1 passed (1)`, `Tests 33 passed (33)`

- [ ] **Step 4: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.html libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.scss
git commit -m "Show a lock icon next to masked sensitive values"
```

---

## Self-Review Notes

- **Spec coverage:** Detection heuristic, backend masking (list + history), redundancy-check preservation, edit-dialog pre-fill fix, and the lock-icon nicety all map to Tasks 1-3.
- **Placeholder scan:** none found — all steps contain complete code.
- **Type consistency:** `isSensitive: Boolean` (Kotlin DTO, Task 1) → `isSensitive: boolean` (TS model, Task 2) → `property().isSensitive` (Task 3 template) use identical semantics throughout. `MASKED_VALUE = "******"` is the single masking constant, used in both `toPropertyResponseDto` and `getPropertyHistory`.
- **User verification requirement scan:** Original request ("mask property values for sensitive fields") does not ask for human sign-off — answer is NO. Given this is security-sensitive, verification here means real Gradle/Vitest test runs at every step, not just a visual check — no dedicated verification task created since no human sign-off was requested, but Task 1 explicitly runs the Kotlin test suite rather than only a self-review (unlike this project's frontend-only tasks, which sometimes fall back to self-review when no browser is reachable — that constraint doesn't apply to a plain `./gradlew test` run).
