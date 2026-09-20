# Maia Props Dashboard — Design

## Goal

A new Angular project `maia-props` (package `@maia/maia-props`) that lets an operator view every resolved application property (Spring `Environment` values merged with DB-stored overrides from `maia-props-parent`) and add, edit, remove, or inspect the history of a property override. Paired with the backend REST/service layer that doesn't exist yet.

## Context

`maia-props-parent` currently has `domain`/`dao`/`repo`/`spec`/`api`/`autoconfigure`/`starter` modules but no web layer. `Props` (read accessor, env fallback) and `PropsManager` (`setProperty`/`removeProperty`) live in `maia-props-api`. `PropsRepo` already exposes `getAllProperties()` and `getPropertyHistory(name)`. There is no REST controller anywhere exposing this to a UI.

The closest precedent for a hand-rolled (non-generated-CRUD-blotter) ops dashboard is `maia-job-parent` / `maia-jobs`, and for dialog + signal-store list UX, `maia-elasticsearch-parent` / `maia-elasticsearch`'s `elastic-indices` feature. Both are followed closely below.

## Backend

### Module layout

- **`maia-props-service`** (new, plain `maia.kotlin-library-conventions`): `api(project(":libs:maia-props-parent:maia-props-repo"))`. `PropsManager` **moves here** from `maia-props-api`, gains a `ConfigurableEnvironment` constructor parameter, and gains the new methods below. `setProperty` now returns `PropertyResponseDto` instead of `Unit`.
- **`maia-props-api`** shrinks to just `Props.kt` (unchanged otherwise).
- **`maia-props-autoconfigure`**: add `api(project(":libs:maia-props-parent:maia-props-service"))`; the `propsManager(...)` bean now also injects `Environment`.
- **`maia-elasticsearch/build.gradle.kts`**: add `api(project(":libs:maia-props-parent:maia-props-service"))` (it still needs `Props` from `maia-props-api` too) — it constructs `EsIndexActiveVersionManager` with both `Props` and `PropsManager`.
- **`maia-props-web`** (new, mirrors `maia-job-web`): `api(maia-props-service)`, `api(maia-props-domain)`, `api(maia-webapp-domain)`, `spring-boot-starter-security`, `spring-boot-starter-web`, `maiagen` task running `WebLayerModuleGeneratorKt` against `PropsApplicationSpec`. Contains hand-written `MaiaPropsEndpoint.kt`.
- `settings.gradle.kts`: add `include("libs:maia-props-parent:maia-props-service")` and `include("libs:maia-props-parent:maia-props-web")`.

### `PropsSpec.kt` additions

- `authority("MAIA_PROPS_READ")`, `authority("MAIA_PROPS_WRITE")`
- `simpleResponseDto("org.maiaframework.props", "Property")`: `propertyName`, `effectiveValue`, `isOverridden: Boolean`, `environmentValue: String?`, `sourceName: String?`, `lastModifiedByUsername: String?`, `lastModifiedTimestamp: Instant?`, `comment: String?` — the last three populated only when overridden.
- `simpleResponseDto("org.maiaframework.props", "PropertyHistoryItem")`: `propertyName`, `propertyValue`, `changeType`, `lastModifiedByUsername`, `lastModifiedTimestamp`, `comment`, `version`.

Both generated into `maia-props-domain`, reachable from `maia-props-service` via the existing `repo → api(dao) → api(domain)` chain.

### `PropsManager.getAllProperties()` merge logic

1. Enumerate every `EnumerablePropertySource` in the injected `ConfigurableEnvironment`'s `MutablePropertySources`, collecting `(propertyName, sourceName)` for each name (first source wins per name, matching normal Spring resolution order).
2. Union that name set with the names of all `PropsRepo.getAllProperties()` DB overrides, so an override for a name absent from the Environment is still included.
3. For each name: if a DB override exists, `effectiveValue` = override value, `isOverridden = true`, `sourceName = "DB override"`, plus the override's `lastModifiedByUsername`/`lastModifiedTimestamp`/`comment`; `environmentValue` = `env.getProperty(name)` (may be null) for comparison. Otherwise `effectiveValue` = `env.getProperty(name)`, `isOverridden = false`, `sourceName` = the owning property source's name, `environmentValue` = same as `effectiveValue`.

### `MaiaPropsEndpoint.kt` (in `maia-props-web`)

Base URL `${maia.props.web.base-url:/api/ops}`, mirroring the job endpoint's config style:

- `GET /props` → `List<PropertyResponseDto>` — `MAIA_PROPS_READ`
- `GET /props/{propertyName}/history` → `List<PropertyHistoryItemResponseDto>` — `MAIA_PROPS_READ`
- `POST /props/{propertyName}` (body: `propertyValue`, `comment?`) → `PropertyResponseDto` — `MAIA_PROPS_WRITE`, username via `CurrentUserHolder`
- `DELETE /props/{propertyName}` (body/param: `comment?`) → 204 — `MAIA_PROPS_WRITE`

