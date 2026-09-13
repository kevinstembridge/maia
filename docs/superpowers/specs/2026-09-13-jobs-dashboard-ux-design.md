# Jobs Dashboard UX Improvements — Design

## Goal
Improve the UX of the jobs dashboard (`libs/maia-ui-workspace/projects/maia-jobs`): a real card grid, a name filter, a single running/failed/idle status indicator per job, elapsed running time, periodic auto-refresh, and a batched backend response that eliminates the current N+1 API call pattern.

## Background
Current state:
- `jobs-dashboard-page.component.html`/`.ts`: a flat `@for` of `<maia-job-state>` cards, no grid, no filter, a bare spinner-or-list toggle, plain `<p>No jobs</p>` empty state. Fetches `JobState[]` once in `ngOnInit` via `JobsApiService.getJobsState()` (`GET /jobs/current_state`) — no polling, no refresh.
- `job-state.component.html`/`.ts`: each card independently calls `JobsApiService.getRecentlyFailedJobExecutions(jobName)` (`GET /job/recently_failed/{jobName}`) in its own `ngOnInit` — an N+1 API call pattern (one HTTP request per job, per page load). Renders job name, a "Run..." button, an always-visible "Running Executions" list (execution id + a "Metrics" button per row), and an always-visible "Recently-failed Executions" list (error message + "stacktrace" button per row), all as unstyled `<h2>`/`<h4>`/`<ul>` markup.
- `JobState { jobName, jobDescription, runningJobs: JobExecutionState[] }`. `JobExecutionState { id, startTime, endTime, status, exitCode, exitDescription, metrics }`. `JobExecutionSummary { jobExecutionId, jobName, startTimestamp, endTimestamp, errorMessage }`. `JobExecutionDetail` (used by the stacktrace dialog, unchanged by this work).
- Backend: `MaiaJobEndpoint.kt` (`libs/maia-job-parent/maia-job-web`) maps the 5 REST routes the frontend calls; `MaiaJobService.kt` + `JobExecutionRepo.kt` (`libs/maia-job-parent/maia-job`) implement them. `JobExecutionRepo.recentFailedExecutions(jobName)` runs a single generated-DAO query: `filters.jobName eq jobName, filters.completionStatus eq JobCompletionStatus.FAILED`, sorted by `endTimestamp desc`, `PageRequest.of(0, 10, sort)`. The generated filter DSL already supports an `in` operator (`filters.jobName in jobNames`), so batching this query across all job names is a small, low-risk change, not a rewrite.
- `JobCompletionStatus` (`libs/maia-job-parent/maia-job-domain`) is `enum class JobCompletionStatus { SUCCESS, FAILED }` — only terminal states are persisted. "Running" is tracked separately, in-memory only, via `MaiaJobService.runningJobs: MultiValueMap<JobName, RunningJob>`. There is no queued/retrying/cancelled concept anywhere in the system today; introducing one is out of scope (confirmed with user — it would be its own backend project, not a dashboard UX pass).
- `getRecentlyFailedJobExecutions`/`GET /job/recently_failed/{jobName}` has no other consumers anywhere in the codebase (confirmed via full-repo search) — safe to remove outright rather than keep as a compatibility shim.
- This is a sibling dashboard to the elastic-indices dashboard (`libs/maia-ui-workspace/projects/maia-elasticsearch`, redesigned in a prior pass in this same series): card grid, name filter, colored status dot, count summary, loading/error/empty states, and a pure-function-module-plus-signal-store architecture are all established patterns here and are reused for consistency, not reinvented.

## Behavior

### Backend change
- Add `recentlyFailedExecutions: JobExecutionSummary[]` to the `JobState` Kotlin DTO (mirrors the existing frontend `JobExecutionSummary` shape).
- In `MaiaJobService`'s current-state assembly, replace the per-job-name query with one batched call to `JobExecutionRepo` using `filters.jobName in jobNames` (all job names being assembled), sorted by `endTimestamp desc`, same `PageRequest.of(0, 10, ...)` limit semantics — group the returned rows back onto each `JobState` by job name in the service layer.
- Delete `MaiaJobEndpoint`'s `GET /job/recently_failed/{jobName}` mapping and `MaiaJobService`'s single-job-name method that backed it, once nothing references them.
- On the frontend, delete `JobsApiService.getRecentlyFailedJobExecutions` and its call site in `JobStateComponent.ngOnInit` — `recentlyFailedExecutions` now arrives as part of the `JobState` returned by `getJobsState()`.

### Status model & priority
Exactly three states, matching what the backend can actually report:
- **Running** — `jobState.runningJobs.length > 0`. Blue.
- **Failed** — `jobState.recentlyFailedExecutions.length > 0` (and not running). Red.
- **Idle** — neither. Green.

When a job is both running and has recent failures, **running wins** the status dot (confirmed with user) — the failure remains visible via the collapsed failures toggle (below), just not reflected in the dot color. The page-level count summary applies the same priority: each job is counted in exactly one bucket (running > failed > idle), so counts always sum to the total job count.

