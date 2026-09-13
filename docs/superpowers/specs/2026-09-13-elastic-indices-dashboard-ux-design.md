# Elastic Indices Dashboard UX Improvements — Design

## Goal
Improve the UX of the Elasticsearch indices dashboard (`libs/maia-ui-workspace/projects/maia-elasticsearch`) without changing the backend API or `EsIndexStateDto` shape: a real card grid, a name filter, a scannable health indicator, a count summary, and loading/empty/error states.

## Background
Current state (`elastic-indices-page.html`/`.ts`, `elastic-indices-page-store.ts`, `elastic-index.html`/`.ts`):
- The page renders a flat vertical `@for` of `<maia-elastic-index>` — no grid.
- Only filter is a `hideSystemIndices` slide-toggle; no text filter exists.
- Each card shows `health?.status` and `summary?.isActiveVersion` as raw `<h5>` text — no color coding, no visual hierarchy.
- `isLoading` exists in store state but is never read by the template. There is no empty state and no error state — fetch errors are swallowed to `console.error` only.
- No count/summary of what's currently displayed.
- `EsIndexStateDto` = `{ indexName, indexExists, summary: { indexName, description, isActiveVersion }, health: { indexName, status } }`. This design stays entirely within these fields — no backend/DTO changes.
- Tailwind utility classes are already available and used in this template (`class="mt-5"`), so the grid layout uses Tailwind classes rather than a new stylesheet.
- No spec files currently exist for this lib. Workspace-wide, only two spec files exist at all (`maia-ui.spec.ts` — boilerplate, and `date-time-functions.spec.ts` — pure utility functions), so the testing convention here is minimal: pure-logic tests only, no component/TestBed tests.

## Behavior

### Store (`elastic-indices-page-store.ts`)

State additions:
```ts
type ElasticIndicesPageState = {
    hideSystemIndices: boolean;
    indexStateDtos: EsIndexStateDto[];
    isLoading: boolean;
    nameFilter: string;      // new, default ''
    error: string | null;    // new, default null
};
```

Computed signals:
- `toggleFilteredIndexStateDtos` — `indexStateDtos` filtered by `hideSystemIndices` only (existing logic, extracted). This is the "browsing universe": its length is the denominator for the count summary.
- `visibleIndexStateDtos` — `toggleFilteredIndexStateDtos`, further filtered by case-insensitive substring match of `nameFilter` against `indexName` only (not description), then sorted alphabetically by `indexName`. This is what's rendered as cards.
- `statusCounts` — a `Record<string, number>` grouping `visibleIndexStateDtos()` by `health.status` (matched case-insensitively; the raw status string, lowercased, is used as both the record key and the color-mapping key described below). Statuses with a count of 0 are simply absent from the record (nothing to iterate in the template).

Methods:
- `onNameFilterChanged(value: string): void` — `patchState(store, {nameFilter: value})` directly, no debounce. Unlike `fetchAllIndices` (which debounces to avoid excessive API calls), this filters data already held in memory, so debouncing would only add latency with no benefit.
- `fetchAllIndices`'s `tapResponse` error handler now also does `patchState(store, {error: 'Failed to load indices.'})` (in addition to `console.error`); the `next` handler does `patchState(store, {error: null, ...})` alongside the existing fields.
- `retryFetch(): void` — calls `this.fetchAllIndices()`. Exists as a distinctly-named method so the template's retry button reads as "retry an error", not "trigger the load method" — same underlying rxMethod.

### Page template (`elastic-indices-page.html`)

Layout, top to bottom:
1. Header row: page title on the left, count summary on the right.
   - Count summary text: when `visibleIndexStateDtos().length === toggleFilteredIndexStateDtos().length` (name filter isn't narrowing anything), render `"<N> indices · <status> <count>, ..."` (e.g. `"12 indices · 8 green · 3 yellow · 1 red"`). When the filter does narrow the set, render `"<visible> of <total> indices · ..."` (e.g. `"3 of 12 indices · 2 green · 1 red"`). Status segments are omitted when their count is 0. Segments are always ordered `green`, `yellow`, `red` first (matching the fixed color mapping below), then any other status keys alphabetically — so the order is stable regardless of iteration order over `statusCounts`.
2. Filter row: a `matInput` text field (`(input)` → `store.onNameFilterChanged($event.target.value)`, placeholder "Filter indices...") next to the existing `mat-slide-toggle` for "Hide system indices". **This row stays visible in all states below** (loading, error, empty) — only the content area under it changes.
3. Content area, in priority order:
   - `isLoading` → a centered `mat-spinner`.
   - else `error` → inline banner with the error message and a "Retry" button wired to `store.retryFetch()`.
   - else `visibleIndexStateDtos().length === 0` → "No indices match your filter." message (this also naturally covers "toggle hides everything").
   - else → the card grid: a Tailwind grid container (`grid grid-cols-[repeat(auto-fill,minmax(260px,1fr))] gap-3`) of `<maia-elastic-index>` components, same inputs/outputs as today.

### Card component (`elastic-index.html`/`.ts`)

- Header row: index name (unchanged) + a colored status dot, shown only when `index().indexExists` is true (nothing to show health for otherwise).
  - Dot is a small `span` with `background-color` mapped from `health.status.toLowerCase()`: `green` → `#4caf50`, `yellow` → `#fbc02d`, `red` → `#d32f2f`, anything else → `#9e9e9e` (gray, defensive fallback).
  - No visible text label next to the dot — a native `title` attribute carries the raw status string, so it's still available on hover/inspection without cluttering the card.
- "Active version" badge: shown only when `index().indexExists && index().summary?.isActiveVersion`. When the index exists but isn't the active version, no badge is shown — the existing "Set as Active Version..." button already communicates that state, so a badge would be redundant.
- Description text and both action buttons (`Create...` / `Set as Active Version...`) are unchanged in behavior, only restyled to fit the new card shell.

## Out of scope
- No backend/API or `EsIndexStateDto` changes (confirmed with user) — no doc count, size, shard count, etc.
- No manual sort control — sort is always alphabetical by `indexName` (confirmed with user; explicitly *not* severity-based).
- No debounce on the name filter (client-side data, not an API call).
- No component-level or TestBed tests — matches existing lib convention of pure-logic-only specs.
- Filter does not match against `description`, only `indexName`.

## Verification
- Add `elastic-indices-page-store.spec.ts` covering, with plain in-memory `EsIndexStateDto[]` fixtures (no TestBed/HTTP mocking needed since these are computed signals over injected state):
  - `toggleFilteredIndexStateDtos` / `visibleIndexStateDtos`: system-index filtering, name-filter substring matching (case-insensitive, name-only), alphabetical sort.
  - `statusCounts`: correct grouping, zero-count statuses absent.
- Manually run the showcase app against the local ES/dev stack and confirm:
  - Grid renders responsively at different widths.
  - Typing in the filter narrows the grid and updates the count summary to the "X of Y" form; clearing it reverts to the plain "Y indices" form.
  - Toggling "Hide system indices" updates both the grid and the denominator in the count summary.
  - Simulate a fetch error (e.g. stop the backend) → banner + Retry appears; clicking Retry re-fetches.
  - An empty filter result shows the "No indices match" message.
  - Status dot colors match `green`/`yellow`/`red`; "Active version" badge appears only for the active, existing index.

## Unresolved questions
None.
