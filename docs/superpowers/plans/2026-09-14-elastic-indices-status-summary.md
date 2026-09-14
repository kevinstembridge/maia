# Elastic Indices Status Summary Tiles Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the elastic-indices dashboard's small text count summary with a row of clickable stat tiles (Green / Yellow / Red / Not created) that filter the index grid by status.

**Architecture:** Add a `DisplayStatus` concept (green/yellow/red/not-created) and pure helper functions to `elastic-indices-filtering.ts`. Wire a new `statusFilter` field through `ElasticIndicesPageStore`'s computed chain so tile counts reflect the name/toggle-filtered set and the grid reflects the status filter on top. Update the page template to render tiles instead of the old text summary, and refactor the card component to reuse the same status-color map instead of its own copy.

**Tech Stack:** Angular 20 (standalone components, signals, `@ngrx/signals` signalStore), TypeScript, Vitest.

**User Verification:** NO — no explicit user sign-off was requested for this feature; standard build/test verification is sufficient.

---

### Task 1: Add DisplayStatus helpers to elastic-indices-filtering.ts

**Goal:** Replace `countByStatus`/`buildIndexCountSummary` with `deriveDisplayStatus`, `countByDisplayStatus`, and `filterByStatus`, covering the "not created" case.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.spec.ts`

**Acceptance Criteria:**
- [ ] `deriveDisplayStatus` returns `'not-created'` when `indexExists` is `false`, regardless of any health data
- [ ] `deriveDisplayStatus` returns the lowercased health status (`'green'`/`'yellow'`/`'red'`) for existing indices
- [ ] `deriveDisplayStatus` returns `undefined` for an existing index with no/unrecognized health status
- [ ] `countByDisplayStatus` returns a full `{green, yellow, red, 'not-created'}` record (zeros included) for any input
- [ ] `filterByStatus(indices, null)` returns the input unchanged; a specific status returns only matches
- [ ] `countByStatus` and `buildIndexCountSummary` are removed, along with their now-obsolete tests
- [ ] `STATUS_COLORS` and `STATUS_TILE_ORDER` are exported for reuse by the card component and page

**Verify:** `cd libs/maia-ui-workspace && ng test maia-elasticsearch --watch=false` → all tests pass

**Steps:**

- [ ] **Step 1: Replace the test file with tests for the new functions**

Replace the full contents of `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.spec.ts` with:

```ts
import {describe, expect, it} from 'vitest';
import {countByDisplayStatus, deriveDisplayStatus, filterAndSortByName, filterBySystemIndices, filterByStatus} from './elastic-indices-filtering';
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


    describe('deriveDisplayStatus()', () => {

        it('returns "not-created" when the index does not exist, even if health data is present', () => {
            const index = indexDto({indexName: 'a', indexExists: false, health: {indexName: 'a', status: 'green'}});
            expect(deriveDisplayStatus(index)).toEqual('not-created');
        });

        it('returns the lowercased health status for an existing index', () => {
            const index = indexDto({indexName: 'a', indexExists: true, health: {indexName: 'a', status: 'YELLOW'}});
            expect(deriveDisplayStatus(index)).toEqual('yellow');
        });

        it('returns undefined for an existing index with no health status', () => {
            const index = indexDto({indexName: 'a', indexExists: true, health: undefined as unknown as EsIndexStateDto['health']});
            expect(deriveDisplayStatus(index)).toBeUndefined();
        });

        it('returns undefined for an existing index with an unrecognized health status', () => {
            const index = indexDto({indexName: 'a', indexExists: true, health: {indexName: 'a', status: 'purple'}});
            expect(deriveDisplayStatus(index)).toBeUndefined();
        });

    });


    describe('countByDisplayStatus()', () => {

        it('counts every display status, including zeros for statuses with no matches', () => {
            const indices = [
                indexDto({indexName: 'a', health: {indexName: 'a', status: 'GREEN'}}),
                indexDto({indexName: 'b', health: {indexName: 'b', status: 'green'}}),
                indexDto({indexName: 'c', health: {indexName: 'c', status: 'red'}}),
                indexDto({indexName: 'd', indexExists: false})
            ];
            expect(countByDisplayStatus(indices)).toEqual({green: 2, yellow: 0, red: 1, 'not-created': 1});
        });

        it('returns all zeros for an empty list', () => {
            expect(countByDisplayStatus([])).toEqual({green: 0, yellow: 0, red: 0, 'not-created': 0});
        });

    });


    describe('filterByStatus()', () => {

        it('returns all indices unchanged when status is null', () => {
            const indices = [indexDto({indexName: 'a'}), indexDto({indexName: 'b', indexExists: false})];
            expect(filterByStatus(indices, null)).toEqual(indices);
        });

        it('returns only indices matching the given status', () => {
            const green = indexDto({indexName: 'a', health: {indexName: 'a', status: 'green'}});
            const red = indexDto({indexName: 'b', health: {indexName: 'b', status: 'red'}});
            expect(filterByStatus([green, red], 'red')).toEqual([red]);
        });

        it('matches "not-created" against indices that do not exist', () => {
            const missing = indexDto({indexName: 'a', indexExists: false});
            const existing = indexDto({indexName: 'b'});
            expect(filterByStatus([missing, existing], 'not-created')).toEqual([missing]);
        });

    });


});
```

- [ ] **Step 2: Run tests to verify they fail (functions don't exist yet)**

Run: `cd libs/maia-ui-workspace && ng test maia-elasticsearch --watch=false`
Expected: FAIL — `deriveDisplayStatus`, `countByDisplayStatus`, `filterByStatus` are not exported from `./elastic-indices-filtering`

- [ ] **Step 3: Replace the implementation file**

Replace the full contents of `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.ts` with:

```ts
import {EsIndexStateDto} from '../models/EsIndexStateDto';

