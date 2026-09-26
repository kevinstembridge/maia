# Props Redundant Override Flag Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an `isRedundant` flag to `PropertyResponseDto` (override value == environment value) and show it in a "Redundant" column on the props dashboard.

**Architecture:** Flag defined in the spec DSL, regenerated into the Kotlin DTO, computed in `PropsManager.toPropertyResponseDto`. Frontend adds the field to the hand-written TS DTO and a new mat-table column.

**Tech Stack:** Kotlin, Maia spec DSL/generator, JUnit 5 + AssertJ, Spring `StandardEnvironment`; Angular 20+ Material table, `@angular/build:unit-test`.

**User Verification:** NO — no user verification required

**Spec:** `docs/superpowers/specs/2026-09-24-props-redundant-override-design.md`

---

### Task 1: Backend `isRedundant` flag

**Goal:** `PropertyResponseDto.isRedundant` is true iff an override exists and its value equals the environment value.

**Files:**
- Modify: `libs/maia-props-parent/maia-props-spec/src/main/kotlin/org/maiaframework/props/spec/PropsSpec.kt` (`propertyDtoDef`)
- Regenerate: `libs/maia-props-parent/maia-props-domain/src/generated/kotlin/main/org/maiaframework/props/PropertyResponseDto.kt`
- Modify: `libs/maia-props-parent/maia-props-service/build.gradle.kts`
- Modify: `libs/maia-props-parent/maia-props-service/src/main/kotlin/org/maiaframework/props/PropsManager.kt`
- Create: `libs/maia-props-parent/maia-props-service/src/test/kotlin/org/maiaframework/props/PropsManagerTest.kt`

**Acceptance Criteria:**
- [ ] Override equal to env value → `isRedundant = true`
- [ ] Override different from env value → `false`
- [ ] Override with no env value → `false`
- [ ] No override → `false`
- [ ] `setProperty` response carries the flag

**Verify:** `./gradlew :libs:maia-props-parent:maia-props-service:test` → BUILD SUCCESSFUL, 5 tests pass

**Steps:**

- [ ] **Step 1: Add field to spec** — in `propertyDtoDef`, after `field("isOverridden", FieldTypes.boolean)`:

```kotlin
        field("isRedundant", FieldTypes.boolean)
```

- [ ] **Step 2: Regenerate**

Run: `./gradlew :libs:maia-props-parent:maia-props-domain:maiaGeneration`
Expected: `PropertyResponseDto.kt` now has `val isRedundant: Boolean` (fields alphabetical, after `isOverridden`). `PropsManager` no longer compiles until Step 6.

- [ ] **Step 3: Add test deps** to `maia-props-service/build.gradle.kts` dependencies block:

```kotlin
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
```

(Versions come from `maia-platform`, already on the classpath via `maia-props-repo`. If resolution fails, add `testImplementation(platform(project(":maia-platform")))`.)

- [ ] **Step 4: Write failing test** `PropsManagerTest.kt`:

```kotlin
package org.maiaframework.props

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.props.repo.InMemoryPropsRepo
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment

class PropsManagerTest {


    private val propsRepo = InMemoryPropsRepo()

    private val environment = StandardEnvironment().apply {
        propertySources.addFirst(MapPropertySource("test", mapOf("test.prop" to "abc")))
    }

    private val propsManager = PropsManager(propsRepo, environment)


    @Test
    fun `override matching environment value is redundant`() {

        propsRepo.setPropertyOverride("test.prop", "abc", "user", null)

        assertThat(propertyNamed("test.prop").isRedundant).isTrue()

    }


    @Test
    fun `override differing from environment value is not redundant`() {

        propsRepo.setPropertyOverride("test.prop", "xyz", "user", null)

        assertThat(propertyNamed("test.prop").isRedundant).isFalse()

    }


    @Test
    fun `override with no environment value is not redundant`() {

        propsRepo.setPropertyOverride("test.override-only", "abc", "user", null)

        assertThat(propertyNamed("test.override-only").isRedundant).isFalse()

    }


    @Test
    fun `property without override is not redundant`() {

        assertThat(propertyNamed("test.prop").isRedundant).isFalse()

    }


    @Test
    fun `setProperty response flags redundant override`() {

        val result = propsManager.setProperty("test.prop", "abc", "user", null)

        assertThat(result.isRedundant).isTrue()

    }


    private fun propertyNamed(propertyName: String): PropertyResponseDto {

        return propsManager.getAllProperties().single { it.propertyName == propertyName }

    }


}
```

- [ ] **Step 5: Run test to verify it fails**

Run: `./gradlew :libs:maia-props-parent:maia-props-service:test`
Expected: compile failure — `PropertyResponseDto` constructor missing `isRedundant`.

- [ ] **Step 6: Implement** in `PropsManager.toPropertyResponseDto` — override branch, after `isOverridden = true,`:

```kotlin
                isRedundant = override.propertyValue == environmentValue,
```

non-override branch, after `isOverridden = false,`:

```kotlin
                isRedundant = false,
```

- [ ] **Step 7: Run tests**

Run: `./gradlew :libs:maia-props-parent:maia-props-service:test`
Expected: BUILD SUCCESSFUL, 5 tests pass.

- [ ] **Step 8: Compile dependents**

Run: `./gradlew :libs:maia-props-parent:maia-props-web:compileKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit**

```bash
git add libs/maia-props-parent/maia-props-spec libs/maia-props-parent/maia-props-domain/src/generated libs/maia-props-parent/maia-props-service
git commit -m "Add isRedundant flag to PropertyResponseDto"
```

---

### Task 2: Dashboard "Redundant" column

**Goal:** Props dashboard shows a "Redundant" column after "Overridden".

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/models/PropertyResponseDto.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html`
- Modify: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts:32`
- Modify: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts` (`aProperty` fixture)

**Acceptance Criteria:**
- [ ] TS DTO has `isRedundant: boolean`
- [ ] Column header "Redundant", cell `Redundant` when true, blank otherwise
- [ ] Column positioned immediately after `isOverridden`
- [ ] Specs pass, library builds

**Verify:** `cd libs/maia-ui-workspace && npx ng test maia-props --watch=false && npx ng build maia-props` → all pass, `Built @maia/maia-props`

**Steps:**

- [ ] **Step 1: TS DTO** — after `isOverridden: boolean;`:

```ts
    isRedundant: boolean;
```

- [ ] **Step 2: Fixture** — in `aProperty`, after `isOverridden: false,`:

```ts
        isRedundant: false,
```

- [ ] **Step 3: Column** — in `props-dashboard-page.html`, after the `isOverridden` `ng-container`:

```html
        <ng-container matColumnDef="isRedundant">
            <th mat-header-cell *matHeaderCellDef>Redundant</th>
            <td mat-cell *matCellDef="let row">{{row.isRedundant ? 'Redundant' : ''}}</td>
        </ng-container>
```

- [ ] **Step 4: displayedColumns** in `props-dashboard-page.ts`:

```ts
    readonly displayedColumns = ['propertyName', 'effectiveValue', 'isOverridden', 'isRedundant', 'sourceName', 'lastModifiedByUsername', 'lastModifiedTimestamp', 'actions'];
```

- [ ] **Step 5: Test + build**

Run: `cd libs/maia-ui-workspace && npx ng test maia-props --watch=false && npx ng build maia-props`
Expected: all specs pass; `Built @maia/maia-props`.

- [ ] **Step 6: Commit** (only these files — working tree has unrelated changes)

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/models/PropertyResponseDto.ts \
        libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html \
        libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts \
        libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts
git commit -m "Show Redundant column on props dashboard"
```
