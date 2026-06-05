# Edit Page Width Constraint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Constrain generated edit form pages to a max width of 36rem and center them in the page.

**Architecture:** `EntityEditPageHtmlRenderer.kt` wraps the edit form component in `<div class="edit-form-container">`. A matching CSS rule in `styles.scss` applies the max-width and centering. `maiaGeneration` regenerates all edit page HTML files.

**Tech Stack:** Kotlin (Maia generator), Angular 21, SCSS

**User Verification:** NO

---

## File Map

| File | Action | Purpose |
|------|--------|---------|
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityEditPageHtmlRenderer.kt` | Modify | Wrap edit form in `.edit-form-container` div |
| `maia-showcase/maia-showcase-ui/src/styles.scss` | Modify | Add `.edit-form-container` CSS rule |
| `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/**/*edit-page.html` | Regenerated | Picks up wrapper div after `maiaGeneration` |

---

## Task 1: Add container CSS, update generator, regenerate

**Goal:** Add the `.edit-form-container` CSS rule, update the generator to emit the wrapper div, and regenerate all edit page HTML files.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityEditPageHtmlRenderer.kt`
- Modify: `maia-showcase/maia-showcase-ui/src/styles.scss`
- Regenerated: `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/**/*edit-page.html`

**Acceptance Criteria:**
- [ ] `EntityEditPageHtmlRenderer.kt` wraps the form selector in `<div class="edit-form-container">`
- [ ] `styles.scss` contains `.edit-form-container` with `max-width: 36rem` and `margin: 0 auto`
- [ ] All generated `*edit-page.html` files contain `edit-form-container`
- [ ] No generated `*edit-page.html` file is missing the wrapper

**Verify:**
```bash
grep -r "edit-form-container" maia-showcase/maia-showcase-ui/src/generated/
```
Expected: one match per generated edit page file

**Steps:**

- [ ] **Step 1: Update `EntityEditPageHtmlRenderer.kt`**

Full file content:

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityEditPageDef


class EntityEditPageHtmlRenderer(
    private val entityEditPageDef: EntityEditPageDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return entityEditPageDef.editPageAngularComponentNames.htmlRenderedFilePath

    }


    override fun renderSource(): String {

        append("""
            |<app-page-layout pageTitle="${entityEditPageDef.pageTitle}" dataPageId="${entityEditPageDef.dataPageId}">
            |    @if (entityId(); as id) {
            |        <div class="edit-form-container">
            |            <${entityEditPageDef.editFormAngularComponentNames.componentSelector}
            |                [entityId]="id"
            |                (onSave)="onSaveClicked()"
            |                (onCancel)="onCancelClicked()"
            |            />
            |        </div>
            |    }
            |</app-page-layout>
            |""".trimMargin())

        return sourceCode.toString()

    }


}
```

- [ ] **Step 2: Add CSS rule to `styles.scss`**

Append to the end of `maia-showcase/maia-showcase-ui/src/styles.scss`:

```scss
.edit-form-container {
  max-width: 36rem;
  margin: 0 auto;
}
```

- [ ] **Step 3: Regenerate Angular UI files**

Run from the repo root:

```bash
./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Verify generated files contain the wrapper**

```bash
grep -r "edit-form-container" maia-showcase/maia-showcase-ui/src/generated/
```

Expected: one match per generated edit page file. Also spot-check:

```bash
cat maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-entity-edit-page.html
```

Expected output:
```html
<app-page-layout pageTitle="Edit Simple" dataPageId="simple_edit">
    @if (entityId(); as id) {
        <div class="edit-form-container">
            <app-simple-entity-edit-form
                [entityId]="id"
                (onSave)="onSaveClicked()"
                (onCancel)="onCancelClicked()"
            />
        </div>
    }
</app-page-layout>
```

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityEditPageHtmlRenderer.kt
git add maia-showcase/maia-showcase-ui/src/styles.scss
git add maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: constrain edit form pages to max-width 36rem centered"
```
