# Elastic Indices & Props Dashboard Filter Deep Linking — Design

## Goal
Apply the same URL query-param deep linking already built for the jobs pages (`docs/superpowers/specs/2026-09-21-jobs-filter-deep-linking-design.md`) to `elastic-indices-page` (`libs/maia-ui-workspace/projects/maia-elasticsearch`) and `props-dashboard-page` (`libs/maia-ui-workspace/projects/maia-props`), so their filtered views are bookmarkable/shareable too.

## Background
- Both pages are simpler than either jobs page: all filtering is purely client-side over already-fetched data (`ElasticIndicesPageStore.fetchAllIndices()` / `PropsDashboardStore.fetchAllProperties()` each run once; every filter is a `computed()` in the store, same shape as `jobs-dashboard`'s `nameFilter`). No filter change ever triggers a new API call, so seeding from the URL is always cheap — there's no `applyInitialFilters`-style "avoid a redundant search" concern here (unlike `job-history-page`).
- `elastic-indices-page` has three filter-affecting fields: `nameFilter` (free-text substring on `indexName`), `statusFilter` (`DisplayStatus | null`, single-select via clickable status tiles), and `hideSystemIndices` (boolean toggle, defaults to `true`).
- `props-dashboard-page` has two: `nameFilter` (free-text substring on `propertyName`) and `overriddenOnly` (boolean toggle, defaults to `false`).
- Both `*-filtering.ts` files already follow the established convention (pure, colocated, unit-tested functions — `elastic-indices-filtering.ts`/`.spec.ts`, `props-dashboard-filtering.ts`/`.spec.ts`) that the parse/build functions will extend.
- **A gap found in the jobs work's final review, applied here from the start:** seeding a store field from the URL only affects filtering logic — it does nothing for the visible form control unless the control has a `[value]`/`[checked]` binding to that same store signal. Auditing both templates now, before writing the plan:
  - `elastic-indices-page.html:19` — the name filter `<input matInput>` has no `[value]` binding (bug, same shape as the one found and fixed on `jobs-dashboard-page`).
  - `elastic-indices-page.html:21-25` — the `hideSystemIndices` toggle already has `[checked]="store.hideSystemIndices()"` — no fix needed.
  - `elastic-indices-page.html:1-14` — the status tiles already read `store.statusFilter()` directly for their `active`/`aria-pressed` state — no fix needed.
  - `props-dashboard-page.html:4` — the name filter `<input matInput>` has no `[value]` binding (same bug).
  - `props-dashboard-page.html:6` — the `overriddenOnly` `<mat-slide-toggle>` has **no `[checked]` binding at all**, even independent of deep linking — it's currently a write-only control that never reflects `store.overriddenOnly()`. This is a pre-existing latent bug that deep linking would otherwise silently seed state into without ever showing it.
- This design folds those template bindings into each task's initial implementation rather than as a follow-up.

## Behavior

### Param shape & sync mechanism (identical mechanism to the jobs work)
- `elastic-indices` route (`?indexName=&status=&hideSystemIndices=`): `indexName` (substring, omitted when empty), `status` (`green|yellow|red|not-created`, omitted when `null`), `hideSystemIndices` (`true|false`, omitted when equal to its default `true` — so the URL only grows when the user deviates from the default, keeping the common case clean).
- `props-dashboard` route (`?propertyName=&overriddenOnly=`): `propertyName` (substring, omitted when empty), `overriddenOnly` (`true|false`, omitted when equal to its default `false`).
- Same bidirectional sync as before: each page component's constructor reads `route.snapshot.queryParamMap` once to seed the store (via each field's existing `onXChanged` setter — cheap, since none of these trigger an API call), then an `effect()` pushes signal changes to the URL via `router.navigate([], {relativeTo: route, queryParams, replaceUrl: true})`.

### `elastic-indices-page.ts` / `elastic-indices-filtering.ts`
- New pure functions in `elastic-indices-filtering.ts`: `parseElasticIndicesFiltersFromParams(params: ParamMap): ElasticIndicesFilters` (`{nameFilter: string, statusFilter: DisplayStatus | null, hideSystemIndices: boolean}`) and `buildElasticIndicesQueryParams(filters: ElasticIndicesFilters): Params`.
- Component constructor seeds via the existing `onNameFilterChanged`/`onStatusFilterToggled`-equivalent path — since `onStatusFilterToggled` *toggles* (not sets), seeding needs a direct `patchState`-based setter instead; add a small `ElasticIndicesPageStore.applyInitialFilters(filters: ElasticIndicesFilters)` method (patches all three fields directly, analogous in spirit to `JobHistoryStore.applyInitialFilters` but here purely for correctness of "set the toggle's initial value," not to avoid a redundant search — there's no search to avoid, so this is just the cleanest way to seed three fields with a documented single call).
- Template fix: add `[value]="store.nameFilter()"` to the name filter input (`elastic-indices-page.html:19`).

### `props-dashboard-page.ts` / `props-dashboard-filtering.ts`
- New pure functions in `props-dashboard-filtering.ts`: `parsePropsFiltersFromParams(params: ParamMap): PropsFilters` (`{nameFilter: string, overriddenOnly: boolean}`) and `buildPropsQueryParams(filters: PropsFilters): Params`.
- Component constructor seeds via the existing `onNameFilterChanged`/`onOverriddenOnlyToggled` setters directly (both are plain setters, not toggles, so no new store method is needed here — unlike elastic-indices' `onStatusFilterToggled`).
- Template fixes: add `[value]="store.nameFilter()"` to the name filter input (`props-dashboard-page.html:4`) and `[checked]="store.overriddenOnly()"` to the toggle (`props-dashboard-page.html:6`) — the latter fixes the pre-existing bug noted above, not just the new deep-linking gap.

## Out of scope
- No changes to either page's data-fetch triggering (both fetch once on init regardless of filters, unchanged).
- No debouncing of URL writes (same rationale as the jobs work — these are local `computed()` filters, not API calls; `replaceUrl: true` keeps history clean regardless of write frequency).
- No component-level (TestBed) tests — neither page has existing component tests; only the new pure functions get unit tests.

## Verification
- `elastic-indices-filtering.spec.ts`: `parseElasticIndicesFiltersFromParams`/`buildElasticIndicesQueryParams` — all-present params, all-absent params (verifying `hideSystemIndices` defaults to `true` when absent), invalid `status` value falls back to `null`, round-trip.
- `props-dashboard-filtering.spec.ts`: `parsePropsFiltersFromParams`/`buildPropsQueryParams` — all-present params, all-absent params (verifying `overriddenOnly` defaults to `false` when absent), round-trip.
- Manual: in the showcase app, set each filter/toggle on both pages, confirm the URL updates and the boolean params are omitted only at their default value; reload with params set, confirm both the visible controls (inputs, toggles, status tiles) *and* the filtered result set reflect the URL on load — this was the exact check that was skipped (`User Verification: NO`) and then caught in final review on the jobs work, so it's called out explicitly here.

## Unresolved questions
None.
