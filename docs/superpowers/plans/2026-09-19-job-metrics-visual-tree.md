# Job Metrics Visual Tree Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the raw-JSON dump in `JobMetricsDialogComponent` with a recursive visual tree component tailored to the `JobMetrics.metricsReport()` shape.

**Architecture:** A new self-recursive standalone Angular component, `JobMetricsNodeComponent` (`maia-job-metrics-node`), renders one `JobMetricsReport` node as a card (name, elapsed time, optional timing stats/context/counters/ratios) and recurses into its own `childJobs`. The dialog swaps its `<p>{{ metrics | json }}</p>` for this component, keeping the existing Close/copy-to-clipboard buttons.

**Tech Stack:** Angular 21 (standalone components, signals, new `@if`/`@for` control flow), Angular Material, vitest + `@angular/core/testing` (via `@angular/build:unit-test`), zoneless change detection.

**User Verification:** NO — no user sign-off requested in the spec; verification is automated tests plus a manual smoke check.

---

## Task 1: Add `JobMetricsReport` model and `JobMetricsNodeComponent`

**Goal:** A typed, recursive model for the metrics JSON shape, and a self-recursive component that renders it as a visual tree, fully covered by tests.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobMetricsReport.ts`
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/components/job-metrics-node/job-metrics-node.component.ts`
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/components/job-metrics-node/job-metrics-node.component.html`
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/components/job-metrics-node/job-metrics-node.component.scss`
- Test: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/components/job-metrics-node/job-metrics-node.component.spec.ts`

**Acceptance Criteria:**
- [ ] `JobMetricsReport` models `jobName`, `jobCount`, `context`, `totalElapsedTime`, optional `min`/`median`/`95th`/`max`/`mean`, optional `counters`, optional `ratios`, optional recursive `childJobs`
- [ ] `JobMetricsNodeComponent` renders job name and `totalElapsedTime.formatted` for a leaf node
- [ ] Timing stats section renders only when `min` is present
- [ ] Context/counters/ratios sections each render only when present
- [ ] `childJobs` render recursively (verified 2 levels deep)
- [ ] Chevron toggle hides/shows the child list

**Verify:** `cd libs/maia-ui-workspace && npx ng test maia-jobs` → all tests pass (confirmed baseline: 2 files / 23 tests pass before this task)

**Steps:**

- [ ] **Step 1: Create the `JobMetricsReport` model**

```ts
// libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobMetricsReport.ts

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

- [ ] **Step 2: Write the failing spec for `JobMetricsNodeComponent`**

