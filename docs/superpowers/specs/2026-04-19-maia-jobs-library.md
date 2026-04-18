# maia-jobs Angular Library — Design Spec

## Goal

Extract the `jobs-dashboard` UI from `la-ui` into a standalone Angular library (`@maia/maia-jobs`) in `libs/maia-ui-workspace/projects/maia-jobs`, following the same pattern as `maia-elasticsearch`.

## Library Structure

```
projects/maia-jobs/
  package.json                          @maia/maia-jobs
  ng-package.json                       dest: ../../dist/maia-jobs
  tsconfig.lib.json
  tsconfig.lib.prod.json
  tsconfig.spec.json
  src/
    public-api.ts
    lib/
      jobs-dashboard/
        models/
          JobState.ts
          JobExecutionState.ts
          JobExecutionSummary.ts
          JobExecutionDetail.ts
        services/
          jobs-api-base-url.token.ts    InjectionToken<string> 'jobsApiBaseUrl', default '/api/ops'
          jobs-api.service.ts           @Injectable(), inject(JOBS_API_BASE_URL) for all paths
        components/
          job-state/
            job-state.component.ts      selector: maia-job-state
            job-state.component.html
        dialogs/
          job-metrics-dialog/
            job-metrics-dialog.component.ts   selector: maia-job-metrics-dialog
            job-metrics-dialog.component.html
          run-job-dialog/
            run-job-dialog.component.ts       selector: maia-run-job-dialog
            run-job-dialog.component.html
          stacktrace-dialog/
            stacktrace-dialog.component.ts    selector: maia-stacktrace-dialog
            stacktrace-dialog.component.html
        jobs-dashboard-page.component.ts      selector: maia-jobs-dashboard-page
        jobs-dashboard-page.component.html
```

## Key Design Decisions

**No page layout wrapper.** The page component renders job cards directly with no wrapping shell. Consumers provide their own layout around `<maia-jobs-dashboard-page>`.

**Configurable base URL.** `JOBS_API_BASE_URL` injection token defaults to `/api/ops`. All API paths are built as `` `${baseUrl}/jobs/current_state` ``, etc. Consumers override it via `providers: [{ provide: JOBS_API_BASE_URL, useValue: '/custom/prefix' }]`.

**Selector prefix `maia-`.** All selectors renamed from `app-` to `maia-` for consistency with the workspace pattern.

**No ngrx store.** The original uses simple Observable subscriptions. No state management abstraction added.

**Self-contained providers.** `JobsDashboardPageComponent` declares `providers: [JobsApiService]`, same as the original.

## API Surface (public-api.ts exports)

- All four models
- `JOBS_API_BASE_URL` token
- `JobsApiService`
- `JobStateComponent`
- `JobMetricsDialogComponent`, `RunJobDialogComponent`, `StacktraceDialogComponent`
- `JobsDashboardPageComponent`

## Peer Dependencies

- `@angular/common`, `@angular/core`, `@angular/material`, `@angular/cdk`

## File Mapping from Source

| Source (`la-ui/...jobs-dashboard/`) | Target (`lib/jobs-dashboard/`) |
|---|---|
| `models/JobState.ts` | `models/JobState.ts` (unchanged) |
| `models/JobExecutionState.ts` | `models/JobExecutionState.ts` (unchanged) |
| `models/JobExecutionSummary.ts` | `models/JobExecutionSummary.ts` (unchanged) |
| `models/JobExecutionDetail.ts` | `models/JobExecutionDetail.ts` (unchanged) |
| `services/jobs-api.service.ts` | `services/jobs-api.service.ts` (use base URL token, fix imports) |
| `components/job-state/job-state.component.ts` | same path (rename selector, fix imports) |
| `components/job-state/job-state.component.html` | same path (update selector references) |
| `dialogs/*/` | same paths (rename selectors, fix imports) |
| `jobs-dashboard-page.component.ts` | same path (rename selector, remove page-layout, fix imports) |
| `jobs-dashboard-page.component.html` | same path (remove `<app-page-layout>` wrapper) |

## angular.json Entry

Add a `maia-jobs` project entry to `angular.json` following the same shape as `maia-elasticsearch`.
