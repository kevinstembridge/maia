# Jobs Filter Deep Linking Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the filters on `jobs-dashboard-page` (jobName) and `job-history-page` (jobName, status, from/to date) read from and write to the URL query string, so filtered views are bookmarkable/shareable.

**Architecture:** Add pure, unit-tested parse/serialize functions to the existing `jobs-filtering.ts` / `job-history-filtering.ts` files. Each page component reads `route.snapshot.queryParamMap` once in its constructor to seed the store, then uses an Angular `effect()` to push store signal changes back to the URL via `router.navigate([], {relativeTo, queryParams, replaceUrl: true})`.

**Tech Stack:** Angular 21 + `@ngrx/signals`, Angular Router, Angular Material, Luxon, Vitest (`ng test`) — `libs/maia-ui-workspace/projects/maia-jobs`.

**User Verification:** NO — no user verification required.

**Design doc:** `docs/superpowers/specs/2026-09-21-jobs-filter-deep-linking-design.md`

---

## Task 1: Deep link `jobs-dashboard-page`'s `nameFilter`

**Goal:** `jobs-dashboard-page` reads `?jobName=` from the URL on load and keeps the URL in sync as the user types in the filter box.

**Files:**
- Modify: `projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.ts`
- Modify: `projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.spec.ts`
- Modify: `projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.ts`

**Acceptance Criteria:**
- [ ] `parseNameFilterFromParams` and `buildNameFilterQueryParams` exist in `jobs-filtering.ts`, unit-tested
- [ ] Loading `/jobs-dashboard?jobName=nightly` seeds `store.nameFilter()` with `'nightly'`
- [ ] Typing in the filter box updates the URL's `jobName` query param; clearing it removes the param
- [ ] `npx ng build maia-jobs` (from `libs/maia-ui-workspace`) compiles cleanly

**Verify:** `npx ng test maia-jobs --watch=false` (from `libs/maia-ui-workspace`) → all tests pass, including the new ones

**Steps:**

- [ ] **Step 1: Write the failing tests**

Add to `projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.spec.ts`. Update the top import line to pull in the two new functions plus `convertToParamMap` from `@angular/router`:

```ts
import {describe, expect, it} from 'vitest';
import {convertToParamMap} from '@angular/router';
import {
    buildJobCountSummary,
    buildNameFilterQueryParams,
    countByStatus,
    deriveJobStatus,
    filterAndSortByName,
    formatElapsed,
    parseNameFilterFromParams
} from './jobs-filtering';
```

Add these two new `describe` blocks at the end of the file, just before the final closing `});` of the outer `describe('jobs-filtering', ...)` block:

```ts
    describe('parseNameFilterFromParams()', () => {

        it('returns the jobName param value when present', () => {
            expect(parseNameFilterFromParams(convertToParamMap({jobName: 'nightly'}))).toEqual('nightly');
        });

        it('returns an empty string when the jobName param is absent', () => {
            expect(parseNameFilterFromParams(convertToParamMap({}))).toEqual('');
        });

    });


    describe('buildNameFilterQueryParams()', () => {

        it('includes the jobName param when the filter is non-empty', () => {
            expect(buildNameFilterQueryParams('nightly')).toEqual({jobName: 'nightly'});
        });

        it('sets the jobName param to null (omitting it from the URL) when the filter is empty', () => {
            expect(buildNameFilterQueryParams('')).toEqual({jobName: null});
        });

    });
```

- [ ] **Step 2: Run tests to verify they fail**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-jobs --watch=false`
Expected: FAIL to compile — `jobs-filtering.ts` has no exported member `parseNameFilterFromParams` (and `buildNameFilterQueryParams`).

- [ ] **Step 3: Implement the pure functions**

Add to the top of `projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.ts`, changing the first import line and adding a second:

```ts
import {ParamMap, Params} from '@angular/router';
import {JobState} from '../models/JobState';
```

Append these two functions at the end of the file (after `formatElapsed`):

```ts
export function parseNameFilterFromParams(params: ParamMap): string {
    return params.get('jobName') ?? '';
}


