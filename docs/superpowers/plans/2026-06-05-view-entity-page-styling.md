# View Entity Page Styling Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the unstyled label/value rows on generated detail view pages with a clean two-column definition grid, and give the page title proper heading treatment.

**Architecture:** Two independent changes: (1) update the `PageLayout` component to render `pageTitle` as a styled `<h1>`, and (2) rename the CSS classes emitted by `EntityDetailViewContentHtmlRenderer` from the generic `.row`/`.col` to semantic `.detail-row`/`.detail-label`/`.detail-value`, add corresponding global CSS, and regenerate. The CSS uses Material 3 CSS variables throughout so it respects the active theme.

**Tech Stack:** Angular 21, Angular Material 3, SCSS, Kotlin (Maia generator)

**User Verification:** NO

---

## File Map

| File | Action | Purpose |
|------|--------|---------|
| `maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.html` | Modify | Render title as `<h1>` instead of `<span>` |
| `maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.ts` | Modify | Add `styleUrl` reference |
| `maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.scss` | Create | Scoped `.page-title` styles |
| `maia-showcase/maia-showcase-ui/src/styles.scss` | Modify | Add `.detail-row`, `.detail-label`, `.detail-value` global rules |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityDetailViewContentHtmlRenderer.kt` | Modify | Emit new semantic class names |
| `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/**/*detail-view.html` | Regenerated | Picks up new class names after `maiaGeneration` |

---

## Task 1: Update PageLayout to render page title as h1

**Goal:** Replace the unstyled `<span>` title in `PageLayout` with a styled `<h1>` using scoped component CSS.

**Files:**
- Modify: `maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.html`
- Modify: `maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.ts`
- Create: `maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.scss`

**Acceptance Criteria:**
- [ ] `page-layout.html` renders `pageTitle` inside `<h1 class="page-title">` not `<span>`
- [ ] `page-layout.ts` references `page-layout.scss` via `styleUrl`
- [ ] `page-layout.scss` exists with `.page-title` rule using M3 CSS variables
- [ ] Browser shows bold heading with bottom border on any detail view page

**Verify:** Open http://localhost:4200 in browser, navigate to any entity detail view → page title renders as a large bold heading with a 2px bottom border

**Steps:**

- [ ] **Step 1: Update `page-layout.html`**

Replace the `<span>` with an `<h1>`. Keep `[attr.data-page-id]` on the heading element so Playwright tests can still find it.

Full file content:

```html
<div>
  <div class="page-content">
    <h1 class="page-title" [attr.data-page-id]="dataPageId">{{pageTitle}}</h1>
    <ng-content></ng-content>
  </div>
</div>
```

- [ ] **Step 2: Create `page-layout.scss`**

```scss
.page-title {
  font-size: 1.375rem;
  font-weight: 700;
  margin: 0 0 0.875rem;
  padding-bottom: 0.625rem;
  border-bottom: 2px solid var(--mat-sys-on-surface);
  letter-spacing: -0.02em;
}
```

- [ ] **Step 3: Update `page-layout.ts` to reference the stylesheet**

Full file content:

```typescript
import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-page-layout',
  standalone: true,
  styleUrl: './page-layout.scss',
  templateUrl: './page-layout.html'
})
export class PageLayout {

  @Input() pageTitle!: string;
  @Input() dataPageId?: string;

}
```

- [ ] **Step 4: Verify in browser**

With `ng serve` already running (hot reload picks up the change automatically), navigate to any detail view page. The page title should appear as a bold heading (~22px) with a 2px solid bottom border. Previously it was an unstyled inline `<span>`.

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/components/page-layout/
git commit -m "feat: render page title as styled h1 in PageLayout component"
```

---

## Task 2: Rename detail view CSS classes in generator, add CSS, regenerate

