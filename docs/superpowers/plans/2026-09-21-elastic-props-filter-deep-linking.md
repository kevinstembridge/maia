# Elastic Indices & Props Dashboard Filter Deep Linking Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply the same URL query-param deep linking already built for the jobs pages to `elastic-indices-page` and `props-dashboard-page`, so their filtered views are bookmarkable/shareable.

**Architecture:** Identical mechanism to the jobs work: pure, unit-tested parse/serialize functions added to each page's existing `*-filtering.ts` file; each page component's constructor seeds its store once from `route.snapshot.queryParamMap`, then an Angular `effect()` pushes store signal changes back to the URL via `router.navigate([], {relativeTo, queryParams, replaceUrl: true})`. Unlike `job-history-page`, neither page here triggers an API call on filter change (both fetch once on init, filter purely client-side), so seeding is always cheap.

**Tech Stack:** Angular 21 + `@ngrx/signals`, Angular Router, Angular Material, Vitest (`ng test`) — `libs/maia-ui-workspace/projects/maia-elasticsearch` and `libs/maia-ui-workspace/projects/maia-props`.

**User Verification:** NO — no user verification required.

**Design doc:** `docs/superpowers/specs/2026-09-21-elastic-props-filter-deep-linking-design.md`

---

## Task 1: Deep link `elastic-indices-page`'s filters

**Goal:** `elastic-indices-page` reads `?indexName=&status=&hideSystemIndices=` from the URL on load and keeps the URL in sync as any of the three filters change, with all three visible controls (name input, status tiles, toggle) reflecting the seeded value.

**Files:**
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.spec.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-page-store.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.html`

**Acceptance Criteria:**
- `parseElasticIndicesFiltersFromParams`/`buildElasticIndicesQueryParams` exist in `elastic-indices-filtering.ts`, unit-tested; `hideSystemIndices` defaults to `true` and is omitted from the URL only when `true`
- `ElasticIndicesPageStore.applyInitialFilters` seeds all three filter fields in one call
- Loading `/elastic-indices?indexName=audit&status=red&hideSystemIndices=false` seeds all three filters
- The name filter input shows the seeded value on load (not just an internally-filtered-but-visually-empty box)
- Changing/clearing any filter updates/removes its URL query param
- `npx ng build maia-elasticsearch` (from `libs/maia-ui-workspace`) compiles cleanly

**Verify:** `npx ng test maia-elasticsearch --watch=false` (from `libs/maia-ui-workspace`) → all tests pass, including the new ones

**Steps:**

- [ ] **Step 1: Write the failing tests**

Add to `projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.spec.ts`. Update the top import line to pull in `convertToParamMap` and the two new functions:

```ts
import {describe, expect, it} from 'vitest';
import {convertToParamMap} from '@angular/router';
import {
    buildElasticIndicesQueryParams,
    countByDisplayStatus,
    deriveDisplayStatus,
    filterAndSortByName,
    filterBySystemIndices,
    filterByStatus,
    parseElasticIndicesFiltersFromParams
} from './elastic-indices-filtering';
import {EsIndexStateDto} from '../models/EsIndexStateDto';
```

Add these two new `describe` blocks at the end of the file, just before the final closing `});` of the outer `describe('elastic-indices-filtering', ...)` block:

```ts
    describe('parseElasticIndicesFiltersFromParams()', () => {

        it('parses all three filters when all params are present', () => {
            const params = convertToParamMap({indexName: 'audit', status: 'red', hideSystemIndices: 'false'});
            expect(parseElasticIndicesFiltersFromParams(params)).toEqual({
                nameFilter: 'audit',
                statusFilter: 'red',
                hideSystemIndices: false,
            });
        });

        it('defaults hideSystemIndices to true and the rest to empty/null when no params are present', () => {
            expect(parseElasticIndicesFiltersFromParams(convertToParamMap({}))).toEqual({
                nameFilter: '',
                statusFilter: null,
                hideSystemIndices: true,
            });
        });

        it('falls back to a null statusFilter for an invalid status value', () => {
            const params = convertToParamMap({status: 'purple'});
            expect(parseElasticIndicesFiltersFromParams(params).statusFilter).toBeNull();
        });

    });


    describe('buildElasticIndicesQueryParams()', () => {

        it('includes all params when they differ from their defaults', () => {
            expect(buildElasticIndicesQueryParams({nameFilter: 'audit', statusFilter: 'red', hideSystemIndices: false}))
                .toEqual({indexName: 'audit', status: 'red', hideSystemIndices: 'false'});
        });

        it('omits indexName, status, and hideSystemIndices when all filters are at their defaults', () => {
            expect(buildElasticIndicesQueryParams({nameFilter: '', statusFilter: null, hideSystemIndices: true}))
                .toEqual({indexName: null, status: null, hideSystemIndices: null});
        });

    });