## Frontend

New project `libs/maia-ui-workspace/projects/maia-props`, package `@maia/maia-props`, structured like `maia-elasticsearch`:

```
src/lib/props-dashboard/
  models/
    PropertyResponseDto.ts
    PropertyHistoryItemResponseDto.ts
  services/
    props-api-base-url.token.ts     (InjectionToken, default '/api/ops')
    props-api.service.ts            (getAllProperties / getPropertyHistory / setProperty / removeProperty)
  state/
    props-dashboard-filtering.ts (+ .spec.ts)   — pure filter/sort helpers
    props-dashboard-store.ts                    — @ngrx/signals store
  dialogs/
    edit-property-dialog/           — used for both "add override" and "edit override"
    remove-override-dialog/         — confirm + optional comment
    property-history-dialog/        — audit trail for one property
  props-dashboard-page.ts / .html / .scss
src/public-api.ts
```

Plus `package.json`, `ng-package.json`, `tsconfig.lib.json`, `tsconfig.lib.prod.json`, `tsconfig.spec.json` mirroring `maia-elasticsearch`'s, and registration in `libs/maia-ui-workspace/angular.json` + `libs/maia-ui-workspace/tsconfig.json`.

**`PropsDashboardStore`** (mirrors `ElasticIndicesPageStore`): state = `properties: PropertyResponseDto[]`, `isLoading`, `error`, `nameFilter`, `overriddenOnlyFilter`. Computed `visibleProperties`. Methods: `fetchAllProperties` (rxMethod), `retryFetch`, `onNameFilterChanged`, `onOverriddenOnlyToggled`, `applyPropertyUpdate`/`applyPropertyRemoval` (patch local state after a successful write instead of a full refetch).

**`PropsDashboardPage`**: `mat-table` (columns: Name, Effective Value, Overridden badge, Source, Last Modified By/When, Actions), name filter input, "overridden only" toggle, "Add override" button, per-row Edit / Remove (enabled only when `isOverridden`) / History actions.

**Edit dialog**: one form (property name, value, comment) for both add and edit — property name is free-text when adding, read-only when editing an existing row. Submits via `PropsApiService.setProperty`.

## Showcase integration

- `maia-showcase-ui/src/app/pages/props-dashboard/props-dashboard-page.ts` — thin wrapper using `PageLayout` + `<maia-props-dashboard-page />`, mirroring `elastic-indices-page.ts`
- New lazy route `props-dashboard` in `app.routes.ts`
- `maia-showcase-ui/tsconfig.json` path mapping for `@maia/maia-props`
- Grant `MAIA_PROPS_READ`/`MAIA_PROPS_WRITE` to the showcase admin/test user fixture

## Testing — black-box Playwright CRUD journey

Follows the established JVM-Playwright, page-object pattern (see `docs/superpowers/specs/2026-05-31-right-many-crud-playwright-test-design.md`) instead of unit-testing the merge logic in isolation.

New files in `maia-showcase/app/src/test/kotlin/`:
- `.../testing/pages/PropsDashboardPage.kt` — filter by name, click Add/Edit/Remove/History per row, assert table contains/lacks a value, assert overridden badge state
- `.../testing/pages/EditPropertyDialogPage.kt`, `RemoveOverrideDialogPage.kt`, `PropertyHistoryDialogPage.kt`
- `.../props/PropsCrudPlaywrightTest.kt` — single `crud journey` test:
  1. Login as admin, navigate to props dashboard
  2. Add an override for a property that already has a real value in `application.yml` (not a synthetic name) — the only way to black-box-verify override-beats-environment precedence and revert-on-remove
  3. Assert the row shows "Overridden" + the new value
  4. Edit the override to a different value, assert updated
  5. Open History, assert both change entries appear
  6. Remove the override, assert the row reverts to the original environment value and "not overridden"
- `AbstractPlaywrightTest.kt` — register the new page objects

## Decisions made during design

- Full Environment enumeration (not just DB overrides, not a curated allow-list) — chosen explicitly over the simpler "overrides + fallback context" option.
- `PropsManager` (not a new `PropsService` class) is extended and relocated to the new `maia-props-service` module.
- One edit dialog reused for add + edit, not two separate dialogs.
- Backend REST layer is in scope for this work, not deferred.
- Merge logic is verified via the Playwright CRUD journey rather than a dedicated unit test, consistent with the "no hand-written unit tests in this feature area" convention already in `maia-job-parent`/`maia-props-parent`.
