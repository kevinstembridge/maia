# Password Visibility Toggle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Every generated `<input>` with `HtmlInputType.password` gets a show/hide toggle button automatically, in both the signal-form and reactive-form (incl. entity-form) Angular renderers, with no spec DSL changes.

**Architecture:** `MatFormFieldRenderer.kt` gains a password-specific branch in its two input-rendering functions: a `[type]` binding driven by a per-field boolean, plus a `mat-icon-button matSuffix` toggle button (mirrors the existing `mat-datepicker-toggle` suffix pattern). The two component-class renderers (`AngularReactiveFormComponentRenderer.kt`, `AngularSignalFormComponentRenderer.kt`) each gain a step that emits a `signal(false)` field per password field and conditionally imports `MatIconModule`.

**Tech Stack:** Kotlin code generator (`maia-gen-generator`), generated Angular 20 Material components, verified via Gradle regeneration of `maia-showcase-ui` and a Playwright/Kotlin e2e test in `maia-showcase/app`.

**User Verification:** NO — no user sign-off requested; verified via regenerated output inspection + an automated Playwright test.

---

## Reference facts (from design doc, `docs/superpowers/specs/2026-07-07-password-visibility-toggle-design.md`)

- Only `AngularFormFieldDef` drives `<input>` rendering; `HtmlFormFieldDef`/`HtmlFormEntityFieldDef` (which also expose `htmlInputType`) only feed enum-constant generation in `AppModuleGenerator` and are out of scope.
- Two render functions in `MatFormFieldRenderer.kt` need the change: `renderHtmlInputField` (private, signal-form) and `renderReactiveFormHtmlInputField` (private, reactive/entity-form).
- `signal` (`@angular/core`) and `MatButtonModule` are already unconditionally imported by both component renderers. `MatIconModule` is not imported by default in either — it must be added conditionally.
- Naming: `<fieldName>PasswordVisible` (avoids collision with the existing `<field>IsVisible` signal used for linked-field visibility).
- The only real spec usage of `HtmlInputType.password` today is `maia-showcase`'s login form (`MaiaShowcaseSpec.kt:124-130`), which goes through the **reactive**-form path (`FormHtmlRenderer` + `AngularReactiveFormComponentRenderer`). The signal-form path (`AbstractCrudFormHtmlRenderer` + `AngularSignalFormComponentRenderer`) currently has no way to produce a password field from any existing spec (`EntityCreateApiDef`/`EntityUpdateApiDef` hardcode `HtmlInputType.text` with no override), so it cannot be exercised end-to-end today — implement it for consistency and correctness, verify it by code inspection/symmetry with the reactive path rather than a live generated example.

---

### Task 1: Render the password toggle markup in `MatFormFieldRenderer.kt`