export function buildNameFilterQueryParams(nameFilter: string): Params {
    return {jobName: nameFilter.length > 0 ? nameFilter : null};
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-jobs --watch=false`
Expected: PASS — all tests in `jobs-filtering.spec.ts`, including the 4 new ones.

- [ ] **Step 5: Wire the component to the URL**

In `projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.ts`, change the imports (lines 1 and 7) from:

```ts
import {Component, inject, OnInit} from '@angular/core';
```
```ts
import {RouterLink} from '@angular/router';
```

to:

```ts
import {Component, effect, inject, OnInit} from '@angular/core';
```
```ts
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
```

Add an import for the new pure functions, alongside the existing `JobsDashboardStore` import:

```ts
import {JobsDashboardStore} from './state/jobs-dashboard-store';
import {buildNameFilterQueryParams, parseNameFilterFromParams} from './state/jobs-filtering';
```

Replace the existing constructor (and the `readonly store = inject(JobsDashboardStore);` field just above it) with:

```ts
    readonly store = inject(JobsDashboardStore);

    private route = inject(ActivatedRoute);
    private router = inject(Router);


    constructor(
        private jobsService: JobsApiService,
        private dialog: MatDialog
    ) {

        this.store.onNameFilterChanged(parseNameFilterFromParams(this.route.snapshot.queryParamMap));

        effect(() => {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: buildNameFilterQueryParams(this.store.nameFilter()),
                replaceUrl: true,
            });
        });

    }
```

- [ ] **Step 6: Verify the library builds**

Run (from `libs/maia-ui-workspace/`): `npx ng build maia-jobs`
Expected: build succeeds with no TypeScript errors.

- [ ] **Step 7: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.spec.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.ts
git commit -m "Deep link jobs-dashboard-page's jobName filter"
```

---

## Task 2: Deep link `job-history-page`'s filters

**Goal:** `job-history-page` reads `?jobName=&status=&from=&to=` from the URL on load (seeding the store with exactly one resulting search, not one per field) and keeps the URL in sync as filters change.

**Files:**
- Modify: `projects/maia-jobs/src/lib/job-history/state/job-history-filtering.ts`
- Modify: `projects/maia-jobs/src/lib/job-history/state/job-history-filtering.spec.ts`
- Modify: `projects/maia-jobs/src/lib/job-history/state/job-history-store.ts`
- Modify: `projects/maia-jobs/src/lib/job-history/job-history-page.component.ts`

**Acceptance Criteria:**
- [ ] `toStartOfDayIso`/`toEndOfDayIso` moved from the component into `job-history-filtering.ts` (exported, unit-tested)
- [ ] `parseHistoryFiltersFromParams`/`buildHistoryFilterQueryParams` exist in `job-history-filtering.ts`, unit-tested, and round-trip correctly
- [ ] `JobHistoryStore.applyInitialFilters` patches filter state without triggering a `search()`
- [ ] Loading `/jobs-history?jobName=nightly-job&status=FAILED&from=2026-09-01&to=2026-09-10` seeds all four filters and triggers exactly one search
- [ ] Changing any filter control updates the corresponding URL query param(s); clearing a filter removes its param
- [ ] `npx ng build maia-jobs` (from `libs/maia-ui-workspace`) compiles cleanly

**Verify:** `npx ng test maia-jobs --watch=false` (from `libs/maia-ui-workspace`) → all tests pass, including the new ones

**Steps:**

- [ ] **Step 1: Write the failing tests**

Replace the full contents of `projects/maia-jobs/src/lib/job-history/state/job-history-filtering.spec.ts` with:

```ts
import {describe, expect, it} from 'vitest';
import {convertToParamMap} from '@angular/router';
import {DateTime} from 'luxon';
import {
    buildHistoryFilterQueryParams,
    deriveHistoryStatus,
    formatDuration,
    parseHistoryFiltersFromParams,
    toEndOfDayIso,
    toStartOfDayIso
} from './job-history-filtering';
import {JobExecutionHistoryItem} from '../../jobs-dashboard/models/JobExecutionHistoryItem';

function makeItem(overrides: Partial<JobExecutionHistoryItem>): JobExecutionHistoryItem {
    return Object.assign(new JobExecutionHistoryItem(), {
        jobExecutionId: 'exec-1',
        jobName: 'some-job',
        invokedBy: 'someone',
        startTimestamp: '2026-01-01T00:00:00Z',
        endTimestamp: '2026-01-01T00:00:10Z',
        status: 'SUCCESS',
        errorMessage: null,
        metrics: {},
    }, overrides);
}

describe('deriveHistoryStatus', () => {

    it('returns running when status is RUNNING', () => {
        expect(deriveHistoryStatus(makeItem({status: 'RUNNING'}))).toBe('running');
    });

    it('returns success when status is SUCCESS', () => {
        expect(deriveHistoryStatus(makeItem({status: 'SUCCESS'}))).toBe('success');
    });

    it('returns failed when status is FAILED', () => {
        expect(deriveHistoryStatus(makeItem({status: 'FAILED'}))).toBe('failed');
    });

});

describe('formatDuration', () => {

    it('returns Running… when endTimestamp is null', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', null)).toBe('Running…');
    });

    it('formats sub-minute durations as seconds', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', '2026-01-01T00:00:42Z')).toBe('42s');
    });

    it('formats sub-hour durations as minutes and seconds', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', '2026-01-01T00:04:12Z')).toBe('4m 12s');
    });

    it('formats hour-plus durations as hours and zero-padded minutes', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', '2026-01-01T01:03:00Z')).toBe('1h 03m');
    });

});

describe('toStartOfDayIso()', () => {

    it('returns null when date is null', () => {
        expect(toStartOfDayIso(null)).toBeNull();
    });

    it('returns the UTC start-of-day ISO string for the given date', () => {
        const date = DateTime.fromISO('2026-09-10T15:30:00', {zone: 'utc'});
        expect(toStartOfDayIso(date)).toEqual('2026-09-10T00:00:00.000Z');
    });

});

describe('toEndOfDayIso()', () => {

    it('returns null when date is null', () => {
        expect(toEndOfDayIso(null)).toBeNull();
    });

    it('returns the UTC end-of-day ISO string for the given date', () => {
        const date = DateTime.fromISO('2026-09-10T15:30:00', {zone: 'utc'});
        expect(toEndOfDayIso(date)).toEqual('2026-09-10T23:59:59.999Z');
    });

});

describe('parseHistoryFiltersFromParams()', () => {

    it('parses all four filters when all params are present', () => {
        const params = convertToParamMap({jobName: 'nightly-job', status: 'FAILED', from: '2026-09-01', to: '2026-09-10'});
        expect(parseHistoryFiltersFromParams(params)).toEqual({
            jobNameFilter: 'nightly-job',
            statusFilter: 'FAILED',
            fromDate: '2026-09-01T00:00:00.000Z',
            toDate: '2026-09-10T23:59:59.999Z',
        });
    });

    it('returns all-null filters when no params are present', () => {
        expect(parseHistoryFiltersFromParams(convertToParamMap({}))).toEqual({
            jobNameFilter: null,
            statusFilter: null,
            fromDate: null,
            toDate: null,
        });
    });

    it('falls back to a null statusFilter for an invalid status value', () => {
        const params = convertToParamMap({status: 'BOGUS'});
        expect(parseHistoryFiltersFromParams(params).statusFilter).toBeNull();
    });

});

describe('buildHistoryFilterQueryParams()', () => {

    it('round-trips a fully-populated filter set back to its URL param form', () => {
        const params = convertToParamMap({jobName: 'nightly-job', status: 'FAILED', from: '2026-09-01', to: '2026-09-10'});
        const filters = parseHistoryFiltersFromParams(params);
        expect(buildHistoryFilterQueryParams(filters)).toEqual({
            jobName: 'nightly-job',
            status: 'FAILED',
            from: '2026-09-01',
            to: '2026-09-10',
        });
    });

    it('sets every param to null when all filters are null', () => {
        expect(buildHistoryFilterQueryParams({jobNameFilter: null, statusFilter: null, fromDate: null, toDate: null}))
            .toEqual({jobName: null, status: null, from: null, to: null});
    });

});
```