export type DisplayStatus = 'green' | 'yellow' | 'red' | 'not-created';

export const STATUS_TILE_ORDER: DisplayStatus[] = ['green', 'yellow', 'red', 'not-created'];

export const STATUS_COLORS: Record<DisplayStatus, string> = {
    green: '#4caf50',
    yellow: '#fbc02d',
    red: '#d32f2f',
    'not-created': '#9e9e9e',
};


export function filterBySystemIndices(indices: EsIndexStateDto[], hideSystemIndices: boolean): EsIndexStateDto[] {
    return indices.filter((it) => hideSystemIndices === false || it.indexName.startsWith('.') === false);
}


export function filterAndSortByName(indices: EsIndexStateDto[], nameFilter: string): EsIndexStateDto[] {
    const normalizedFilter = nameFilter.toLowerCase();
    return indices
        .filter((it) => it.indexName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.indexName.localeCompare(b.indexName));
}


export function deriveDisplayStatus(index: EsIndexStateDto): DisplayStatus | undefined {
    if (!index.indexExists) {
        return 'not-created';
    }
    const status = index.health?.status?.toLowerCase();
    return status === 'green' || status === 'yellow' || status === 'red' ? status : undefined;
}


export function countByDisplayStatus(indices: EsIndexStateDto[]): Record<DisplayStatus, number> {
    const counts: Record<DisplayStatus, number> = {green: 0, yellow: 0, red: 0, 'not-created': 0};
    for (const index of indices) {
        const status = deriveDisplayStatus(index);
        if (status) {
            counts[status]++;
        }
    }
    return counts;
}


