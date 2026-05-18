# Blotter Page Generation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extend the code generator to produce blotter page wrapper components (`.ts` + `.html`) so handcoded pages like `simple-blotter-page` are no longer needed.

**Architecture:** Mirror the existing `EntityEditPageDef` opt-in pattern — add `withBlotterPage {}` to `CrudBlotterDefBuilder`, collect the new `CrudBlotterPageDef`s through `ModelDef`, and emit them via two new renderers in `AngularUiModuleGenerator`. The generated files land in `src/generated/typescript/main/app/gen-components/…` alongside all other generated UI files.

**Tech Stack:** Kotlin, Angular 19, Maia spec DSL, Gradle

**User Verification:** NO

---

## File Map

| File | Action | Purpose |
|------|--------|---------|
| `maia-gen/maia-gen-spec/…/CrudBlotterPageDef.kt` | Create | Data class: page title, dataPageId, Angular component names |
| `maia-gen/maia-gen-spec/…/builders/CrudBlotterPageDefBuilder.kt` | Create | DSL builder for page def |
| `maia-gen/maia-gen-spec/…/builders/CrudBlotterDefBuilder.kt` | Modify | Add `withBlotterPage {}` + store page def |
| `maia-gen/maia-gen-spec/…/ModelDef.kt` | Modify | Add `crudBlotterPageDefs: List<CrudBlotterPageDef>` |
| `maia-gen/maia-gen-spec/…/AbstractSpec.kt` | Modify | Collect page defs from builder, pass to ModelDef |
| `maia-gen/maia-gen-generator/…/CrudBlotterPageComponentRenderer.kt` | Create | Emits the `.ts` component file |
| `maia-gen/maia-gen-generator/…/CrudBlotterPageHtmlRenderer.kt` | Create | Emits the `.html` template file |
| `maia-gen/maia-gen-generator/…/AngularUiModuleGenerator.kt` | Modify | Call new renderers in `renderCrudBlotters()` |
| `maia-showcase/spec/…/MaiaShowcaseSpec.kt` | Modify | Add `withBlotterPage { pageTitle = "Simple" }` |
| `maia-showcase/maia-showcase-ui/src/app/app.routes.ts` | Modify | Point `simple-blotter` route at generated file |
| `maia-showcase/…/pages/simple-blotter/simple-blotter-page.ts` | Delete | Replaced by generated file |
| `maia-showcase/…/pages/simple-blotter/simple-blotter-page.html` | Delete | Replaced by generated file |

---

### Task 1: Create CrudBlotterPageDef and CrudBlotterPageDefBuilder

**Goal:** Add spec-layer data class and builder for blotter page metadata.

**Files:**
- Create: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/CrudBlotterPageDef.kt`
- Create: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/CrudBlotterPageDefBuilder.kt`

**Acceptance Criteria:**
- [ ] `CrudBlotterPageDef` takes `BlotterDef` + `pageTitle` and exposes `dataPageId`, `pageAngularComponentNames`, `crudBlotterSelector`, `crudBlotterComponentClassName`, `crudBlotterComponentTypescriptImport`
- [ ] `CrudBlotterPageDefBuilder` defaults `pageTitle` to `blotterDef.dtoBaseName.toTitleCase()`
- [ ] Code compiles

**Verify:** `./gradlew :maia-gen:maia-gen-spec:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Create `CrudBlotterPageDef.kt`**

```kotlin
package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.TypescriptImport

class CrudBlotterPageDef(
    private val blotterDef: BlotterDef,
    val pageTitle: String,
) {

    private val genDir = GeneratedTypescriptDir.forPackage(blotterDef.packageName)

    val dataPageId = "${blotterDef.dtoBaseName.toSnakeCase()}_blotter"

    val pageAngularComponentNames = AngularComponentNames(
        blotterDef.packageName,
        "${blotterDef.dtoBaseName}BlotterPage"
    )

    val crudBlotterSelector = "app-${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter"

    val crudBlotterComponentClassName = "${blotterDef.dtoBaseName}CrudBlotterComponent"

    val crudBlotterComponentTypescriptImport = TypescriptImport(
        name = crudBlotterComponentClassName,
        from = "@$genDir/${blotterDef.dtoBaseName.toKebabCase()}-crud-blotter"
    )

}
```

- [ ] **Step 2: Create `CrudBlotterPageDefBuilder.kt`**

```kotlin
package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.CrudBlotterPageDef

