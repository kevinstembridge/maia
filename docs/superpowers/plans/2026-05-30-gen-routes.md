# Generated Module Routes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generate Angular route definitions for generated page components into a per-entity TypeScript file, and incorporate those routes into `app.routes.ts` via a one-time manual import/spread.

**Architecture:** A new `GenRoutesRenderer` (extending `AbstractTypescriptRenderer`) is invoked once per entity that has at least one page def. `AngularUiModuleGenerator.renderGenRoutes()` groups the model's page defs by entity and dispatches a renderer per entity. `app.routes.ts` is edited once to import and spread each generated `*GenRoutes` constant.

**Tech Stack:** Kotlin (spec + generator), Angular/TypeScript (generated output), Gradle (`maiaGeneration` task).

**User Verification:** NO

---

## File Map

| File | Action |
|---|---|
| `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/BlotterPageDef.kt` | Modify — add `routePath` property |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/GenRoutesRenderer.kt` | Create — new renderer |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt` | Modify — add `renderGenRoutes()` call and method |
| `maia-showcase/maia-showcase-ui/src/app/app.routes.ts` | Modify — import and spread generated routes, remove replaced entries |
| `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/*/` | Generated — one `*-gen-routes.ts` per entity |

---

### Task 1: Add `routePath` to `BlotterPageDef`

**Goal:** Expose the Angular route path string from `BlotterPageDef` so the renderer can use it without recomputing.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/BlotterPageDef.kt`

**Acceptance Criteria:**
- [ ] `BlotterPageDef` has a `routePath` property
- [ ] `routePath` value for a blotter with `dtoBaseName = "Simple"` is `"simple-blotter"`
- [ ] Spec module compiles cleanly

**Verify:** `./gradlew :maia-gen:maia-gen-spec:compileKotlin` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Add `routePath` to `BlotterPageDef`**

Open `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/BlotterPageDef.kt`.

After the existing `blotterComponentSelector` property, add:

```kotlin
val routePath = "${blotterDef.dtoBaseName.toKebabCase()}-blotter"
```

The full file should look like:

```kotlin
package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class BlotterPageDef(
    val blotterDef: BlotterDef,
    val pageTitle: String,
) {


    private val genDir = GeneratedTypescriptDir.forPackage(blotterDef.packageName)


    val dataPageId = "${blotterDef.dtoBaseName.toSnakeCase()}_blotter"


    val pageAngularComponentNames = AngularComponentNames(
        blotterDef.packageName,
        "${blotterDef.dtoBaseName}BlotterPage"
    )


    val blotterComponentSelector = "app-${blotterDef.dtoBaseName.toKebabCase()}-blotter"


    val routePath = "${blotterDef.dtoBaseName.toKebabCase()}-blotter"


    val blotterComponentClassName = "${blotterDef.dtoBaseName}Blotter"


    val blotterComponentTypescriptImport = TypescriptImport(
        name = blotterComponentClassName,
        from = "@$genDir/${blotterDef.dtoBaseName.toKebabCase()}-blotter"
    )


}
```

- [ ] **Step 2: Compile**

```bash
./gradlew :maia-gen:maia-gen-spec:compileKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/BlotterPageDef.kt
git commit -m "feat: add routePath to BlotterPageDef"
```

---

### Task 2: Implement `GenRoutesRenderer`

**Goal:** Create the renderer that produces a `*-gen-routes.ts` file for a given entity's page defs.

**Files:**
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/GenRoutesRenderer.kt`

**Acceptance Criteria:**
- [ ] Renderer compiles cleanly
- [ ] File path returned by `renderedFilePath()` is `{genDir}/{entityBaseName-kebab}-gen-routes.ts`
- [ ] Exported const name is `{entityBaseNameFirstLower}GenRoutes`
- [ ] Only non-null page defs emit route entries
- [ ] Route ordering: blotter → view → create → edit
- [ ] Blotter path uses `blotterPageDef.routePath`
- [ ] View path is `viewPageUrl` with leading `/` stripped and `/:id` appended
- [ ] Create path is `createPageUrl` with leading `/` stripped
- [ ] Edit path is `editEntityPageUrl` with leading `/` stripped and `/:id` appended
- [ ] loadComponent imports are relative (e.g. `'./simple-blotter-page'`)

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Create `GenRoutesRenderer.kt`**