```ts
// libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/components/job-metrics-node/job-metrics-node.component.spec.ts

import {describe, expect, it} from 'vitest';
import {TestBed} from '@angular/core/testing';
import {provideZonelessChangeDetection} from '@angular/core';
import {JobMetricsNodeComponent} from './job-metrics-node.component';
import {JobMetricsReport} from '../../../../models/JobMetricsReport';

function makeReport(overrides: Partial<JobMetricsReport> = {}): JobMetricsReport {
    return {
        jobName: 'root-job',
        jobCount: 1,
        context: {},
        totalElapsedTime: {seconds: 1.5, formatted: '1.5s'},
        ...overrides,
    };
}

async function renderNode(report: JobMetricsReport) {
    await TestBed.configureTestingModule({
        imports: [JobMetricsNodeComponent],
        providers: [provideZonelessChangeDetection()],
    }).compileComponents();

    const fixture = TestBed.createComponent(JobMetricsNodeComponent);
    fixture.componentRef.setInput('report', report);
    await fixture.whenStable();

    return fixture;
}

function textOf(fixture: {nativeElement: unknown}): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
}

describe('JobMetricsNodeComponent', () => {

    it('renders the job name and formatted elapsed time for a leaf node', async () => {
        const fixture = await renderNode(makeReport({
            jobName: 'leaf-job',
            totalElapsedTime: {seconds: 2, formatted: '2s'},
        }));

        expect(textOf(fixture)).toContain('leaf-job');
        expect(textOf(fixture)).toContain('2s');
    });

    it('omits the timing stats section when min is absent', async () => {
        const fixture = await renderNode(makeReport());

        expect(textOf(fixture)).not.toContain('Median');
    });

    it('renders timing stats when min is present', async () => {
        const fixture = await renderNode(makeReport({
            min: 0.1, median: 0.2, '95th': 0.3, max: 0.4, mean: 0.25,
        }));
        const text = textOf(fixture);

        expect(text).toContain('0.100s');
        expect(text).toContain('0.200s');
        expect(text).toContain('0.300s');
        expect(text).toContain('0.400s');
        expect(text).toContain('0.250s');
    });

    it('omits context, counters and ratios sections when absent', async () => {
        const fixture = await renderNode(makeReport());

        expect(textOf(fixture)).not.toContain('Counters');
    });

    it('renders context, counters and ratios entries when present', async () => {
        const fixture = await renderNode(makeReport({
            context: {environment: 'test'},
            counters: {itemCount: 5},
            ratios: {successRatio: {toString: '80%', value: 0.8}},
        }));
        const text = textOf(fixture);

        expect(text).toContain('environment: test');
        expect(text).toContain('itemCount: 5');
        expect(text).toContain('successRatio: 80% (0.8)');
    });

    it('renders nested child jobs recursively', async () => {
        const fixture = await renderNode(makeReport({
            jobName: 'root-job',
            childJobs: [
                makeReport({
                    jobName: 'child-job',
                    childJobs: [makeReport({jobName: 'grandchild-job'})],
                }),
            ],
        }));
        const text = textOf(fixture);

        expect(text).toContain('child-job');
        expect(text).toContain('grandchild-job');
    });

    it('hides child jobs when the toggle is collapsed', async () => {
        const fixture = await renderNode(makeReport({
            childJobs: [makeReport({jobName: 'child-job'})],
        }));

        const toggle = (fixture.nativeElement as HTMLElement).querySelector('button.toggle') as HTMLButtonElement;
        toggle.click();
        await fixture.whenStable();

        expect(textOf(fixture)).not.toContain('child-job');
    });

});
```

