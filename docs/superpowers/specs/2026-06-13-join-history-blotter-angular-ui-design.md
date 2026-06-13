# Join-Entity History Blotter Angular UI

## Goal

Plan 1 made `LeftToRightSimpleJoin` history globally searchable on the backend
(`POST /api/left-to-right-simple-join/history/search`, no `{entityId}`), and
`EntityHistoryBlotterDef.isJoinEntityHistory` / `searchEndpointUrlForTypescript`
are already join-aware. This plan makes the **Angular generator** join-aware
to match, and wires up the showcase app so the global history blotter is a
reachable page.

## Architecture

Branch the existing per-entity history blotter renderers on
`def.isJoinEntityHistory` (already computed from `entityDef.isManyToManyJoinEntity`).
For join history: drop all `entityId` / `:id` route-param plumbing and use the
already-global `searchEndpointUrlForTypescript`. Fix the routes generator so an
entity with *only* a history blotter (no blotter/view/create/edit pages) still
gets a routes file. Manually wire the new route + nav entry into the showcase
app shell (these files are hand-maintained, not generated).

## Component changes

### 1. `EntityHistoryBlotterPageComponentRenderer`
- Non-join (unchanged): `entityId` signal derived from `ActivatedRoute.paramMap`.
- Join: drop `ActivatedRoute`/`toSignal`/`map` imports; emit an empty class body
  `export class ${componentName} {}`.

### 2. `EntityHistoryBlotterPageHtmlRenderer`
- Non-join (unchanged): `@if (entityId(); as id) { <sel [entityId]="id" /> }`.
- Join: `<sel />` with no wrapper.

### 3. `EntityHistoryBlotterComponentRenderer`
- Non-join (unchanged): `@Input() entityId!: string`, `implements OnInit`,
  `ngOnInit() { this.datasource.setEntityId(this.entityId); }`.
- Join: no `@Input`, no `OnInit`/`ngOnInit`, drop `Input`/`OnInit` imports.

### 4. `EntityHistoryBlotterServiceRenderer`
- Non-join (unchanged): `search(entityId: string, searchModel: any)` posts to
  `` `/api/${kebab}/${entityId}/history/search` ``.
- Join: `search(searchModel: any)` posts to the literal
  `` `${def.searchEndpointUrlForTypescript}` `` (global path, no entityId param).

### 5. `EntityHistoryBlotterAgGridDatasourceRenderer`
- Non-join (unchanged): `entityId` field + `setEntityId()`, posts to interpolated
  `` `${def.searchEndpointUrlForTypescript}` ``.
- Join: drop `entityId`/`setEntityId`, post to the literal global URL.

`EntityHistoryBlotterHtmlRenderer` (generic ag-grid wrapper) is unchanged - it
has no `entityId` references.

## Routing changes

### `EntityCrudRoutesRenderer.renderHistoryRoute(def)`
- Non-join (unchanged): `path: '${def.routePath}/:id'`.
- Join: `path: '${def.routePath}'` (already `left-to-right-simple-join-history`,
  no `:id` - set by Plan 1).

### `AngularUiModuleGenerator.renderEntityCrudRoutes()`
`allEntities` currently unions entities present in
`blotterPageByEntity`/`viewPageByEntity`/`createPageByEntity`/`editPageByEntity`.
`LeftToRightSimpleJoinEntity` has none of these, so it gets no routes file.

Fix: union in `this.modelDef.entityHistoryBlotterDefs.map { it.entityDef }.toSet()`
so any entity with a `historyBlotterDef` gets a routes file even if it has no
CRUD pages. For entities that already have CRUD pages this is a no-op (already
in the set).

## Manual wiring (hand-maintained files)

### `maia-showcase-ui/src/app/app.routes.ts`
Add:
```ts
import {leftToRightSimpleJoinRoutes} from '@app/gen-components/org/maiaframework/showcase/many-to-many/left-to-right-simple-join-routes';
```
and spread `...leftToRightSimpleJoinRoutes,` into the `routes` array (alphabetical
with existing spreads).

### `maia-showcase-ui/src/app/app.html`
Add a nav menu entry, alphabetically positioned, gated by `hasReadAuthority()`,
matching the existing button style:
```html
@if (hasReadAuthority()) {
    <button mat-menu-item routerLink="/left-to-right-simple-join-history">
        <mat-icon>history</mat-icon>
        <span>Left-Right Join History</span>
    </button>
}
```

## Regeneration & verification

Run `:maia-showcase:maia-showcase-ui:maiaGeneration`, confirm build compiles,
and manually check the page renders in a browser (no automated test exists for
history blotters currently - this matches existing coverage for
`history-sample-blotter`, so no new test is added).

## Out of scope

- Non-join entity history blotters (unchanged, existing behavior preserved).
- Automated/Playwright tests for history blotters (none exist currently; YAGNI).
- Any other join entities besides `LeftToRightSimpleJoin` (no others currently
  have `recordVersionHistory = true`).