Create the file at `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/GenRoutesRenderer.kt` with this content:

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.BlotterPageDef
import org.maiaframework.gen.spec.definition.EntityCreatePageDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityEditPageDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.lang.text.StringFunctions


class GenRoutesRenderer(
    private val entityDef: EntityDef,
    private val blotterPageDef: BlotterPageDef? = null,
    private val entityDetailViewDef: EntityDetailViewDef? = null,
    private val entityCreatePageDef: EntityCreatePageDef? = null,
    private val entityEditPageDef: EntityEditPageDef? = null,
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(entityDef.packageName)
    private val entityBaseName = entityDef.entityBaseName
    private val constName = "${StringFunctions.firstToLower(entityBaseName.value)}GenRoutes"


    init {
        addImport("@angular/router", "Routes")
    }


    override fun renderedFilePath(): String {
        return "$genDir/${entityBaseName.toKebabCase()}-gen-routes.ts"
    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("export const $constName: Routes = [")
        blotterPageDef?.let { renderBlotterRoute(it) }
        entityDetailViewDef?.let { renderViewRoute(it) }
        entityCreatePageDef?.let { renderCreateRoute(it) }
        entityEditPageDef?.let { renderEditRoute(it) }
        appendLine("];")

    }


    private fun renderBlotterRoute(def: BlotterPageDef) {
        appendLine("    {")
        appendLine("        path: '${def.routePath}',")
        appendLine("        loadComponent: () =>")
        appendLine("            import('./${def.pageAngularComponentNames.componentNameKebab}').then(m => m.${def.pageAngularComponentNames.componentName}),")
        appendLine("    },")
    }


    private fun renderViewRoute(def: EntityDetailViewDef) {
        val path = def.viewPageUrl.removePrefix("/") + "/:id"
        appendLine("    {")
        appendLine("        path: '$path',")
        appendLine("        loadComponent: () =>")
        appendLine("            import('./${def.viewPageAngularComponentNames.componentNameKebab}').then(m => m.${def.viewPageAngularComponentNames.componentName}),")
        appendLine("    },")
    }


    private fun renderCreateRoute(def: EntityCreatePageDef) {
        val path = def.createPageUrl.removePrefix("/")
        appendLine("    {")
        appendLine("        path: '$path',")
        appendLine("        loadComponent: () =>")
        appendLine("            import('./${def.createPageAngularComponentNames.componentNameKebab}').then(m => m.${def.createPageAngularComponentNames.componentName}),")
        appendLine("    },")
    }


    private fun renderEditRoute(def: EntityEditPageDef) {
        val path = def.entityDef.editEntityPageUrl.removePrefix("/") + "/:id"
        appendLine("    {")
        appendLine("        path: '$path',")
        appendLine("        loadComponent: () =>")
        appendLine("            import('./${def.editPageAngularComponentNames.componentNameKebab}').then(m => m.${def.editPageAngularComponentNames.componentName}),")
        appendLine("    },")
    }


}
```

- [ ] **Step 2: Compile**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/GenRoutesRenderer.kt
git commit -m "feat: add GenRoutesRenderer"
```

---

### Task 3: Wire `GenRoutesRenderer` into `AngularUiModuleGenerator`

