# Create Page Generation Design

**Date:** 2026-05-19

## Summary

Generate a standalone Angular "create" page for entities alongside the existing view and edit pages. The create page mirrors the content of the currently-generated create-dialog components but wraps them in a `PageLayout` instead of a material dialog. After a successful create, it navigates to the view page of the newly created entity (requiring the backend to return the new entity's `DomainId`).

---

## Architecture

Three layers of change:

1. **Spec layer** — new `EntityCreatePageDef` + DSL wiring into `entityDetailView`
2. **Renderer layer** — new `EntityCreatePageComponentRenderer` (TS) + `EntityCreatePageHtmlRenderer` (HTML)
3. **Endpoint layer** — backend create endpoint returns `DomainId`; frontend service changes `Observable<void>` → `Observable<string>`

---

## Spec Layer

### `EntityCreatePageDef`
Mirrors `EntityEditPageDef`. Key properties:
- `entityDef: EntityDef`
- `pageTitle: String` (default: `"Create ${entityDef.entityBaseName.toTitleCase()}"`)
- `createApiDef: EntityCreateApiDef` — accessed lazily from `entityDef.entityCrudApiDef.createApiDef`; throws if not present
- `createPageAngularComponentNames: AngularComponentNames` — based on `{Entity}EntityCreatePage` (e.g. `SimpleEntityCreatePage`, selector `app-simple-entity-create-page`)
- `dataPageId: String` — `${entityBaseName.toSnakeCase()}_create`
- `viewPageUrl: String` — delegates to `entityDef.viewEntityUrl`

### `EntityCreatePageDefBuilder`
Minimal builder, just exposes `var pageTitle: String`.

### DSL wiring
`EntityDetailViewDefBuilder` gains a `withCreatePage(init: EntityCreatePageDefBuilder.() -> Unit)` method, identical in structure to `withEditPage`. `AbstractSpec.entityDetailView()` registers the resulting `EntityCreatePageDef` into a new `entityCreatePageDefs` mutable list (same pattern as `entityEditPageDefs`).

```kotlin
entityDetailView(myEntity) {
    withEditPage { }
    withCreatePage { }    // new
}
```

### `ModelDef`
Gains `entityCreatePageDefs: List<EntityCreatePageDef>` parameter.

---

## Renderer Layer

### `EntityCreatePageComponentRenderer`
New `AbstractTypescriptRenderer` taking `EntityCreatePageDef`.

**Generated file:** `{entity}-entity-create-page.ts`

**Generated class:** e.g. `SimpleEntityCreatePage implements OnInit`

**Key differences from create dialog:**
| Create dialog | Create page |
|---|---|
| Imports `MatDialogRef`, `MatDialogTitle`, `MatDialogContent`, `MatDialogActions` | None of these |
| Injects `MatDialogRef` | Injects `Router`, `Location` |
| `next: () => { this.dialogRef.close(true); }` | `next: (newId) => { this.router.navigate([viewPageUrl, newId]); }` |
| `onCancel()` calls `dialogRef.close()` | `onCancel()` calls `this.location.back()` |
| No `PageLayout` | Imports and uses `PageLayout` |

**Full feature parity with create dialog:**
- Standard form fields with validation (required, min/max length, etc.)
- Async validators (single-field and multi-field unique index)
- Typeahead fields with subscriptions in `ngOnInit()`
- Many-to-many chip fields

The form construction logic (FormGroup, FormControls, validators) is identical to the create dialog — reuses `FormControlRendererHelper.renderFormControlFor()`.

### `EntityCreatePageHtmlRenderer`
New `AbstractSourceFileRenderer` taking `EntityCreatePageDef`.

**Generated file:** `{entity}-entity-create-page.html`

Structure:
```html
<app-page-layout pageTitle="{pageTitle}" dataPageId="{dataPageId}">
    <form [formGroup]="formGroup" novalidate (ngSubmit)="onSubmit()">
        @if (problemDetail()) {
            <p class="alert alert-warning" role="alert">{{ problemDetail()!.title }}</p>
        }
        <!-- same mat-form-field blocks as the create dialog -->
        <!-- chip fields if any -->
        <div>
            <button mat-flat-button type="submit" color="primary">Submit</button>
            <button mat-flat-button type="button" (click)="onCancel()">Cancel</button>
        </div>
    </form>
</app-page-layout>
```

Reuses `MatFormFieldRenderer.renderFormField()` for individual form fields (reactive form system).

### `EntityCreatePageScssRenderer`
New `AbstractTypescriptRenderer` taking `EntityCreatePageDef`. Generates `{entity}-entity-create-page.scss` with `.mat-mdc-form-field { width: 100%; }` — identical content to the create dialog SCSS. Required because the create page renders form fields directly (unlike the edit page, which delegates to a sub-component that owns its own SCSS).

### `AngularUiModuleGenerator`
New private method `renderEntityCreatePages()` called from `onGenerateSource()`:
```kotlin
private fun renderEntityCreatePages() {
    this.modelDef.entityCreatePageDefs.forEach { pageDef ->
        EntityCreatePageComponentRenderer(pageDef).renderToDir(this.typescriptOutputDir)
        EntityCreatePageHtmlRenderer(pageDef).renderToDir(this.typescriptOutputDir)
        EntityCreatePageScssRenderer(pageDef).renderToDir(this.typescriptOutputDir)
    }
}
```

---

## Endpoint Layer

### Backend: `CrudEndpointRenderer.kt`
Change create endpoint from `Unit` to `DomainId` return type:
```kotlin
// Before
fun create(@RequestBody @Valid createDto: $createDtoUqcn) {
    this.crudService.create(createDto)
}

// After
fun create(@RequestBody @Valid createDto: $createDtoUqcn): DomainId {
    return this.crudService.create(createDto).id
}
```

`DomainId` (a `StringType`) serializes as a plain JSON string via `@JsonValue`.

### Frontend: `DtoCrudServiceTypescriptRenderer.kt`
Change `http.post<void>` to `http.post<string>`:
```typescript
// Before
public create(requestDto: SimpleCreateRequestDto): Observable<void> {
    return this.http.post<void>('/api/...', requestDto, this.httpOptions);
}

// After
public create(requestDto: SimpleCreateRequestDto): Observable<string> {
    return this.http.post<string>('/api/...', requestDto, this.httpOptions);
}
```

**Compatibility:** All existing create dialog callers use `next: () => {}` (ignoring the emitted value). This remains valid TypeScript — no changes required to any dialog components.

---

## Generated File Names (example for `Simple` entity)

| File | Description |
|---|---|
| `simple-entity-create-page.ts` | Page component class |
| `simple-entity-create-page.html` | Page template |
| `simple-entity-create-page.scss` | Form field width styles |

---

## Unresolved Questions

None.