```

- [ ] **Step 2: Run tests to verify they fail**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-elasticsearch --watch=false`
Expected: FAIL to compile — `elastic-indices-filtering.ts` has no exported member `parseElasticIndicesFiltersFromParams` (and `buildElasticIndicesQueryParams`).

- [ ] **Step 3: Implement the pure functions**

In `projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.ts`, change the top import line from:

```ts
import {EsIndexStateDto} from '../models/EsIndexStateDto';
```

to:

```ts
import {ParamMap, Params} from '@angular/router';
import {EsIndexStateDto} from '../models/EsIndexStateDto';
```

Add this interface and constant right after `STATUS_COLORS` (after line 12, before `filterBySystemIndices`):

```ts
export interface ElasticIndicesFilters {
    nameFilter: string;
    statusFilter: DisplayStatus | null;
    hideSystemIndices: boolean;
}

const VALID_STATUS_FILTERS: string[] = ['green', 'yellow', 'red', 'not-created'];
```

Append these two functions at the end of the file (after `filterByStatus`):

```ts
export function parseElasticIndicesFiltersFromParams(params: ParamMap): ElasticIndicesFilters {

    const rawStatus = params.get('status');
    const statusFilter = rawStatus !== null && VALID_STATUS_FILTERS.includes(rawStatus)
        ? rawStatus as DisplayStatus
        : null;

    const rawHideSystemIndices = params.get('hideSystemIndices');
    const hideSystemIndices = rawHideSystemIndices === null ? true : rawHideSystemIndices === 'true';

    return {
        nameFilter: params.get('indexName') ?? '',
        statusFilter,
        hideSystemIndices,
    };

}


export function buildElasticIndicesQueryParams(filters: ElasticIndicesFilters): Params {
    return {
        indexName: filters.nameFilter.length > 0 ? filters.nameFilter : null,
        status: filters.statusFilter,
        hideSystemIndices: filters.hideSystemIndices === true ? null : 'false',
    };
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-elasticsearch --watch=false`
Expected: PASS — all tests in `elastic-indices-filtering.spec.ts`, including the 5 new ones.

- [ ] **Step 5: Add `applyInitialFilters` to the store**

In `projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-page-store.ts`, update the import on line 10 from:

```ts
import {countByDisplayStatus, DisplayStatus, filterAndSortByName, filterBySystemIndices, filterByStatus} from './elastic-indices-filtering';
```

to:

```ts
import {
    countByDisplayStatus,
    DisplayStatus,
    ElasticIndicesFilters,
    filterAndSortByName,
    filterBySystemIndices,
    filterByStatus
} from './elastic-indices-filtering';
```

Add a new method as the first entry in the object returned from `withMethods` (right after `withMethods((store, pageService = inject(ElasticIndicesApiService)) => ({`):

```ts
        applyInitialFilters(filters: ElasticIndicesFilters): void {
            patchState(store, filters);
        },

        fetchAllIndices: rxMethod<void>(
```

- [ ] **Step 6: Wire the component to the URL and fix the missing name-filter binding**

In `projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.ts`, change the imports (lines 1 and 14) from:

```ts
import {Component, inject, OnInit} from '@angular/core';
```
```ts
import {DisplayStatus, STATUS_COLORS, STATUS_TILE_ORDER} from './state/elastic-indices-filtering';
```

to:

```ts
import {Component, effect, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
```
```ts
import {
    buildElasticIndicesQueryParams,
    DisplayStatus,
    parseElasticIndicesFiltersFromParams,
    STATUS_COLORS,
    STATUS_TILE_ORDER
} from './state/elastic-indices-filtering';
```

Replace the class body from `readonly store = inject(ElasticIndicesPageStore);` through the end of the constructor with:

