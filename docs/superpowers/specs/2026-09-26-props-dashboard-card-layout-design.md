# maia-props dashboard: card layout (replace table with per-record cards)

## Goal
Replace the `mat-table` on the props dashboard with one card per record (Variant 2 / "two-tier card" from the reviewed mockups: https://claude.ai/artifact/81QkWMeQrD7V4v9XduFVe7), stacked vertically, one per row.

## New component: `PropsCard`
Location: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/components/props-card/` (`props-card.ts`/`.html`/`.scss`), mirroring the existing `maia-elastic-index` component in maia-elasticsearch (same signal-based `input.required`/`output` pattern, same "one component per grid/list item" convention).

- `property = input.required<PropertyResponseDto>()`
- `edit = output<PropertyResponseDto>()`, `remove = output<PropertyResponseDto>()`, `history = output<PropertyResponseDto>()`
- Selector: `maia-props-card`

### Top tier
- Property name, then effective value, on the same line as today conceptually but now full-width (no forced truncation — both wrap naturally instead of ellipsis-truncating, since the card isn't squeezed into a 320px table column anymore).
- Badges, in this order, each shown conditionally:
  - Exactly one of **Overridden** (`isOverridden` true) or **Default** (`isOverridden` false) — always exactly one shown.
  - **Redundant** — shown only when `isRedundant` is true.
  - **Overdue** — shown only when the property is overdue (see below).
- The 3 action icon buttons (Edit/Remove/History), same `aria-label`s and disabled-when-not-overridden behavior on Remove as today.

### Bottom tier
A labelled 4-column strip: **Source** / **Modified by** / **Modified** / **Review date**, using the exact same `DatePipe` formats currently on the table (`'EEE dd MMM yyyy, HH:mm:ss'` for Modified, `'EEE dd MMM yyyy'` for Review date). Any null value (`lastModifiedByUsername`, `lastModifiedTimestamp`, `reviewDate`) renders as `—` instead of blank — a blank table cell reads fine, a blank value under a label in a card reads like a bug.

### Left accent stripe
A 4px colored stripe indicating the single most severe active status, most severe wins:
**Overdue** (error/red) > **Redundant** (warn/amber) > **Overridden** (primary/blue) > **Default** (neutral/grey).

### "Overdue" is derived, not a field
`PropertyResponseDto` has no `isOverdue` field — it's derived from `reviewDate` exactly like the existing `overdueOnly` filter and `overdueCount` tile already do. Extract the shared predicate into a new exported function in `props-dashboard-filtering.ts`:

```ts
export function isOverdue(reviewDate: string | null): boolean {
    return reviewDate !== null && reviewDate < todayIsoString();
}
```

Refactor `filterProperties`'s `overdueOnly` branch and `countOverdue` to call this instead of duplicating the `reviewDate !== null && reviewDate < today` predicate inline (this predicate currently exists in 2 places; extracting it removes that duplication, flagged as a minor finding in an earlier review). `PropsCard` calls `isOverdue(this.property().reviewDate)` in a computed to decide the Overdue badge/accent.

## Page changes
`props-dashboard-page.ts`/`.html`/`.scss`:
- Drop `MatTableModule` import and the `displayedColumns` field.
- Drop `DatePipe` from the page component's `imports` (moves to `PropsCard`, the only place it's used now).
- Replace the `<table mat-table>` block with:
  ```html
  @for (property of store.visibleProperties(); track property.propertyName) {
      <maia-props-card
          [property]="property"
          (edit)="onEdit($event)"
          (remove)="onRemove($event)"
          (history)="onHistory($event)" />
  }
  ```
- `onEdit`/`onRemove`/`onHistory` methods on the page keep their exact current signatures (`(row: PropertyResponseDto)`) — only their call site changes, from a table row's inline button click to a card's output event.
- `props-dashboard-page.scss`: remove `.property-value-cell` and `.mat-column-actions` (table-only rules, no longer used); add a simple list-spacing rule for the card container (e.g. `.props-card-list { display: flex; flex-direction: column; gap: 8px; }`).
- Keep the same `data-testid` convention: the card list container gets `data-testid="props-dashboard-card-list"` (replacing `data-testid="props-dashboard-table"`), and each card gets `[attr.data-testid]="'property-card-' + property.propertyName"` (replacing `'property-row-' + row.propertyName`) — set on the card component's host or a wrapping element inside `props-card.html`.

## Out of scope
No change to the filter tiles, name filter, "Add override" button, dialogs, backend, or the underlying filter/count logic beyond the `isOverdue` extraction described above. No new component-level test file — this codebase has no precedent for testing dashboard page/card components directly (only the pure `props-dashboard-filtering.ts` functions are unit tested), so this stays consistent; the new `isOverdue` function gets tests in `props-dashboard-filtering.spec.ts` alongside the existing ones.
