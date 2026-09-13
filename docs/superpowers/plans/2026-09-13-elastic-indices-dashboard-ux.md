# Elastic Indices Dashboard UX Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the Elasticsearch indices dashboard in `libs/maia-ui-workspace/projects/maia-elasticsearch` with a card grid, a name filter, a colored health-status indicator, a count summary, and loading/empty/error states — no backend or DTO changes.

**Architecture:** Extract the new filtering/sorting/counting logic into a small pure-function module (`elastic-indices-filtering.ts`) that the NgRx signal store's computed signals delegate to. This keeps the non-trivial logic unit-testable with plain Vitest (matching this workspace's existing convention of pure-function-only specs — `date-time-functions.spec.ts` is the only precedent, and nothing in this workspace uses Angular TestBed) without requiring Angular DI/TestBed to test the store itself, since the store's `withMethods` factory injects `ElasticIndicesApiService` (which needs `HttpClient` + a base-URL token) at construction time. The store, card component, and page template are then updated to use this module and to add the new UI (filter input, spinner, error banner, grid, status dot, active-version badge).

**Tech Stack:** Angular 21 (standalone components, signals, new control-flow syntax), `@ngrx/signals` (signalStore/withState/withComputed/withMethods), Angular Material, Tailwind utility classes, Vitest (via `@angular/build:unit-test`).

**User Verification:** NO — no human sign-off checkpoints were requested for this implementation; verification is via automated tests and the manual browser check in the final task, run by whoever executes this plan.

---

## Task 1: Pure filtering/sorting/count-summary logic

**Goal:** Extract and unit-test the filtering, sorting, status-grouping, and count-summary-formatting logic as plain functions, independent of Angular/NgRx.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.ts`
- Test: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.spec.ts`

**Acceptance Criteria:**
- [ ] `filterBySystemIndices` drops indices whose `indexName` starts with `.` when `hideSystemIndices` is true, and returns everything unchanged when false.
- [ ] `filterAndSortByName` case-insensitively substring-matches `indexName` against a filter string, and returns the result sorted alphabetically by `indexName`.
- [ ] `countByStatus` groups by `health.status` lowercased, skipping indices with no health status.
- [ ] `buildIndexCountSummary` produces `"<total> indices"` (or `"1 index"` for a total of 1) when `visibleCount === totalCount`, or `"<visible> of <total> indices"` otherwise, followed by `" · <count> <status>"` segments ordered `green`, `yellow`, `red`, then any other statuses alphabetically, omitting zero-count segments and the separator entirely when there are no non-zero segments.

**Verify:** `npx ng test maia-elasticsearch` (run from `libs/maia-ui-workspace/`) → all tests in `elastic-indices-filtering.spec.ts` pass.

**Steps:**

- [ ] **Step 1: Write the failing test file**

Create `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.spec.ts`:

