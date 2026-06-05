# Edit Page Width Constraint

## Goal

Constrain the width of edit forms on generated edit pages and center them in the page. Currently the form expands to the full browser width.

## Changes

### 1. Generator — `EntityEditPageHtmlRenderer.kt`

**File:** `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityEditPageHtmlRenderer.kt`

Wrap the edit form component selector in `<div class="edit-form-container">`:

```html
<app-page-layout pageTitle="..." dataPageId="...">
    @if (entityId(); as id) {
        <div class="edit-form-container">
            <app-...-entity-edit-form
                [entityId]="id"
                (onSave)="onSaveClicked()"
                (onCancel)="onCancelClicked()"
            />
        </div>
    }
</app-page-layout>
```

### 2. Global CSS — `styles.scss`

**File:** `maia-showcase/maia-showcase-ui/src/styles.scss`

```scss
.edit-form-container {
  max-width: 36rem;
  margin: 0 auto;
}
```

## Out of Scope

- Create pages (not included in this change)
- View pages (already handled in previous spec)
- Changes to form field layout or spacing within the form

## Regeneration

After changing `EntityEditPageHtmlRenderer.kt`, run:

```bash
./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
```