**Goal:** Call `GenRoutesRenderer` from the generator, grouped by entity, so routes files are emitted during `maiaGeneration`.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt`

**Acceptance Criteria:**
- [ ] `renderGenRoutes()` is called from `onGenerateSource()`
- [ ] One `GenRoutesRenderer` is created per unique entity that appears in any of the four page def lists
- [ ] Blotter page defs with a null `rootEntityDef` are skipped
- [ ] Generator module compiles cleanly

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Add `renderGenRoutes()` call in `onGenerateSource()`**

In `AngularUiModuleGenerator.kt`, add `renderGenRoutes()` to the `onGenerateSource()` method alongside the other `render*()` calls (add after `renderBlotterPages()`):

```kotlin
override fun onGenerateSource() {

    processCrudApiDefs()
    renderAgGridDataSources()
    renderAngularForms()
    renderAsyncValidatorsForIndexes()
    renderAuthorityEnum()
    renderCommonModel()
    renderCrudServices()
    renderBlotterHtml()
    renderDtoServices()
    renderBlotterComponents()
    renderDtosForAsyncValidation()
    renderDtosForFormDefs()
    renderEntityDeleteDialogComponent()
    renderEntityDeleteDialogHtml()
    renderEntityDetailViews()
    renderEntityDetailDtoServices()
    renderEntityCreatePages()
    renderEntityEditPages()
    renderBlotterPages()
    renderGenRoutes()                    // <-- add this line
    renderEntityDetailsDtos()
    renderEnums()
    renderEsDocs()
    renderFetchForEditDtos()
    renderForeignKeyDialogs()
    renderForeignKeyService()
    renderPkAndNameDtos()
    renderRequestDtos()
    renderSimpleResponseDtos()
    renderSearchableResponseDtos()
    renderSearchableServices()
    renderBlotterDto()
    renderTypeaheadDtos()
    renderTypeaheadServices()
    renderValidatorsForTypeaheadFields()

}
```

- [ ] **Step 2: Add `renderGenRoutes()` method**

Add the following method to `AngularUiModuleGenerator`, after the `renderBlotterPages()` method:

```kotlin
private fun renderGenRoutes() {

    val blotterPageByEntity = this.modelDef.blotterPageDefs
        .mapNotNull { pageDef -> pageDef.blotterDef.blotterSourceDef.rootEntityDef?.let { it to pageDef } }
        .toMap()

    val viewPageByEntity = this.modelDef.entityDetailViewDefs
        .associateBy { it.entityDef }

    val createPageByEntity = this.modelDef.entityCreatePageDefs
        .associateBy { it.entityDef }

    val editPageByEntity = this.modelDef.entityEditPageDefs
        .associateBy { it.entityDef }

    val allEntities = (blotterPageByEntity.keys + viewPageByEntity.keys + createPageByEntity.keys + editPageByEntity.keys)
        .distinct()

    allEntities.forEach { entityDef ->
        GenRoutesRenderer(
            entityDef = entityDef,
            blotterPageDef = blotterPageByEntity[entityDef],
            entityDetailViewDef = viewPageByEntity[entityDef],
            entityCreatePageDef = createPageByEntity[entityDef],
            entityEditPageDef = editPageByEntity[entityDef],
        ).renderToDir(this.typescriptOutputDir)
    }

}
```

- [ ] **Step 3: Add import for `GenRoutesRenderer`**

At the top of `AngularUiModuleGenerator.kt`, add the import alongside the existing renderer imports:

```kotlin
import org.maiaframework.gen.renderers.ui.GenRoutesRenderer
```

- [ ] **Step 4: Compile**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt
git commit -m "feat: wire GenRoutesRenderer into AngularUiModuleGenerator"
```

---

### Task 4: Run generation and update `app.routes.ts`

**Goal:** Run the generator to produce the `*-gen-routes.ts` files, verify their content, then update `app.routes.ts` to import and spread them, removing the now-redundant manual entries.

**Files:**
- Generated (inspect): `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/*/`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`

**Acceptance Criteria:**
- [ ] `maiaGeneration` completes successfully
- [ ] A `*-gen-routes.ts` file exists for each entity that has at least one page def
- [ ] Each generated file exports the correct `Routes` constant with correct paths and component names
- [ ] `app.routes.ts` imports and spreads each generated constant
- [ ] All previously manual route entries for generated components are removed from `app.routes.ts`
- [ ] Manual routes (`login`, `user-group-membership-blotter`, `left-many/view/:id`, `elastic-indices`, `jobs-dashboard`) remain untouched

**Verify:** `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Run generation**

