# Props Dashboard Overdue Quick Filter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an "Overdue only" quick-filter toggle to the maia-props dashboard, showing rows whose `reviewDate` is strictly before today.

**Architecture:** Mirrors the existing `overriddenOnly` / `redundantOnly` toggle pattern end-to-end: pure filter function → signal store → page component/template → URL query param sync. No backend changes.

**Tech Stack:** Angular 21 (standalone components), `@ngrx/signals`, Angular Material, Vitest.

**User Verification:** NO — no human sign-off was requested; standard automated test verification per task, plus a manual browser check in the final task per this project's frontend-change convention.

---

## Design reference

`docs/superpowers/specs/2026-09-26-props-dashboard-overdue-filter-design.md`

## Working directory for all commands

`/home/kevin/dev/code/maia/libs/maia-ui-workspace`

---

### Task 1: Add `overdueOnly` to the filtering module

**Goal:** `filterProperties`, `PropsFilters`, `parsePropsFiltersFromParams` and `buildPropsQueryParams` all support a new `overdueOnly` flag, matching the existing `overriddenOnly`/`redundantOnly` conventions.

**Files:**
- Modify: `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts`

**Acceptance Criteria:**
- [ ] `filterProperties(properties, nameFilter, overriddenOnly, redundantOnly, overdueOnly)` takes a 5th `overdueOnly` param
- [ ] A row is "overdue" when `reviewDate !== null && reviewDate < todayIsoString` (strict less-than; a `reviewDate` of today is NOT overdue)
- [ ] `PropsFilters`, `parsePropsFiltersFromParams`, `buildPropsQueryParams` all carry `overdueOnly` using the same true/null query-param convention as the other two flags (query param name: `overdueOnly`)
- [ ] All existing tests still pass with the new parameter threaded through

**Verify:** `npx ng test maia-props` → all test files pass (expect 20 tests: 16 existing + 4 new `overdueOnly` cases, plus the updated parse/build cases)

**Steps:**

- [ ] **Step 1: Replace the filtering module with the new implementation**

Replace the full contents of `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts` with:

```ts
import {ParamMap, Params} from '@angular/router';
import {PropertyResponseDto} from '../models/PropertyResponseDto';


export interface PropsFilters {
    nameFilter: string;
    overriddenOnly: boolean;
    redundantOnly: boolean;
    overdueOnly: boolean;
}


function todayIsoString(): string {
    return new Date().toISOString().slice(0, 10);
}


export function filterProperties(
    properties: PropertyResponseDto[],
    nameFilter: string,
    overriddenOnly: boolean,
    redundantOnly: boolean,
    overdueOnly: boolean
): PropertyResponseDto[] {

    const normalizedFilter = nameFilter.trim().toLowerCase();
    const today = todayIsoString();

    return properties
        .filter(p => !overriddenOnly || p.isOverridden)
        .filter(p => !redundantOnly || p.isRedundant)
        .filter(p => !overdueOnly || (p.reviewDate !== null && p.reviewDate < today))
        .filter(p => normalizedFilter === '' || p.propertyName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.propertyName.localeCompare(b.propertyName));

}


export function countOverridden(properties: PropertyResponseDto[]): number {

    return properties.filter(p => p.isOverridden).length;

}


export function parsePropsFiltersFromParams(params: ParamMap): PropsFilters {

    const rawOverriddenOnly = params.get('overriddenOnly');
    const rawRedundantOnly = params.get('redundantOnly');
    const rawOverdueOnly = params.get('overdueOnly');

    return {
        nameFilter: params.get('propertyName') ?? '',
        overriddenOnly: rawOverriddenOnly === null ? false : rawOverriddenOnly === 'true',
        redundantOnly: rawRedundantOnly === null ? false : rawRedundantOnly === 'true',
        overdueOnly: rawOverdueOnly === null ? false : rawOverdueOnly === 'true',
    };

}


export function buildPropsQueryParams(filters: PropsFilters): Params {
    return {
        propertyName: filters.nameFilter.length > 0 ? filters.nameFilter : null,
        overriddenOnly: filters.overriddenOnly === false ? null : 'true',
        redundantOnly: filters.redundantOnly === false ? null : 'true',
        overdueOnly: filters.overdueOnly === false ? null : 'true',
    };
}
```

- [ ] **Step 2: Replace the spec file with the updated tests**

Replace the full contents of `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts` with:

```ts
import {convertToParamMap} from '@angular/router';
import {buildPropsQueryParams, countOverridden, filterProperties, parsePropsFiltersFromParams} from './props-dashboard-filtering';
import {PropertyResponseDto} from '../models/PropertyResponseDto';

function aProperty(overrides: Partial<PropertyResponseDto> = {}): PropertyResponseDto {
    return {
        propertyName: 'server.port',
        effectiveValue: '3200',
        isOverridden: false,
        isRedundant: false,
        environmentValue: '3200',
        sourceName: 'applicationConfig',
        lastModifiedByUsername: null,
        lastModifiedTimestamp: null,
        comment: null,
        reviewDate: null,
        ...overrides,
    };
}

describe('filterProperties', () => {

    it('returns all properties when nameFilter is empty and overriddenOnly is false', () => {
        const properties = [aProperty({propertyName: 'a'}), aProperty({propertyName: 'b'})];
        expect(filterProperties(properties, '', false, false, false).length).toBe(2);
    });

    it('filters case-insensitively by name substring', () => {
        const properties = [aProperty({propertyName: 'server.port'}), aProperty({propertyName: 'maia.props.web.base-url'})];
        const result = filterProperties(properties, 'PROPS', false, false, false);
        expect(result.length).toBe(1);
        expect(result[0].propertyName).toBe('maia.props.web.base-url');
    });

    it('restricts to overridden properties when overriddenOnly is true', () => {
        const properties = [
            aProperty({propertyName: 'a', isOverridden: true}),
            aProperty({propertyName: 'b', isOverridden: false}),
        ];
        const result = filterProperties(properties, '', true, false, false);
        expect(result.length).toBe(1);
        expect(result[0].propertyName).toBe('a');
    });

    it('sorts results by property name ascending', () => {
        const properties = [aProperty({propertyName: 'zebra'}), aProperty({propertyName: 'alpha'})];
        const result = filterProperties(properties, '', false, false, false);
        expect(result.map(p => p.propertyName)).toEqual(['alpha', 'zebra']);
    });

    it('combines name filter and overriddenOnly filter, keeping only the intersection', () => {
        const properties = [
            aProperty({propertyName: 'maia.props.web.base-url', isOverridden: true}),
            aProperty({propertyName: 'maia.props.web.timeout', isOverridden: false}),
            aProperty({propertyName: 'server.port', isOverridden: true}),
        ];
        const result = filterProperties(properties, 'props', true, false, false);
        expect(result.length).toBe(1);
        expect(result[0].propertyName).toBe('maia.props.web.base-url');
    });

    it('restricts to redundant properties when redundantOnly is true', () => {
        const properties = [
            aProperty({propertyName: 'a', isOverridden: true, isRedundant: true}),
            aProperty({propertyName: 'b', isOverridden: true, isRedundant: false}),
            aProperty({propertyName: 'c', isOverridden: false, isRedundant: false}),
        ];
        const result = filterProperties(properties, '', false, true, false);
        expect(result.map(p => p.propertyName)).toEqual(['a']);
    });

    it('combines name filter and redundantOnly filter, keeping only the intersection', () => {
        const properties = [
            aProperty({propertyName: 'maia.props.web.base-url', isOverridden: true, isRedundant: true}),
            aProperty({propertyName: 'maia.props.web.timeout', isOverridden: true, isRedundant: false}),
            aProperty({propertyName: 'server.port', isOverridden: true, isRedundant: true}),
        ];
        const result = filterProperties(properties, 'props', false, true, false);
        expect(result.map(p => p.propertyName)).toEqual(['maia.props.web.base-url']);
    });

    it('returns an empty array when given an empty array', () => {
        expect(filterProperties([], '', false, false, false)).toEqual([]);
    });

    describe('overdueOnly filtering', () => {

        beforeEach(() => {
            vi.useFakeTimers();
            vi.setSystemTime(new Date('2026-09-26T12:00:00Z'));
        });

        afterEach(() => {
            vi.useRealTimers();
        });

        it('restricts to properties with a reviewDate in the past when overdueOnly is true', () => {
            const properties = [
                aProperty({propertyName: 'a', reviewDate: '2026-09-01'}),
                aProperty({propertyName: 'b', reviewDate: '2026-10-01'}),
            ];
            const result = filterProperties(properties, '', false, false, true);
            expect(result.map(p => p.propertyName)).toEqual(['a']);
        });

        it('excludes properties with a null reviewDate when overdueOnly is true', () => {
            const properties = [
                aProperty({propertyName: 'a', reviewDate: '2026-09-01'}),
                aProperty({propertyName: 'b', reviewDate: null}),
            ];
            const result = filterProperties(properties, '', false, false, true);
            expect(result.map(p => p.propertyName)).toEqual(['a']);
        });

        it('excludes a property whose reviewDate is today when overdueOnly is true', () => {
            const properties = [
                aProperty({propertyName: 'a', reviewDate: '2026-09-01'}),
                aProperty({propertyName: 'b', reviewDate: '2026-09-26'}),
            ];
            const result = filterProperties(properties, '', false, false, true);
            expect(result.map(p => p.propertyName)).toEqual(['a']);
        });

        it('combines name filter and overdueOnly filter, keeping only the intersection', () => {
            const properties = [
                aProperty({propertyName: 'maia.props.web.base-url', reviewDate: '2026-09-01'}),
                aProperty({propertyName: 'maia.props.web.timeout', reviewDate: '2026-10-01'}),
                aProperty({propertyName: 'server.port', reviewDate: '2026-09-01'}),
            ];
            const result = filterProperties(properties, 'props', false, false, true);
            expect(result.map(p => p.propertyName)).toEqual(['maia.props.web.base-url']);
        });

    });

});

describe('countOverridden', () => {

    it('counts only overridden properties in a mixed set', () => {
        const properties = [
            aProperty({propertyName: 'a', isOverridden: true}),
            aProperty({propertyName: 'b', isOverridden: false}),
            aProperty({propertyName: 'c', isOverridden: true}),
        ];
        expect(countOverridden(properties)).toBe(2);
    });

    it('returns 0 when no properties are overridden', () => {
        const properties = [
            aProperty({propertyName: 'a', isOverridden: false}),
            aProperty({propertyName: 'b', isOverridden: false}),
        ];
        expect(countOverridden(properties)).toBe(0);
    });

});

describe('parsePropsFiltersFromParams', () => {

    it('parses all filters when all params are present', () => {
        const params = convertToParamMap({propertyName: 'server.port', overriddenOnly: 'true', redundantOnly: null, overdueOnly: 'true'});
        expect(parsePropsFiltersFromParams(params)).toEqual({nameFilter: 'server.port', overriddenOnly: true, redundantOnly: false, overdueOnly: true});
    });

    it('parses redundantOnly when present', () => {
        const params = convertToParamMap({redundantOnly: 'true'});
        expect(parsePropsFiltersFromParams(params)).toEqual({nameFilter: '', overriddenOnly: false, redundantOnly: true, overdueOnly: false});
    });

    it('parses overdueOnly when present', () => {
        const params = convertToParamMap({overdueOnly: 'true'});
        expect(parsePropsFiltersFromParams(params)).toEqual({nameFilter: '', overriddenOnly: false, redundantOnly: false, overdueOnly: true});
    });

    it('defaults overriddenOnly, redundantOnly and overdueOnly to false and nameFilter to empty string when no params are present', () => {
        expect(parsePropsFiltersFromParams(convertToParamMap({}))).toEqual({nameFilter: '', overriddenOnly: false, redundantOnly: false, overdueOnly: false});
    });

});

describe('buildPropsQueryParams', () => {

    it('includes all params when they differ from their defaults', () => {
        expect(buildPropsQueryParams({nameFilter: 'server.port', overriddenOnly: true, redundantOnly: false, overdueOnly: true}))
            .toEqual({propertyName: 'server.port', overriddenOnly: 'true', redundantOnly: null, overdueOnly: 'true'});
    });

    it('includes redundantOnly when true', () => {
        expect(buildPropsQueryParams({nameFilter: '', overriddenOnly: false, redundantOnly: true, overdueOnly: false}))
            .toEqual({propertyName: null, overriddenOnly: null, redundantOnly: 'true', overdueOnly: null});
    });

    it('includes overdueOnly when true', () => {
        expect(buildPropsQueryParams({nameFilter: '', overriddenOnly: false, redundantOnly: false, overdueOnly: true}))
            .toEqual({propertyName: null, overriddenOnly: null, redundantOnly: null, overdueOnly: 'true'});
    });

    it('omits all params when filters are at their defaults', () => {
        expect(buildPropsQueryParams({nameFilter: '', overriddenOnly: false, redundantOnly: false, overdueOnly: false}))
            .toEqual({propertyName: null, overriddenOnly: null, redundantOnly: null, overdueOnly: null});
    });

});
```