```ts
    readonly store = inject(ElasticIndicesPageStore);

    readonly statusTileOrder = STATUS_TILE_ORDER;
    readonly statusColors = STATUS_COLORS;
    readonly statusLabels = STATUS_LABELS;

    private route = inject(ActivatedRoute);
    private router = inject(Router);


    constructor(
        private elasticIndicesService: ElasticIndicesApiService,
        private dialog: MatDialog
    ) {

        this.store.applyInitialFilters(parseElasticIndicesFiltersFromParams(this.route.snapshot.queryParamMap));

        effect(() => {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: buildElasticIndicesQueryParams({
                    nameFilter: this.store.nameFilter(),
                    statusFilter: this.store.statusFilter(),
                    hideSystemIndices: this.store.hideSystemIndices(),
                }),
                replaceUrl: true,
            });
        });

    }
```

In `projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.html`, change line 19 from:

```html
        <input matInput placeholder="Filter indices..." (input)="onFilterInput($event)">
```

to:

```html
        <input matInput placeholder="Filter indices..." [value]="store.nameFilter()" (input)="onFilterInput($event)">
```

(The `hideSystemIndices` toggle already has `[checked]="store.hideSystemIndices()"`, and the status tiles already read `store.statusFilter()` directly — neither needs a binding fix.)

- [ ] **Step 7: Verify the library builds**

Run (from `libs/maia-ui-workspace/`): `npx ng build maia-elasticsearch`
Expected: build succeeds with no TypeScript errors.

- [ ] **Step 8: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.ts \
        libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.spec.ts \
        libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-page-store.ts \
        libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.ts \
        libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.html
git commit -m "Deep link elastic-indices-page's filters"
```

---

## Task 2: Deep link `props-dashboard-page`'s filters

**Goal:** `props-dashboard-page` reads `?propertyName=&overriddenOnly=` from the URL on load and keeps the URL in sync as either filter changes, with both visible controls (name input, toggle) reflecting the seeded value — including fixing the toggle's pre-existing missing `[checked]` binding.

**Files:**
- Modify: `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html`

**Acceptance Criteria:**
- `parsePropsFiltersFromParams`/`buildPropsQueryParams` exist in `props-dashboard-filtering.ts`, unit-tested; `overriddenOnly` defaults to `false` and is omitted from the URL only when `false`
- Loading `/props-dashboard?propertyName=server.port&overriddenOnly=true` seeds both filters
- The name filter input AND the "Overridden only" toggle both show the seeded value on load (the toggle currently has no `[checked]` binding at all — this task fixes that as part of wiring it to the store)
- Changing/clearing either filter updates/removes its URL query param
- `npx ng build maia-props` (from `libs/maia-ui-workspace`) compiles cleanly

**Verify:** `npx ng test maia-props --watch=false` (from `libs/maia-ui-workspace`) → all tests pass, including the new ones

**Steps:**

- [ ] **Step 1: Write the failing tests**

Add to `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts`. Update the top import line from:

```ts
import {countOverridden, filterProperties} from './props-dashboard-filtering';
```

to:

```ts
import {convertToParamMap} from '@angular/router';
import {buildPropsQueryParams, countOverridden, filterProperties, parsePropsFiltersFromParams} from './props-dashboard-filtering';
```

Add these two new top-level `describe` blocks at the end of the file (matching this file's existing style of top-level `describe`s, not nested inside a wrapping one):

```ts
describe('parsePropsFiltersFromParams', () => {

    it('parses both filters when both params are present', () => {
        const params = convertToParamMap({propertyName: 'server.port', overriddenOnly: 'true'});
        expect(parsePropsFiltersFromParams(params)).toEqual({nameFilter: 'server.port', overriddenOnly: true});
    });

    it('defaults overriddenOnly to false and nameFilter to empty string when no params are present', () => {
        expect(parsePropsFiltersFromParams(convertToParamMap({}))).toEqual({nameFilter: '', overriddenOnly: false});
    });

});

describe('buildPropsQueryParams', () => {

    it('includes both params when they differ from their defaults', () => {
        expect(buildPropsQueryParams({nameFilter: 'server.port', overriddenOnly: true}))
            .toEqual({propertyName: 'server.port', overriddenOnly: 'true'});
    });

    it('omits both params when filters are at their defaults', () => {
        expect(buildPropsQueryParams({nameFilter: '', overriddenOnly: false}))
            .toEqual({propertyName: null, overriddenOnly: null});
    });

});
```

- [ ] **Step 2: Run tests to verify they fail**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-props --watch=false`
Expected: FAIL to compile — `props-dashboard-filtering.ts` has no exported member `parsePropsFiltersFromParams` (and `buildPropsQueryParams`).

