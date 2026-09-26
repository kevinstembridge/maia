# maia-props dashboard: overdue quick filter

## Goal
Add a third quick-filter toggle to the props dashboard, "Overdue only", showing rows whose `reviewDate` is in the past. Mirrors the existing `overriddenOnly` / `redundantOnly` toggles exactly.

## Definition of overdue
`reviewDate !== null && reviewDate < todayIsoString` (strict less-than — a `reviewDate` of today is NOT overdue). `reviewDate` is already an ISO `YYYY-MM-DD` string, so this is a plain string comparison, no `Date` parsing.

## Changes
- `props-dashboard-filtering.ts`: add `overdueOnly` param to `filterProperties`; add `overdueOnly` to `PropsFilters`, `parsePropsFiltersFromParams`, `buildPropsQueryParams` (same true/null query-param convention as the other two toggles).
- `props-dashboard-store.ts`: add `overdueOnly` to state, `onOverdueOnlyToggled` method, thread into `visibleProperties` computed.
- `props-dashboard-page.ts` / `.html`: add `onOverdueOnlyToggled` handler, wire into constructor's initial-filter parse and the query-param sync effect, add a third `mat-slide-toggle` labeled "Overdue only".
- `props-dashboard-filtering.spec.ts`: extend with cases for overdue-only filtering (past date included, null excluded, today excluded, combined with name filter).

## Out of scope
No backend/API changes — `reviewDate` is already returned by `PropertyResponseDto`.