```ts
import {describe, expect, it} from 'vitest';
import {buildIndexCountSummary, countByStatus, filterAndSortByName, filterBySystemIndices} from './elastic-indices-filtering';
import {EsIndexStateDto} from '../models/EsIndexStateDto';


function indexDto(overrides: Partial<EsIndexStateDto> & {indexName: string}): EsIndexStateDto {
    return {
        indexExists: true,
        summary: {indexName: overrides.indexName, description: '', isActiveVersion: false},
        health: {indexName: overrides.indexName, status: 'green'},
        ...overrides
    } as EsIndexStateDto;
}


describe('elastic-indices-filtering', () => {


    describe('filterBySystemIndices()', () => {

        it('returns all indices when hideSystemIndices is false', () => {
            const indices = [indexDto({indexName: '.security'}), indexDto({indexName: 'user-events'})];
            expect(filterBySystemIndices(indices, false)).toEqual(indices);
        });

        it('excludes indices starting with "." when hideSystemIndices is true', () => {
            const visible = indexDto({indexName: 'user-events'});
            const indices = [indexDto({indexName: '.security'}), visible];
            expect(filterBySystemIndices(indices, true)).toEqual([visible]);
        });

    });


    describe('filterAndSortByName()', () => {

        it('returns all indices sorted alphabetically when nameFilter is empty', () => {
            const indices = [indexDto({indexName: 'zeta'}), indexDto({indexName: 'alpha'})];
            expect(filterAndSortByName(indices, '').map((it) => it.indexName)).toEqual(['alpha', 'zeta']);
        });

        it('matches indexName case-insensitively', () => {
            const indices = [indexDto({indexName: 'User-Events'}), indexDto({indexName: 'audit-log'})];
            expect(filterAndSortByName(indices, 'user').map((it) => it.indexName)).toEqual(['User-Events']);
        });

        it('excludes indices whose name does not contain the filter', () => {
            const indices = [indexDto({indexName: 'user-events'}), indexDto({indexName: 'audit-log'})];
            expect(filterAndSortByName(indices, 'zzz')).toEqual([]);
        });

    });


    describe('countByStatus()', () => {

        it('groups indices by lowercased health status', () => {
            const indices = [
                indexDto({indexName: 'a', health: {indexName: 'a', status: 'GREEN'}}),
                indexDto({indexName: 'b', health: {indexName: 'b', status: 'green'}}),
                indexDto({indexName: 'c', health: {indexName: 'c', status: 'red'}})
            ];
            expect(countByStatus(indices)).toEqual({green: 2, red: 1});
        });

        it('ignores indices with no health status', () => {
            const indices = [indexDto({indexName: 'a', health: undefined as unknown as EsIndexStateDto['health']})];
            expect(countByStatus(indices)).toEqual({});
        });

    });


    describe('buildIndexCountSummary()', () => {

        it('shows a plain count with ordered status segments when the filter does not narrow the set', () => {
            expect(buildIndexCountSummary(12, 12, {yellow: 3, green: 8, red: 1}))
                .toEqual('12 indices · 8 green · 3 yellow · 1 red');
        });

        it('shows "X of Y" when the filter narrows the set', () => {
            expect(buildIndexCountSummary(3, 12, {green: 2, red: 1}))
                .toEqual('3 of 12 indices · 2 green · 1 red');
        });

        it('omits status segments with a zero count', () => {
            expect(buildIndexCountSummary(5, 5, {green: 5, yellow: 0}))
                .toEqual('5 indices · 5 green');
        });

        it('orders unknown statuses alphabetically after green/yellow/red', () => {
            expect(buildIndexCountSummary(3, 3, {red: 1, unknown: 1, aqua: 1}))
                .toEqual('3 indices · 1 red · 1 aqua · 1 unknown');
        });

        it('uses singular "index" when the total is 1', () => {
            expect(buildIndexCountSummary(1, 1, {green: 1})).toEqual('1 index · 1 green');
        });

        it('shows just the count label when there are no status counts', () => {
            expect(buildIndexCountSummary(0, 0, {})).toEqual('0 indices');
        });

    });


});
```

- [ ] **Step 2: Run the test to verify it fails**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-elasticsearch`
Expected: FAIL — `elastic-indices-filtering.ts` does not exist (module not found).

- [ ] **Step 3: Write the implementation**

Create `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.ts`:

```ts
import {EsIndexStateDto} from '../models/EsIndexStateDto';

const STATUS_DISPLAY_ORDER = ['green', 'yellow', 'red'];


export function filterBySystemIndices(indices: EsIndexStateDto[], hideSystemIndices: boolean): EsIndexStateDto[] {
    return indices.filter((it) => hideSystemIndices === false || it.indexName.startsWith('.') === false);
}


export function filterAndSortByName(indices: EsIndexStateDto[], nameFilter: string): EsIndexStateDto[] {
    const normalizedFilter = nameFilter.toLowerCase();
    return indices
        .filter((it) => it.indexName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.indexName.localeCompare(b.indexName));
}


export function countByStatus(indices: EsIndexStateDto[]): Record<string, number> {
    const counts: Record<string, number> = {};
    for (const index of indices) {
        const status = index.health?.status?.toLowerCase();
        if (!status) {
            continue;
        }
        counts[status] = (counts[status] ?? 0) + 1;
    }
    return counts;
}


