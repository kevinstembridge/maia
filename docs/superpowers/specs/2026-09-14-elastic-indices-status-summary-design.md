# Elastic Indices Status Summary Tiles

## Problem

The elastic-indices dashboard (`@maia/maia-elasticsearch`) currently shows a small
text summary in the top-right of the page, e.g. `"15 indices · 10 green · 2 yellow · 3 red"`,
built by `buildIndexCountSummary()` in `elastic-indices-filtering.ts`. It's easy to miss,
doesn't call out indices that don't exist yet, and gives no way to jump straight to the
indices in a given status.

## Goal

Replace the text summary with a row of clickable stat tiles — one per status
(Green / Yellow / Red / Not created) — showing a count for each. Clicking a tile filters
the card grid below to that status; clicking the active tile again clears the filter.

## Data model changes (`elastic-indices-filtering.ts`)

Introduce a `DisplayStatus` type covering every state an index can be in, including ones
that haven't been created yet (which have no ES health status):

```ts
export type DisplayStatus = 'green' | 'yellow' | 'red' | 'not-created';

export const STATUS_TILE_ORDER: DisplayStatus[] = ['green', 'yellow', 'red', 'not-created'];

export const STATUS_COLORS: Record<DisplayStatus, string> = {
    green: '#4caf50',
    yellow: '#fbc02d',
    red: '#d32f2f',
    'not-created': '#9e9e9e',
};

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

Removed: `countByStatus()`, `buildIndexCountSummary()`, and the `STATUS_DISPLAY_ORDER` constant
(superseded by `STATUS_TILE_ORDER`).

`elastic-index.ts` (the card component) currently defines its own duplicate
`STATUS_COLORS`/`UNKNOWN_STATUS_COLOR` map for the status dot. It will import the shared
`STATUS_COLORS` from `elastic-indices-filtering.ts` instead, and derive its dot color via
`deriveDisplayStatus()`, returning no dot at all when the status is `'not-created'`
(unchanged visual behavior — the card already shows no dot for indices that don't exist).

## Store changes (`elastic-indices-page-store.ts`)

Add `statusFilter: DisplayStatus | null` to state (default `null`).

Rework the computed chain so status counts reflect the current name/toggle filters
*before* the status filter narrows the grid:

```
toggleFilteredIndexStateDtos        (existing — system-index toggle)
        ↓
nameFilteredIndexStateDtos          (existing filterAndSortByName, renamed from visibleIndexStateDtos)
        ↓                     ↘
statusCounts                  visibleIndexStateDtos = filterByStatus(nameFilteredIndexStateDtos, statusFilter)
= countByDisplayStatus(nameFilteredIndexStateDtos)
```

`visibleIndexStateDtos` (the name used by the template for the card grid) keeps its name
but is now computed one step further down the chain.

New method:

```ts
onStatusFilterToggled(status: DisplayStatus): void {
    patchState(store, {statusFilter: store.statusFilter() === status ? null : status});
}
```

Remove the `countSummary` computed value (no longer used).

## Template & styling (`elastic-indices-page.html` / `.scss`)

Replace the `.toolbar`/`.count` markup with a row of 4 tiles, in the same position
(above the filter bar):

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

`statusTileOrder` (from `STATUS_TILE_ORDER`), `statusColors` (from `STATUS_COLORS`), and a
small `statusLabels` map (`{green: 'Green', yellow: 'Yellow', red: 'Red', 'not-created': 'Not created'}`)
are exposed as plain fields on `ElasticIndicesPage`.

Styling: tiles use `display: flex; flex-wrap: wrap; gap: 0.5rem` on the container so they
wrap on narrow screens. Each tile has a `min-width`, border using `--mat-sys-outline-variant`,
and a `.active` state using `--mat-sys-primary-container` background / `--mat-sys-primary`
border, consistent with the `--mat-sys-*` token convention already used elsewhere on this
page (see `elastic-indices-page.scss`).

No changes needed to the card grid itself — it already renders `store.visibleIndexStateDtos()`,
which now reflects the status filter automatically.

## Testing

`elastic-indices-filtering.spec.ts` currently tests `countByStatus`/`buildIndexCountSummary`.
Replace those tests with coverage for `deriveDisplayStatus`, `countByDisplayStatus`, and
`filterByStatus`, including:
- an index with `indexExists: false` derives `'not-created'` regardless of any health data
- an existing index derives its lowercased health status
- `filterByStatus(indices, null)` returns all indices unchanged
- `filterByStatus` with a status returns only matching indices

## Out of scope

- Multi-select tiles (deferred — one active status at a time only, per requirements)
- Any change to the jobs dashboard (this is elastic-indices only)
- Keeping the old text summary alongside the tiles (tiles fully replace it)