- [ ] **Step 3: Run the spec to verify it fails (component doesn't exist yet)**

Run: `cd libs/maia-ui-workspace && npx ng test maia-jobs`
Expected: FAIL — build error, `Cannot find module './job-metrics-node.component'`

- [ ] **Step 4: Create the component TypeScript**

```ts
// libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/components/job-metrics-node/job-metrics-node.component.ts

import {Component, computed, input, signal} from '@angular/core';
import {KeyValuePipe} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {JobMetricsReport} from '../../../../models/JobMetricsReport';

@Component({
    selector: 'maia-job-metrics-node',
    templateUrl: './job-metrics-node.component.html',
    styleUrl: './job-metrics-node.component.scss',
    imports: [KeyValuePipe, MatButtonModule, MatIconModule, JobMetricsNodeComponent]
})
export class JobMetricsNodeComponent {


    report = input.required<JobMetricsReport>();

    expanded = signal(true);


    hasChildJobs = computed(() => (this.report().childJobs?.length ?? 0) > 0);

    hasTimingStats = computed(() => this.report().min !== undefined);

    hasContext = computed(() => Object.keys(this.report().context ?? {}).length > 0);

    hasCounters = computed(() => Object.keys(this.report().counters ?? {}).length > 0);

    hasRatios = computed(() => Object.keys(this.report().ratios ?? {}).length > 0);


    onToggleExpanded(): void {

        this.expanded.update(value => !value);

    }


    formatSeconds(seconds: number | undefined): string {

        if (seconds === undefined) {
            return '';
        }

        return `${seconds.toFixed(3)}s`;

    }


}
```

- [ ] **Step 5: Create the component template**

```html
<!-- libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/components/job-metrics-node/job-metrics-node.component.html -->

<div class="node">
    <div class="header">
        @if (hasChildJobs()) {
            <button
                mat-icon-button
                type="button"
                class="toggle"
                (click)="onToggleExpanded()"
                [attr.aria-label]="expanded() ? 'Collapse' : 'Expand'">
                <mat-icon>{{ expanded() ? 'expand_more' : 'chevron_right' }}</mat-icon>
            </button>
        }
        <span class="name">{{ report().jobName }}</span>
        <span class="count">{{ report().jobCount }} run{{ report().jobCount === 1 ? '' : 's' }}</span>
    </div>

    <div class="stat-row">
        <span class="stat"><span class="stat-label">Elapsed</span> {{ report().totalElapsedTime.formatted }}</span>
    </div>

    @if (hasTimingStats()) {
        <div class="stat-row">
            <span class="stat"><span class="stat-label">Min</span> {{ formatSeconds(report().min) }}</span>
            <span class="stat"><span class="stat-label">Median</span> {{ formatSeconds(report().median) }}</span>
            <span class="stat"><span class="stat-label">95th</span> {{ formatSeconds(report()['95th']) }}</span>
            <span class="stat"><span class="stat-label">Max</span> {{ formatSeconds(report().max) }}</span>
            <span class="stat"><span class="stat-label">Mean</span> {{ formatSeconds(report().mean) }}</span>
        </div>
    }

    @if (hasContext()) {
        <div class="kv-row">
            <span class="kv-heading">Context</span>
            @for (entry of report().context | keyvalue; track entry.key) {
                <span class="kv">{{ entry.key }}: {{ entry.value }}</span>
            }
        </div>
    }

    @if (hasCounters()) {
        <div class="kv-row">
            <span class="kv-heading">Counters</span>
            @for (entry of report().counters | keyvalue; track entry.key) {
                <span class="kv">{{ entry.key }}: {{ entry.value }}</span>
            }
        </div>
    }

    @if (hasRatios()) {
        <div class="kv-row">
            <span class="kv-heading">Ratios</span>
            @for (entry of report().ratios | keyvalue; track entry.key) {
                <span class="kv">{{ entry.key }}: {{ entry.value.toString }} ({{ entry.value.value }})</span>
            }
        </div>
    }

    @if (hasChildJobs() && expanded()) {
        <div class="children">
            @for (child of report().childJobs; track child.jobName) {
                <maia-job-metrics-node [report]="child" />
            }
        </div>
    }
</div>
```

- [ ] **Step 6: Create the component styles**

```scss
// libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/components/job-metrics-node/job-metrics-node.component.scss

:host {
    display: block;
}

.node {
    display: flex;
    flex-direction: column;
    gap: 0.375rem;
    border-radius: 0.5rem;
    border: 1px solid var(--mat-sys-outline-variant);
    padding: 0.75rem;
}

.header {
    display: flex;
    align-items: center;
    gap: 0.375rem;
}

.toggle {
    flex-shrink: 0;
}

.name {
    font-weight: 500;
}

.count {
    font-size: 0.75rem;
    color: var(--mat-sys-on-surface-variant);
    margin-left: auto;
}

.stat-row {
    display: flex;
    flex-wrap: wrap;
    gap: 1rem;
    font-size: 0.75rem;
}

.stat-label {
    color: var(--mat-sys-on-surface-variant);
    margin-right: 0.25rem;
}

.kv-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.75rem;
}

.kv-heading {
    font-weight: 500;
    color: var(--mat-sys-on-surface-variant);
}

.kv {
    background-color: var(--mat-sys-surface-container-high);
    border-radius: 0.25rem;
    padding: 0.125rem 0.5rem;
}

.children {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    margin-left: 1.25rem;
    padding-left: 0.75rem;
    border-left: 2px solid var(--mat-sys-outline-variant);
}
```

- [ ] **Step 7: Run the spec to verify it passes**

Run: `cd libs/maia-ui-workspace && npx ng test maia-jobs`
Expected: PASS — 3 test files, 30 tests (23 pre-existing + 7 new)

- [ ] **Step 8: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobMetricsReport.ts libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/components/job-metrics-node/
git commit -m "$(cat <<'EOF'
Add recursive JobMetricsNodeComponent for visual metrics rendering

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

## Task 2: Wire `JobMetricsNodeComponent` into `JobMetricsDialogComponent`

**Goal:** The metrics dialog shows the visual tree instead of raw JSON, with Close and copy-to-clipboard behavior unchanged.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/job-metrics-dialog.component.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/job-metrics-dialog.component.html`

**Acceptance Criteria:**
- [ ] Dialog's injected `metrics` data is typed `JobMetricsReport`, not `any`
- [ ] Dialog content renders `<maia-job-metrics-node [report]="metrics" />` instead of `<p>{{ metrics | json }}</p>`
- [ ] Close button and copy-to-clipboard button (`[cdkCopyToClipboard]="metrics | json"`) are unchanged

**Verify:** `cd libs/maia-ui-workspace && npx ng build maia-jobs` → builds with no errors

**Steps:**

- [ ] **Step 1: Update the dialog component**

```ts
// libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/job-metrics-dialog.component.ts

import {Component, Inject} from '@angular/core';
import {JsonPipe} from '@angular/common';
import {ClipboardModule} from '@angular/cdk/clipboard';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {JobMetricsReport} from '../../models/JobMetricsReport';
import {JobMetricsNodeComponent} from './components/job-metrics-node/job-metrics-node.component';

@Component({
    selector: 'maia-job-metrics-dialog',
    templateUrl: './job-metrics-dialog.component.html',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule, ClipboardModule, MatIconModule, JsonPipe, JobMetricsNodeComponent]
})
export class JobMetricsDialogComponent {