@MaiaDslMarker
class CrudBlotterPageDefBuilder(private val blotterDef: BlotterDef) {

    var pageTitle: String = blotterDef.dtoBaseName.toTitleCase()

    fun build(): CrudBlotterPageDef {
        return CrudBlotterPageDef(blotterDef, pageTitle)
    }

}
```

- [ ] **Step 3: Compile**

```bash
./gradlew :maia-gen:maia-gen-spec:compileKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/CrudBlotterPageDef.kt
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/CrudBlotterPageDefBuilder.kt
git commit -m "feat(spec): add CrudBlotterPageDef and builder"
```

---

### Task 2: Wire CrudBlotterPageDef into spec DSL and ModelDef

**Goal:** Make `withBlotterPage {}` available in the spec DSL and expose the page defs through `ModelDef`.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/CrudBlotterDefBuilder.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ModelDef.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/AbstractSpec.kt`

**Acceptance Criteria:**
- [ ] `CrudBlotterDefBuilder` has `withBlotterPage {}` method and `internal var crudBlotterPageDef: CrudBlotterPageDef?`
- [ ] `ModelDef` has `val crudBlotterPageDefs: List<CrudBlotterPageDef>`
- [ ] `AbstractSpec.crudBlotter()` collects page defs and passes them to `ModelDef`
- [ ] Code compiles

**Verify:** `./gradlew :maia-gen:maia-gen-spec:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Update `CrudBlotterDefBuilder.kt`**

Replace the entire file with:

```kotlin
package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.CrudBlotterDef
import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.CrudBlotterPageDef
import org.maiaframework.gen.spec.definition.EntityCrudApiDef

@MaiaDslMarker
class CrudBlotterDefBuilder(
    private val blotterDef: BlotterDef,
    private val entityCrudApiDef: EntityCrudApiDef
) {

    internal var crudBlotterPageDef: CrudBlotterPageDef? = null

    fun withBlotterPage(init: CrudBlotterPageDefBuilder.() -> Unit) {
        val builder = CrudBlotterPageDefBuilder(blotterDef)
        builder.init()
        this.crudBlotterPageDef = builder.build()
    }

    fun build(): CrudBlotterDef {
        return CrudBlotterDef(blotterDef, entityCrudApiDef)
    }

}
```

- [ ] **Step 2: Update `ModelDef.kt`**

`ModelDef` is a data class (or regular class) that takes constructor params. Add `crudBlotterPageDefs: List<CrudBlotterPageDef>` as the last constructor parameter (after `entityEditPageDefs`). Also add the import. The exact file is at `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ModelDef.kt`. Add the new parameter and import.

- [ ] **Step 3: Update `AbstractSpec.kt`**

In `AbstractSpec`, add a `private val crudBlotterPageDefs = mutableListOf<CrudBlotterPageDef>()` field alongside the other lists.

In the `crudBlotter()` function, after `this.crudBlotterDefs.add(def)`, add:
```kotlin
builder.crudBlotterPageDef?.let { this.crudBlotterPageDefs.add(it) }
```

In the `modelDef` property getter, pass `this.crudBlotterPageDefs` as the last argument to `ModelDef(...)`.

Also add the import for `CrudBlotterPageDef` to AbstractSpec's import list.

- [ ] **Step 4: Compile**

```bash
./gradlew :maia-gen:maia-gen-spec:compileKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/CrudBlotterDefBuilder.kt
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ModelDef.kt
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/AbstractSpec.kt
git commit -m "feat(spec): wire CrudBlotterPageDef into DSL and ModelDef"
```

---

### Task 3: Create blotter page renderers and wire into generator

**Goal:** Add the two renderers that emit the `.ts` and `.html` files, and call them from `AngularUiModuleGenerator`.

**Files:**
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/CrudBlotterPageComponentRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/CrudBlotterPageHtmlRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt`

