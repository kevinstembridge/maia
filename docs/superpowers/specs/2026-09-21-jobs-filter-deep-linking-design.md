# Jobs Filter Deep Linking — Design

## Goal
Make the filters on `jobs-dashboard-page` (jobName substring) and `job-history-page` (jobName, status, from/to date) readable from, and reflected back into, the URL's query string, so a filtered view is bookmarkable/shareable and the back/forward-safe URL always matches the visible filter state.

## Background
- `JobsDashboardPageComponent` filters purely client-side: `JobsDashboardStore.nameFilter` drives a `computed()` (`filterAndSortByName` in `jobs-filtering.ts`) over already-fetched `jobStates`. No API call is triggered by a filter change.
- `JobHistoryPageComponent` filters server-side: each `onJobNameFilterChanged`/`onStatusFilterChanged`/`onDateRangeChanged` call on `JobHistoryStore` patches state, resets `pageIndex` to 0, and triggers a fresh `search()` API call. `init()` (called from `ngOnInit`) separately loads `availableJobNames` and does the first `search()`.
- Both modules already have a convention of pure, unit-tested filter/format functions colocated with the feature (`jobs-filtering.ts` + `.spec.ts`, `job-history-filtering.ts` + `.spec.ts`), with thin components/stores wired around them. This design extends that same convention rather than introducing a new abstraction.
- No existing code in `maia-ui-workspace` uses `ActivatedRoute`/`Router` query params — `RouterLink` is already used in `jobs-dashboard-page.component.ts` for static navigation, so adding `Router`/`ActivatedRoute` injection to these page components is consistent with existing coupling to Angular Router.
- Pagination (`pageIndex`/`pageSize`) on the history page is explicitly out of scope — only the actual filter fields go in the URL.

## Behavior

### Param shape
- `jobs-dashboard` (`?jobName=<substring>`): param omitted from the URL when the filter is empty.
- `jobs-history` (`?jobName=&status=&from=&to=`): each param omitted when its corresponding store field is `null`/unset. `status` is one of `RUNNING`/`SUCCESS`/`FAILED` (matches `HistoryStatusFilter`). `from`/`to` are plain `yyyy-MM-dd` dates (day-level, no time/timezone in the URL).

### Sync mechanism (both pages)
- **URL → state (on load):** component constructor reads `route.snapshot.queryParamMap` once and parses it via a new pure function into initial filter values.
- **State → URL (on change):** a signal `effect()`, set up in the component's constructor (injection context), watches the relevant store signal(s) and calls `router.navigate([], {relativeTo: route, queryParams, replaceUrl: true})` whenever they change. `replaceUrl: true` so filtering/typing doesn't pollute browser history — back button doesn't step through every keystroke or selection change.
- The effect and the initial parse both go through the *same* pure serialize function, so the URL the effect produces from a given state is exactly what the initial parse would read back (round-trip consistency), which is what the unit tests below verify.

### `jobs-dashboard-page.component.ts`
- `jobs-filtering.ts` gains two pure functions:
  - `parseNameFilterFromParams(params: ParamMap): string` — returns `params.get('jobName') ?? ''`.
  - `buildNameFilterQueryParams(nameFilter: string): Params` — `{jobName: nameFilter || null}` (Angular's `router.navigate` drops `null`-valued params from the URL).
- Component constructor: `inject(ActivatedRoute)`/`inject(Router)`; seed via `store.onNameFilterChanged(parseNameFilterFromParams(route.snapshot.queryParamMap))` — cheap, no API call since filtering is local. `effect(() => router.navigate([], {relativeTo: route, queryParams: buildNameFilterQueryParams(store.nameFilter()), replaceUrl: true}))`.
- Template/`onFilterInput` unchanged.

### `job-history-page.component.ts` + `job-history-store.ts`
- `job-history-filtering.ts` gains:
  - `parseHistoryFiltersFromParams(params: ParamMap): {jobNameFilter: string | null, statusFilter: HistoryStatusFilter, fromDate: string | null, toDate: string | null}` — reads `jobName`/`status`/`from`/`to`; `from`/`to` are converted from `yyyy-MM-dd` to full start-of-day/end-of-day UTC ISO strings using the same Luxon logic currently private in the component (`toStartOfDayIso`/`toEndOfDayIso`), moved here so both the initial-parse path and the existing datepicker `(dateChange)` handlers share one implementation. Invalid/missing `status` values fall back to `null`.
  - `buildHistoryFilterQueryParams(jobNameFilter, statusFilter, fromDate, toDate): Params` — `fromDate`/`toDate` (full ISO strings) converted back to `yyyy-MM-dd` via `DateTime.fromISO(...).toISODate()`; any `null` field omitted (mapped to `null` in the `Params` object).
- `JobHistoryStore` gains `applyInitialFilters(filters: {jobNameFilter, statusFilter, fromDate, toDate}): void` — `patchState` only, no `search()` call, so `init()` (called right after from `ngOnInit`) performs exactly one `search()` with the already-correct filters instead of one wasted search per seeded field.
- Component constructor: seed via `store.applyInitialFilters(parseHistoryFiltersFromParams(route.snapshot.queryParamMap))`. `ngOnInit` still calls `store.init()` unchanged. `effect(() => router.navigate([], {relativeTo: route, queryParams: buildHistoryFilterQueryParams(store.jobNameFilter(), store.statusFilter(), store.fromDate(), store.toDate()), replaceUrl: true}))`.
- `toStartOfDayIso`/`toEndOfDayIso` are removed from the component (now live in `job-history-filtering.ts`); `onFromDateChanged`/`onToDateChanged` call the relocated functions.

## Out of scope
- Pagination (`pageIndex`/`pageSize`) is not reflected in the URL.
- No debouncing of URL writes — `jobs-dashboard`'s `nameFilter` updates the URL on every keystroke via `replaceUrl: true`; this matches the existing lack of debounce on the filter itself (it's a pure client-side `computed()`, not an API call) and avoids adding a new debounce mechanism for a `replaceState`-only cost.
- No changes to `RouterLink` navigation between the two pages (e.g. "View History" button) — filters don't carry over from one page to the other.
- No new component-level (TestBed) tests — neither page has existing component tests; only the new pure functions get unit tests, consistent with current coverage.

## Verification
- `jobs-filtering.spec.ts`: `parseNameFilterFromParams` (present/absent param), `buildNameFilterQueryParams` (non-empty → param present, empty string → `null`).
- `job-history-filtering.spec.ts`: `parseHistoryFiltersFromParams` (all params present; all absent; invalid `status` value falls back to `null`; `from`/`to` correctly expand to start/end-of-day UTC ISO) and `buildHistoryFilterQueryParams` (round-trips back to the same `yyyy-MM-dd` given a parsed date; all-null state → all params `null`).
- Manual: in the showcase app, set filters on each page, confirm the URL updates; reload the page with a filtered URL and confirm the filter/results reflect it on load; confirm back button doesn't step through per-keystroke/per-selection URL changes (only lands on prior distinct navigations, e.g. between pages).

## Unresolved questions
None.