**Goal:** Update `EntityDetailViewContentHtmlRenderer` to emit semantic class names, add the corresponding global CSS rules, and regenerate all detail view HTML files.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityDetailViewContentHtmlRenderer.kt`
- Modify: `maia-showcase/maia-showcase-ui/src/styles.scss`
- Regenerated: `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/**/*detail-view.html`

**Acceptance Criteria:**
- [ ] Renderer emits `detail-row`, `detail-label`, `detail-value` (not `row`, `col`)
- [ ] `styles.scss` contains rules for all three new classes using M3 CSS variables
- [ ] All `*detail-view.html` files in `src/generated/` use the new class names
- [ ] Browser shows two-column definition grid with muted uppercase labels and normal-weight values

**Verify:**
```bash
grep -r "class=\"row\"\|class=\"col\"" maia-showcase/maia-showcase-ui/src/generated/
```
Expected: no output (all old class names replaced)

**Steps:**

- [ ] **Step 1: Update `EntityDetailViewContentHtmlRenderer.kt`**

Change `"row"` → `"detail-row"`, first `"col"` (label) → `"detail-label"`, second `"col"` (value) → `"detail-value"` in both the standard field branch and the `PkAndNameFieldType` branch.

Full file content:

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType

class EntityDetailViewContentHtmlRenderer(private val entityDetailViewDef: EntityDetailViewDef) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String {

        return entityDetailViewDef.viewContentComponentHtmlRenderedFilePath

    }


    override fun renderSource(): String {

        appendLine("@if (detailDto$ | async; as detailDto) {")
        appendLine("  <div>")

        this.entityDetailViewDef.dtoDef.allFields.forEach { classFieldDef ->

            val pipes = if (classFieldDef.pipes.isEmpty()) {
                ""
            } else {
                classFieldDef.pipes.joinToString(prefix = " | ", separator = " | ")
            }

            val fieldType = classFieldDef.fieldType

            if (fieldType is PkAndNameFieldType) {

                append("""
                    |    <div class="detail-row">
                    |      <div class="detail-label">${classFieldDef.displayName}</div>
                    |      <div class="detail-value">{{detailDto.${classFieldDef.classFieldName}.name$pipes}}</div>
                    |    </div>
                    |""".trimMargin()
                )

            } else {

                append("""
                    |    <div class="detail-row">
                    |      <div class="detail-label">${classFieldDef.displayName}</div>
                    |      <div class="detail-value">{{detailDto.${classFieldDef.classFieldName}$pipes}}</div>
                    |    </div>
                    |""".trimMargin()
                )

            }

        }

        appendLine("  </div>")
        appendLine("} @else {")
        appendLine("  <mat-spinner></mat-spinner>")
        appendLine("}")

        return sourceCode.toString()

    }


}
```

- [ ] **Step 2: Add CSS rules to `styles.scss`**

Append these three rules to the end of `maia-showcase/maia-showcase-ui/src/styles.scss`:

```scss
.detail-row {
  display: grid;
  grid-template-columns: 11.25rem 1fr;
  border-bottom: 1px solid var(--mat-sys-surface-variant);
}

.detail-label {
  padding: 0.4375rem 0;
  font-size: 0.6875rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--mat-sys-outline);
}

.detail-value {
  padding: 0.4375rem 0;
  font-size: 0.8125rem;
  color: var(--mat-sys-on-surface);
}
```

- [ ] **Step 3: Regenerate Angular UI files**

```bash
./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
```

Expected: BUILD SUCCESSFUL. The task runs `AngularUiModuleGeneratorKt` and overwrites all files under `maia-showcase/maia-showcase-ui/src/generated/typescript/`.

- [ ] **Step 4: Verify generated files use new class names**

```bash
grep -r "class=\"row\"\|class=\"col\"" maia-showcase/maia-showcase-ui/src/generated/
```

Expected: no output.

```bash
grep -c "detail-row" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-entity-detail-view.html
```

Expected: `3` (one per field in the simple entity).

- [ ] **Step 5: Verify in browser**

With `ng serve` running, navigate to any entity detail view page. Each field should display as a two-column row: muted uppercase label on the left (~11.25rem wide), normal-weight value on the right, with a subtle divider between rows.

- [ ] **Step 6: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityDetailViewContentHtmlRenderer.kt
git add maia-showcase/maia-showcase-ui/src/styles.scss
git add maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: style detail view field rows as definition grid with semantic CSS classes"
```