    constructor(
        public dialogRef: MatDialogRef<JobMetricsDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public metrics: JobMetricsReport
    ) {}


    onClose(): void {

        this.dialogRef.close();

    }


}
```

- [ ] **Step 2: Update the dialog template**

```html
<!-- libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/job-metrics-dialog.component.html -->

<h1 mat-dialog-title>Job Metrics</h1>
<div mat-dialog-content>
    <maia-job-metrics-node [report]="metrics" />
</div>
<div mat-dialog-actions>
    <button mat-flat-button (click)="onClose()">Close</button>
    <button mat-flat-button [cdkCopyToClipboard]="metrics | json"><mat-icon>content_copy</mat-icon></button>
</div>
```

- [ ] **Step 3: Build to verify no compile errors**

Run: `cd libs/maia-ui-workspace && npx ng build maia-jobs`
Expected: `Built @maia/maia-jobs` with no errors

- [ ] **Step 4: Run the full test suite**

Run: `cd libs/maia-ui-workspace && npx ng test maia-jobs`
Expected: PASS — same 3 test files / 30 tests as Task 1

- [ ] **Step 5: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/job-metrics-dialog.component.ts libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/dialogs/job-metrics-dialog/job-metrics-dialog.component.html
git commit -m "$(cat <<'EOF'
Show job metrics as a visual tree instead of raw JSON

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

---

## Task 3: Rebuild, reinstall in la-ui, and manually verify in the browser

**Goal:** Confirm the visual metrics tree actually renders correctly end-to-end in the running showcase/la-ui app, for both a leaf job and a job with nested child jobs.

**Files:** None (build/install/manual verification only).

**Acceptance Criteria:**
- [ ] `maia-jobs` library rebuilt
- [ ] `la-ui`'s `node_modules/@maia/maia-jobs` reinstalled from the fresh build
- [ ] Job History page's Metrics dialog renders the tree (not raw JSON) for a real execution's metrics
- [ ] Copy-to-clipboard button still copies the full raw JSON

**Verify:** Manual browser check at `http://localhost:4200` (or wherever `la-ui`/showcase is running)

**Steps:**

- [ ] **Step 1: Rebuild the library**

Run: `cd libs/maia-ui-workspace && npx ng build maia-jobs`
Expected: `Built @maia/maia-jobs`

- [ ] **Step 2: Reinstall into la-ui**

```bash
cd /home/kevin/dev/code/mahana/apps/littleaircraft/la-ui
rm -rf node_modules/@maia/maia-jobs
npm install @maia/maia-jobs
```

Expected: install completes with no errors (the `file:` dependency copy always needs a forced reinstall — `npm install` alone reports "up to date" without picking up a rebuilt `file:` dependency).

- [ ] **Step 3: Restart the dev server if one is running**

If `ng serve` is already running for `la-ui`, kill it and restart (`npx ng serve --port 4200`) so its dependency pre-bundle picks up the reinstalled package — a running dev server does not detect `node_modules` package rebuilds on its own.

- [ ] **Step 4: Manually verify in the browser**

Open the Job History page, find an execution with metrics that include child jobs (or use the showcase app's sample job), open its Metrics dialog, and confirm:
- The tree renders as cards, not raw JSON
- Child jobs nest visually and the chevron toggle expands/collapses them
- The copy-to-clipboard button still copies the full raw JSON (paste it somewhere to confirm)

- [ ] **Step 5: Commit if anything needed fixing**

Only if Step 4 surfaced a bug requiring a code fix — commit that fix separately with a message describing what was wrong. If Step 4 passes as-is, there is nothing to commit for this task.

---

## Unresolved questions
None.