- [ ] **Step 3: Run the tests**

Run: `npx ng test maia-props` (from `libs/maia-ui-workspace`)
Expected: `Test Files 1 passed (1)`, `Tests 20 passed (20)`

- [ ] **Step 4: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts
git commit -m "Add overdueOnly filter to props dashboard filtering module"
```

---

### Task 2: Wire `overdueOnly` into the signal store

**Goal:** `PropsDashboardStore` exposes `overdueOnly` state, an `onOverdueOnlyToggled` method, and includes it in the `visibleProperties` computed.

**Files:**
- Modify: `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-store.ts`

**Acceptance Criteria:**
- [ ] State has `overdueOnly: boolean` defaulting to `false`
- [ ] `onOverdueOnlyToggled(value: boolean)` patches state
- [ ] `visibleProperties` computed passes `overdueOnly()` through to `filterProperties`
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
import {countOverridden, filterProperties} from './props-dashboard-filtering';

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
        return {visibleProperties, overriddenCount};
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
Expected: `Application bundle generation complete` with no TypeScript errors

- [ ] **Step 3: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-store.ts
git commit -m "Wire overdueOnly into props dashboard store"
```

---

### Task 3: Add the "Overdue only" toggle to the page

**Goal:** The props dashboard page has a third slide-toggle, "Overdue only", that reads/writes `store.overdueOnly()` and participates in the URL query-param sync, matching the other two toggles.