**Acceptance Criteria:**
- [ ] `CrudBlotterPageComponentRenderer` emits a valid Angular component `.ts` with `ChangeDetectionStrategy.OnPush`, correct imports for `PageLayout` and the crud blotter component
- [ ] `CrudBlotterPageHtmlRenderer` emits `<app-page-layout pageTitle="..." dataPageId="...">` wrapping the crud blotter selector
- [ ] `AngularUiModuleGenerator` calls both renderers for each `crudBlotterPageDef`
- [ ] Generator module compiles

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Create `CrudBlotterPageComponentRenderer.kt`**

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.CrudBlotterPageDef

class CrudBlotterPageComponentRenderer(
    private val crudBlotterPageDef: CrudBlotterPageDef
) : AbstractTypescriptRenderer() {

    init {
        addImport("@angular/core", "ChangeDetectionStrategy")
        addImport("@angular/core", "Component")
        addImport("@app/components/page-layout/page-layout", "PageLayout")
        addImport(crudBlotterPageDef.crudBlotterComponentTypescriptImport)
    }

    override fun renderedFilePath(): String {
        return crudBlotterPageDef.pageAngularComponentNames.componentRenderedFilePath
    }

    override fun renderSourceBody() {
        append("""
            |
            |@Component({
            |    changeDetection: ChangeDetectionStrategy.OnPush,
            |    imports: [
            |        PageLayout,
            |        ${crudBlotterPageDef.crudBlotterComponentClassName}
            |    ],
            |    selector: '${crudBlotterPageDef.pageAngularComponentNames.componentSelector}',
            |    templateUrl: './${crudBlotterPageDef.pageAngularComponentNames.htmlFileName}'
            |})
            |export class ${crudBlotterPageDef.pageAngularComponentNames.componentName} {}
            |""".trimMargin())
    }

}
```

- [ ] **Step 2: Create `CrudBlotterPageHtmlRenderer.kt`**

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.CrudBlotterPageDef

class CrudBlotterPageHtmlRenderer(
    private val crudBlotterPageDef: CrudBlotterPageDef
) : AbstractSourceFileRenderer() {

    override fun renderedFilePath(): String {
        return crudBlotterPageDef.pageAngularComponentNames.htmlRenderedFilePath
    }

    override fun renderSource(): String {
        append("""
            |<app-page-layout pageTitle="${crudBlotterPageDef.pageTitle}" dataPageId="${crudBlotterPageDef.dataPageId}">
            |    <${crudBlotterPageDef.crudBlotterSelector}></${crudBlotterPageDef.crudBlotterSelector}>
            |</app-page-layout>
            |""".trimMargin())
        return sourceCode.toString()
    }

}
```

- [ ] **Step 3: Update `AngularUiModuleGenerator.kt`**

Add `import`s for the two new renderers at the top of the file.

In `renderCrudBlotters()`, append calls after the existing `forEach`:

```kotlin
private fun renderCrudBlotters() {

    this.modelDef.crudBlotterDefs.forEach { crudBlotterDef ->

        val entityIsReferencedByForeignKeys = this.modelDef.entityIsReferencedByForeignKeys(crudBlotterDef.entityCrudApiDef.entityDef)
        val hasEditEntityPage = this.modelDef.hasEditEntityPage(crudBlotterDef.entityCrudApiDef.entityDef)
        CrudBlotterHtmlRenderer(crudBlotterDef).renderToDir(this.typescriptOutputDir)
        CrudBlotterComponentRenderer(crudBlotterDef, entityIsReferencedByForeignKeys, hasEditEntityPage).renderToDir(this.typescriptOutputDir)

    }

    this.modelDef.crudBlotterPageDefs.forEach { pageDef ->
        CrudBlotterPageComponentRenderer(pageDef).renderToDir(this.typescriptOutputDir)
        CrudBlotterPageHtmlRenderer(pageDef).renderToDir(this.typescriptOutputDir)
    }

}
```

