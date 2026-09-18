# Job Metrics Visual Tree — Design

## Goal
Replace the raw-JSON dump in `JobMetricsDialogComponent` (`libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog`) with a visual, recursive rendering of the job metrics tree.

## Background
- `JobExecutionHistoryCardComponent`'s "Metrics" button opens `JobMetricsDialogComponent` with `historyItem.metrics` (typed `any`) as `MAT_DIALOG_DATA`. The dialog currently renders `<p>{{ metrics | json }}</p>` — an unreadable JSON blob — plus a Close button and a copy-to-clipboard button.
- `metrics` is not arbitrary JSON — it's always the output of `JobMetrics.metricsReport()` (`libs/maia-metrics/src/main/kotlin/org/maiaframework/metrics/JobMetrics.kt:44`), a fixed, recursive shape:
  - `jobName: string`, `jobCount: number` (timer count), `context: Record<string, string>`, `totalElapsedTime: {seconds: number, formatted: string}` (always present)
  - `min`, `median`, `95th`, `max`, `mean` (numbers, seconds) — only present when `jobCount > 1`
  - `counters: Record<string, number>` — only present when at least one counter was recorded
  - `ratios: Record<string, {toString: string, value: number}>` — only present when at least one ratio was recorded
  - `childJobs: <same shape>[]` — only present when child jobs were recorded; this is the recursive element
- No frontend type currently models this shape (`JobExecutionHistoryItem.metrics` is `any`).
- The existing `JobExecutionHistoryCardComponent` card styling (border, radius, padding — `job-execution-history-card.component.scss`) is the established "card" visual language in this module and is reused here for consistency.

## Behavior

### New model
- `JobMetricsReport.ts` (new file, `jobs-dashboard/models/`):
  ```ts
  export interface JobMetricsReport {
      jobName: string;
      jobCount: number;
      context: Record<string, string>;
      totalElapsedTime: { seconds: number; formatted: string };
      min?: number;
      median?: number;
      '95th'?: number;
      max?: number;
      mean?: number;
      counters?: Record<string, number>;
      ratios?: Record<string, { toString: string; value: number }>;
      childJobs?: JobMetricsReport[];
  }
  ```

### New component — `JobExecutionMetricsNodeComponent`
- Selector `maia-job-metrics-node`, new folder `job-metrics-dialog/components/job-metrics-node/` (`.ts`/`.html`/`.scss`).
- Input: `report = input.required<JobMetricsReport>()`.
- Self-recursive: template renders `<maia-job-metrics-node [report]="child">` for each entry in `report().childJobs`; the component adds itself to its own standalone `imports` array (supported Angular pattern for self-referencing standalone components).
- Local expand/collapse state: `expanded = signal(true)` (default **expanded**, toggled by a chevron button in the header). The toggle button only renders when `report().childJobs?.length` is truthy — leaf nodes have no toggle.
- Card sections, each conditionally rendered only when the underlying data is present:
  - **Header**: job name, run count (`jobCount`), expand/collapse chevron (if it has children)
  - **Elapsed**: `totalElapsedTime.formatted` (already human-formatted server-side — no reformatting needed)
  - **Timing stats**: `min` / `median` / `95th` / `max` / `mean`, each rounded to 3 decimal places with an `s` suffix, shown as a row of labeled stats — rendered only when `min !== undefined` (backend omits all five together)
  - **Context**: `key: value` list, rendered only when `context` has entries
  - **Counters**: `name: count` list, rendered only when `counters` present
  - **Ratios**: `name: toString (value)` list, rendered only when `ratios` present
  - **Child jobs**: indented list of nested `<maia-job-metrics-node>`, visible only when `expanded()` is true and `childJobs` is present
- Styling (`.scss`): same card look (border, radius, padding) as `job-execution-history-card`; nested child cards get a left indent (e.g. `margin-left`) and a lighter/muted border to visually distinguish tree depth.

### Dialog changes
- `job-metrics-dialog.component.html`: replace `<p>{{ metrics | json }}</p>` with `<maia-job-metrics-node [report]="metrics" />`.
- `job-metrics-dialog.component.ts`: type the constructor's injected data as `JobMetricsReport` instead of `any`; import `JobExecutionMetricsNodeComponent` into the dialog's standalone `imports`.
- Close button and `[cdkCopyToClipboard]="metrics | json"` button are unchanged — raw JSON remains copyable, just no longer displayed inline.

### Exports
- `public-api.ts`: no changes. Neither `JobMetricsDialogComponent` nor `StacktraceDialogComponent` is exported today — both are dialog-internal implementation details opened only from within this module. `JobMetricsReport` and `JobExecutionMetricsNodeComponent` follow the same convention and stay unexported.

## Out of scope
- No generic/reusable JSON-tree viewer — this component is intentionally coupled to the `JobMetricsReport` shape, not arbitrary JSON (per the "tailored metrics view" decision).
- No backend changes — `JobMetrics.metricsReport()` already produces exactly the shape modeled above.
- No changes to how `metrics` reaches the dialog (still passed inline via `MAT_DIALOG_DATA`, no new fetch).
- No persistence of expand/collapse state across dialog opens — each open starts fully expanded.

## Verification
- New `job-metrics-node.component.spec.ts`: renders a leaf node's elapsed time and job name; renders timing stats only when `min` is present; renders context/counters/ratios rows only when each is present; renders nested `childJobs` recursively (2+ levels deep); chevron toggle hides/shows the child list.
- Manually run the showcase app: open a job's metrics dialog for a job with child jobs (e.g. a job using `timeChildJob`/`timeCountableChildJob`) and confirm the tree renders correctly nested, expand/collapse works per-node, and the copy-to-clipboard button still copies the full raw JSON.

## Unresolved questions
None.