### Store / pure-function module
Mirrors the elastic-indices architecture:
- A new pure-function module (e.g. `state/jobs-filtering.ts`) exporting:
  - `filterAndSortByName(jobs, nameFilter)` — case-insensitive substring match on `jobName` only (not description, per user decision), sorted alphabetically by `jobName`.
  - `deriveJobStatus(jobState)` — returns `'running' | 'failed' | 'idle'` per the priority rule above.
  - `countByStatus(jobs)` — groups the (already name-filtered) jobs by `deriveJobStatus`, using the same priority so counts partition the set.
  - `buildJobCountSummary(visibleCount, totalCount, statusCounts)` — same "`N` jobs" / "`X` of `N` jobs" plus ordered non-zero status segments (`running`, `failed`, `idle` in that fixed order) format as the elastic-indices summary, reusing the same shape of logic (separate implementation, since the domain/status vocabulary differs — no shared module between the two dashboards).
  - `formatElapsed(startTime, now)` — formats a duration from a start timestamp and a reference "now" instant (passed in, not read from `Date.now()` internally, so it stays a pure, testable function). Rule: under 60s → `"<N>s"` (e.g. `"42s"`); under 1 hour → `"<M>m <S>s"` (e.g. `"4m 12s"`); 1 hour or more → `"<H>h <MM>m"` with minutes zero-padded to 2 digits (e.g. `"1h 03m"`). No days/weeks unit — batch jobs running that long are treated as an extreme edge case, not designed for.
- An `JobsDashboardStore` (NgRx signal store) holds `jobStates: JobState[]`, `nameFilter: string`, `isLoading`, `error`, and computed `visibleJobStates`, `statusCounts`, `countSummary` — same shape as `ElasticIndicesPageStore`.
- Polling: `fetchJobsState` becomes an `rxMethod` triggered both once immediately and then every 15 seconds via `timer(0, 15000)`, `switchMap`-ing into `getJobsState()`, updating `jobStates`/`isLoading`/`error` on each tick. No `distinctUntilChanged()` on this pipe (the elastic-indices implementation's Retry-button bug — `distinctUntilChanged()` silently swallowing repeated void/identical triggers — is a known pitfall to explicitly avoid here). On a fetch error, the timer keeps running on its normal 15s schedule regardless (self-healing once the backend recovers, no manual action required) — the error banner's "Retry" button exists only to let the user force an immediate re-check instead of waiting for the next tick; it does not pause, reset, or otherwise alter the timer's schedule.

### Card layout (`job-state.component.html`/`.ts`)
Per the approved "status dot + inline running row" mockup:
- Header row: job name + status dot (colored per `deriveJobStatus`, same visual treatment — dot + `title`/`aria-label` — as the elastic-indices status dot, no visible text label).
- Description text (`jobDescription`) shown as secondary text, when present.
- If running: one highlighted row per entry in `runningJobs`, each showing `"Running for " + formatElapsed(execution.startTime, now)`, plus the existing "Metrics" button for that execution. `now` is supplied by the store (refreshed on each poll tick), not computed independently per card, so all cards agree on the same instant.
- If not running and idle (no recent failures either): a plain "Not currently running" line (no running rows, no failures toggle).
- If there are recent failures (`recentlyFailedExecutions.length > 0`): a closed-by-default disclosure toggle labeled `"1 recent failure"` (singular, count of exactly 1) or `"<N> recent failures"` (plural, count ≥ 2), which expands to the existing per-failure list (error message + "stacktrace" button per row) — same interaction as today, just collapsed by default.
- "Run..." button and its existing confirmation dialog (`RunJobDialogComponent`) are unchanged.

### Page layout (`jobs-dashboard-page.component.html`/`.ts`)
Same structural pattern as the elastic-indices page:
1. Header: title + `store.countSummary()`.
2. Filter row (name-only text input), always visible across all states below.
3. Content area, exactly one of: spinner (loading) / error banner with a "Retry" button / "No jobs match your filter." empty message / the card grid (Tailwind `grid-cols-[repeat(auto-fill,minmax(260px,1fr))]` or similar, matching elastic-indices' grid).
4. Cards render in alphabetical order by job name (not grouped by status — confirmed with user, consistent with the elastic-indices decision).

## Out of scope
- No queued/retrying/cancelled states — the backend has no concept of these today; adding them would be a separate, larger backend project.
- No change to the "Run..." confirmation dialog, the stacktrace dialog, or the job-metrics dialog beyond what's needed to keep their existing bindings working against the restructured card.
- No filtering on job description, only job name.
- No manual sort control — alphabetical by job name only.
- No backwards-compatibility shim for the removed `/job/recently_failed/{jobName}` endpoint — confirmed unused elsewhere.
- No per-second live-ticking timer for elapsed time — it's recomputed on each 15s poll tick only.

## Verification
- New pure-function module gets a Vitest spec covering: name filtering/sorting, `deriveJobStatus`'s running-wins priority (including the both-running-and-failed case), `countByStatus`'s partitioning, `buildJobCountSummary`'s formatting (including the zero-count-segment-omission and singular/plural rules), and `formatElapsed`'s duration formatting across a few boundary durations (e.g. under a minute, several minutes, over an hour).
- Backend: confirm `JobExecutionRepo`'s batched query change against existing test patterns for that module (if any exist — check during planning), and manually confirm `GET /jobs/current_state` now returns `recentlyFailedExecutions` per job and `GET /job/recently_failed/{jobName}` returns 404/is gone.
- Manually run the showcase app and confirm: grid renders responsively; typing in the filter narrows the grid and count summary; a running job shows a live-ish elapsed time that increases every ~15s; a job with both a running execution and a past failure shows a blue (not red) dot with the failure available behind the collapsed toggle; the page keeps auto-refreshing without user action; stopping the backend shows the error banner with a working Retry, and polling resumes correctly afterward.

## Unresolved questions
None.