- [ ] **Step 2: Run tests to verify they fail**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-jobs --watch=false`
Expected: FAIL to compile — `job-history-filtering.ts` has no exported members `toStartOfDayIso`, `toEndOfDayIso`, `parseHistoryFiltersFromParams`, `buildHistoryFilterQueryParams`.

- [ ] **Step 3: Implement the pure functions**

Replace the full contents of `projects/maia-jobs/src/lib/job-history/state/job-history-filtering.ts` with:

```ts
import {DateTime} from 'luxon';
import {ParamMap, Params} from '@angular/router';
import {JobExecutionHistoryItem} from '../../jobs-dashboard/models/JobExecutionHistoryItem';

export type HistoryStatus = 'running' | 'success' | 'failed';

export type HistoryStatusFilter = 'RUNNING' | 'SUCCESS' | 'FAILED' | null;

export interface HistoryFilters {
    jobNameFilter: string | null;
    statusFilter: HistoryStatusFilter;
    fromDate: string | null;
    toDate: string | null;
}

const STATUS_MAP: Record<JobExecutionHistoryItem['status'], HistoryStatus> = {
    RUNNING: 'running',
    SUCCESS: 'success',
    FAILED: 'failed',
};

const VALID_STATUS_FILTERS: string[] = ['RUNNING', 'SUCCESS', 'FAILED'];


export function deriveHistoryStatus(item: JobExecutionHistoryItem): HistoryStatus {
    return STATUS_MAP[item.status];
}


