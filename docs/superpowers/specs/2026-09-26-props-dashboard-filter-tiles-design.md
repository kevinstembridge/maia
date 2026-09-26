# maia-props dashboard: filter tiles (replace slide-toggles with count-buttons)

## Goal
Replace the three `mat-slide-toggle` quick filters ("Overridden only", "Redundant only", "Overdue only") with clickable count-tile buttons, styled like maia-elasticsearch's status tiles, and restyle the existing "X of Y properties" text as a matching non-clickable total tile.

## Reference
`libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.html` + `.scss` (`.status-tiles`, `.status-tile`, `.status-tile-count`, `.status-tile-label`, `.active` state).

## Layout
Two rows (matching the elasticsearch reference):
1. `.props-filter-tiles` — a flex row of 4 tiles: **Total** (non-clickable), **Overridden**, **Redundant**, **Overdue**.
2. Existing `.props-dashboard-toolbar` row — name filter input + "Add override" button (slide-toggles removed).

## Tile behavior
- The 3 filter tiles are independently clickable buttons that flip their respective boolean (`overriddenOnly`/`redundantOnly`/`overdueOnly`) on click — same AND-combinable multi-select semantics as the current slide-toggles, just a different control.
- Active tile: `.active` class + `[attr.aria-pressed]="<bool>"`.
- Total tile is not clickable (`type="button"` omitted or disabled styling, matching elasticsearch's `.status-tile-total { cursor: default; }`).

## Tile counts
Each filter tile's count reflects that condition **alone**, ignoring the other two toggles and the name filter (i.e., "how many of all loaded properties are Overridden", full stop) — not a faceted/narrowing count. Backed by:
- `overriddenCount` — already exists in `PropsDashboardStore` (currently unused; now wired up).
- `redundantCount` — new computed, backed by a new `countRedundant(properties)` function in `props-dashboard-filtering.ts`, mirroring the existing `countOverridden`.
- `overdueCount` — new computed, backed by a new `countOverdue(properties)` function, reusing the existing private `todayIsoString()` helper for the same "reviewDate < today" comparison already used in `filterProperties`.

The Total tile keeps its current text and behavior — `{{store.visibleProperties().length}} of {{store.properties().length}}` with `aria-live="polite"` — just restyled as a tile with label "Total".

## Labels
Short, matching the tile style: "Total", "Overridden", "Redundant", "Overdue" (dropping "only").

## Styling
Copy the `.status-tile*` CSS rules from `elastic-indices-page.scss` into `props-dashboard-page.scss` under `.props-filter-tile*` names — border, border-radius, padding, cursor, and the `.active` state's `border-color`/`background-color`/`color` using the same `--mat-sys-primary`/`--mat-sys-primary-container`/`--mat-sys-on-primary-container` tokens. No color-dot indicator (that was for per-status color-coding in elasticsearch; not applicable here — all three props filters are equivalent in kind).

## Code removal
`MatSlideToggleModule` and `MatSlideToggleChange` are dropped from `props-dashboard-page.ts` (import and `imports: [...]` array) once no slide-toggles remain. The three `onXOnlyToggled(change: MatSlideToggleChange)` handlers become parameterless click handlers that read-then-flip the store's current boolean, e.g. `onOverriddenOnlyToggled(): void { this.store.onOverriddenOnlyToggled(!this.store.overriddenOnly()); }`. The store's existing `onOverriddenOnlyToggled(value: boolean)`/etc. methods are unchanged (still take an explicit boolean) — only the page-level click handlers change shape.

## Files touched
- `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts` + `.spec.ts` — new `countRedundant`/`countOverdue` functions + tests
- `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-store.ts` — new `redundantCount`/`overdueCount` computeds
- `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts` — new click handlers, drop slide-toggle imports/handlers
- `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html` — restructured two-row markup
- `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.scss` — new tile styles

## Out of scope
No backend changes. No change to the underlying filtering/URL-sync logic (`filterProperties`, `parsePropsFiltersFromParams`, `buildPropsQueryParams` are untouched) — only how the three booleans are toggled and how their counts are surfaced.
