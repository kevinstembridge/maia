# Props Dashboard Card Layout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the `mat-table` on the props dashboard with one card per record (mockup Variant 2 / "two-tier card": https://claude.ai/artifact/81QkWMeQrD7V4v9XduFVe7), via a new `PropsCard` component mirroring the existing `maia-elastic-index` per-item component pattern.

**Architecture:** Extract a shared `isOverdue(reviewDate)` predicate in the filtering module (removing an existing duplication) → new standalone `PropsCard` component (signal `input`/`output`, two-tier layout, left status-accent stripe, conditional badges) → page swaps its `<table mat-table>` for a `@for` loop rendering `<maia-props-card>`.

**Tech Stack:** Angular 21 (standalone components, signal `input`/`output`), Angular Material, Vitest.

**User Verification:** NO — no human sign-off was requested; standard automated test verification per task, plus a manual browser check (or thorough self-review where a live browser isn't reachable) per this project's frontend-change convention.

---

## Design reference

`docs/superpowers/specs/2026-09-26-props-dashboard-card-layout-design.md`

## Component pattern reference (read, do not modify)

`libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/components/elastic-index/` (`elastic-index.ts`/`.html`/`.scss`) — the existing "one component per list item" convention this plan follows.

## Working directory for all commands

`/home/kevin/dev/code/maia/libs/maia-ui-workspace`

---

### Task 1: Extract `isOverdue` in the filtering module

**Goal:** `props-dashboard-filtering.ts` exports a reusable `isOverdue(reviewDate)` predicate; `filterProperties` and `countOverdue` both use it instead of duplicating the "reviewDate in the past" check inline.

**Files:**
- Modify: `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts`

**Acceptance Criteria:**
- [ ] `isOverdue(reviewDate: string | null): boolean` is exported, returns `reviewDate !== null && reviewDate < todayIsoString()`
- [ ] `filterProperties`'s `overdueOnly` filter calls `isOverdue(p.reviewDate)` instead of the inline predicate
- [ ] `countOverdue` calls `isOverdue(p.reviewDate)` instead of the inline predicate
- [ ] All existing behavior is unchanged (same test outcomes as before, just via the extracted function) and all existing tests still pass
- [ ] New direct unit tests for `isOverdue` exist

**Verify:** `npx ng test maia-props` (from `libs/maia-ui-workspace`) → `Test Files 1 passed (1)`, `Tests 33 passed (33)` (29 existing + 4 new)

**Steps:**

- [ ] **Step 1: Insert the `isOverdue` function**

In `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts`, find:

```ts
function todayIsoString(): string {
    return DateTime.local().toISODate()!;
}


export function filterProperties(
```

and replace it with:

```ts
function todayIsoString(): string {
    return DateTime.local().toISODate()!;
}


export function isOverdue(reviewDate: string | null): boolean {

    return reviewDate !== null && reviewDate < todayIsoString();

}


export function filterProperties(
```

- [ ] **Step 2: Use it in `filterProperties`**

In the same file, find:

```ts
    const normalizedFilter = nameFilter.trim().toLowerCase();
    const today = todayIsoString();

    return properties
        .filter(p => !overriddenOnly || p.isOverridden)
        .filter(p => !redundantOnly || p.isRedundant)
        .filter(p => !overdueOnly || (p.reviewDate !== null && p.reviewDate < today))
        .filter(p => normalizedFilter === '' || p.propertyName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.propertyName.localeCompare(b.propertyName));
```

and replace it with:

```ts
    const normalizedFilter = nameFilter.trim().toLowerCase();

    return properties
        .filter(p => !overriddenOnly || p.isOverridden)
        .filter(p => !redundantOnly || p.isRedundant)
        .filter(p => !overdueOnly || isOverdue(p.reviewDate))
        .filter(p => normalizedFilter === '' || p.propertyName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.propertyName.localeCompare(b.propertyName));
```

- [ ] **Step 3: Use it in `countOverdue`**

In the same file, find:

```ts
export function countOverdue(properties: PropertyResponseDto[]): number {

    const today = todayIsoString();
    return properties.filter(p => p.reviewDate !== null && p.reviewDate < today).length;

}
```

and replace it with:

```ts
export function countOverdue(properties: PropertyResponseDto[]): number {

    return properties.filter(p => isOverdue(p.reviewDate)).length;

}
```

- [ ] **Step 4: Update the spec file's import line**

In `projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts`, find:

```ts
import {buildPropsQueryParams, countOverdue, countOverridden, countRedundant, filterProperties, parsePropsFiltersFromParams} from './props-dashboard-filtering';
```

and replace it with:

```ts
import {buildPropsQueryParams, countOverdue, countOverridden, countRedundant, filterProperties, isOverdue, parsePropsFiltersFromParams} from './props-dashboard-filtering';
```

- [ ] **Step 5: Add tests for `isOverdue`**

In the same file, find:

```ts
});

describe('countOverridden', () => {
```

(this is the end of the `describe('filterProperties', ...)` block) and replace it with:

```ts
});

describe('isOverdue', () => {

    beforeEach(() => {
        vi.useFakeTimers();
        vi.setSystemTime(new Date('2026-09-26T12:00:00Z'));
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('returns true for a reviewDate in the past', () => {
        expect(isOverdue('2026-09-01')).toBe(true);
    });

    it('returns false for a null reviewDate', () => {
        expect(isOverdue(null)).toBe(false);
    });

    it('returns false for a reviewDate of today', () => {
        expect(isOverdue('2026-09-26')).toBe(false);
    });

    it('returns false for a reviewDate in the future', () => {
        expect(isOverdue('2026-10-01')).toBe(false);
    });

});

describe('countOverridden', () => {
```

- [ ] **Step 6: Run the tests**

Run: `npx ng test maia-props` (from `libs/maia-ui-workspace`)
Expected: `Test Files 1 passed (1)`, `Tests 33 passed (33)`

- [ ] **Step 7: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts
git commit -m "Extract isOverdue predicate in props dashboard filtering module"
```

---

### Task 2: Create the `PropsCard` component

**Goal:** A new standalone `PropsCard` component renders one property as a two-tier card (name/value/badges/actions on top, a Source/Modified-by/Modified/Review-date strip below), with a left accent stripe colored by the most severe active status.

**Files:**
- Create: `projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.ts`
- Create: `projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.html`
- Create: `projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.scss`

**Acceptance Criteria:**
- [ ] `property = input.required<PropertyResponseDto>()`; `edit`/`remove`/`history` each `output<PropertyResponseDto>()`
- [ ] Selector `maia-props-card`
- [ ] Top tier: property name, effective value (both wrapping, not truncating), badges, 3 action icon buttons with the same `aria-label`s and Remove-disabled-when-not-overridden behavior as the old table
- [ ] Badges: exactly one of Overridden/Default always shown; Redundant shown only if `isRedundant`; Overdue shown only if `isOverdue(property().reviewDate)` is true
- [ ] Bottom tier: a 4-column Source/Modified-by/Modified/Review-date strip, using the exact same `DatePipe` formats as the old table (`'EEE dd MMM yyyy, HH:mm:ss'` for Modified, `'EEE dd MMM yyyy'` for Review date); `lastModifiedByUsername`, `lastModifiedTimestamp`, `reviewDate` show `—` when null (Source does not get this treatment — matches the design doc)
- [ ] Left accent stripe color: overdue (error) > redundant (tertiary) > overridden (primary) > default (neutral), using the same `--mat-sys-*` tokens already used elsewhere in this file's sibling styles
- [ ] Host element has `[attr.data-testid]="'property-card-' + property().propertyName"` (replaces the old `'property-row-' + row.propertyName`)
- [ ] Project compiles

**Verify:** `npx ng build maia-props` (from `libs/maia-ui-workspace`) → build succeeds with no errors

**Steps:**

- [ ] **Step 1: Create the component class**

Create `projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.ts`:

```ts
import {Component, computed, input, output} from '@angular/core';
import {DatePipe} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {PropertyResponseDto} from '../../models/PropertyResponseDto';
import {isOverdue} from '../../state/props-dashboard-filtering';


@Component({
    selector: 'maia-props-card',
    imports: [DatePipe, MatButtonModule, MatIconModule],
    templateUrl: './props-card.html',
    styleUrl: './props-card.scss'
})
export class PropsCard {


    property = input.required<PropertyResponseDto>();

    edit = output<PropertyResponseDto>();
    remove = output<PropertyResponseDto>();
    history = output<PropertyResponseDto>();

    overdue = computed<boolean>(() => isOverdue(this.property().reviewDate));

    accentStatus = computed<'overdue' | 'redundant' | 'overridden' | 'default'>(() => {
        if (this.overdue()) {
            return 'overdue';
        }
        if (this.property().isRedundant) {
            return 'redundant';
        }
        if (this.property().isOverridden) {
            return 'overridden';
        }
        return 'default';
    });


    onEdit(): void {
        this.edit.emit(this.property());
    }


    onRemove(): void {
        this.remove.emit(this.property());
    }


    onHistory(): void {
        this.history.emit(this.property());
    }


}
```

- [ ] **Step 2: Create the template**

Create `projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.html`:

```html
<div class="card" [attr.data-testid]="'property-card-' + property().propertyName">
    <div class="accent" [class]="'accent-' + accentStatus()"></div>
    <div class="card-inner">
        <div class="tier-top">
            <div class="identity">
                <span class="prop-name">{{property().propertyName}}</span>
                <span class="prop-value">{{property().effectiveValue}}</span>
            </div>
            <div class="badges">
                @if (property().isOverridden) {
                    <span class="badge badge-overridden">Overridden</span>
                } @else {
                    <span class="badge badge-default">Default</span>
                }
                @if (property().isRedundant) {
                    <span class="badge badge-redundant">Redundant</span>
                }
                @if (overdue()) {
                    <span class="badge badge-overdue">Overdue</span>
                }
            </div>
            <div class="actions">
                <button mat-icon-button [attr.aria-label]="'Edit ' + property().propertyName" (click)="onEdit()">
                    <mat-icon>edit</mat-icon>
                </button>
                <button mat-icon-button [attr.aria-label]="'Remove ' + property().propertyName" [disabled]="!property().isOverridden" (click)="onRemove()">
                    <mat-icon>delete</mat-icon>
                </button>
                <button mat-icon-button [attr.aria-label]="'History ' + property().propertyName" (click)="onHistory()">
                    <mat-icon>history</mat-icon>
                </button>
            </div>
        </div>
        <div class="tier-bottom">
            <div class="meta-item">
                <div class="meta-label">Source</div>
                <div class="meta-value">{{property().sourceName}}</div>
            </div>
            <div class="meta-item">
                <div class="meta-label">Modified by</div>
                <div class="meta-value">{{property().lastModifiedByUsername ?? '—'}}</div>
            </div>
            <div class="meta-item">
                <div class="meta-label">Modified</div>
                <div class="meta-value">{{property().lastModifiedTimestamp ? (property().lastModifiedTimestamp | date: 'EEE dd MMM yyyy, HH:mm:ss') : '—'}}</div>
            </div>
            <div class="meta-item">
                <div class="meta-label">Review date</div>
                <div class="meta-value">{{property().reviewDate ? (property().reviewDate | date: 'EEE dd MMM yyyy') : '—'}}</div>
            </div>
        </div>
    </div>
</div>
```

- [ ] **Step 3: Create the stylesheet**

Create `projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.scss`:

```scss
:host {
    display: block;
}

.card {
    position: relative;
    overflow: hidden;
    border: 1px solid var(--mat-sys-outline-variant);
    border-radius: 0.5rem;
    background: var(--mat-sys-surface-container);
}

.accent {
    position: absolute;
    inset-block: 0;
    inset-inline-start: 0;
    width: 4px;
}

.accent-overdue {
    background: var(--mat-sys-error);
}

.accent-redundant {
    background: var(--mat-sys-tertiary);
}

.accent-overridden {
    background: var(--mat-sys-primary);
}

.accent-default {
    background: var(--mat-sys-outline-variant);
}

.card-inner {
    padding: 0.875rem 1rem 0.875rem 1.25rem;
}

.tier-top {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 0.625rem;
}

.identity {
    display: flex;
    flex-direction: column;
    gap: 0.125rem;
    min-width: 0;
    flex: 1;
}

.prop-name {
    font-weight: 500;
    overflow-wrap: anywhere;
}

.prop-value {
    font-size: 0.8125rem;
    color: var(--mat-sys-on-surface-variant);
    overflow-wrap: anywhere;
}

.badges {
    display: flex;
    gap: 0.375rem;
    flex-shrink: 0;
}

.badge {
    display: inline-flex;
    align-items: center;
    font-size: 0.6875rem;
    font-weight: 500;
    padding: 0.125rem 0.5rem;
    border-radius: 9999px;
    white-space: nowrap;
}

.badge-overridden {
    background: var(--mat-sys-primary-container);
    color: var(--mat-sys-on-primary-container);
}

.badge-default {
    background: var(--mat-sys-surface-variant);
    color: var(--mat-sys-on-surface-variant);
}

.badge-redundant {
    background: var(--mat-sys-tertiary-container);
    color: var(--mat-sys-on-tertiary-container);
}

.badge-overdue {
    background: var(--mat-sys-error-container);
    color: var(--mat-sys-on-error-container);
}

.actions {
    display: flex;
    flex-shrink: 0;
}

.tier-bottom {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 0.5rem 1.25rem;
    padding-top: 0.625rem;
    border-top: 1px solid var(--mat-sys-outline-variant);
}

.meta-label {
    font-size: 0.6875rem;
    text-transform: uppercase;
    letter-spacing: 0.04em;
    color: var(--mat-sys-outline);
}

.meta-value {
    font-size: 0.8125rem;
    color: var(--mat-sys-on-surface);
    overflow-wrap: anywhere;
}

@media (max-width: 760px) {
    .tier-bottom {
        grid-template-columns: repeat(2, 1fr);
    }
}
```

- [ ] **Step 4: Verify the build**

Run: `npx ng build maia-props` (from `libs/maia-ui-workspace`)
Expected: `Built @maia/maia-props` with no TypeScript errors

Also run: `npx ng test maia-props` → 33/33 still pass (no spec file exists for `elastic-index`, the component this mirrors — matching that established pattern, don't add one for `PropsCard` either)

- [ ] **Step 5: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.html libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/components/props-card/props-card.scss
git commit -m "Add PropsCard component"
```

---

### Task 3: Replace the table with `PropsCard` on the page

**Goal:** `props-dashboard-page` renders `@for (property of store.visibleProperties(); ...) { <maia-props-card ... /> }` instead of a `mat-table`, with the exact same edit/remove/history wiring as before.

**Files:**
- Modify: `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts`
- Modify: `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html`
- Modify: `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.scss`

**Acceptance Criteria:**
- [ ] `MatTableModule` and `DatePipe` are no longer imported or referenced in `props-dashboard-page.ts`; `displayedColumns` is removed
- [ ] `PropsCard` is imported and added to the component's `imports` array
- [ ] Template renders one `<maia-props-card>` per visible property inside a `.props-card-list` container with `data-testid="props-dashboard-card-list"` (replacing `data-testid="props-dashboard-table"`)
- [ ] `onEdit`/`onRemove`/`onHistory` page methods are unchanged (same `(row: PropertyResponseDto)` signature) — only wired via the card's output events now instead of inline table-row buttons
- [ ] `.property-value-cell` and `.mat-column-actions` (table-only rules) are removed from `props-dashboard-page.scss`; a `.props-card-list` rule (flex column, gap) is added
- [ ] Project compiles

**Verify:** `npx ng build maia-props` (from `libs/maia-ui-workspace`) → build succeeds with no errors; `npx ng test maia-props` → 33/33 pass; then the manual/self-review check in Step 4

**Steps:**

- [ ] **Step 1: Update the page component's imports and decorator**

In `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts`, find:

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
```

and replace it with:

```ts
import {Component, effect, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
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
import {PropsCard} from './components/props-card/props-card';
```

Then find:

```ts
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
```

and replace it with:

```ts
@Component({
    selector: 'maia-props-dashboard-page',
    templateUrl: './props-dashboard-page.html',
    styleUrl: './props-dashboard-page.scss',
    imports: [MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, PropsCard],
    providers: [PropsApiService, PropsDashboardStore]
})
export class PropsDashboardPage implements OnInit {


    readonly store = inject(PropsDashboardStore);

    private route = inject(ActivatedRoute);
```

- [ ] **Step 2: Replace the table with the card list in the template**

In `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html`, find this entire block (the current `@else` branch's contents):

```html
    <table mat-table [dataSource]="store.visibleProperties()" data-testid="props-dashboard-table">

        <ng-container matColumnDef="propertyName">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let row">{{row.propertyName}}</td>
        </ng-container>

        <ng-container matColumnDef="effectiveValue">
            <th mat-header-cell *matHeaderCellDef>Effective Value</th>
            <td mat-cell *matCellDef="let row" class="property-value-cell">{{row.effectiveValue}}</td>
        </ng-container>

        <ng-container matColumnDef="isOverridden">
            <th mat-header-cell *matHeaderCellDef>Overridden</th>
            <td mat-cell *matCellDef="let row" class="property-overridden-badge">{{row.isOverridden ? 'Overridden' : 'Default'}}</td>
        </ng-container>

        <ng-container matColumnDef="isRedundant">
            <th mat-header-cell *matHeaderCellDef>Redundant</th>
            <td mat-cell *matCellDef="let row">{{row.isRedundant ? 'Redundant' : ''}}</td>
        </ng-container>

        <ng-container matColumnDef="sourceName">
            <th mat-header-cell *matHeaderCellDef>Source</th>
            <td mat-cell *matCellDef="let row">{{row.sourceName}}</td>
        </ng-container>

        <ng-container matColumnDef="lastModifiedByUsername">
            <th mat-header-cell *matHeaderCellDef>Last Modified By</th>
            <td mat-cell *matCellDef="let row">{{row.lastModifiedByUsername}}</td>
        </ng-container>

        <ng-container matColumnDef="lastModifiedTimestamp">
            <th mat-header-cell *matHeaderCellDef>Last Modified</th>
            <td mat-cell *matCellDef="let row">{{row.lastModifiedTimestamp | date: 'EEE dd MMM yyyy, HH:mm:ss'}}</td>
        </ng-container>

        <ng-container matColumnDef="reviewDate">
            <th mat-header-cell *matHeaderCellDef>Review Date</th>
            <td mat-cell *matCellDef="let row">{{row.reviewDate | date: 'EEE dd MMM yyyy'}}</td>
        </ng-container>

        <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let row">
                <button mat-icon-button [attr.aria-label]="'Edit ' + row.propertyName" (click)="onEdit(row)">
                    <mat-icon>edit</mat-icon>
                </button>
                <button mat-icon-button [attr.aria-label]="'Remove ' + row.propertyName" [disabled]="!row.isOverridden" (click)="onRemove(row)">
                    <mat-icon>delete</mat-icon>
                </button>
                <button mat-icon-button [attr.aria-label]="'History ' + row.propertyName" (click)="onHistory(row)">
                    <mat-icon>history</mat-icon>
                </button>
            </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;" [attr.data-testid]="'property-row-' + row.propertyName"></tr>

    </table>
```

and replace it with:

```html
    <div class="props-card-list" data-testid="props-dashboard-card-list">
        @for (property of store.visibleProperties(); track property.propertyName) {
            <maia-props-card
                [property]="property"
                (edit)="onEdit($event)"
                (remove)="onRemove($event)"
                (history)="onHistory($event)" />
        }
    </div>
```

(The `@if (store.isLoading())` / `@else if (store.error())` branches above it, and everything in the file above that `@if`, are unchanged.)

- [ ] **Step 3: Update the stylesheet**

In `projects/maia-props/src/lib/props-dashboard/props-dashboard-page.scss`, find:

```scss
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

and replace it with:

```scss
.props-card-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}
```

- [ ] **Step 4: Manually verify (or thoroughly self-review if no live environment is reachable)**

If a running showcase app is reachable: navigate to `/props-dashboard` and confirm:
- Properties render as cards, one per row, instead of a table
- Each card shows the correct badges (exactly one of Overridden/Default, plus Redundant/Overdue when applicable) and the correct accent stripe color
- Edit/Remove/History buttons work exactly as before (Remove still disabled for non-overridden properties)
- The Source/Modified-by/Modified/Review-date strip shows the same data as the old table columns, with `—` for missing modified-by/modified/review-date

If no live environment is reachable in this session (same constraint as prior plans in this codebase — no `ng serve` app target, real data requires a running Spring Boot backend), do a careful line-by-line self-review instead: re-read the final `props-card.ts`/`.html` together with `props-dashboard-page.ts`/`.html` and trace the `[property]`/`(edit)`/`(remove)`/`(history)` bindings back to the exact page methods, confirming no typos and that clicking each action emits the right property to the right handler.

- [ ] **Step 5: Run build and tests**

Run: `npx ng build maia-props` (from `libs/maia-ui-workspace`)
Expected: `Built @maia/maia-props` with no TypeScript errors

Run: `npx ng test maia-props` (from `libs/maia-ui-workspace`)
Expected: `Test Files 1 passed (1)`, `Tests 33 passed (33)`

- [ ] **Step 6: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.scss
git commit -m "Replace props dashboard table with PropsCard list"
```

---

## Self-Review Notes

- **Spec coverage:** All design-doc sections (new component, badges, accent stripe, derived `isOverdue`, page changes, out-of-scope note) map to Tasks 1-3.
- **Placeholder scan:** none found — all steps contain complete code.
- **Type consistency:** `isOverdue(reviewDate: string | null): boolean` (Task 1) → `PropsCard.overdue`/`accentStatus` computeds (Task 2) → `<maia-props-card [property] (edit) (remove) (history)>` bindings (Task 3) use identical names throughout; `onEdit`/`onRemove`/`onHistory` signatures on the page are untouched end to end.
- **User verification requirement scan:** Original request ("change table to cards", "show me mockups") does not ask for human sign-off — answer is NO. No dedicated verification task created; Task 3 includes a manual-or-self-review step per this project's standing frontend-change convention, not as a formal gate.
