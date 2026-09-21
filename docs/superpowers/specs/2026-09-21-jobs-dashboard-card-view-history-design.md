# Jobs Dashboard Card "View History" Button

## Goal
Each job card on the jobs-dashboard gets a "View History" button that navigates to
the Job History view pre-filtered to that job.

## Design
- Add a "View History" action to `job-state.component.html`
  (`libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/components/job-state/job-state.component.html`),
  styled like the existing Run/Metrics/stacktrace actions.
- Implement as `[routerLink]="'/jobs-history'" [queryParams]="{jobName: <job's name>}"`
  directly on the card — pure navigation, so no `@Output()` round-trip through
  `JobsDashboardPageComponent` is needed (unlike Run/Metrics/stacktrace, which trigger
  parent-owned side effects).
- No changes needed on the Job History page: `job-history-filtering.ts`
  (`parseHistoryFiltersFromParams`) already reads a `jobName` query param.
- The existing toolbar-level static "View History" link (`jobs-dashboard-page.component.html`,
  no query params) is untouched — this is an additive, per-card action.

## Out of scope
- Changing the toolbar-level "View History" link.
- Any change to the Job History page itself.
