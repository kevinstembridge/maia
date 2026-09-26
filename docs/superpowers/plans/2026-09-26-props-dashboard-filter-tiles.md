# Props Dashboard Filter Tiles Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the three `mat-slide-toggle` quick filters on the maia-props dashboard with clickable count-tile buttons styled like maia-elasticsearch's status tiles, and restyle the existing "X of Y properties" total as a matching non-clickable tile.

**Architecture:** Pure filtering/count functions (already exist for `overriddenCount`; add two siblings) → store computeds → a two-row page template (tiles row + name-filter/add-button row) with click handlers replacing slide-toggle change handlers. No change to underlying filter/URL-sync logic.

**Tech Stack:** Angular 21 (standalone components), `@ngrx/signals`, Angular Material, Vitest.

**User Verification:** NO — no human sign-off was requested; standard automated test verification per task, plus a manual browser check (or thorough self-review where a live browser isn't reachable) per this project's frontend-change convention.

---

## Design reference

`docs/superpowers/specs/2026-09-26-props-dashboard-filter-tiles-design.md`

## Styling reference (copy from this file, do not modify it)

`libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.scss` (`.status-tile*` rules)

## Working directory for all commands

`/home/kevin/dev/code/maia/libs/maia-ui-workspace`

---

### Task 1: Add `countRedundant`/`countOverdue` to the filtering module

**Goal:** `props-dashboard-filtering.ts` exports two new pure functions, `countRedundant` and `countOverdue`, mirroring the existing `countOverridden`.

**Files:**
- Modify: `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts`

**Acceptance Criteria:**
- [ ] `countRedundant(properties)` returns the count of properties where `isRedundant` is `true`
- [ ] `countOverdue(properties)` returns the count of properties where `reviewDate !== null && reviewDate < todayIsoString` (same local-date logic already used by `filterProperties`, reusing the existing private `todayIsoString()` helper — do not duplicate it)
- [ ] Both are unaffected by any filter state — they count over the full input array as given
- [ ] All existing tests still pass

**Verify:** `npx ng test maia-props` (from `libs/maia-ui-workspace`) → all tests pass (expect 31: 23 existing + 4 `countRedundant`/`countOverdue`-style tests... see exact test list in Step 2 — 8 new tests)

**Steps:**

- [ ] **Step 1: Add the two functions**

In `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts`, insert immediately after the existing `countOverridden` function (do not change anything else in the file):

```ts
export function countRedundant(properties: PropertyResponseDto[]): number {

    return properties.filter(p => p.isRedundant).length;

}


export function countOverdue(properties: PropertyResponseDto[]): number {

    const today = todayIsoString();
    return properties.filter(p => p.reviewDate !== null && p.reviewDate < today).length;

}
```

- [ ] **Step 2: Add tests**

In `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts`:

1. Update the import line to include the two new functions:

```ts
import {buildPropsQueryParams, countOverdue, countOverridden, countRedundant, filterProperties, parsePropsFiltersFromParams} from './props-dashboard-filtering';
```

2. Insert these two new `describe` blocks immediately after the existing `describe('countOverridden', ...)` block (and before `describe('parsePropsFiltersFromParams', ...)`):

```ts
describe('countRedundant', () => {

    it('counts only redundant properties in a mixed set', () => {
        const properties = [
            aProperty({propertyName: 'a', isRedundant: true}),
            aProperty({propertyName: 'b', isRedundant: false}),
            aProperty({propertyName: 'c', isRedundant: true}),
        ];
        expect(countRedundant(properties)).toBe(2);
    });

    it('returns 0 when no properties are redundant', () => {
        const properties = [
            aProperty({propertyName: 'a', isRedundant: false}),
            aProperty({propertyName: 'b', isRedundant: false}),
        ];
        expect(countRedundant(properties)).toBe(0);
    });

});

describe('countOverdue', () => {

    beforeEach(() => {
        vi.useFakeTimers();
        vi.setSystemTime(new Date('2026-09-26T12:00:00Z'));
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('counts only properties with a reviewDate in the past', () => {
        const properties = [
            aProperty({propertyName: 'a', reviewDate: '2026-09-01'}),
            aProperty({propertyName: 'b', reviewDate: '2026-10-01'}),
            aProperty({propertyName: 'c', reviewDate: '2026-09-15'}),
        ];
        expect(countOverdue(properties)).toBe(2);
    });

    it('excludes properties with a null reviewDate', () => {
        const properties = [
            aProperty({propertyName: 'a', reviewDate: '2026-09-01'}),
            aProperty({propertyName: 'b', reviewDate: null}),
        ];
        expect(countOverdue(properties)).toBe(1);
    });

    it('excludes a property whose reviewDate is today', () => {
        const properties = [
            aProperty({propertyName: 'a', reviewDate: '2026-09-26'}),
        ];
        expect(countOverdue(properties)).toBe(0);
    });

    it('returns 0 when no properties are overdue', () => {
        const properties = [
            aProperty({propertyName: 'a', reviewDate: '2026-10-01'}),
        ];
        expect(countOverdue(properties)).toBe(0);
    });

});
```

- [ ] **Step 3: Run the tests**

Run: `npx ng test maia-props` (from `libs/maia-ui-workspace`)
Expected: `Test Files 1 passed (1)`, `Tests 31 passed (31)` (23 existing + 8 new)

- [ ] **Step 4: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts
git commit -m "Add countRedundant and countOverdue to props dashboard filtering module"
```

---

### Task 2: Wire `redundantCount`/`overdueCount` into the signal store

**Goal:** `PropsDashboardStore` exposes `redundantCount` and `overdueCount` computed signals, alongside the existing (currently unused) `overriddenCount`.

**Files:**
- Modify: `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-store.ts`

**Acceptance Criteria:**
- [ ] `redundantCount` computed returns `countRedundant(properties())`
- [ ] `overdueCount` computed returns `countOverdue(properties())`
- [ ] Both are exposed from the store alongside `visibleProperties` and `overriddenCount`
- [ ] Project still compiles

**Verify:** `npx ng build maia-props` (from `libs/maia-ui-workspace`) → build succeeds with no errors

**Steps:**

- [ ] **Step 1: Replace the store with the updated implementation**

Replace the full contents of `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-store.ts` with:

```ts
import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {PropertyResponseDto} from '../models/PropertyResponseDto';
import {PropsApiService} from '../services/props-api.service';
import {countOverdue, countOverridden, countRedundant, filterProperties} from './props-dashboard-filtering';

type PropsDashboardState = {
    properties: PropertyResponseDto[];
    isLoading: boolean;
    error: string | null;
    nameFilter: string;
    overriddenOnly: boolean;
    redundantOnly: boolean;
    overdueOnly: boolean;
};

const initialState: PropsDashboardState = {
    properties: [],
    isLoading: false,
    error: null,
    nameFilter: '',
    overriddenOnly: false,
    redundantOnly: false,
    overdueOnly: false,
};

export const PropsDashboardStore = signalStore(

    withState(initialState),

    withComputed(({properties, nameFilter, overriddenOnly, redundantOnly, overdueOnly}) => {
        const visibleProperties = computed<PropertyResponseDto[]>(() =>
            filterProperties(properties(), nameFilter(), overriddenOnly(), redundantOnly(), overdueOnly())
        );
        const overriddenCount = computed<number>(() =>
            countOverridden(properties())
        );
        const redundantCount = computed<number>(() =>
            countRedundant(properties())
        );
        const overdueCount = computed<number>(() =>
            countOverdue(properties())
        );
        return {visibleProperties, overriddenCount, redundantCount, overdueCount};
    }),

    withMethods((store, propsService = inject(PropsApiService)) => ({

        fetchAllProperties: rxMethod<void>(
            pipe(
                tap(() => patchState(store, {isLoading: true})),
                switchMap(() =>
                    propsService.getAllProperties().pipe(
                        tapResponse({
                            next: (properties) => patchState(store, {properties, isLoading: false, error: null}),
                            error: (err) => {
                                patchState(store, {isLoading: false, error: 'Failed to load properties.'});
                                console.error(err);
                            },
                        })
                    )
                )
            )
        ),

        retryFetch(): void {
            this.fetchAllProperties();
        },

        onNameFilterChanged(value: string): void {
            patchState(store, {nameFilter: value});
        },

        onOverriddenOnlyToggled(value: boolean): void {
            patchState(store, {overriddenOnly: value});
        },

        onRedundantOnlyToggled(value: boolean): void {
            patchState(store, {redundantOnly: value});
        },

        onOverdueOnlyToggled(value: boolean): void {
            patchState(store, {overdueOnly: value});
        },

        applyPropertyUpdate(updated: PropertyResponseDto): void {
            const properties = store.properties().filter(p => p.propertyName !== updated.propertyName);
            patchState(store, {properties: [...properties, updated]});
        },

    }))

);
```

- [ ] **Step 2: Verify the build**

Run: `npx ng build maia-props` (from `libs/maia-ui-workspace`)
Expected: `Built @maia/maia-props` with no TypeScript errors

Also run: `npx ng test maia-props` (from `libs/maia-ui-workspace`) → 31/31 still pass (no store-level spec file exists — that's the established pattern, don't add one)

- [ ] **Step 3: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-store.ts
git commit -m "Wire redundantCount and overdueCount into props dashboard store"
```

---

### Task 3: Replace the slide-toggles with filter tiles on the page

**Goal:** The props dashboard page shows a row of 4 tiles (Total, Overridden, Redundant, Overdue) above the name-filter/Add-override toolbar. The 3 filter tiles are clickable buttons showing each condition's count and an active/pressed state; clicking one flips that filter, same AND-combinable semantics as before. The old `mat-slide-toggle` controls and their imports are removed.

**Files:**
- Modify: `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html`
- Modify: `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.scss`

**Acceptance Criteria:**
- [ ] `MatSlideToggleModule`/`MatSlideToggleChange` are no longer imported or referenced anywhere in `props-dashboard-page.ts`
- [ ] Three parameterless click handlers (`onOverriddenOnlyToggled`, `onRedundantOnlyToggled`, `onOverdueOnlyToggled`) each flip their store boolean via `this.store.onXToggled(!this.store.xOnly())`
- [ ] Template has a `.props-filter-tiles` row with 4 tiles: a non-clickable Total tile (unchanged "X of Y" text + `aria-live="polite"`, now labeled "Total") and 3 clickable `<button>` tiles labeled "Overridden"/"Redundant"/"Overdue", each showing `store.overriddenCount()`/`store.redundantCount()`/`store.overdueCount()` and toggling `.active`/`aria-pressed` off `store.overriddenOnly()`/etc.
- [ ] The existing `.props-dashboard-toolbar` row now contains only the name filter and the "Add override" button
- [ ] `.props-filter-tile*` SCSS rules are copied from `elastic-indices-page.scss`'s `.status-tile*` rules (border/radius/padding/cursor, `.active` state colors via `--mat-sys-primary`/`--mat-sys-primary-container`/`--mat-sys-on-primary-container`), with no color-dot element (not applicable here)
- [ ] The old `.props-dashboard-count` SCSS rule is removed (superseded by `.props-filter-tile-count`/`.props-filter-tile-label`)
- [ ] Manually verified in a running browser (or, if unreachable, a thorough static self-review): clicking each tile toggles its filter and visual active state; counts match total loaded properties matching each condition regardless of other active filters; the Total tile's "X of Y" still updates live

**Verify:** `npx ng build maia-props` (from `libs/maia-ui-workspace`) → build succeeds with no errors; `npx ng test maia-props` → 31/31 pass; then the manual check described in Step 4

**Steps:**

- [ ] **Step 1: Replace the page component**

Replace the full contents of `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts` with:

```ts
import {Component, effect, inject, OnInit} from '@angular/core';
import {DatePipe} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {MatTableModule} from '@angular/material/table';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {PropsApiService} from './services/props-api.service';
import {PropsDashboardStore} from './state/props-dashboard-store';
import {buildPropsQueryParams, parsePropsFiltersFromParams} from './state/props-dashboard-filtering';
import {PropertyResponseDto} from './models/PropertyResponseDto';
import {EditPropertyDialog, EditPropertyDialogData, EditPropertyDialogResult} from './dialogs/edit-property-dialog/edit-property-dialog';
import {RemoveOverrideDialog, RemoveOverrideDialogData, RemoveOverrideDialogResult} from './dialogs/remove-override-dialog/remove-override-dialog';
import {PropertyHistoryDialog, PropertyHistoryDialogData} from './dialogs/property-history-dialog/property-history-dialog';


@Component({
    selector: 'maia-props-dashboard-page',
    templateUrl: './props-dashboard-page.html',
    styleUrl: './props-dashboard-page.scss',
    imports: [MatTableModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, DatePipe],
    providers: [PropsApiService, PropsDashboardStore]
})
export class PropsDashboardPage implements OnInit {


    readonly store = inject(PropsDashboardStore);

    readonly displayedColumns = ['propertyName', 'effectiveValue', 'isOverridden', 'isRedundant', 'sourceName', 'lastModifiedByUsername', 'lastModifiedTimestamp', 'reviewDate', 'actions'];

    private route = inject(ActivatedRoute);
    private router = inject(Router);


    constructor(
        private propsService: PropsApiService,
        private dialog: MatDialog
    ) {

        const initialFilters = parsePropsFiltersFromParams(this.route.snapshot.queryParamMap);
        this.store.onNameFilterChanged(initialFilters.nameFilter);
        this.store.onOverriddenOnlyToggled(initialFilters.overriddenOnly);
        this.store.onRedundantOnlyToggled(initialFilters.redundantOnly);
        this.store.onOverdueOnlyToggled(initialFilters.overdueOnly);

        effect(() => {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: buildPropsQueryParams({
                    nameFilter: this.store.nameFilter(),
                    overriddenOnly: this.store.overriddenOnly(),
                    redundantOnly: this.store.redundantOnly(),
                    overdueOnly: this.store.overdueOnly(),
                }),
                replaceUrl: true,
            });
        });

    }


    ngOnInit() {
        this.store.fetchAllProperties();
    }


    onNameFilterInput(event: Event) {
        this.store.onNameFilterChanged((event.target as HTMLInputElement).value);
    }


    onOverriddenOnlyToggled(): void {
        this.store.onOverriddenOnlyToggled(!this.store.overriddenOnly());
    }


    onRedundantOnlyToggled(): void {
        this.store.onRedundantOnlyToggled(!this.store.redundantOnly());
    }


    onOverdueOnlyToggled(): void {
        this.store.onOverdueOnlyToggled(!this.store.overdueOnly());
    }


    onAddOverride() {

        this.openEditDialog({propertyName: null, currentValue: null, currentReviewDate: null});

    }


    onEdit(row: PropertyResponseDto) {

        this.openEditDialog({propertyName: row.propertyName, currentValue: row.effectiveValue, currentReviewDate: row.reviewDate});

    }


    private openEditDialog(data: EditPropertyDialogData) {

        const dialogRef = this.dialog.open(EditPropertyDialog, {width: '600px', data});

        dialogRef.afterClosed().subscribe((result: EditPropertyDialogResult | undefined) => {
            if (result) {
                this.propsService.setProperty(result.propertyName, result.propertyValue, result.comment, result.reviewDate).subscribe(updated => {
                    this.store.applyPropertyUpdate(updated);
                });
            }
        });

    }


    onRemove(row: PropertyResponseDto) {

        const data: RemoveOverrideDialogData = {propertyName: row.propertyName};
        const dialogRef = this.dialog.open(RemoveOverrideDialog, {width: '480px', data});

        dialogRef.afterClosed().subscribe((result: RemoveOverrideDialogResult | undefined) => {
            if (result) {
                // Deliberately refetches rather than using store.applyPropertyRemoval(): removing an
                // override doesn't necessarily remove the row — if the property also has a real
                // Environment value (the common case), the row must revert to showing that value,
                // not disappear. Only a refetch can know which outcome applies.
                this.propsService.removeProperty(row.propertyName, result.comment).subscribe(() => {
                    this.store.retryFetch();
                });
            }
        });

    }


    onHistory(row: PropertyResponseDto) {

        this.propsService.getPropertyHistory(row.propertyName).subscribe(historyItems => {
            const data: PropertyHistoryDialogData = {propertyName: row.propertyName, historyItems};
            this.dialog.open(PropertyHistoryDialog, {width: '600px', data});
        });

    }


}
```

- [ ] **Step 2: Replace the template's toolbar section**

In `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html`, replace this entire block (lines 1-14 currently):

```html
<div class="props-dashboard-toolbar">
    <mat-form-field class="name-filter">
        <mat-label>Filter by name</mat-label>
        <input matInput [value]="store.nameFilter()" (input)="onNameFilterInput($event)">
    </mat-form-field>
    <mat-slide-toggle [checked]="store.overriddenOnly()" (change)="onOverriddenOnlyToggled($event)">Overridden only</mat-slide-toggle>
    <mat-slide-toggle [checked]="store.redundantOnly()" (change)="onRedundantOnlyToggled($event)">Redundant only</mat-slide-toggle>
    <mat-slide-toggle [checked]="store.overdueOnly()" (change)="onOverdueOnlyToggled($event)">Overdue only</mat-slide-toggle>
    <span class="props-dashboard-count" aria-live="polite">{{store.visibleProperties().length}} of {{store.properties().length}} properties</span>
    <button mat-flat-button class="add-override-button" aria-label="Add override" (click)="onAddOverride()">
        <mat-icon>add</mat-icon>
        Add override
    </button>
</div>
```

with:

```html
<div class="props-filter-tiles">
    <div class="props-filter-tile props-filter-tile-total">
        <span class="props-filter-tile-count" aria-live="polite">{{store.visibleProperties().length}} of {{store.properties().length}}</span>
        <span class="props-filter-tile-label">Total</span>
    </div>
    <button
        type="button"
        class="props-filter-tile"
        [class.active]="store.overriddenOnly()"
        [attr.aria-pressed]="store.overriddenOnly()"
        (click)="onOverriddenOnlyToggled()">
        <span class="props-filter-tile-count">{{store.overriddenCount()}}</span>
        <span class="props-filter-tile-label">Overridden</span>
    </button>
    <button
        type="button"
        class="props-filter-tile"
        [class.active]="store.redundantOnly()"
        [attr.aria-pressed]="store.redundantOnly()"
        (click)="onRedundantOnlyToggled()">
        <span class="props-filter-tile-count">{{store.redundantCount()}}</span>
        <span class="props-filter-tile-label">Redundant</span>
    </button>
    <button
        type="button"
        class="props-filter-tile"
        [class.active]="store.overdueOnly()"
        [attr.aria-pressed]="store.overdueOnly()"
        (click)="onOverdueOnlyToggled()">
        <span class="props-filter-tile-count">{{store.overdueCount()}}</span>
        <span class="props-filter-tile-label">Overdue</span>
    </button>
</div>

<div class="props-dashboard-toolbar">
    <mat-form-field class="name-filter">
        <mat-label>Filter by name</mat-label>
        <input matInput [value]="store.nameFilter()" (input)="onNameFilterInput($event)">
    </mat-form-field>
    <button mat-flat-button class="add-override-button" aria-label="Add override" (click)="onAddOverride()">
        <mat-icon>add</mat-icon>
        Add override
    </button>
</div>
```

(Everything below this block — the `@if (store.isLoading())` section and the table — is unchanged.)

- [ ] **Step 3: Replace the stylesheet**

Replace the full contents of `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.scss` with:

```scss
.props-filter-tiles {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 16px;
}

.props-filter-tile {
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
        color: var(--mat-sys-on-primary-container);
    }

    &.active .props-filter-tile-label {
        color: inherit;
    }
}

.props-filter-tile-total {
    cursor: default;
}

.props-filter-tile-count {
    font-size: 1.125rem;
    font-weight: 500;
}

.props-filter-tile-label {
    font-size: 0.8125rem;
    color: var(--mat-sys-on-surface-variant);
}

.props-dashboard-toolbar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
}

.name-filter {
    width: 400px;
}

.add-override-button {
    margin-left: auto;
}

.property-value-cell {
    max-width: 320px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.mat-column-actions {
    min-width: 160px;
    white-space: nowrap;
}
```

- [ ] **Step 4: Manually verify (or thoroughly self-review if no live environment is reachable)**

If a running showcase app is reachable: navigate to `/props-dashboard` and confirm:
- Four tiles appear in a row: Total, Overridden, Redundant, Overdue
- Clicking "Overridden" toggles its active (highlighted) state and filters the table to overridden rows only; clicking again un-filters
- The 3 filter tiles can be combined (clicking 2+ narrows further, matching AND semantics)
- Each tile's count matches the number of properties satisfying that condition among ALL loaded properties, regardless of which other tiles are active
- The Total tile's "X of Y" still updates as filters/name-search change

If no live environment is reachable in this session (same constraint as the prior overdue-filter plan — no `ng serve` app target, real data requires a running Spring Boot backend), do a careful line-by-line self-review instead: re-read the final `props-dashboard-store.ts` and `props-dashboard-page.ts`/`.html` together and trace each tile's `[class.active]`/`[attr.aria-pressed]`/`(click)` bindings back to the exact store signal/method names, confirming no typos and correct argument flow (click → flip → patchState → computed recompute → template re-render).

- [ ] **Step 5: Run build and tests**

Run: `npx ng build maia-props` (from `libs/maia-ui-workspace`)
Expected: `Built @maia/maia-props` with no TypeScript errors

Run: `npx ng test maia-props` (from `libs/maia-ui-workspace`)
Expected: `Test Files 1 passed (1)`, `Tests 31 passed (31)`

- [ ] **Step 6: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.scss
git commit -m "Replace props dashboard slide-toggles with filter tiles"
```

---

## Self-Review Notes

- **Spec coverage:** All spec sections (layout, tile behavior, tile counts, labels, styling, code removal) map to Tasks 1-3. Out-of-scope note confirmed — `filterProperties`/`parsePropsFiltersFromParams`/`buildPropsQueryParams` are untouched by this plan.
- **Placeholder scan:** none found — all steps contain complete code.
- **Type consistency:** `countRedundant`/`countOverdue` (Task 1) → `redundantCount`/`overdueCount` computeds (Task 2) → `store.redundantCount()`/`store.overdueCount()` template bindings (Task 3) use identical names throughout.
- **User verification requirement scan:** Original request ("change toggles to buttons with counts", "copy elasticsearch styling") does not ask for human sign-off — answer is NO. No dedicated verification task created; Task 3 includes a manual-or-self-review step per this project's standing frontend-change convention, not as a formal gate.