**Files:**
- Modify: `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html`

**Acceptance Criteria:**
- [ ] Constructor parses `overdueOnly` from the initial query params and applies it to the store
- [ ] The query-param sync `effect()` includes `overdueOnly` in `buildPropsQueryParams`
- [ ] `onOverdueOnlyToggled(change: MatSlideToggleChange)` handler exists and delegates to the store
- [ ] Template has a third `mat-slide-toggle` labeled "Overdue only", positioned after "Redundant only"
- [ ] Manually verified in a running browser: toggling "Overdue only" filters the table to rows with a past `reviewDate`, and the choice survives a page reload (URL query param)

**Verify:** `npx ng build maia-props` (from `libs/maia-ui-workspace`) → build succeeds with no errors; then the manual browser check described in Step 3

**Steps:**

- [ ] **Step 1: Replace the page component with the updated implementation**

Replace the full contents of `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts` with:

```ts
import {Component, effect, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {MatTableModule} from '@angular/material/table';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSlideToggleModule, MatSlideToggleChange} from '@angular/material/slide-toggle';
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
    imports: [MatTableModule, MatFormFieldModule, MatInputModule, MatSlideToggleModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
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


    onOverriddenOnlyToggled(change: MatSlideToggleChange) {
        this.store.onOverriddenOnlyToggled(change.checked);
    }


    onRedundantOnlyToggled(change: MatSlideToggleChange) {
        this.store.onRedundantOnlyToggled(change.checked);
    }


    onOverdueOnlyToggled(change: MatSlideToggleChange) {
        this.store.onOverdueOnlyToggled(change.checked);
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

- [ ] **Step 2: Add the toggle to the template**

In `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html`, replace:

```html
    <mat-slide-toggle [checked]="store.redundantOnly()" (change)="onRedundantOnlyToggled($event)">Redundant only</mat-slide-toggle>
    <button mat-flat-button aria-label="Add override" (click)="onAddOverride()">Add override</button>
```

with:

```html
    <mat-slide-toggle [checked]="store.redundantOnly()" (change)="onRedundantOnlyToggled($event)">Redundant only</mat-slide-toggle>
    <mat-slide-toggle [checked]="store.overdueOnly()" (change)="onOverdueOnlyToggled($event)">Overdue only</mat-slide-toggle>
    <button mat-flat-button aria-label="Add override" (click)="onAddOverride()">Add override</button>
```

- [ ] **Step 3: Manually verify in the browser**

Start the showcase backend and frontend per `reference_running_showcase_backend_locally` (or the project's normal local-run docs), navigate to `/props-dashboard`, and confirm:
- The "Overdue only" toggle appears after "Redundant only"
- With at least one property having a past `reviewDate` and one with a future/no `reviewDate`, toggling it on shows only the past-`reviewDate` row(s)
- Reloading the page with the toggle on keeps it on (check the URL has `overdueOnly=true`)

- [ ] **Step 4: Run the build**

Run: `npx ng build maia-props` (from `libs/maia-ui-workspace`)
Expected: `Application bundle generation complete` with no TypeScript errors

- [ ] **Step 5: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html
git commit -m "Add Overdue only toggle to props dashboard page"
```

---

## Self-Review Notes

- **Spec coverage:** All four spec bullets (filtering logic, URL sync, store, page/template, tests) map to Tasks 1-3. Out-of-scope backend note confirmed — no task touches `maia-props-web` or `maia-props-service`.
- **Placeholder scan:** none found — all steps contain complete code.
- **Type consistency:** `overdueOnly: boolean` and `onOverdueOnlyToggled(value: boolean)` names are identical across Tasks 1, 2, and 3.
- **User verification requirement scan:** Original request ("add a quick filter") does not ask for human sign-off — answer is NO. No dedicated verification task created; Task 3 includes a manual browser check as a step per this project's standing frontend-change convention, not as a formal gate.