**Goal:** Both `renderHtmlInputField` and `renderReactiveFormHtmlInputField` emit a `[type]` binding and a suffix toggle button when `htmlInputType == HtmlInputType.password`, and are unchanged for every other input type.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/MatFormFieldRenderer.kt`

**Acceptance Criteria:**
- [ ] For a password field, the generated `<input>` has `[type]="<field>PasswordVisible() ? 'text' : 'password'"` instead of a static `type="password"` attribute.
- [ ] For a password field, a `<button mat-icon-button matSuffix type="button">` with a `<mat-icon>` toggling between `visibility`/`visibility_off` is emitted right after the `<input .../>` tag, inside the same `<mat-form-field>`.
- [ ] For every other `HtmlInputType` value, output is byte-for-byte unchanged (still a static `type="..."` attribute, no button).
- [ ] This applies in both `renderHtmlInputField` and `renderReactiveFormHtmlInputField`.

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL (full behavioral verification happens in Task 4/5; there is no unit test harness for this renderer today).

**Steps:**

- [ ] **Step 1: Add the `HtmlInputType` import**

At the top of `MatFormFieldRenderer.kt`, add:

```kotlin
import org.maiaframework.gen.spec.definition.HtmlInputType
```

- [ ] **Step 2: Update `renderHtmlInputField`**

Replace:

```kotlin
        r.appendLine("$indent        type=\"${htmlFormField.htmlInputType}\"")

        htmlFormField.autocomplete?.let {
            r.appendLine("$indent        autocomplete=\"$it\"")
        }

        if (htmlFormField.autoFocus) {
            r.appendLine("$indent        autoFocus")
        }

        r.appendLine("$indent        matInput")

        r.appendLine("$indent    />")

        if (classFieldDef.hasAnyValidationConstraints() || classFieldDef.isUnique) {
```

(inside `renderHtmlInputField`, currently around line 138) with:

```kotlin
        if (htmlFormField.htmlInputType == HtmlInputType.password) {
            r.appendLine("$indent        [type]=\"${classFieldName}PasswordVisible() ? 'text' : 'password'\"")
        } else {
            r.appendLine("$indent        type=\"${htmlFormField.htmlInputType}\"")
        }

        htmlFormField.autocomplete?.let {
            r.appendLine("$indent        autocomplete=\"$it\"")
        }

        if (htmlFormField.autoFocus) {
            r.appendLine("$indent        autoFocus")
        }

        r.appendLine("$indent        matInput")

        r.appendLine("$indent    />")

        if (htmlFormField.htmlInputType == HtmlInputType.password) {
            r.appendLine("$indent    <button mat-icon-button matSuffix type=\"button\" (click)=\"${classFieldName}PasswordVisible.set(!${classFieldName}PasswordVisible())\" [attr.aria-label]=\"${classFieldName}PasswordVisible() ? 'Hide password' : 'Show password'\">")
            r.appendLine("$indent        <mat-icon>{{ ${classFieldName}PasswordVisible() ? 'visibility_off' : 'visibility' }}</mat-icon>")
            r.appendLine("$indent    </button>")
        }

        if (classFieldDef.hasAnyValidationConstraints() || classFieldDef.isUnique) {
```

- [ ] **Step 3: Update `renderReactiveFormHtmlInputField`**

Replace:

```kotlin
        r.appendLine("$indent        type=\"${htmlFormField.htmlInputType}\"")

        htmlFormField.autocomplete?.let {
            r.appendLine("$indent        autocomplete=\"$it\"")
        }

        if (htmlFormField.autoFocus) {
            r.appendLine("$indent        autoFocus")
        }

        r.appendLine("$indent        matInput")

        r.appendLine("$indent    />")

        if (classFieldDef.hasAnyValidationConstraints() || classFieldDef.isUnique) {

            if (fieldLabel == null) {
                throw IllegalStateException("Field label is null for DTO field ${htmlFormField.fieldKey}")
            }
```

(inside `renderReactiveFormHtmlInputField`, currently around line 201) with:

```kotlin
        if (htmlFormField.htmlInputType == HtmlInputType.password) {
            r.appendLine("$indent        [type]=\"${classFieldName}PasswordVisible() ? 'text' : 'password'\"")
        } else {
            r.appendLine("$indent        type=\"${htmlFormField.htmlInputType}\"")
        }

        htmlFormField.autocomplete?.let {
            r.appendLine("$indent        autocomplete=\"$it\"")
        }

        if (htmlFormField.autoFocus) {
            r.appendLine("$indent        autoFocus")
        }

        r.appendLine("$indent        matInput")

        r.appendLine("$indent    />")

        if (htmlFormField.htmlInputType == HtmlInputType.password) {
            r.appendLine("$indent    <button mat-icon-button matSuffix type=\"button\" (click)=\"${classFieldName}PasswordVisible.set(!${classFieldName}PasswordVisible())\" [attr.aria-label]=\"${classFieldName}PasswordVisible() ? 'Hide password' : 'Show password'\">")
            r.appendLine("$indent        <mat-icon>{{ ${classFieldName}PasswordVisible() ? 'visibility_off' : 'visibility' }}</mat-icon>")
            r.appendLine("$indent    </button>")
        }

        if (classFieldDef.hasAnyValidationConstraints() || classFieldDef.isUnique) {

            if (fieldLabel == null) {
                throw IllegalStateException("Field label is null for DTO field ${htmlFormField.fieldKey}")
            }
```

- [ ] **Step 4: Compile**

Run: `./gradlew :maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/MatFormFieldRenderer.kt
git commit -m "feat: render password visibility toggle in MatFormFieldRenderer"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/MatFormFieldRenderer.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-generator:compileKotlin", "acceptanceCriteria": ["password fields get [type] binding + toggle button", "non-password fields unchanged", "applies to both signal-form and reactive-form render functions"], "requiresUserVerification": false}
```

---

### Task 2: Generate the visibility signal + `MatIconModule` import in `AngularReactiveFormComponentRenderer.kt`

**Goal:** For every password field in `angularFormDef.htmlFormFields`, the generated reactive-form component class declares a `<fieldName>PasswordVisible = signal(false);` field, and `MatIconModule` is imported whenever any password field is present.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt`

**Acceptance Criteria:**
- [ ] A `<fieldName>PasswordVisible = signal(false);` class field is emitted for each password field.
- [ ] `MatIconModule` is imported (once) when the form has at least one password field; not imported when it doesn't (unless already required by timestamped/chip fields, which is untouched existing behavior).
- [ ] No change in output for forms with no password fields.

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add the `HtmlInputType` import**

At the top of `AngularReactiveFormComponentRenderer.kt`, add:

```kotlin
import org.maiaframework.gen.spec.definition.HtmlInputType
```

- [ ] **Step 2: Add the new class-fields step**

Add a call to the new step inside `render class fields`() (in `private fun \`render class fields\`()`), right after the existing call to `` `render class fields for timestamped fields`() ``:

```kotlin
        `render class fields for timestamped fields`()

        `render class fields for password visibility toggle`()

        `render class field for formGroup `()
```

Add the new function next to `` `render class fields for timestamped fields`() ``:

```kotlin
    private fun `render class fields for password visibility toggle`() {

        angularFormDef.htmlFormFields
            .filter { it.htmlInputType == HtmlInputType.password }
            .forEach { field ->

            append("""
                |
                |
                |    ${field.fieldName}PasswordVisible = signal(false);
                |""".trimMargin())

        }

    }
```

- [ ] **Step 3: Conditionally import `MatIconModule`**

In `` `add imports`() ``, immediately after this existing line:

```kotlin
        addImport("@angular/material/input", "MatInputModule", isModule = true)
```

add:

```kotlin
        if (angularFormDef.htmlFormFields.any { it.htmlInputType == HtmlInputType.password }) {
            addImport("@angular/material/icon", "MatIconModule", isModule = true)
        }
```

- [ ] **Step 4: Compile**

Run: `./gradlew :maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt
git commit -m "feat: emit password visibility signal and MatIconModule import for reactive forms"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-generator:compileKotlin", "acceptanceCriteria": ["signal field emitted per password field", "MatIconModule imported conditionally"], "requiresUserVerification": false}
```

---

### Task 3: Generate the visibility signal + `MatIconModule` import in `AngularSignalFormComponentRenderer.kt`

**Goal:** Mirror Task 2 for the signal-form component renderer, so the signal-form path is consistent even though no live spec exercises it today.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularSignalFormComponentRenderer.kt`

**Acceptance Criteria:**
- [ ] A `<fieldName>PasswordVisible = signal(false);` class field is emitted for each password field in `angularFormDef.htmlFormFields`.
- [ ] `MatIconModule` is imported when the form has at least one password field.
- [ ] No change in output for forms with no password fields.

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add the `HtmlInputType` import**

At the top of `AngularSignalFormComponentRenderer.kt`, add:

```kotlin
import org.maiaframework.gen.spec.definition.HtmlInputType
```

- [ ] **Step 2: Add the new class-fields step**

In `private fun \`render class fields\`()`, add a call right after the existing call to `` `render class field problemDetail`() ``:

```kotlin
        `render class field dialogFormModel`()
        `render class field dialogForm`()
        `render class field problemDetail`()

        `render class fields for password visibility toggle`()
```

Add the new function next to `` `render class field problemDetail`() ``:

```kotlin
    private fun `render class fields for password visibility toggle`() {

        this.angularFormDef.htmlFormFields
            .filter { it.htmlInputType == HtmlInputType.password }
            .forEach { field ->

            append("""
                |
                |
                |    ${field.fieldName}PasswordVisible = signal(false);
                |""".trimMargin())

        }

    }
```

- [ ] **Step 3: Conditionally import `MatIconModule`**

In the `init { ... }` block, immediately after this existing line:

```kotlin
        addImport("@angular/material/form-field", "MatFormFieldModule", isModule = true)
```

add:

```kotlin
        if (this.angularFormDef.htmlFormFields.any { it.htmlInputType == HtmlInputType.password }) {
            addImport("@angular/material/icon", "MatIconModule", isModule = true)
        }
```

- [ ] **Step 4: Compile**

Run: `./gradlew :maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularSignalFormComponentRenderer.kt
git commit -m "feat: emit password visibility signal and MatIconModule import for signal forms"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularSignalFormComponentRenderer.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-generator:compileKotlin", "acceptanceCriteria": ["signal field emitted per password field", "MatIconModule imported conditionally"], "requiresUserVerification": false}
```

---

### Task 4: Regenerate `maia-showcase-ui` and verify the login form output

**Goal:** Confirm the real, live password field (login form) regenerates with the expected `[type]` binding, toggle button, signal field, and import.

**Files:**
- Generated (verify only, do not hand-edit): `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/login/login-form.html`
- Generated (verify only, do not hand-edit): `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/login/login-form.ts`

**Acceptance Criteria:**
- [ ] `login-form.html` contains `[type]="passwordPasswordVisible() ? 'text' : 'password'"` on the password `<input>`.
- [ ] `login-form.html` contains the `<button mat-icon-button matSuffix ...>` / `<mat-icon>` toggle block immediately after that input.
- [ ] `login-form.ts` contains `passwordPasswordVisible = signal(false);` and imports `MatIconModule` from `@angular/material/icon`.
- [ ] The `emailAddress` field (still `HtmlInputType.text`) is unaffected — still a static `type="text"` attribute, no toggle button.

**Verify:** `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration` then inspect the two generated files above.

**Steps:**

- [ ] **Step 1: Regenerate**

Run: `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Inspect `login-form.html`**

Open `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/login/login-form.html` and confirm it now contains (in place of the old static `type="password"` input):

```html
<mat-form-field appearance="outline">
    <input
        formControlName="password"
        name="password"
        placeholder="Password..."
        [type]="passwordPasswordVisible() ? 'text' : 'password'"
        autocomplete="current-password"
        matInput
    />
    <button mat-icon-button matSuffix type="button" (click)="passwordPasswordVisible.set(!passwordPasswordVisible())" [attr.aria-label]="passwordPasswordVisible() ? 'Hide password' : 'Show password'">
        <mat-icon>{{ passwordPasswordVisible() ? 'visibility_off' : 'visibility' }}</mat-icon>
    </button>
    @if (formGroup.controls['password'].hasError('required')) {
        <mat-error>Password is required.</mat-error>
    }
</mat-form-field>
```

- [ ] **Step 3: Inspect `login-form.ts`**

Confirm it contains `passwordPasswordVisible = signal(false);` as a class field, and `import { MatIconModule } from '@angular/material/icon';` (or the framework's equivalent generated import line) alongside the existing `MatButtonModule`/`MatFormFieldModule`/`MatInputModule` imports.

- [ ] **Step 4: Commit the regenerated output**

```bash
git add maia-showcase/maia-showcase-ui/src/generated
git commit -m "chore: regenerate maia-showcase-ui with password visibility toggle"
```

```json:metadata
{"files": ["maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/login/login-form.html", "maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/login/login-form.ts"], "verifyCommand": "./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration", "acceptanceCriteria": ["login-form.html has [type] binding and toggle button", "login-form.ts has signal field and MatIconModule import", "emailAddress field unaffected"], "requiresUserVerification": false}
```

---

### Task 5: Extend the login Playwright test to cover the toggle

**Goal:** An automated end-to-end test proves the toggle actually switches the password input between masked and plain text in a running browser.

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LoginPage.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/login/LoginPageTest.kt`

**Acceptance Criteria:**
- [ ] `LoginPage` exposes a way to click the toggle and to assert the password input's current `type` attribute.
- [ ] A new test fills the password field, asserts it starts masked (`type="password"`), toggles to plain text (`type="text"`), then toggles back (`type="password"`).

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.login.LoginPageTest"` → BUILD SUCCESSFUL, both tests pass.

**Steps:**

- [ ] **Step 1: Add a locator + helpers to `LoginPage.kt`**

Current file:

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class LoginPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/login",
    "login_page"
) {


    private val emailAddressInput = this.page.locator("input[name=emailAddress]")


    private val passwordInput = this.page.locator("input[name=password]")


    fun submitForm(
        emailAddress: EmailAddress,
        password: String
    ) {

        this.emailAddressInput.clear()
        this.emailAddressInput.fill(emailAddress.value)

        this.passwordInput.clear()
        this.passwordInput.fill(password)

        this.passwordInput.press("Enter")

    }


}
```

Replace with:

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class LoginPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/login",
    "login_page"
) {


    private val emailAddressInput = this.page.locator("input[name=emailAddress]")


    private val passwordInput = this.page.locator("input[name=password]")


    private val passwordVisibilityToggle = this.page.locator("mat-form-field:has(input[name=password]) button")


    fun submitForm(
        emailAddress: EmailAddress,
        password: String
    ) {

        this.emailAddressInput.clear()
        this.emailAddressInput.fill(emailAddress.value)

        this.passwordInput.clear()
        this.passwordInput.fill(password)

        this.passwordInput.press("Enter")

    }


    fun togglePasswordVisibility() {

        this.passwordVisibilityToggle.click()

    }


    fun assertPasswordFieldType(expectedType: String) {

        assertThat(this.passwordInput).hasAttribute("type", expectedType)

    }


}
```

- [ ] **Step 2: Add the test to `LoginPageTest.kt`**

Current file:

```kotlin
package org.maiaframework.showcase.login

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.testing.fixtures.UserFixture

class LoginPageTest : AbstractPlaywrightTest() {


    private lateinit var user: UserFixture


    @BeforeAll
    fun setUp() {

        user = fixtures.aUser()
        fixtures.resetDatabaseState()

    }


    @Test
    fun `user can navigate to the login page and submit the form`() {

        loginPage.navigateToMe()

        Thread.sleep(300)

        loginPage.submitForm(
            user.emailAddressEntity.emailAddress,
            user.rawPassword
        )

        homePage.assertOnPage()

    }


}
```

Replace with:

```kotlin
package org.maiaframework.showcase.login

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.testing.fixtures.UserFixture

class LoginPageTest : AbstractPlaywrightTest() {


    private lateinit var user: UserFixture


    @BeforeAll
    fun setUp() {

        user = fixtures.aUser()
        fixtures.resetDatabaseState()

    }


    @Test
    fun `user can navigate to the login page and submit the form`() {

        loginPage.navigateToMe()

        Thread.sleep(300)

        loginPage.submitForm(
            user.emailAddressEntity.emailAddress,
            user.rawPassword
        )

        homePage.assertOnPage()

    }


    @Test
    fun `password field visibility can be toggled`() {

        loginPage.navigateToMe()

        Thread.sleep(300)

        loginPage.assertPasswordFieldType("password")

        loginPage.togglePasswordVisibility()

        loginPage.assertPasswordFieldType("text")

        loginPage.togglePasswordVisibility()

        loginPage.assertPasswordFieldType("password")

    }


}
```

- [ ] **Step 3: Run the test**

Run: `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.login.LoginPageTest"`
Expected: BUILD SUCCESSFUL, both `LoginPageTest` methods pass.

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LoginPage.kt maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/login/LoginPageTest.kt
git commit -m "test: cover password visibility toggle on login form"
```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LoginPage.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/login/LoginPageTest.kt"], "verifyCommand": "./gradlew :maia-showcase:app:test --tests \"org.maiaframework.showcase.login.LoginPageTest\"", "acceptanceCriteria": ["toggle switches input type from password to text and back", "existing login submit test still passes"], "requiresUserVerification": false}
```

---

## Unresolved questions

None.