export function filterByStatus(indices: EsIndexStateDto[], status: DisplayStatus | null): EsIndexStateDto[] {
    return status === null ? indices : indices.filter((it) => deriveDisplayStatus(it) === status);
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd libs/maia-ui-workspace && ng test maia-elasticsearch --watch=false`
Expected: `Tests  17 passed (17)`

- [ ] **Step 5: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.ts libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.spec.ts
git commit -m "Replace status-count text summary helpers with DisplayStatus tile helpers"
```

```json:metadata
{"files": ["libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.ts", "libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.spec.ts"], "verifyCommand": "cd libs/maia-ui-workspace && ng test maia-elasticsearch --watch=false", "acceptanceCriteria": ["deriveDisplayStatus handles not-created", "countByDisplayStatus returns full record with zeros", "filterByStatus filters or passes through", "old countByStatus/buildIndexCountSummary removed"], "requiresUserVerification": false}
```

---

### Task 2: Reuse shared STATUS_COLORS in the card component

**Goal:** Remove the duplicate color map from `elastic-index.ts` and derive its status dot color via `deriveDisplayStatus`/`STATUS_COLORS`, with no visual change to the card.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/components/elastic-index/elastic-index.ts`

**Acceptance Criteria:**
- [ ] `elastic-index.ts` no longer defines its own `STATUS_COLORS`/`UNKNOWN_STATUS_COLOR` constants
- [ ] `statusColor` still returns `undefined` for indices that don't exist (unchanged behavior — no dot shown)
- [ ] `statusColor` still returns the correct color for green/yellow/red, and `undefined` for an unrecognized status
- [ ] Full workspace build still succeeds

**Verify:** `cd libs/maia-ui-workspace && ng build maia-elasticsearch` → `Built @maia/maia-elasticsearch`

**Steps:**

- [ ] **Step 1: Update elastic-index.ts to use the shared helpers**

Replace the full contents of `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/components/elastic-index/elastic-index.ts` with:

```ts
import {Component, computed, input, output} from '@angular/core';
import {EsIndexStateDto} from '../../models/EsIndexStateDto';
import {MatButtonModule} from '@angular/material/button';
import {deriveDisplayStatus, STATUS_COLORS} from '../../state/elastic-indices-filtering';

@Component({
    imports: [MatButtonModule],
    selector: 'maia-elastic-index',
    templateUrl: './elastic-index.html',
    styleUrl: './elastic-index.scss'
})
export class ElasticIndex {

    index = input.required<EsIndexStateDto>();

    createIndex = output<EsIndexStateDto>();
    setIndexVersionActive = output<EsIndexStateDto>();

    statusColor = computed<string | undefined>(() => {
        const status = deriveDisplayStatus(this.index());
        return status && status !== 'not-created' ? STATUS_COLORS[status] : undefined;
    });

    onCreateIndex() {
        this.createIndex.emit(this.index());
    }

    onSetIndexVersionActive() {
        this.setIndexVersionActive.emit(this.index());
    }

}
```

- [ ] **Step 2: Build to verify it compiles**

Run: `cd libs/maia-ui-workspace && ng build maia-elasticsearch`
Expected: `Built @maia/maia-elasticsearch`

- [ ] **Step 3: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/components/elastic-index/elastic-index.ts
git commit -m "Reuse shared STATUS_COLORS in the elastic-index card component"
```

```json:metadata
{"files": ["libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/components/elastic-index/elastic-index.ts"], "verifyCommand": "cd libs/maia-ui-workspace && ng build maia-elasticsearch", "acceptanceCriteria": ["no duplicate STATUS_COLORS in elastic-index.ts", "statusColor behavior unchanged for existing/missing indices", "build passes"], "requiresUserVerification": false}
```

---

### Task 3: Add statusFilter to ElasticIndicesPageStore

**Goal:** Wire `statusFilter` state and `statusCounts`/`visibleIndexStateDtos` computeds through the store so tile counts and the grid reflect it correctly.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-page-store.ts`

**Acceptance Criteria:**
- [ ] Store has a `statusFilter: DisplayStatus | null` state field, default `null`
- [ ] `statusCounts` reflects the name+toggle filtered set (not further narrowed by `statusFilter`)
- [ ] `visibleIndexStateDtos` (used by the template for the grid) applies `statusFilter` on top of the name+toggle filtered set
- [ ] `onStatusFilterToggled(status)` sets `statusFilter` to `status`, or clears it to `null` if `status` is already active
- [ ] The old `countSummary` computed is removed
- [ ] Full workspace build still succeeds

**Verify:** `cd libs/maia-ui-workspace && ng build maia-elasticsearch` → `Built @maia/maia-elasticsearch`

**Steps:**

- [ ] **Step 1: Replace the store file**

Replace the full contents of `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-page-store.ts` with:

```ts
import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {debounceTime, switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {EsIndexStateDto} from '../models/EsIndexStateDto';
import {ElasticIndicesApiService} from '../services/elastic-indices-api-service';
import {MatSlideToggleChange} from '@angular/material/slide-toggle';
import {countByDisplayStatus, DisplayStatus, filterAndSortByName, filterBySystemIndices, filterByStatus} from './elastic-indices-filtering';

type ElasticIndicesPageState = {
    hideSystemIndices: boolean;
    indexStateDtos: EsIndexStateDto[];
    isLoading: boolean;
    nameFilter: string;
    statusFilter: DisplayStatus | null;
    error: string | null;
};

const initialState: ElasticIndicesPageState = {
    hideSystemIndices: true,
    indexStateDtos: [],
    isLoading: false,
    nameFilter: '',
    statusFilter: null,
    error: null,
};

export const ElasticIndicesPageStore = signalStore(

    withState(initialState),

    withComputed(({indexStateDtos, hideSystemIndices, nameFilter, statusFilter}) => {
        const toggleFilteredIndexStateDtos = computed<EsIndexStateDto[]>(() =>
            filterBySystemIndices(indexStateDtos(), hideSystemIndices())
        );
        const nameFilteredIndexStateDtos = computed<EsIndexStateDto[]>(() =>
            filterAndSortByName(toggleFilteredIndexStateDtos(), nameFilter())
        );
        const statusCounts = computed<Record<DisplayStatus, number>>(() =>
            countByDisplayStatus(nameFilteredIndexStateDtos())
        );
        const visibleIndexStateDtos = computed<EsIndexStateDto[]>(() =>
            filterByStatus(nameFilteredIndexStateDtos(), statusFilter())
        );
        return {toggleFilteredIndexStateDtos, nameFilteredIndexStateDtos, statusCounts, visibleIndexStateDtos};
    }),

    withMethods((store, pageService = inject(ElasticIndicesApiService)) => ({

        fetchAllIndices: rxMethod<void>(
            pipe(
                debounceTime(300),
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

        onStatusFilterToggled(status: DisplayStatus): void {
            patchState(store, {statusFilter: store.statusFilter() === status ? null : status});
        },

    }))

);
```

- [ ] **Step 2: Build to verify it compiles**

Run: `cd libs/maia-ui-workspace && ng build maia-elasticsearch`
Expected: `Built @maia/maia-elasticsearch`

Note: `elastic-indices-page.html` still references `store.countSummary()`, which no longer exists — this will fail template type-checking. That's expected here; Task 4 fixes the template. If the build fails specifically on `countSummary` being unknown, that confirms Task 3 is otherwise correct — proceed to Task 4 rather than trying to make Task 3's build pass in isolation.

- [ ] **Step 3: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-page-store.ts
git commit -m "Add statusFilter to ElasticIndicesPageStore for status tile filtering"
```

```json:metadata
{"files": ["libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-page-store.ts"], "verifyCommand": "cd libs/maia-ui-workspace && ng build maia-elasticsearch", "acceptanceCriteria": ["statusFilter state added", "statusCounts computed before status filter applied", "visibleIndexStateDtos applies status filter", "onStatusFilterToggled toggles correctly", "countSummary removed"], "requiresUserVerification": false}
```

---

### Task 4: Render status tiles in the page template

**Goal:** Replace the toolbar text summary with a row of clickable status tiles, styled consistently with the rest of the page.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.html`
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.scss`

**Acceptance Criteria:**
- [ ] Page renders 4 tiles in `STATUS_TILE_ORDER` order, each showing a colored dot, count, and label
- [ ] Clicking a tile calls `store.onStatusFilterToggled(status)`
- [ ] The active tile has `aria-pressed="true"` and an `.active` class
- [ ] Full workspace build succeeds
- [ ] `ng build` of `maia-showcase-ui` succeeds end-to-end

**Verify:** `cd maia-showcase/maia-showcase-ui && npx ng build` → `Application bundle generation complete`

**Steps:**

- [ ] **Step 1: Add tile metadata fields to elastic-indices-page.ts**

Replace the full contents of `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.ts` with:

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
import {DisplayStatus, STATUS_COLORS, STATUS_TILE_ORDER} from './state/elastic-indices-filtering';

const STATUS_LABELS: Record<DisplayStatus, string> = {
    green: 'Green',
    yellow: 'Yellow',
    red: 'Red',
    'not-created': 'Not created',
};


@Component({
    imports: [ElasticIndex, MatSlideToggle, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule, MatButtonModule],
    providers: [ElasticIndicesApiService, ElasticIndicesPageStore],
    selector: 'maia-elastic-indices-page',
    templateUrl: './elastic-indices-page.html',
    styleUrl: './elastic-indices-page.scss'
})
export class ElasticIndicesPage implements OnInit {


    readonly store = inject(ElasticIndicesPageStore);

    readonly statusTileOrder = STATUS_TILE_ORDER;
    readonly statusColors = STATUS_COLORS;
    readonly statusLabels = STATUS_LABELS;


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

- [ ] **Step 2: Replace the toolbar markup with tiles in elastic-indices-page.html**

In `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.html`, replace:

```html
<div class="toolbar">
    <span class="count">{{ store.countSummary() }}</span>
</div>
```

with:

```html
<div class="status-tiles">
    @for (status of statusTileOrder; track status) {
        <button
            type="button"
            class="status-tile"
            [class.active]="store.statusFilter() === status"
            [attr.aria-pressed]="store.statusFilter() === status"
            (click)="store.onStatusFilterToggled(status)">
            <span class="status-tile-dot" [style.background-color]="statusColors[status]"></span>
            <span class="status-tile-count">{{ store.statusCounts()[status] }}</span>
            <span class="status-tile-label">{{ statusLabels[status] }}</span>
        </button>
    }
</div>
```

The rest of the file (filter bar, loading/error/empty states, card grid) is unchanged.

- [ ] **Step 3: Replace `.toolbar`/`.count` styles with tile styles in elastic-indices-page.scss**

In `libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.scss`, replace:

```scss
.toolbar {
    display: flex;
    align-items: center;
    justify-content: flex-end;
}

.count {
    font-size: 0.875rem;
    color: var(--mat-sys-on-surface-variant);
}
```

with:

```scss
.status-tiles {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
}

.status-tile {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    min-width: 8rem;
    border: 1px solid var(--mat-sys-outline-variant);
    border-radius: 0.5rem;
    background: none;
    padding: 0.5rem 0.75rem;
    font: inherit;
    cursor: pointer;

    &.active {
        border-color: var(--mat-sys-primary);
        background-color: var(--mat-sys-primary-container);
    }
}

.status-tile-dot {
    display: inline-block;
    flex-shrink: 0;
    width: 0.625rem;
    height: 0.625rem;
    border-radius: 9999px;
}

.status-tile-count {
    font-size: 1.125rem;
    font-weight: 500;
}

.status-tile-label {
    font-size: 0.8125rem;
    color: var(--mat-sys-on-surface-variant);
}
```

The rest of the file (`.filter-bar`, `.filter-field`, `.loading`, `.error-banner`, `.empty-message`, `.index-grid`) is unchanged.

- [ ] **Step 4: Run the maia-elasticsearch unit tests**

Run: `cd libs/maia-ui-workspace && ng test maia-elasticsearch --watch=false`
Expected: `Tests  17 passed (17)`

- [ ] **Step 5: Build the library**

Run: `cd libs/maia-ui-workspace && ng build maia-elasticsearch`
Expected: `Built @maia/maia-elasticsearch`

- [ ] **Step 6: Build the full showcase app end-to-end**

Run: `cd maia-showcase/maia-showcase-ui && npx ng build`
Expected: `Application bundle generation complete` (the existing initial-bundle-size warning is pre-existing and unrelated)

- [ ] **Step 7: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.ts libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.html libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.scss
git commit -m "Render clickable status summary tiles on the elastic-indices dashboard"
```

```json:metadata
{"files": ["libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.ts", "libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.html", "libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.scss"], "verifyCommand": "cd maia-showcase/maia-showcase-ui && npx ng build", "acceptanceCriteria": ["4 tiles render in order", "clicking a tile calls onStatusFilterToggled", "active tile has aria-pressed and .active class", "full showcase build passes"], "requiresUserVerification": false}
```

---

## Self-Review Notes

- **Spec coverage:** All spec sections (data model, store, template/styling, testing) map to Task 1–4 above. Out-of-scope items (multi-select tiles, jobs dashboard, keeping old text) are correctly excluded.
- **Type consistency:** `DisplayStatus`, `STATUS_COLORS`, `STATUS_TILE_ORDER` are defined once in Task 1 and imported (not redefined) in Tasks 2–4. `onStatusFilterToggled` name matches between Task 3 (store) and Task 4 (template).
- **User verification:** The original request ("dashboard should include a summary...") contains no request for human sign-off during implementation — confirmed NO, no dedicated verification task needed.
