# View Entity Page Styling

## Goal

Replace the unstyled label/value rows on generated detail view pages with a clean two-column definition grid (Option C — minimal), and give the page title proper heading treatment. Action buttons remain below the field list.

## Changes

### 1. `PageLayout` component

**File:** `maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.html`

Change the `pageTitle` binding from a `<span>` to an `<h1 class="page-title">`.

**File:** `maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.ts`

Add `styleUrl: './page-layout.scss'` to the `@Component` decorator.

**File:** `maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.scss` (new)

Scoped styles for `.page-title` within the component.

### 2. Generator — `EntityDetailViewContentHtmlRenderer.kt`

**File:** `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityDetailViewContentHtmlRenderer.kt`

Rename emitted CSS classes:

| Old | New |
|-----|-----|
| `class="row"` | `class="detail-row"` |
| `class="col"` (label, first) | `class="detail-label"` |
| `class="col"` (value, second) | `class="detail-value"` |

No structural change to the HTML. After changing the renderer, re-run `maiaGeneration` for all modules that produce detail views.

### 3. Global CSS — `styles.scss`

**File:** `maia-showcase/maia-showcase-ui/src/styles.scss`

Add three rules:

**`.detail-row`**
- `display: grid`
- `grid-template-columns: 11.25rem 1fr`
- `border-bottom: 1px solid var(--mat-sys-surface-variant)`

**`.detail-label`**
- `padding: 0.4375rem 0`
- `font-size: 0.6875rem`
- `text-transform: uppercase`
- `letter-spacing: 0.04em`
- `color: var(--mat-sys-outline)`

**`.detail-value`**
- `padding: 0.4375rem 0`
- `font-size: 0.8125rem`
- `color: var(--mat-sys-on-surface)`

All color values use Material 3 CSS variables so they respect the active palette and light/dark mode from `material-theme.scss`.

## Out of Scope

- Action button placement (buttons remain below the field list, no named content projection)
- Field grouping or sections within the detail view
- Changes to pages other than entity detail views
- Changes to create/edit form pages

## Regeneration

After changing `EntityDetailViewContentHtmlRenderer.kt`, run:

```
./gradlew :maia-showcase:maia-showcase-domain:maiaGeneration
./gradlew :maia-showcase:maia-showcase-web:maiaGeneration
```

(or whichever modules produce Angular detail view files — confirm with `find src/generated -name "*detail-view.html"` in the UI project)
