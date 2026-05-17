---
name: simple-edit-page-generation
description: Generate the simple-edit page and edit form as part of the code generation framework, bundled with entityDetailView
metadata:
  type: project
---

# Simple Edit Page Generation

## Goal

Make the 5 manual files under `maia-showcase-ui/src/app/pages/simple-edit/` generated, following the same pattern as the generated `simple-entity-detail-view-*` files. The edit page definition is bundled with `entityDetailView()` in the spec, but registered as an independent collection in the model.

## Spec Layer

New class `EntityEditPageDef` in `maia-gen-spec`, parallel to `EntityDetailViewDef`:

- Fields: `entityDef`, `pageTitle`, `authority: AuthorityDef?`
- Computes `editFormAngularComponentNames` (suffix `EntityEditForm`) and `editFormPageAngularComponentNames` (suffix `EntityEditFormPage`)
- Computes `dataPageId`, `viewPageUrl = "/$modulePath${entityBaseName.toSnakeCase()}/view"` (navigates back to view on save/cancel)
- Exposes `updateApiDef` via `entityDef.entityCrudApiDef!!.updateApiDef!!`

The `entityDetailView()` builder gains an optional `withEditPage { }` lambda. Calling it creates an `EntityEditPageDef` and registers it into a new `modelDef.entityEditPageDefs` list (independent of `entityDetailViewDefs`).

Spec usage:
```kotlin
val simpleEntityDetailViewDef = entityDetailView(simpleEntityDef) {
    withEditPage {
        pageTitle = "Edit Simple"
        authority = partySpec.adminAuthority
    }
}
```

Cleanup:
- Remove `withEditEntityPage = HasEditEntityPage.TRUE` from the crud block
- Remove `withEntityForm` parameter from `update()` — edit page def is the sole trigger
- Delete `HasEditEntityPage` flag class and all references

## Generator Layer

Five renderers in `maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/`:

| Renderer | Output file | Analogue |
|---|---|---|
| `EntityEditFormPageComponentRenderer` | `simple-entity-edit-form-page.ts` | `EntityDetailViewPageComponentRenderer` |
| `EntityEditFormPageHtmlRenderer` | `simple-entity-edit-form-page.html` | `EntityDetailViewPageHtmlRenderer` |
| `EntityEditFormComponentRenderer` | `simple-entity-edit-form.ts` | `EntityDetailViewComponentRenderer` — uses `input.required<string>()` + `onSave`/`onCancel output()` instead of `MAT_DIALOG_DATA` |
| `EntityEditReactiveFormHtmlRenderer` | `simple-entity-edit-form.html` | already exists, no signature change — called with `entityEditPageDef.updateApiDef` from `renderEntityEditPages()` |
| `EntityEditFormScssRenderer` | `simple-entity-edit-form.scss` | already exists — fix: currently uses `angularDialogComponentNames` for path; change to `angularFormComponentNames` |

`AngularUiModuleGenerator` gains `renderEntityEditPages()` iterating `modelDef.entityEditPageDefs`, invoking all five renderers. The existing `renderEntityEditFormHtml()` (gated on `withEntityForm`) is left in place for backward compatibility.

Generated files land in `src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/`.

### `EntityEditFormComponentRenderer` shape

Mirrors `EntityDetailViewComponentRenderer` structure but generates edit form logic:
- `input.required<string>()` for entity ID
- `onSave = output()` and `onCancel = output()` signals
- `loading` and `problemDetail` signals
- `FormGroup` constructed in `constructor()`
- `ngOnInit()` calls `fetchForEdit()` and patches form
- `onSubmit()` calls `edit()` on the crud service
- `onCancelClicked()` emits `onCancel`
- Imports all async validators from the update API def

### `EntityEditFormPageComponentRenderer` shape

- Reads `:id` from `ActivatedRoute`
- On save: navigates to `viewPageUrl/:id`
- On cancel: navigates to `viewPageUrl/:id`
- `ChangeDetectionStrategy.OnPush`
- Imports `PageLayout` and `EntityEditFormComponent`

## Showcase Changes

1. Update `MaiaShowcaseSpec.kt` as shown above
2. Delete 5 manual files: `simple-edit-page.ts`, `simple-edit-page.html`, `simple-entity-edit-form.ts`, `simple-entity-edit-form.html`, `simple-entity-edit-form.scss`
3. Update `app.routes.ts`: `simple/edit/:id` imports from `...showcase/simple/simple-entity-edit-form-page` → `SimpleEntityEditFormPage`
4. Run `maiaGeneration` on the UI module
5. Delete `HasEditEntityPage` flag class and all references across `CrudDef`, `CrudDefBuilder`, `EntityDef`, `EntityDetailViewDef`

## Out of Scope

- Route generation (routes remain manually maintained in `app.routes.ts`)
- Auth guard wiring in the route (developer manually applies the auth guard when registering the route, using the authority from the spec)
- Changes to blotter edit action column navigation