- [ ] **Step 4: Compile**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/CrudBlotterPageComponentRenderer.kt
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/CrudBlotterPageHtmlRenderer.kt
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt
git commit -m "feat(gen): add CrudBlotterPage renderers"
```

---

### Task 4: Update showcase spec and regenerate, migrate handcoded page

**Goal:** Add `withBlotterPage {}` to `simpleCrudDef` in the showcase spec, regenerate, update the route, and delete the handcoded page.

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`
- Delete: `maia-showcase/maia-showcase-ui/src/app/pages/simple-blotter/simple-blotter-page.ts`
- Delete: `maia-showcase/maia-showcase-ui/src/app/pages/simple-blotter/simple-blotter-page.html`
- Generated (verify existence): `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-blotter-page.ts`
- Generated (verify existence): `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-blotter-page.html`

**Acceptance Criteria:**
- [ ] `simpleCrudDef` calls `withBlotterPage { pageTitle = "Simple" }`
- [ ] `maiaGeneration` produces `simple-blotter-page.ts` and `simple-blotter-page.html` in the generated dir
- [ ] Generated `.ts` has `ChangeDetectionStrategy.OnPush`, class `SimpleBlotterPage`, selector `app-simple-blotter-page`, imports `SimpleCrudBlotterComponent` and `PageLayout`
- [ ] Generated `.html` has `pageTitle="Simple"` and `dataPageId="simple_blotter"` and uses `<app-simple-crud-blotter>`
- [ ] `app.routes.ts` `simple-blotter` path imports from `../generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-blotter-page`
- [ ] Handcoded files deleted
- [ ] Full build succeeds

**Verify:** `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration` → generated files exist; `./gradlew :maia-showcase:maia-showcase-ui:build` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Update `MaiaShowcaseSpec.kt`**

Find `val simpleCrudDef = crudBlotter(simpleBlotterDef, simpleEntityDef.entityCrudApiDef!!)` and add the init block:

```kotlin
val simpleCrudDef = crudBlotter(simpleBlotterDef, simpleEntityDef.entityCrudApiDef!!) {
    withBlotterPage {
        pageTitle = "Simple"
    }
}
```

- [ ] **Step 2: Run maiaGeneration**

```bash
./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
```
Expected: BUILD SUCCESSFUL

Verify generated files exist:
```bash
ls maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-blotter-page.*
```
Expected: two files listed (`.ts` and `.html`)

- [ ] **Step 3: Verify generated content**

Check `.ts`:
```bash
cat maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-blotter-page.ts
```
Expected: Contains `SimpleBlotterPage`, `SimpleCrudBlotterComponent`, `ChangeDetectionStrategy.OnPush`, `app-simple-blotter-page`.

Check `.html`:
```bash
cat maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-blotter-page.html
```
Expected: Contains `pageTitle="Simple"`, `dataPageId="simple_blotter"`, `app-simple-crud-blotter`.

- [ ] **Step 4: Update `app.routes.ts`**

Replace the `simple-blotter` route entry:

```typescript
{
    path: 'simple-blotter',
    loadComponent: () =>
        import('../generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-blotter-page').then(
            (m) => m.SimpleBlotterPage,
        ),
},
```

- [ ] **Step 5: Delete handcoded files**

```bash
rm maia-showcase/maia-showcase-ui/src/app/pages/simple-blotter/simple-blotter-page.ts
rm maia-showcase/maia-showcase-ui/src/app/pages/simple-blotter/simple-blotter-page.html
rmdir maia-showcase/maia-showcase-ui/src/app/pages/simple-blotter
```

- [ ] **Step 6: Full build**

```bash
./gradlew :maia-showcase:maia-showcase-ui:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt
git add maia-showcase/maia-showcase-ui/src/app/app.routes.ts
git add maia-showcase/maia-showcase-ui/src/generated/
git add -u maia-showcase/maia-showcase-ui/src/app/pages/simple-blotter/
git commit -m "feat(showcase): generate simple-blotter-page, remove handcoded wrapper"
```