```bash
./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Verify generated files exist**

```bash
find maia-showcase/maia-showcase-ui/src/generated -name "*-gen-routes.ts"
```

Expected output should list one file per entity with page defs, e.g.:

```
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-gen-routes.ts
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/all-field-types/all-field-types-gen-routes.ts
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/join/bravo-gen-routes.ts
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/versioned/some-versioned-gen-routes.ts
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/composite-pk/composite-primary-key-gen-routes.ts
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-gen-routes.ts
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-gen-routes.ts
```

Adjust for the actual entities in the spec. Verify the content of one file looks correct, e.g. for simple:

```bash
cat "maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-gen-routes.ts"
```

Expected content:
```typescript
// This source was generated by the Maia Framework code generator
// Renderer class: class org.maiaframework.gen.renderers.ui.GenRoutesRenderer

import {Routes} from '@angular/router';

export const simpleGenRoutes: Routes = [
    {
        path: 'simple-blotter',
        loadComponent: () =>
            import('./simple-blotter-page').then(m => m.SimpleBlotterPage),
    },
    {
        path: 'simple/view/:id',
        loadComponent: () =>
            import('./simple-entity-detail-view-page').then(m => m.SimpleEntityDetailViewPage),
    },
    {
        path: 'simple/create',
        loadComponent: () =>
            import('./simple-entity-create-page').then(m => m.SimpleEntityCreatePage),
    },
    {
        path: 'simple/edit/:id',
        loadComponent: () =>
            import('./simple-entity-edit-page').then(m => m.SimpleEntityEditPage),
    },
];
```

- [ ] **Step 3: Update `app.routes.ts`**

Replace `maia-showcase/maia-showcase-ui/src/app/app.routes.ts` with the following. Add one import per generated routes file (found in step 2), spread each constant into the `routes` array, and keep the manual routes untouched.

Use the actual generated file paths found in step 2 to fill in the imports. The structure should be:

```typescript
import {Routes} from '@angular/router';
import {allFieldTypesGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/all-field-types/all-field-types-gen-routes';
import {bravoGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/join/bravo-gen-routes';
import {compositePrimaryKeyGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/composite-pk/composite-primary-key-gen-routes';
import {leftManyGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-gen-routes';
import {simpleGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-gen-routes';
import {someVersionedGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/versioned/some-versioned-gen-routes';
import {userGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-gen-routes';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./pages/home/home-page').then(
                (m) => m.HomePage,
            ),
    },
    ...allFieldTypesGenRoutes,
    ...bravoGenRoutes,
    ...compositePrimaryKeyGenRoutes,
    ...leftManyGenRoutes,
    ...simpleGenRoutes,
    ...someVersionedGenRoutes,
    ...userGenRoutes,
    {
        path: 'login',
        loadComponent: () =>
            import('./pages/login/login-page').then(
                (m) => m.LoginPage,
            ),
    },
    {
        path: 'user-group-membership-blotter',
        loadComponent: () =>
            import('@app/pages/user-group-membership-blotter/user-group-membership-blotter-page').then(
                (m) => m.UserGroupMembershipBlotterPage,
            ),
    },
    {
        path: 'left-many/view/:id',
        loadComponent: () =>
            import('./pages/left-many-view/left-many-view-page').then(
                (m) => m.LeftManyViewPage,
            ),
    },
    {
        path: 'elastic-indices',
        loadComponent: () =>
            import('./pages/elastic-indices/elastic-indices-page').then(
                (m) => m.ElasticIndicesPage,
            ),
    },
    {
        path: 'jobs-dashboard',
        loadComponent: () =>
            import('./pages/jobs-dashboard/jobs-dashboard-page').then(
                (m) => m.JobsDashboardPage,
            ),
    },
];
```

Add or remove spread entries based on the actual files found in step 2. Adjust import paths to match the actual generated file locations.

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/
git add maia-showcase/maia-showcase-ui/src/app/app.routes.ts
git commit -m "feat: switch app.routes.ts to use generated gen-routes spreads"
```