export function buildIndexCountSummary(
    visibleCount: number,
    totalCount: number,
    statusCounts: Record<string, number>
): string {
    const indexWord = totalCount === 1 ? 'index' : 'indices';
    const countLabel = visibleCount === totalCount
        ? `${totalCount} ${indexWord}`
        : `${visibleCount} of ${totalCount} ${indexWord}`;

    const orderedStatuses = [
        ...STATUS_DISPLAY_ORDER,
        ...Object.keys(statusCounts).filter((status) => !STATUS_DISPLAY_ORDER.includes(status)).sort()
    ];

    const statusSegments = orderedStatuses
        .filter((status) => (statusCounts[status] ?? 0) > 0)
        .map((status) => `${statusCounts[status]} ${status}`);

    return statusSegments.length === 0
        ? countLabel
        : `${countLabel} · ${statusSegments.join(' · ')}`;
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-elasticsearch`
Expected: PASS — all `elastic-indices-filtering` tests green.

- [ ] **Step 5: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.ts \
        libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.spec.ts
git commit -m "Add pure filtering/sorting/count-summary logic for elastic indices dashboard"
```

---

## Task 2: Wire the store to the new logic, filter, and error state

**Goal:** Add `nameFilter`/`error` state, the `toggleFilteredIndexStateDtos`/`visibleIndexStateDtos`/`statusCounts`/`countSummary` computed signals (built on Task 1's functions), and the `onNameFilterChanged`/`retryFetch` methods to `ElasticIndicesPageStore`.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-page-store.ts`

**Acceptance Criteria:**
- [ ] State has `nameFilter: string` (default `''`) and `error: string | null` (default `null`).
- [ ] `toggleFilteredIndexStateDtos` = `filterBySystemIndices(indexStateDtos(), hideSystemIndices())`.
- [ ] `visibleIndexStateDtos` = `filterAndSortByName(toggleFilteredIndexStateDtos(), nameFilter())`.
- [ ] `statusCounts` = `countByStatus(visibleIndexStateDtos())`.
- [ ] `countSummary` = `buildIndexCountSummary(visibleIndexStateDtos().length, toggleFilteredIndexStateDtos().length, statusCounts())`.
- [ ] `onNameFilterChanged(value: string)` patches `nameFilter` directly (no debounce — this filters in-memory data, not an API call).
- [ ] On fetch success, `error` is reset to `null`. On fetch failure, `error` is set to `'Failed to load indices.'` (in addition to the existing `console.error`).
- [ ] `retryFetch()` re-invokes `fetchAllIndices()`.

**Verify:** `npx ng build maia-elasticsearch` (run from `libs/maia-ui-workspace/`) → builds with no TypeScript errors.

**Steps:**

- [ ] **Step 1: Replace the store file**

Replace the full contents of `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-page-store.ts` with:

```ts
import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {debounceTime, distinctUntilChanged, switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {EsIndexStateDto} from '../models/EsIndexStateDto';
import {ElasticIndicesApiService} from '../services/elastic-indices-api-service';
import {MatSlideToggleChange} from '@angular/material/slide-toggle';
import {buildIndexCountSummary, countByStatus, filterAndSortByName, filterBySystemIndices} from './elastic-indices-filtering';

type ElasticIndicesPageState = {
    hideSystemIndices: boolean;
    indexStateDtos: EsIndexStateDto[];
    isLoading: boolean;
    nameFilter: string;
    error: string | null;
};

const initialState: ElasticIndicesPageState = {
    hideSystemIndices: true,
    indexStateDtos: [],
    isLoading: false,
    nameFilter: '',
    error: null,
};

export const ElasticIndicesPageStore = signalStore(

    withState(initialState),

    withComputed(({indexStateDtos, hideSystemIndices, nameFilter}) => {
        const toggleFilteredIndexStateDtos = computed<EsIndexStateDto[]>(() =>
            filterBySystemIndices(indexStateDtos(), hideSystemIndices())
        );
        const visibleIndexStateDtos = computed<EsIndexStateDto[]>(() =>
            filterAndSortByName(toggleFilteredIndexStateDtos(), nameFilter())
        );
        const statusCounts = computed<Record<string, number>>(() =>
            countByStatus(visibleIndexStateDtos())
        );
        const countSummary = computed<string>(() =>
            buildIndexCountSummary(
                visibleIndexStateDtos().length,
                toggleFilteredIndexStateDtos().length,
                statusCounts()
            )
        );
        return {toggleFilteredIndexStateDtos, visibleIndexStateDtos, statusCounts, countSummary};
    }),

    withMethods((store, pageService = inject(ElasticIndicesApiService)) => ({

        fetchAllIndices: rxMethod<void>(
            pipe(
                debounceTime(300),
                distinctUntilChanged(),
                tap(() => patchState(store, {isLoading: true})),
                switchMap(() =>
                    pageService.getIndexDefinitions().pipe(
                        tapResponse({
                            next: (indexStateDtos) => patchState(store, {indexStateDtos, isLoading: false, error: null}),
                            error: (err) => {
                                patchState(store, {isLoading: false, error: 'Failed to load indices.'});
                                console.error(err);
                            },
                        })
                    )
                )
            )
        ),

        retryFetch(): void {
            this.fetchAllIndices();
        },

        onHideSystemIndicesChanged(toggleChange: MatSlideToggleChange): void {
            patchState(store, {hideSystemIndices: toggleChange.checked});
        },

        onNameFilterChanged(value: string): void {
            patchState(store, {nameFilter: value});
        },

    }))

);
```

- [ ] **Step 2: Verify it compiles**

Run (from `libs/maia-ui-workspace/`): `npx ng build maia-elasticsearch`
Expected: build succeeds, no TypeScript errors (note: `elastic-indices-page.html` still references only the pre-existing store members at this point, so the build should already pass before Task 4 touches the template).

- [ ] **Step 3: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-page-store.ts
git commit -m "Add name filter, error state, and count-summary signals to elastic indices store"
```

---

## Task 3: Card status dot and active-version badge

**Goal:** Replace the plain-text health status and "Is Active Version" lines in `ElasticIndex` with a colored status dot and a conditional "Active version" badge.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/components/elastic-index/elastic-index.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/components/elastic-index/elastic-index.html`

**Acceptance Criteria:**
- [ ] A colored dot appears next to the index name only when `index().indexExists` is true and `health.status` is present; color mapping (case-insensitive): `green` → `#4caf50`, `yellow` → `#fbc02d`, `red` → `#d32f2f`, anything else → `#9e9e9e`.
- [ ] The dot carries a `title` attribute with the raw (un-lowercased) status string — no visible text label next to it.
- [ ] The "Active version" badge is shown only when `index().indexExists && index().summary?.isActiveVersion`; otherwise no badge is shown (the existing "Set as Active Version..." button already communicates the non-active case).
- [ ] Existing `Create...` / `Set as Active Version...` buttons and their `(click)` handlers are unchanged in behavior.

**Verify:** `npx ng build maia-elasticsearch` (run from `libs/maia-ui-workspace/`) → builds with no errors.

**Steps:**

- [ ] **Step 1: Update the component class**

Replace the contents of `elastic-index.ts` with:

```ts
import {Component, computed, input, output} from '@angular/core';
import {EsIndexStateDto} from '../../models/EsIndexStateDto';
import {MatButtonModule} from '@angular/material/button';

const STATUS_COLORS: Record<string, string> = {
    green: '#4caf50',
    yellow: '#fbc02d',
    red: '#d32f2f',
};
const UNKNOWN_STATUS_COLOR = '#9e9e9e';

@Component({
    imports: [MatButtonModule],
    selector: 'maia-elastic-index',
    templateUrl: './elastic-index.html'
})
export class ElasticIndex {

    index = input.required<EsIndexStateDto>();

    createIndex = output<EsIndexStateDto>();
    setIndexVersionActive = output<EsIndexStateDto>();

    statusColor = computed<string | undefined>(() => {
        if (!this.index().indexExists) {
            return undefined;
        }
        const status = this.index().health?.status?.toLowerCase();
        return status ? (STATUS_COLORS[status] ?? UNKNOWN_STATUS_COLOR) : undefined;
    });

    onCreateIndex() {
        this.createIndex.emit(this.index());
    }

    onSetIndexVersionActive() {
        this.setIndexVersionActive.emit(this.index());
    }

}
```

- [ ] **Step 2: Update the template**

Replace the contents of `elastic-index.html` with:

```html
<div class="flex flex-col gap-2 rounded-lg border border-gray-300 p-3.5">
    <div class="flex items-center justify-between gap-2">
        <span class="font-medium">{{ index().indexName }}</span>
        @if (statusColor(); as color) {
            <span
                class="inline-block h-2.5 w-2.5 shrink-0 rounded-full"
                [style.background-color]="color"
                [title]="index().health?.status">
            </span>
        }
    </div>
    @if (index().summary?.description) {
        <p class="text-sm text-gray-500">{{ index().summary?.description }}</p>
    }
    @if (index().indexExists && index().summary?.isActiveVersion) {
        <span class="self-start rounded-full bg-gray-200 px-2 py-0.5 text-xs text-blue-700">Active version</span>
    }
    @if (!index().indexExists) {
        <div class="flex justify-end">
            <button mat-flat-button (click)="onCreateIndex()" color="primary">Create...</button>
        </div>
    }
    @if (index().indexExists && !index().summary?.isActiveVersion) {
        <div class="flex justify-end">
            <button mat-flat-button (click)="onSetIndexVersionActive()" color="primary">Set as Active Version...</button>
        </div>
    }
</div>
```

- [ ] **Step 3: Verify it compiles**

Run (from `libs/maia-ui-workspace/`): `npx ng build maia-elasticsearch`
Expected: build succeeds, no TypeScript/template errors.

- [ ] **Step 4: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/components/elastic-index/elastic-index.ts \
        libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/components/elastic-index/elastic-index.html
git commit -m "Replace text health status and active-version line with a status dot and badge"
```

---

## Task 4: Page layout — header, filter row, states, grid

**Goal:** Rebuild `elastic-indices-page.html`/`.ts` per the approved layout: title + count summary, a filter row (text input + existing toggle) that stays visible across all states, and a content area that shows a spinner, an error banner with retry, an empty message, or the card grid.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.html`

**Acceptance Criteria:**
- [ ] Header row shows a page title and `store.countSummary()`.
- [ ] Filter row has a `matInput` (placeholder "Filter indices...") wired to `store.onNameFilterChanged(...)` on `(input)`, next to the existing `mat-slide-toggle`. This row renders regardless of loading/error/empty state.
- [ ] Below the filter row, exactly one of these renders at a time: a spinner (`store.isLoading()`), an error banner with a "Retry" button calling `store.retryFetch()` (`store.error()`), an empty message (`store.visibleIndexStateDtos().length === 0`), or the card grid.
- [ ] The card grid uses a Tailwind CSS grid (`grid-cols-[repeat(auto-fill,minmax(260px,1fr))]`) and still passes the same `[index]`, `(createIndex)`, `(setIndexVersionActive)` bindings to `<maia-elastic-index>` as before.
- [ ] `onCreateIndex`/`onSetIndexVersionActive` dialog-handling logic in the component class is unchanged.

**Verify:** `npx ng build maia-elasticsearch` (run from `libs/maia-ui-workspace/`) → builds with no errors.

**Steps:**

- [ ] **Step 1: Update the component class**

In `elastic-indices-page.ts`, add the new Material module imports and an `onFilterInput` handler. Replace the full file with:

```ts
import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatButtonModule} from '@angular/material/button';
import {EsIndexStateDto} from './models/EsIndexStateDto';
import {ElasticIndicesApiService} from './services/elastic-indices-api-service';
import {ElasticIndicesPageStore} from './state/elastic-indices-page-store';
import {ElasticIndex} from './components/elastic-index/elastic-index';
import {CreateIndexDialog} from './dialogs/create-index-dialog/create-index-dialog';
import {SetIndexVersionActiveDialog} from './dialogs/set-index-version-active-dialog/set-index-version-active-dialog';


@Component({
    imports: [ElasticIndex, MatSlideToggle, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule, MatButtonModule],
    providers: [ElasticIndicesApiService, ElasticIndicesPageStore],
    selector: 'maia-elastic-indices-page',
    templateUrl: './elastic-indices-page.html'
})
export class ElasticIndicesPage implements OnInit {


    readonly store = inject(ElasticIndicesPageStore);


    constructor(
        private elasticIndicesService: ElasticIndicesApiService,
        private dialog: MatDialog
    ) {}


    ngOnInit() {
        this.store.fetchAllIndices();
    }


    onFilterInput(event: Event) {
        this.store.onNameFilterChanged((event.target as HTMLInputElement).value);
    }


    onCreateIndex(dto: EsIndexStateDto) {
        const dialogRef = this.dialog.open(CreateIndexDialog, {
            width: '400px',
            data: dto
        });
        dialogRef.afterClosed().subscribe((result) => {
            if (result) {
                this.elasticIndicesService.createIndex(dto.indexName).subscribe(() => {
                    this.store.fetchAllIndices();
                });
            }
        });
    }


    onSetIndexVersionActive(dto: EsIndexStateDto) {
        const dialogRef = this.dialog.open(SetIndexVersionActiveDialog, {
            data: dto
        });
        dialogRef.afterClosed().subscribe((result) => {
            if (result) {
                this.elasticIndicesService.onSetIndexVersionActive(dto.indexName);
            }
        });
    }


}
```

- [ ] **Step 2: Update the template**

Replace the full contents of `elastic-indices-page.html` with:

```html
<div class="flex items-center justify-between">
    <h2 class="text-lg font-medium">Elasticsearch Indices</h2>
    <span class="text-sm text-gray-500">{{ store.countSummary() }}</span>
</div>

<div class="mt-2 flex items-center gap-4 border-b border-gray-300 pb-2.5">
    <mat-form-field subscriptSizing="dynamic" class="max-w-[240px] flex-1">
        <input matInput placeholder="Filter indices..." (input)="onFilterInput($event)">
    </mat-form-field>
    <mat-slide-toggle
        [checked]="store.hideSystemIndices()"
        (change)="store.onHideSystemIndicesChanged($event)">
        Hide system indices
    </mat-slide-toggle>
</div>

@if (store.isLoading()) {
    <div class="flex justify-center py-10">
        <mat-spinner diameter="40"></mat-spinner>
    </div>
} @else if (store.error(); as error) {
    <div class="mt-5 flex items-center justify-between rounded border border-red-300 bg-red-50 p-3.5 text-red-800">
        <span>{{ error }}</span>
        <button mat-flat-button color="primary" (click)="store.retryFetch()">Retry</button>
    </div>
} @else if (store.visibleIndexStateDtos().length === 0) {
    <p class="mt-5 text-gray-500">No indices match your filter.</p>
} @else {
    <div class="mt-5 grid grid-cols-[repeat(auto-fill,minmax(260px,1fr))] gap-3">
        @for (index of store.visibleIndexStateDtos(); track index.indexName) {
            <maia-elastic-index
                [index]="index"
                (createIndex)="onCreateIndex($event)"
                (setIndexVersionActive)="onSetIndexVersionActive($event)">
            </maia-elastic-index>
        }
    </div>
}
```

- [ ] **Step 3: Verify it compiles**

Run (from `libs/maia-ui-workspace/`): `npx ng build maia-elasticsearch`
Expected: build succeeds, no errors.

- [ ] **Step 4: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.ts \
        libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.html
git commit -m "Rebuild elastic indices page with filter bar, card grid, and loading/error/empty states"
```

---

## Task 5: Manual verification in the showcase app

**Goal:** Confirm the redesigned dashboard behaves correctly end-to-end in the real app, not just via unit tests/compile checks.

**Files:** None (verification only).

**Acceptance Criteria:**
- [ ] The showcase app builds and serves without console errors on the `/elastic-indices` route.
- [ ] Grid renders responsively (reflows to fewer columns as the window narrows).
- [ ] Typing in the filter narrows the grid and switches the count summary to the "X of Y" form; clearing it reverts to the plain "Y indices" form.
- [ ] Toggling "Hide system indices" updates both the grid and the denominator in the count summary.
- [ ] Status dots show the correct color per index and the correct status on hover (`title` tooltip).
- [ ] The "Active version" badge appears only on the index that is both existing and the active version.
- [ ] If the backend/Elasticsearch is unreachable, the error banner with "Retry" appears in place of the grid, and the filter row remains visible and usable.

**Verify:** Manual browser check — this task has no automated test; it exercises Tasks 1–4 together in the running app.

**Steps:**

- [ ] **Step 1: Start the showcase UI**

From `maia-showcase/maia-showcase-ui/`, run: `npm start`
This serves the app (default Angular dev server, typically `http://localhost:4200`).

- [ ] **Step 2: Navigate and exercise the page**

Open the served URL's `/elastic-indices` route in a browser. Walk through each Acceptance Criteria item above. If the showcase backend or a local Elasticsearch instance isn't running, that's expected to surface the error-banner path — confirm that path specifically (banner text, Retry button, filter row still visible/usable) rather than treating it as a failure.

- [ ] **Step 3: Note and fix any discrepancy**

If any Acceptance Criteria item fails, fix it in the relevant Task's files (1–4) and re-verify — do not commit a "fix" as part of this task; amend/extend the appropriate earlier task's commit scope by making a new small commit here referencing what was wrong, e.g.:

```bash
git add <fixed files>
git commit -m "Fix <specific defect found during manual verification>"
```

- [ ] **Step 4: Stop the dev server**

Stop the `npm start` process once verification is complete.
