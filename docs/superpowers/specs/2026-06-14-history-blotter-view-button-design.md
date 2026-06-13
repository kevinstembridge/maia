# History Blotter "View" Button Design

## Overview

Add a "View" button to entity history blotter pages (e.g. `history-sample/history/:id`) that navigates back to that entity's detail View page (e.g. `/history-sample/view/:id`). This is the reverse of the existing "History" button on detail View pages.

## Scope

Applies only to non-join-entity history blotters (`EntityHistoryBlotterDef.isJoinEntityHistory == false`) whose entity has a generated `EntityDetailViewDef` (found via `ModelDef.findEntityDetailViewDef(entityDef)`). Global join-entity history blotter pages are unaffected — join entities have no detail view page.

## Changes

### `EntityHistoryBlotterPageHtmlRenderer`

When `viewPageDef != null && !def.isJoinEntityHistory`, add a button after the blotter component, inside `app-page-layout`:

```html
<button matButton aria-label="View" (click)="onViewClicked()">
    <mat-icon>visibility</mat-icon>
    View
</button>
```

### `EntityHistoryBlotterPageComponentRenderer`

When `viewPageDef != null && !def.isJoinEntityHistory`:
- Add imports: `Router` (from `@angular/router`), `MatButtonModule`, `MatIconModule` (from `@angular/material/button` / `@angular/material/icon`)
- Inject `Router`
- Add method:

```typescript
onViewClicked(): void {
    const id = this.entityId();
    if (id) {
        this.router.navigate(['${entityDef.viewEntityPageUrl}', id]);
    }
}
```

### Renderer constructors

Both `EntityHistoryBlotterPageHtmlRenderer` and `EntityHistoryBlotterPageComponentRenderer` gain a new constructor param:

```kotlin
viewPageDef: EntityDetailViewDef?
```

### `AngularUiModuleGenerator.renderEntityHistoryBlotters()`

Pass `this.modelDef.findEntityDetailViewDef(def.entityDef)` as `viewPageDef` to both page renderers.

## Regeneration

After the generator change, regenerate the showcase Angular UI. Entities with both `recordVersionHistory` and a detail view page (e.g. `HistorySample`, `Organization`, `CompositePrimaryKey`, `NonSurrogatePrimaryKey`) will get the new "View" button on their history blotter pages.

## Testing

Existing renderer unit tests for `EntityHistoryBlotterPageHtmlRenderer` / `EntityHistoryBlotterPageComponentRenderer` should be extended to cover:
- `viewPageDef != null`, `isJoinEntityHistory == false` → button + method rendered
- `viewPageDef == null` → no button/method
- `isJoinEntityHistory == true` → no button/method (regardless of `viewPageDef`)