export function formatDuration(startTimestamp: string, endTimestamp: string | null): string {

    if (endTimestamp === null) {
        return 'Running…';
    }

    const elapsedMs = Math.max(0, new Date(endTimestamp).getTime() - new Date(startTimestamp).getTime());
    const totalSeconds = Math.floor(elapsedMs / 1000);

    if (totalSeconds < 60) {
        return `${totalSeconds}s`;
    }

    if (totalSeconds < 3600) {
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes}m ${seconds}s`;
    }

    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    return `${hours}h ${String(minutes).padStart(2, '0')}m`;

}


export function toStartOfDayIso(date: DateTime | null): string | null {

    if (!date) {
        return null;
    }

    return date.startOf('day').toUTC().toISO();

}


export function toEndOfDayIso(date: DateTime | null): string | null {

    if (!date) {
        return null;
    }

    return date.endOf('day').toUTC().toISO();

}


export function parseHistoryFiltersFromParams(params: ParamMap): HistoryFilters {

    const rawStatus = params.get('status');
    const statusFilter = rawStatus !== null && VALID_STATUS_FILTERS.includes(rawStatus)
        ? rawStatus as HistoryStatusFilter
        : null;

    const from = params.get('from');
    const to = params.get('to');

    return {
        jobNameFilter: params.get('jobName'),
        statusFilter,
        fromDate: toStartOfDayIso(from ? DateTime.fromISO(from, {zone: 'utc'}) : null),
        toDate: toEndOfDayIso(to ? DateTime.fromISO(to, {zone: 'utc'}) : null),
    };

}


export function buildHistoryFilterQueryParams(filters: HistoryFilters): Params {
    return {
        jobName: filters.jobNameFilter,
        status: filters.statusFilter,
        from: filters.fromDate ? DateTime.fromISO(filters.fromDate, {zone: 'utc'}).toISODate() : null,
        to: filters.toDate ? DateTime.fromISO(filters.toDate, {zone: 'utc'}).toISODate() : null,
    };
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-jobs --watch=false`
Expected: PASS — all tests in `job-history-filtering.spec.ts`.

- [ ] **Step 5: Add `applyInitialFilters` to the store**

In `projects/maia-jobs/src/lib/job-history/state/job-history-store.ts`, update the import on line 9 from:

```ts
import {HistoryStatusFilter} from './job-history-filtering';
```

to:

```ts
import {HistoryFilters, HistoryStatusFilter} from './job-history-filtering';
```

Add a new method to the object returned from `withMethods`, right before `init()`:

```ts
            applyInitialFilters(filters: HistoryFilters): void {
                patchState(store, {...filters, pageIndex: 0});
            },

            init(): void {
                loadAvailableJobNames();
                search();
            },
```

- [ ] **Step 6: Wire the component to the URL**

In `projects/maia-jobs/src/lib/job-history/job-history-page.component.ts`, update the imports (lines 1 and 16) from:

```ts
import {Component, inject, OnInit} from '@angular/core';
```
```ts
import {HistoryStatusFilter} from './state/job-history-filtering';
```

to:

```ts
import {Component, effect, inject, OnInit} from '@angular/core';
```
```ts
import {
    buildHistoryFilterQueryParams,
    HistoryStatusFilter,
    parseHistoryFiltersFromParams,
    toEndOfDayIso,
    toStartOfDayIso
} from './state/job-history-filtering';
```

Add an import for `ActivatedRoute`/`Router` — there is no existing `@angular/router` import in this file, so add a new line after the `luxon` import:

```ts
import {DateTime} from 'luxon';
import {ActivatedRoute, Router} from '@angular/router';
```

Replace the class body from `readonly store = inject(JobHistoryStore);` through the closing `ngOnInit` block with:

```ts
    readonly store = inject(JobHistoryStore);

    private dialog = inject(MatDialog);

    private jobsService = inject(JobsApiService);

    private route = inject(ActivatedRoute);

    private router = inject(Router);


    constructor() {

        this.store.applyInitialFilters(parseHistoryFiltersFromParams(this.route.snapshot.queryParamMap));

        effect(() => {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: buildHistoryFilterQueryParams({
                    jobNameFilter: this.store.jobNameFilter(),
                    statusFilter: this.store.statusFilter(),
                    fromDate: this.store.fromDate(),
                    toDate: this.store.toDate(),
                }),
                replaceUrl: true,
            });
        });

    }


    ngOnInit() {
        this.store.init();
    }
```

Replace the existing `onFromDateChanged`/`onToDateChanged` methods with versions that call the (now-imported) pure functions instead of private methods:

```ts
    onFromDateChanged(date: DateTime | null) {
        this.store.onDateRangeChanged(toStartOfDayIso(date), this.store.toDate());
    }


    onToDateChanged(date: DateTime | null) {
        this.store.onDateRangeChanged(this.store.fromDate(), toEndOfDayIso(date));
    }
```

Delete the now-unused private `toStartOfDayIso`/`toEndOfDayIso` methods at the bottom of the class (previously the last two methods before the closing brace).

- [ ] **Step 7: Verify the library builds**

Run (from `libs/maia-ui-workspace/`): `npx ng build maia-jobs`
Expected: build succeeds with no TypeScript errors.

- [ ] **Step 8: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-filtering.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-filtering.spec.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-store.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/job-history-page.component.ts
git commit -m "Deep link job-history-page's filters"
```

---

## Manual verification (after both tasks)

Run the showcase app (see `reference_running_showcase_backend_locally` memory / project README for how to run the frontend + backend locally), then:

1. On `/jobs-dashboard`, type a filter — confirm the URL's `?jobName=` updates live. Reload with `?jobName=<something>` in the URL — confirm the filter box and visible jobs reflect it on load.
2. On `/jobs-history`, set each filter (job name, status, from, to) — confirm the URL updates and only one network request fires per change. Reload with all four params set — confirm all four controls and the result list reflect them, with exactly one initial search request (check the Network tab).
3. Clear each filter on both pages — confirm its query param disappears from the URL.
4. Confirm the browser back button does not step through every keystroke/selection change (only through prior distinct navigations, e.g. arriving at the page vs. leaving it).