- [ ] **Step 3: Implement the pure functions**

In `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts`, change the top import line from:

```ts
import {PropertyResponseDto} from '../models/PropertyResponseDto';
```

to:

```ts
import {ParamMap, Params} from '@angular/router';
import {PropertyResponseDto} from '../models/PropertyResponseDto';


export interface PropsFilters {
    nameFilter: string;
    overriddenOnly: boolean;
}
```

Append these two functions at the end of the file (after `countOverridden`):

```ts
export function parsePropsFiltersFromParams(params: ParamMap): PropsFilters {

    const rawOverriddenOnly = params.get('overriddenOnly');

    return {
        nameFilter: params.get('propertyName') ?? '',
        overriddenOnly: rawOverriddenOnly === null ? false : rawOverriddenOnly === 'true',
    };

}


export function buildPropsQueryParams(filters: PropsFilters): Params {
    return {
        propertyName: filters.nameFilter.length > 0 ? filters.nameFilter : null,
        overriddenOnly: filters.overriddenOnly === false ? null : 'true',
    };
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-props --watch=false`
Expected: PASS — all tests in `props-dashboard-filtering.spec.ts`, including the 4 new ones.

- [ ] **Step 5: Wire the component to the URL and fix the missing bindings**

In `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts`, change the imports (line 1, and the `PropsDashboardStore` import) from:

```ts
import {Component, inject, OnInit} from '@angular/core';
```
```ts
import {PropsDashboardStore} from './state/props-dashboard-store';
```

to:

```ts
import {Component, effect, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
```
```ts
import {PropsDashboardStore} from './state/props-dashboard-store';
import {buildPropsQueryParams, parsePropsFiltersFromParams} from './state/props-dashboard-filtering';
```

Replace the class body from `readonly store = inject(PropsDashboardStore);` through the end of the constructor with:

```ts
    readonly store = inject(PropsDashboardStore);

    readonly displayedColumns = ['propertyName', 'effectiveValue', 'isOverridden', 'sourceName', 'lastModifiedByUsername', 'lastModifiedTimestamp', 'actions'];

    private route = inject(ActivatedRoute);
    private router = inject(Router);


    constructor(
        private propsService: PropsApiService,
        private dialog: MatDialog
    ) {

        const initialFilters = parsePropsFiltersFromParams(this.route.snapshot.queryParamMap);
        this.store.onNameFilterChanged(initialFilters.nameFilter);
        this.store.onOverriddenOnlyToggled(initialFilters.overriddenOnly);

        effect(() => {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: buildPropsQueryParams({
                    nameFilter: this.store.nameFilter(),
                    overriddenOnly: this.store.overriddenOnly(),
                }),
                replaceUrl: true,
            });
        });

    }
```

In `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html`, change lines 4 and 6 from:

```html
        <input matInput (input)="onNameFilterInput($event)">
```
```html
    <mat-slide-toggle (change)="onOverriddenOnlyToggled($event)">Overridden only</mat-slide-toggle>
```

to:

```html
        <input matInput [value]="store.nameFilter()" (input)="onNameFilterInput($event)">
```
```html
    <mat-slide-toggle [checked]="store.overriddenOnly()" (change)="onOverriddenOnlyToggled($event)">Overridden only</mat-slide-toggle>
```

- [ ] **Step 6: Verify the library builds**

Run (from `libs/maia-ui-workspace/`): `npx ng build maia-props`
Expected: build succeeds with no TypeScript errors.

- [ ] **Step 7: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts \
        libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts \
        libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts \
        libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html
git commit -m "Deep link props-dashboard-page's filters"
```

---

## Manual verification (after both tasks)

Run the showcase app (see `reference_running_showcase_backend_locally` memory / project README for how to run the frontend + backend locally), then, for **each** page:

1. Set every filter/toggle — confirm the URL updates, and that a boolean param disappears from the URL when toggled back to its default (`hideSystemIndices` back to `true`, `overriddenOnly` back to `false`).
2. Reload the page with all params set in the URL — confirm **both** the visible controls (name input, status tiles/toggle) **and** the filtered result set reflect the URL on load. This is the specific check that was skipped and then caught in final review on the prior jobs-filter-deep-linking work — don't skip it here.
3. Clear each filter — confirm its query param disappears from the URL.
