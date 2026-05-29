# Generated Module Routes

## Goal

Generate Angular route definitions for generated page components into a per-entity TypeScript file, then incorporate those routes into `app.routes.ts` via a one-time manual import and spread.

## Context

`app.routes.ts` currently mixes ~14 manually maintained route entries for generated components (blotter pages, create/edit/view pages) with ~5 truly manual routes (login, hand-written pages). The generated entries must be kept in sync by hand whenever new entities or pages are added to the spec. This design eliminates that maintenance burden for the generated entries.

## Integration Pattern

**Option A — manual spread (one-time edit).** The generator produces a `*-gen-routes.ts` file per entity. `app.routes.ts` is hand-edited once to import and spread each file's exported `Routes` constant alongside the remaining manual routes. No generator ever touches `app.routes.ts` again.

## File Granularity

One generated routes file per entity that has at least one page def. Entities with only a blotter page (no create/edit/view) still get their own file.

## Spec Changes

### `BlotterPageDef` — add `routePath`

`BlotterPageDef` does not currently expose a route path string. Add:

```kotlin
val routePath = "${blotterDef.dtoBaseName.toKebabCase()}-blotter"
```

The other page defs already carry what is needed:
- `EntityCreatePageDef.createPageUrl` — e.g. `/simple/create`
- `EntityDetailViewDef.viewPageUrl` — e.g. `/simple/view`
- `EntityEditPageDef` — via `entityDef.editEntityPageUrl` e.g. `/simple/edit`

The renderer strips the leading `/` from these URLs and appends `/:id` for view and edit routes.

## New Renderer: `GenRoutesRenderer`

Location: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/GenRoutesRenderer.kt`

Receives per-entity page defs:
- `blotterPageDef: BlotterPageDef?`
- `entityDetailViewDef: EntityDetailViewDef?`
- `entityCreatePageDef: EntityCreatePageDef?`
- `entityEditPageDef: EntityEditPageDef?`

At least one must be non-null. Only entries for non-null defs are emitted.

**Output file path:** `{genDir}/{entityBaseName.toKebabCase()}-gen-routes.ts`

Where `genDir` is derived from the entity's package name via `GeneratedTypescriptDir.forPackage(packageName)`, and `entityBaseName` is the entity's base name (e.g. `Simple`, `Bravo`).

**Exported constant name:** `{entityBaseName.firstToLower()}GenRoutes` (e.g. `simpleGenRoutes`)

**Route ordering:** blotter → view → create → edit (matches existing `app.routes.ts` convention).

**Import style:** relative imports within the same gen-components directory (e.g. `'./simple-blotter-page'`).

### Example output — `simple-gen-routes.ts`

```ts
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

## Generator Change: `AngularUiModuleGenerator`

Add `renderGenRoutes()` called from `onGenerateSource()`.

**Grouping logic:** collect all entities that have at least one page def in this model:

1. Build a map from `EntityDef` → collected page defs by checking:
   - `modelDef.blotterPageDefs` — link via `blotterDef.blotterSourceDef.rootEntityDef`
   - `modelDef.entityDetailViewDefs` — link via `entityDef`
   - `modelDef.entityCreatePageDefs` — link via `entityDef`
   - `modelDef.entityEditPageDefs` — link via `entityDef`
2. For each entity in the map, instantiate `GenRoutesRenderer` with its collected defs and call `renderToDir(typescriptOutputDir)`.

Blotter page defs whose `rootEntityDef` is null are skipped (no entity to key on).

## App Change: `app.routes.ts` (one-time)

For each generated routes file, add one import and spread it into the `routes` array:

```ts
import {simpleGenRoutes} from '../generated/typescript/main/app/gen-components/org/maiaframework/showcase/simple/simple-gen-routes';

export const routes: Routes = [
    ...simpleGenRoutes,
    // ...other generated spreads...
    { path: 'login', loadComponent: … },
    // ...remaining manual routes...
];
```

Remove the now-redundant individual route entries that are covered by the spread.
