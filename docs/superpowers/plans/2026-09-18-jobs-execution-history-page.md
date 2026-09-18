# Job Execution History Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `GET /job/execution_history` backend endpoint (paginated, filterable by job name/status/date range) and a new `maia-job-history-page` Angular page in `maia-jobs` that renders every job execution as one card per row, wired into the showcase app.

**Architecture:** A new generated response DTO (`JobExecutionHistoryItemResponseDto`, via `simpleResponseDto` in `MaiaJobSpec.kt`) carries full execution data (status/timestamps/invoker/error/metrics) that today only exists piecemeal. `JobExecutionRepo` gains a dynamic-filter search method built from the already-generated `JobExecutionEntityFilters`/`JobExecutionDao.findAllBy(filter, pageable)`/`count(filter)`, wrapped in the existing generic `SearchResultPage<T>`. The frontend adds a new `job-history/` feature area (store + card component + page component) inside the existing `maia-jobs` library, reusing the existing `JobsApiService`, `StacktraceDialogComponent`, and `JobMetricsDialogComponent` rather than duplicating them.

**Tech Stack:** Kotlin/Spring Boot backend (`libs/maia-job-parent`), Angular 21 + `@ngrx/signals` frontend (`libs/maia-ui-workspace/projects/maia-jobs`), Angular Material, Vitest (`ng test`).

**User Verification:** NO — the original request ("create a page to display job history, cards not a table") does not ask for a human sign-off checkpoint during implementation. The agent implementing this plan must still manually verify the running showcase app in a browser before declaring the work complete (per standard UI-change practice), but that is agent-performed verification, not a user-facing checkpoint.

---

## Reference: full spec

See `docs/superpowers/specs/2026-09-18-jobs-execution-history-page-design.md` for the full design rationale. This plan implements it task-by-task.

---

### Task 0: Backend — add `JobExecutionHistoryItem` DTO to the spec and regenerate

**Goal:** Generate `JobExecutionHistoryItemResponseDto` (with status/timestamps/invoker/error/metrics) via the spec DSL, since response DTOs in this module are generated code, not hand-written.

**Files:**
- Modify: `libs/maia-job-parent/maia-job-spec/src/main/kotlin/org/maiaframework/job/spec/MaiaJobSpec.kt`
- Generated (do not hand-edit): `libs/maia-job-parent/maia-job-domain/src/generated/kotlin/main/org/maiaframework/job/JobExecutionHistoryItemResponseDto.kt`

**Acceptance Criteria:**
- [ ] `MaiaJobSpec.kt` declares `jobExecutionHistoryItemDtoDef` with fields `jobExecutionId`, `jobName`, `invokedBy`, `startTimestamp`, `endTimestamp` (nullable), `completionStatus` (nullable), `errorMessage` (nullable), `metrics`.
- [ ] Running `maiaGeneration` produces `JobExecutionHistoryItemResponseDto.kt` with a constructor ordered alphabetically by field name: `(completionStatus, endTimestamp, errorMessage, invokedBy, jobExecutionId, jobName, metrics, startTimestamp)`.
- [ ] `:libs:maia-job-parent:maia-job-domain:compileKotlin` succeeds.

**Verify:** `./gradlew :libs:maia-job-parent:maia-job-domain:maiaGeneration :libs:maia-job-parent:maia-job-domain:compileKotlin` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Add the new DTO definition to the spec**

In `libs/maia-job-parent/maia-job-spec/src/main/kotlin/org/maiaframework/job/spec/MaiaJobSpec.kt`, add this new `val` directly after `jobExecutionSummaryDtoDef` (before `jobStateDtoDef`, since declaration order only matters for cross-references and this DTO isn't referenced by another):

```kotlin
    val jobExecutionHistoryItemDtoDef = simpleResponseDto("org.maiaframework.job", "JobExecutionHistoryItem") {
        field("jobExecutionId", FieldTypes.domainId)
        field("jobName", jobNameStringType)
        field("invokedBy", FieldTypes.string)
        field("startTimestamp", FieldTypes.instant)
        field("endTimestamp", FieldTypes.instant) {
            nullable()
        }
        field("completionStatus", jobCompletionStatusEnumDef) {
            nullable()
        }
        field("errorMessage", FieldTypes.string) {
            nullable()
        }
        field("metrics", FieldTypes.mapOfStringToAny())
    }
```

- [ ] **Step 2: Regenerate `maia-job-domain`**

Run: `./gradlew :libs:maia-job-parent:maia-job-domain:maiaGeneration`
Expected: `BUILD SUCCESSFUL`, and a new file exists at `libs/maia-job-parent/maia-job-domain/src/generated/kotlin/main/org/maiaframework/job/JobExecutionHistoryItemResponseDto.kt`.

- [ ] **Step 3: Read the generated file to confirm the constructor parameter order**

Read `libs/maia-job-parent/maia-job-domain/src/generated/kotlin/main/org/maiaframework/job/JobExecutionHistoryItemResponseDto.kt` and confirm the `data class` constructor parameters appear in this exact alphabetical order — this order is required for correctly calling the constructor by position in Task 2:

```
completionStatus, endTimestamp, errorMessage, invokedBy, jobExecutionId, jobName, metrics, startTimestamp
```

If the order differs, use whatever order is actually generated in Task 2 instead of assuming this one.

- [ ] **Step 4: Compile to confirm the generated code is valid**

Run: `./gradlew :libs:maia-job-parent:maia-job-domain:compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add libs/maia-job-parent/maia-job-spec/src/main/kotlin/org/maiaframework/job/spec/MaiaJobSpec.kt \
        libs/maia-job-parent/maia-job-domain/src/generated/kotlin/main/org/maiaframework/job/JobExecutionHistoryItemResponseDto.kt
git commit -m "Add JobExecutionHistoryItem response DTO to job spec"
```

---

### Task 1: Backend — `JobExecutionRepo.searchExecutionHistory`

**Goal:** A repository method that builds a dynamic filter (job name exact-match, status incl. synthetic "RUNNING", date range) over `JobExecutionEntity` and returns a paginated `SearchResultPage`.

**Files:**
- Modify: `libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/JobExecutionRepo.kt`

**Acceptance Criteria:**
- [ ] `searchExecutionHistory(jobName, status, from, to, offset, limit)` returns a `SearchResultPage<JobExecutionEntity>` sorted by `startTimestamp` descending.
- [ ] Passing no filters returns all executions (paginated); each filter narrows results independently and in combination.
- [ ] `status = "RUNNING"` filters to `completionStatus is null`; `"SUCCESS"`/`"FAILED"` filter to the matching enum value.
- [ ] `:libs:maia-job-parent:maia-job:compileKotlin` succeeds.

**Verify:** `./gradlew :libs:maia-job-parent:maia-job:compileKotlin` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Add the import**

In `libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/JobExecutionRepo.kt`, add to the import block (after `import org.maiaframework.domain.DomainId`):

```kotlin
import org.maiaframework.domain.search.SearchResultPage
```

- [ ] **Step 2: Add the search method**

Add this method to `JobExecutionRepo`, directly after `recentFailedExecutions`:

```kotlin
    fun searchExecutionHistory(
        jobName: JobName?,
        status: String?,
        from: Instant?,
        to: Instant?,
        offset: Int,
        limit: Int
    ): SearchResultPage<JobExecutionEntity> {

        val filters = JobExecutionEntityFilters()
        val conditions = mutableListOf<JobExecutionEntityFilter>()

        jobName?.let { conditions.add(filters.jobName eq it) }

        when (status) {
            "RUNNING" -> conditions.add(filters.completionStatus.isNull())
            "SUCCESS" -> conditions.add(filters.completionStatus eq JobCompletionStatus.SUCCESS)
            "FAILED" -> conditions.add(filters.completionStatus eq JobCompletionStatus.FAILED)
        }

        from?.let { conditions.add(filters.startTimestamp gte it) }
        to?.let { conditions.add(filters.startTimestamp lte it) }

        val filter = if (conditions.isEmpty()) {
            JobExecutionEntityFilters.NoopFilter()
        } else {
            filters.and(*conditions.toTypedArray())
        }

        val sort = Sort.by(Sort.Order.desc(JobExecutionEntityMeta.startTimestamp))
        val pageRequest = PageRequest.of(offset / limit, limit, sort)

        val results = this.jobExecutionDao.findAllBy(filter, pageRequest)
        val totalCount = this.jobExecutionDao.count(filter)

        return SearchResultPage(results, totalCount, offset, limit)

    }
```

- [ ] **Step 3: Compile**

Run: `./gradlew :libs:maia-job-parent:maia-job:compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/JobExecutionRepo.kt
git commit -m "Add searchExecutionHistory query to JobExecutionRepo"
```

---

### Task 2: Backend — `MaiaJobService.searchJobExecutionHistory`

**Goal:** Service-layer method mapping the repo's `SearchResultPage<JobExecutionEntity>` to `SearchResultPage<JobExecutionHistoryItemResponseDto>`.

**Files:**
- Modify: `libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/MaiaJobService.kt`

**Acceptance Criteria:**
- [ ] `searchJobExecutionHistory(...)` delegates to `JobExecutionRepo.searchExecutionHistory` and maps each entity to `JobExecutionHistoryItemResponseDto`, preserving `totalResultCount`/`offset`/`limit`.
- [ ] `:libs:maia-job-parent:maia-job:compileKotlin` succeeds.

**Verify:** `./gradlew :libs:maia-job-parent:maia-job:compileKotlin` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Add the import**

In `libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/MaiaJobService.kt`, add to the import block:

```kotlin
import org.maiaframework.domain.search.SearchResultPage
```

- [ ] **Step 2: Add the service method and mapper**

Add this method to `MaiaJobService`, directly after `getJobExecutionDetailDto`:

```kotlin
    fun searchJobExecutionHistory(
        jobName: JobName?,
        status: String?,
        from: Instant?,
        to: Instant?,
        offset: Int,
        limit: Int
    ): SearchResultPage<JobExecutionHistoryItemResponseDto> {

        val page = this.jobExecutionRepo.searchExecutionHistory(jobName, status, from, to, offset, limit)

        return SearchResultPage(
                page.results.map { toJobExecutionHistoryItemDto(it) },
                page.totalResultCount,
                page.offset,
                page.limit)

    }


    private fun toJobExecutionHistoryItemDto(entity: JobExecutionEntity): JobExecutionHistoryItemResponseDto {

        return JobExecutionHistoryItemResponseDto(
                entity.completionStatus,
                entity.endTimestamp,
                entity.errorMessage,
                entity.invokedBy,
                entity.id,
                entity.jobName,
                entity.metrics,
                entity.startTimestamp)

    }
```

**If Task 0's Step 3 found a different constructor parameter order**, reorder the arguments in `JobExecutionHistoryItemResponseDto(...)` above to match exactly.

- [ ] **Step 3: Compile**

Run: `./gradlew :libs:maia-job-parent:maia-job:compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/MaiaJobService.kt
git commit -m "Add searchJobExecutionHistory to MaiaJobService"
```

---

### Task 3: Backend — `GET /job/execution_history` endpoint

**Goal:** Expose the search as a REST endpoint with query-param filters and offset/limit paging.

**Files:**
- Modify: `libs/maia-job-parent/maia-job-web/src/main/kotlin/org/maiaframework/job/MaiaJobEndpoint.kt`

**Acceptance Criteria:**
- [ ] `GET /api/ops/job/execution_history` (default base URL) returns a `SearchResultPage<JobExecutionHistoryItemResponseDto>` as JSON.
- [ ] `jobName`, `status`, `from`, `to` are optional query params; `offset` defaults to `0`, `limit` defaults to `20`.
- [ ] Requires `MAIA_JOB_READ` authority, matching the other read endpoints in this controller.
- [ ] `:libs:maia-job-parent:maia-job-web:compileKotlin` succeeds.

**Verify:** `./gradlew :libs:maia-job-parent:maia-job-web:compileKotlin` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Add imports**

In `libs/maia-job-parent/maia-job-web/src/main/kotlin/org/maiaframework/job/MaiaJobEndpoint.kt`, add to the import block:

```kotlin
import org.maiaframework.domain.search.SearchResultPage
import org.springframework.web.bind.annotation.RequestParam
import java.time.Instant
```

- [ ] **Step 2: Add the endpoint**

Add this method to `MaiaJobEndpoint`, directly after `getExecutionDetail`:

```kotlin
    @GetMapping("/job/execution_history", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('MAIA_JOB_READ')")
    fun searchExecutionHistory(
        @RequestParam(required = false) jobName: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) from: String?,
        @RequestParam(required = false) to: String?,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): SearchResultPage<JobExecutionHistoryItemResponseDto> {

        return this.jobService.searchJobExecutionHistory(
                jobName?.let { JobName(it) },
                status,
                from?.let { Instant.parse(it) },
                to?.let { Instant.parse(it) },
                offset,
                limit)

    }
```

- [ ] **Step 3: Compile**

Run: `./gradlew :libs:maia-job-parent:maia-job-web:compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add libs/maia-job-parent/maia-job-web/src/main/kotlin/org/maiaframework/job/MaiaJobEndpoint.kt
git commit -m "Add GET /job/execution_history endpoint"
```

---

### Task 4: Frontend — history models

**Goal:** Hand-written TypeScript models for the new DTO shape and the generic search-result envelope.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobExecutionHistoryItem.ts`
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/SearchResultPage.ts`

**Acceptance Criteria:**
- [ ] `JobExecutionHistoryItem` has all 8 fields matching `JobExecutionHistoryItemResponseDto`'s JSON shape.
- [ ] `SearchResultPage<T>` mirrors the backend envelope (`results`, `totalResultCount`, `offset`, `limit`).
- [ ] `npx ng build maia-jobs` succeeds (type-checks the new files).

**Verify:** `npx ng build maia-jobs` (from `libs/maia-ui-workspace`) → build succeeds with no TypeScript errors

**Steps:**

- [ ] **Step 1: Create the history item model**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobExecutionHistoryItem.ts`:

```typescript
export class JobExecutionHistoryItem {
    jobExecutionId!: string;
    jobName!: string;
    invokedBy!: string;
    startTimestamp!: string;
    endTimestamp!: string | null;
    completionStatus!: 'SUCCESS' | 'FAILED' | null;
    errorMessage!: string | null;
    metrics!: any;
}
```

- [ ] **Step 2: Create the generic search-result-page model**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/SearchResultPage.ts`:

```typescript
export interface SearchResultPage<T> {
    results: T[];
    totalResultCount: number;
    offset: number;
    limit: number;
}
```

- [ ] **Step 3: Build to type-check**

Run (from `libs/maia-ui-workspace`): `npx ng build maia-jobs`
Expected: build succeeds

- [ ] **Step 4: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobExecutionHistoryItem.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/SearchResultPage.ts
git commit -m "Add JobExecutionHistoryItem and SearchResultPage frontend models"
```

---

### Task 5: Frontend — `JobsApiService.searchJobExecutionHistory`

**Goal:** New method on the existing `JobsApiService` that calls the new backend endpoint.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/services/jobs-api.service.ts`

**Acceptance Criteria:**
- [ ] `searchJobExecutionHistory(criteria)` builds `HttpParams` from non-null criteria fields and calls `GET {baseUrl}/job/execution_history`.
- [ ] On error, falls back to an empty page with the requested `offset`/`limit` preserved (same `catchError` pattern as the service's other methods).
- [ ] `npx ng build maia-jobs` succeeds.

**Verify:** `npx ng build maia-jobs` (from `libs/maia-ui-workspace`) → build succeeds

**Steps:**

- [ ] **Step 1: Add imports**

In `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/services/jobs-api.service.ts`, add:

```typescript
import {HttpClient, HttpParams} from '@angular/common/http';
import {JobExecutionHistoryItem} from '../models/JobExecutionHistoryItem';
import {SearchResultPage} from '../models/SearchResultPage';
```

(Replace the existing `import {HttpClient} from '@angular/common/http';` line with the `HttpParams`-inclusive version above.)

- [ ] **Step 2: Add the search method**

Add this method to `JobsApiService`, directly after `getJobExecutionDetail`:

```typescript
    searchJobExecutionHistory(criteria: {
        jobName: string | null;
        status: string | null;
        from: string | null;
        to: string | null;
        offset: number;
        limit: number;
    }): Observable<SearchResultPage<JobExecutionHistoryItem>> {

        let params = new HttpParams()
            .set('offset', criteria.offset)
            .set('limit', criteria.limit);

        if (criteria.jobName) {
            params = params.set('jobName', criteria.jobName);
        }
        if (criteria.status) {
            params = params.set('status', criteria.status);
        }
        if (criteria.from) {
            params = params.set('from', criteria.from);
        }
        if (criteria.to) {
            params = params.set('to', criteria.to);
        }

        return this.http.get<SearchResultPage<JobExecutionHistoryItem>>(`${this.baseUrl}/job/execution_history`, {params}).pipe(
            catchError(this.handleError<SearchResultPage<JobExecutionHistoryItem>>('searchJobExecutionHistory', {
                results: [],
                totalResultCount: 0,
                offset: criteria.offset,
                limit: criteria.limit,
            }))
        );

    }
```

- [ ] **Step 3: Build to type-check**

Run (from `libs/maia-ui-workspace`): `npx ng build maia-jobs`
Expected: build succeeds

- [ ] **Step 4: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/services/jobs-api.service.ts
git commit -m "Add searchJobExecutionHistory to JobsApiService"
```

---

### Task 6: Frontend — `job-history-filtering.ts` pure functions (TDD)

**Goal:** Pure, unit-tested helpers for deriving a card's status and formatting its duration.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-filtering.ts`
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-filtering.spec.ts`

**Acceptance Criteria:**
- [ ] `deriveHistoryStatus` returns `'running'` when `completionStatus` is `null`, else `'success'`/`'failed'` from the enum value.
- [ ] `formatDuration` returns `'Running…'` when `endTimestamp` is `null`, else formats the elapsed time using the `<Ns>` / `<Mm> <Ss>` / `<Hh> <MMm>` rules (same thresholds as the dashboard's `formatElapsed`).
- [ ] `ng test maia-jobs` passes, including the new spec file.

**Verify:** `npx ng test maia-jobs --watch=false` (from `libs/maia-ui-workspace`) → all tests pass

**Steps:**

- [ ] **Step 1: Write the failing test**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-filtering.spec.ts`:

```typescript
import {describe, expect, it} from 'vitest';
import {deriveHistoryStatus, formatDuration} from './job-history-filtering';
import {JobExecutionHistoryItem} from '../../jobs-dashboard/models/JobExecutionHistoryItem';

function makeItem(overrides: Partial<JobExecutionHistoryItem>): JobExecutionHistoryItem {
    return Object.assign(new JobExecutionHistoryItem(), {
        jobExecutionId: 'exec-1',
        jobName: 'some-job',
        invokedBy: 'someone',
        startTimestamp: '2026-01-01T00:00:00Z',
        endTimestamp: '2026-01-01T00:00:10Z',
        completionStatus: 'SUCCESS',
        errorMessage: null,
        metrics: {},
    }, overrides);
}

describe('deriveHistoryStatus', () => {

    it('returns running when completionStatus is null', () => {
        expect(deriveHistoryStatus(makeItem({completionStatus: null}))).toBe('running');
    });

    it('returns success when completionStatus is SUCCESS', () => {
        expect(deriveHistoryStatus(makeItem({completionStatus: 'SUCCESS'}))).toBe('success');
    });

    it('returns failed when completionStatus is FAILED', () => {
        expect(deriveHistoryStatus(makeItem({completionStatus: 'FAILED'}))).toBe('failed');
    });

});

describe('formatDuration', () => {

    it('returns Running… when endTimestamp is null', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', null)).toBe('Running…');
    });

    it('formats sub-minute durations as seconds', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', '2026-01-01T00:00:42Z')).toBe('42s');
    });

    it('formats sub-hour durations as minutes and seconds', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', '2026-01-01T00:04:12Z')).toBe('4m 12s');
    });

    it('formats hour-plus durations as hours and zero-padded minutes', () => {
        expect(formatDuration('2026-01-01T00:00:00Z', '2026-01-01T01:03:00Z')).toBe('1h 03m');
    });

});
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `npx ng test maia-jobs --watch=false` (from `libs/maia-ui-workspace`)
Expected: FAIL — `job-history-filtering.ts` module not found

- [ ] **Step 3: Write the implementation**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-filtering.ts`:

```typescript
import {JobExecutionHistoryItem} from '../../jobs-dashboard/models/JobExecutionHistoryItem';

export type HistoryStatus = 'running' | 'success' | 'failed';

export type HistoryStatusFilter = 'RUNNING' | 'SUCCESS' | 'FAILED' | null;


export function deriveHistoryStatus(item: JobExecutionHistoryItem): HistoryStatus {
    if (item.completionStatus === null) {
        return 'running';
    }
    return item.completionStatus === 'SUCCESS' ? 'success' : 'failed';
}


export function formatDuration(startTimestamp: string, endTimestamp: string | null): string {

    if (endTimestamp === null) {
        return 'Running…';
    }

    const elapsedMs = Math.max(0, new Date(endTimestamp).getTime() - new Date(startTimestamp).getTime());
    const totalSeconds = Math.floor(elapsedMs / 1000);

    if (totalSeconds < 60) {
        return `${totalSeconds}s`;
    }

    if (totalSeconds < 3600) {
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes}m ${seconds}s`;
    }

    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    return `${hours}h ${String(minutes).padStart(2, '0')}m`;

}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `npx ng test maia-jobs --watch=false` (from `libs/maia-ui-workspace`)
Expected: PASS, all 7 new test cases green

- [ ] **Step 5: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-filtering.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-filtering.spec.ts
git commit -m "Add job-history-filtering pure functions with tests"
```

---

### Task 7: Frontend — `JobHistoryStore`

**Goal:** An `@ngrx/signals` store holding filters, pagination, and fetched results, refetching on any filter/page change.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-store.ts`

**Acceptance Criteria:**
- [ ] State includes `items`, `totalCount`, `isLoading`, `error`, `jobNameFilter`, `statusFilter`, `fromDate`, `toDate`, `pageIndex`, `pageSize`, `availableJobNames`.
- [ ] `init()` loads `availableJobNames` (from `getJobsState()`, distinct sorted job names) and triggers the first search.
- [ ] `onJobNameFilterChanged`/`onStatusFilterChanged`/`onDateRangeChanged` reset `pageIndex` to `0` and refetch; `onPageChanged` refetches without resetting.
- [ ] `retryFetch()` re-triggers the last search.
- [ ] `npx ng build maia-jobs` succeeds.

**Verify:** `npx ng build maia-jobs` (from `libs/maia-ui-workspace`) → build succeeds

**Steps:**

- [ ] **Step 1: Create the store**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-store.ts`:

```typescript
import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';
import {inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {JobExecutionHistoryItem} from '../../jobs-dashboard/models/JobExecutionHistoryItem';
import {JobsApiService} from '../../jobs-dashboard/services/jobs-api.service';
import {HistoryStatusFilter} from './job-history-filtering';

type JobHistoryState = {
    items: JobExecutionHistoryItem[];
    totalCount: number;
    isLoading: boolean;
    error: string | null;
    jobNameFilter: string | null;
    statusFilter: HistoryStatusFilter;
    fromDate: string | null;
    toDate: string | null;
    pageIndex: number;
    pageSize: number;
    availableJobNames: string[];
};

const initialState: JobHistoryState = {
    items: [],
    totalCount: 0,
    isLoading: false,
    error: null,
    jobNameFilter: null,
    statusFilter: null,
    fromDate: null,
    toDate: null,
    pageIndex: 0,
    pageSize: 20,
    availableJobNames: [],
};

export const JobHistoryStore = signalStore(

    withState(initialState),

    withMethods((store, jobsService = inject(JobsApiService)) => {

        const search = rxMethod<void>(
            pipe(
                tap(() => patchState(store, {isLoading: true})),
                switchMap(() =>
                    jobsService.searchJobExecutionHistory({
                        jobName: store.jobNameFilter(),
                        status: store.statusFilter(),
                        from: store.fromDate(),
                        to: store.toDate(),
                        offset: store.pageIndex() * store.pageSize(),
                        limit: store.pageSize(),
                    }).pipe(
                        tapResponse({
                            next: (page) => patchState(store, {
                                items: page.results,
                                totalCount: page.totalResultCount,
                                isLoading: false,
                                error: null,
                            }),
                            error: (err) => {
                                patchState(store, {isLoading: false, error: 'Failed to load job history.'});
                                console.error(err);
                            },
                        })
                    )
                )
            )
        );

        const loadAvailableJobNames = rxMethod<void>(
            pipe(
                switchMap(() =>
                    jobsService.getJobsState().pipe(
                        tapResponse({
                            next: (jobStates) => patchState(store, {
                                availableJobNames: [...new Set(jobStates.map((j) => j.jobName))].sort()
                            }),
                            error: (err) => console.error(err),
                        })
                    )
                )
            )
        );

        return {

            init(): void {
                loadAvailableJobNames();
                search();
            },

            retryFetch(): void {
                search();
            },

            onJobNameFilterChanged(value: string | null): void {
                patchState(store, {jobNameFilter: value, pageIndex: 0});
                search();
            },

            onStatusFilterChanged(value: HistoryStatusFilter): void {
                patchState(store, {statusFilter: value, pageIndex: 0});
                search();
            },

            onDateRangeChanged(fromDate: string | null, toDate: string | null): void {
                patchState(store, {fromDate, toDate, pageIndex: 0});
                search();
            },

            onPageChanged(pageIndex: number, pageSize: number): void {
                patchState(store, {pageIndex, pageSize});
                search();
            },

        };

    })

);
```

- [ ] **Step 2: Build to type-check**

Run (from `libs/maia-ui-workspace`): `npx ng build maia-jobs`
Expected: build succeeds

- [ ] **Step 3: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/state/job-history-store.ts
git commit -m "Add JobHistoryStore"
```

---

### Task 8: Frontend — `JobExecutionHistoryCardComponent`

**Goal:** A presentational card component for one execution history record.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/components/job-execution-history-card/job-execution-history-card.component.ts`
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/components/job-execution-history-card/job-execution-history-card.component.html`
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/components/job-execution-history-card/job-execution-history-card.component.scss`

**Acceptance Criteria:**
- [ ] Renders job name, status dot+label, invoked-by, formatted start timestamp, and `formatDuration(...)`.
- [ ] Shows the error message and a "stacktrace" button only when `status() === 'failed'`.
- [ ] "Metrics" button emits `displayJobMetrics` with the item's `metrics` field directly (no HTTP call).
- [ ] `npx ng build maia-jobs` succeeds.

**Verify:** `npx ng build maia-jobs` (from `libs/maia-ui-workspace`) → build succeeds

**Steps:**

- [ ] **Step 1: Create the component class**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/components/job-execution-history-card/job-execution-history-card.component.ts`:

```typescript
import {Component, computed, input, output} from '@angular/core';
import {DatePipe} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {JobExecutionHistoryItem} from '../../../jobs-dashboard/models/JobExecutionHistoryItem';
import {deriveHistoryStatus, formatDuration, HistoryStatus} from '../../state/job-history-filtering';

const STATUS_COLORS: Record<HistoryStatus, string> = {
    running: '#2196f3',
    success: '#4caf50',
    failed: '#d32f2f',
};

const STATUS_LABELS: Record<HistoryStatus, string> = {
    running: 'Running',
    success: 'Success',
    failed: 'Failed',
};

@Component({
    selector: 'maia-job-execution-history-card',
    templateUrl: './job-execution-history-card.component.html',
    styleUrl: './job-execution-history-card.component.scss',
    imports: [MatButtonModule, DatePipe]
})
export class JobExecutionHistoryCardComponent {


    historyItem = input.required<JobExecutionHistoryItem>();


    displayStackTrace = output<string>();

    displayJobMetrics = output<any>();


    status = computed<HistoryStatus>(() => deriveHistoryStatus(this.historyItem()));

    statusColor = computed<string>(() => STATUS_COLORS[this.status()]);

    statusLabel = computed<string>(() => STATUS_LABELS[this.status()]);

    durationLabel = computed<string>(() =>
        formatDuration(this.historyItem().startTimestamp, this.historyItem().endTimestamp)
    );


    onDisplayStackTrace() {
        this.displayStackTrace.emit(this.historyItem().jobExecutionId);
    }


    onDisplayJobMetrics() {
        this.displayJobMetrics.emit(this.historyItem().metrics);
    }


}
```

- [ ] **Step 2: Create the template**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/components/job-execution-history-card/job-execution-history-card.component.html`:

```html
<div class="card">
    <div class="header">
        <span class="name">{{ historyItem().jobName }}</span>
        <span
            class="status-dot"
            [style.background-color]="statusColor()"
            role="img"
            [attr.aria-label]="statusLabel()">
        </span>
    </div>

    <div class="meta-row">
        <span>Invoked by {{ historyItem().invokedBy }}</span>
        <span>Started {{ historyItem().startTimestamp | date:'medium' }}</span>
        <span>{{ durationLabel() }}</span>
    </div>

    @if (status() === 'failed') {
        <div class="failure-row">
            <span>{{ historyItem().errorMessage }}</span>
            <button mat-flat-button type="button" (click)="onDisplayStackTrace()">stacktrace</button>
        </div>
    }

    <div class="actions">
        <button mat-flat-button (click)="onDisplayJobMetrics()">Metrics</button>
    </div>
</div>
```

- [ ] **Step 3: Create the styles**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/components/job-execution-history-card/job-execution-history-card.component.scss`:

```scss
.card {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    border-radius: 0.5rem;
    border: 1px solid var(--mat-sys-outline-variant);
    padding: 0.875rem;
}

.header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
}

.name {
    font-weight: 500;
}

.status-dot {
    display: inline-block;
    flex-shrink: 0;
    width: 0.625rem;
    height: 0.625rem;
    border-radius: 9999px;
}

.meta-row {
    display: flex;
    align-items: center;
    gap: 1.25rem;
    font-size: 0.75rem;
    color: var(--mat-sys-on-surface-variant);
}

.failure-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
    border-radius: 0.25rem;
    background-color: var(--mat-sys-error-container);
    color: var(--mat-sys-on-error-container);
    padding: 0.375rem 0.625rem;
    font-size: 0.75rem;
}

.actions {
    display: flex;
    justify-content: flex-end;
}
```

- [ ] **Step 4: Build to type-check**

Run (from `libs/maia-ui-workspace`): `npx ng build maia-jobs`
Expected: build succeeds

- [ ] **Step 5: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/components/job-execution-history-card/
git commit -m "Add JobExecutionHistoryCardComponent"
```

---

### Task 9: Frontend — `JobHistoryPageComponent`

**Goal:** The page component: filter bar (job name/status/date range), paginator, and a single-column stack of history cards.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/job-history-page.component.ts`
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/job-history-page.component.html`
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/job-history-page.component.scss`

**Acceptance Criteria:**
- [ ] Filter bar has job-name select (populated from `store.availableJobNames()`), status select (All/Running/Success/Failed), and From/To date pickers.
- [ ] `mat-paginator` is bound to `store.totalCount()`/`pageIndex`/`pageSize`, options `[10, 20, 50]`.
- [ ] Cards render one per row (vertical stack, not a grid).
- [ ] Loading spinner / error banner shown only when there's no data yet (`items().length === 0`); once data has loaded, a later error leaves the list showing.
- [ ] `npx ng build maia-jobs` succeeds.

**Verify:** `npx ng build maia-jobs` (from `libs/maia-ui-workspace`) → build succeeds

**Steps:**

- [ ] **Step 1: Create the component class**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/job-history-page.component.ts`:

```typescript
import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatButtonModule} from '@angular/material/button';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {JobExecutionHistoryCardComponent} from './components/job-execution-history-card/job-execution-history-card.component';
import {JobsApiService} from '../jobs-dashboard/services/jobs-api.service';
import {StacktraceDialogComponent} from '../jobs-dashboard/dialogs/stacktrace-dialog/stacktrace-dialog.component';
import {JobMetricsDialogComponent} from '../jobs-dashboard/dialogs/job-metrics-dialog/job-metrics-dialog.component';
import {JobHistoryStore} from './state/job-history-store';
import {HistoryStatusFilter} from './state/job-history-filtering';

@Component({
    imports: [
        JobExecutionHistoryCardComponent, MatFormFieldModule, MatSelectModule, MatDatepickerModule,
        MatButtonModule, MatPaginatorModule, MatProgressSpinnerModule
    ],
    providers: [JobsApiService, JobHistoryStore],
    selector: 'maia-job-history-page',
    templateUrl: './job-history-page.component.html',
    styleUrl: './job-history-page.component.scss'
})
export class JobHistoryPageComponent implements OnInit {


    readonly store = inject(JobHistoryStore);

    private dialog = inject(MatDialog);

    private jobsService = inject(JobsApiService);


    ngOnInit() {
        this.store.init();
    }


    onJobNameFilterChanged(value: string | null) {
        this.store.onJobNameFilterChanged(value);
    }


    onStatusFilterChanged(value: HistoryStatusFilter) {
        this.store.onStatusFilterChanged(value);
    }


    onFromDateChanged(date: Date | null) {
        this.store.onDateRangeChanged(this.toStartOfDayIso(date), this.store.toDate());
    }


    onToDateChanged(date: Date | null) {
        this.store.onDateRangeChanged(this.store.fromDate(), this.toEndOfDayIso(date));
    }


    onPageChanged(event: PageEvent) {
        this.store.onPageChanged(event.pageIndex, event.pageSize);
    }


    onDisplayStackTrace(jobExecutionId: string) {

        this.jobsService.getStacktrace(jobExecutionId).subscribe(res => {
            this.dialog.open(StacktraceDialogComponent, {data: res.stacktrace});
        });

    }


    onDisplayJobMetrics(metrics: any) {

        this.dialog.open(JobMetricsDialogComponent, {data: metrics});

    }


    private toStartOfDayIso(date: Date | null): string | null {

        if (!date) {
            return null;
        }

        const startOfDay = new Date(date);
        startOfDay.setHours(0, 0, 0, 0);
        return startOfDay.toISOString();

    }


    private toEndOfDayIso(date: Date | null): string | null {

        if (!date) {
            return null;
        }

        const endOfDay = new Date(date);
        endOfDay.setHours(23, 59, 59, 999);
        return endOfDay.toISOString();

    }


}
```

- [ ] **Step 2: Create the template**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/job-history-page.component.html`:

```html
<div class="filter-bar">
    <mat-form-field subscriptSizing="dynamic" class="filter-field">
        <mat-label>Job name</mat-label>
        <mat-select [value]="store.jobNameFilter()" (selectionChange)="onJobNameFilterChanged($event.value)">
            <mat-option [value]="null">All jobs</mat-option>
            @for (jobName of store.availableJobNames(); track jobName) {
                <mat-option [value]="jobName">{{ jobName }}</mat-option>
            }
        </mat-select>
    </mat-form-field>

    <mat-form-field subscriptSizing="dynamic" class="filter-field">
        <mat-label>Status</mat-label>
        <mat-select [value]="store.statusFilter()" (selectionChange)="onStatusFilterChanged($event.value)">
            <mat-option [value]="null">All statuses</mat-option>
            <mat-option value="RUNNING">Running</mat-option>
            <mat-option value="SUCCESS">Success</mat-option>
            <mat-option value="FAILED">Failed</mat-option>
        </mat-select>
    </mat-form-field>

    <mat-form-field subscriptSizing="dynamic" class="filter-field">
        <mat-label>From</mat-label>
        <input matInput [matDatepicker]="fromPicker" (dateChange)="onFromDateChanged($event.value)">
        <mat-datepicker-toggle matIconSuffix [for]="fromPicker"></mat-datepicker-toggle>
        <mat-datepicker #fromPicker></mat-datepicker>
    </mat-form-field>

    <mat-form-field subscriptSizing="dynamic" class="filter-field">
        <mat-label>To</mat-label>
        <input matInput [matDatepicker]="toPicker" (dateChange)="onToDateChanged($event.value)">
        <mat-datepicker-toggle matIconSuffix [for]="toPicker"></mat-datepicker-toggle>
        <mat-datepicker #toPicker></mat-datepicker>
    </mat-form-field>
</div>

@if (store.isLoading() && store.items().length === 0) {
    <div class="loading">
        <mat-spinner diameter="40"></mat-spinner>
    </div>
} @else if (store.error() && store.items().length === 0) {
    <div class="error-banner" role="alert">
        <span>{{ store.error() }}</span>
        <button mat-flat-button color="primary" (click)="store.retryFetch()">Retry</button>
    </div>
} @else if (store.items().length === 0) {
    <p class="empty-message">No executions match your filters.</p>
} @else {
    <div class="history-list">
        @for (item of store.items(); track item.jobExecutionId) {
            <maia-job-execution-history-card
                [historyItem]="item"
                (displayStackTrace)="onDisplayStackTrace($event)"
                (displayJobMetrics)="onDisplayJobMetrics($event)">
            </maia-job-execution-history-card>
        }
    </div>

    <mat-paginator
        [length]="store.totalCount()"
        [pageIndex]="store.pageIndex()"
        [pageSize]="store.pageSize()"
        [pageSizeOptions]="[10, 20, 50]"
        (page)="onPageChanged($event)">
    </mat-paginator>
}
```

- [ ] **Step 3: Create the styles**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/job-history-page.component.scss`:

```scss
.filter-bar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 1rem;
    padding-bottom: 0.625rem;
    border-bottom: 1px solid var(--mat-sys-outline-variant);
}

.filter-field {
    flex: 1;
    max-width: 220px;
}

.loading {
    display: flex;
    justify-content: center;
    padding: 2.5rem 0;
}

.error-banner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 1.25rem;
    border-radius: 0.25rem;
    border: 1px solid var(--mat-sys-error);
    background-color: var(--mat-sys-error-container);
    color: var(--mat-sys-on-error-container);
    padding: 0.875rem;
}

.empty-message {
    margin-top: 1.25rem;
    color: var(--mat-sys-on-surface-variant);
}

.history-list {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    margin-top: 1.25rem;
}
```

- [ ] **Step 4: Build to type-check**

Run (from `libs/maia-ui-workspace`): `npx ng build maia-jobs`
Expected: build succeeds

- [ ] **Step 5: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/job-history-page.component.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/job-history-page.component.html \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/job-history/job-history-page.component.scss
git commit -m "Add JobHistoryPageComponent"
```

---

### Task 10: Frontend — export new page, add "View History" link on dashboard

**Goal:** Make the new page consumable from `@maia/maia-jobs`, and add navigation to it from the existing dashboard.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/public-api.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.html`
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.scss`

**Acceptance Criteria:**
- [ ] `public-api.ts` exports `JobExecutionHistoryItem`, `SearchResultPage`, `JobExecutionHistoryCardComponent`, `JobHistoryPageComponent`.
- [ ] The dashboard's toolbar shows a "View History" link/button routing to `/jobs-history`.
- [ ] `npx ng build maia-jobs` succeeds.

**Verify:** `npx ng build maia-jobs` (from `libs/maia-ui-workspace`) → build succeeds

**Steps:**

- [ ] **Step 1: Add exports to `public-api.ts`**

In `libs/maia-ui-workspace/projects/maia-jobs/src/public-api.ts`, add these lines after the existing `JobExecutionSummary`/`JobState` exports:

```typescript
export * from './lib/jobs-dashboard/models/JobExecutionHistoryItem';
export * from './lib/jobs-dashboard/models/SearchResultPage';
export * from './lib/job-history/components/job-execution-history-card/job-execution-history-card.component';
export * from './lib/job-history/job-history-page.component';
```

- [ ] **Step 2: Add `RouterLink` to the dashboard page component**

In `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.ts`, add the import:

```typescript
import {RouterLink} from '@angular/router';
```

And add `RouterLink` to the `imports` array in the `@Component` decorator:

```typescript
    imports: [JobStateComponent, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule, MatButtonModule, RouterLink],
```

- [ ] **Step 3: Add the "View History" link to the toolbar**

In `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.html`, replace:

```html
<div class="toolbar">
    <span class="count">{{ store.countSummary() }}</span>
</div>
```

with:

```html
<div class="toolbar">
    <span class="count">{{ store.countSummary() }}</span>
    <a mat-stroked-button routerLink="/jobs-history">View History</a>
</div>
```

- [ ] **Step 4: Update the toolbar layout to space the two elements apart**

In `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.scss`, change:

```scss
.toolbar {
    display: flex;
    align-items: center;
    justify-content: flex-end;
}
```

to:

```scss
.toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
}
```

- [ ] **Step 5: Build to type-check**

Run (from `libs/maia-ui-workspace`): `npx ng build maia-jobs`
Expected: build succeeds

- [ ] **Step 6: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/public-api.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.html \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.scss
git commit -m "Export job history page and link to it from the jobs dashboard"
```

---

### Task 11: Showcase app — route, page wrapper, nav menu link

**Goal:** Wire the new library page into the showcase app so it's reachable in the browser.

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/job-history/job-history-page.ts`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.html`

**Acceptance Criteria:**
- [ ] Navigating to `/jobs-history` renders `<maia-job-history-page>` inside `<maia-page-layout pageTitle="Job History">`.
- [ ] The nav menu has a "Job History" item linking to `/jobs-history`, next to "Jobs Dashboard".
- [ ] `npx ng build maia-showcase-ui` succeeds.

**Verify:** `npx ng build` (from `maia-showcase/maia-showcase-ui` — its own separate Angular workspace/`package.json`, distinct from `libs/maia-ui-workspace`) → build succeeds

**Note:** `maia-showcase-ui`'s `tsconfig.json` resolves `@maia/maia-jobs` to `../../libs/maia-ui-workspace/dist/maia-jobs` — the **built output**, not the library source. Task 10's Step 5 (`npx ng build maia-jobs`) must have already run and succeeded so that `dist/maia-jobs` contains `JobHistoryPageComponent` before this task's build can succeed.

**Steps:**

- [ ] **Step 1: Create the showcase page wrapper**

Create `maia-showcase/maia-showcase-ui/src/app/pages/job-history/job-history-page.ts`:

```typescript
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@maia/maia-ui';
import {JobHistoryPageComponent} from '@maia/maia-jobs';

@Component({
    imports: [PageLayout, JobHistoryPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <maia-page-layout pageTitle="Job History">
            <maia-job-history-page />
        </maia-page-layout>
    `
})
export class JobHistoryPage {}
```

- [ ] **Step 2: Add the route**

In `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`, add this entry to the `routes` array, directly after the `jobs-dashboard` route:

```typescript
    {
        path: 'jobs-history',
        loadComponent: () =>
            import('./pages/job-history/job-history-page').then(
                (m) => m.JobHistoryPage,
            ),
    },
```

- [ ] **Step 3: Add the nav menu item**

In `maia-showcase/maia-showcase-ui/src/app/app.html`, the existing "Jobs Dashboard" item (lines 42-47) is gated behind `hasMaiaJobWriteAuthority()` (since it can trigger job runs), but the history page only needs read access — `AppComponent` (`app.ts:53-54`) already exposes an unused `hasMaiaJobReadAuthority` computed for exactly this. Add a new, separately-gated block directly after the "Jobs Dashboard" block's closing `}`:

```html
        @if (hasMaiaJobReadAuthority()) {
            <button mat-menu-item routerLink="/jobs-history">
                <mat-icon>storage</mat-icon>
                <span>Job History</span>
            </button>
        }
```

- [ ] **Step 4: Build the showcase app**

Run (from `maia-showcase/maia-showcase-ui`): `npx ng build`
Expected: build succeeds

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/pages/job-history/job-history-page.ts \
        maia-showcase/maia-showcase-ui/src/app/app.routes.ts \
        maia-showcase/maia-showcase-ui/src/app/app.html
git commit -m "Wire job history page into the showcase app"
```

---

### Task 12: End-to-end manual verification

**Goal:** Confirm the whole feature works against a real running backend + frontend, not just compiles.

**Files:** None (verification only).

**Acceptance Criteria:**
- [ ] Backend starts and `GET /api/ops/job/execution_history` returns data for at least: no filters, `jobName=<a real job>`, each of `status=RUNNING|SUCCESS|FAILED`, and a `from`/`to` range.
- [ ] Showcase app: "Job History" is reachable from the nav menu and from the dashboard's "View History" button.
- [ ] Cards render one per row (not a grid/table), each showing status, job name, invoked-by, timestamps, and duration.
- [ ] Filters and paginator correctly narrow/paginate results; changing a filter resets to page 0, changing only the page does not.
- [ ] "stacktrace" button on a failed card opens `StacktraceDialogComponent` with real stacktrace content; "Metrics" button opens `JobMetricsDialogComponent` with the execution's metrics.
- [ ] Empty-filter-match state and the error-banner-with-Retry state (test by stopping the backend) both render correctly.

**Verify:** Manual — no single command; see steps below.

**Steps:**

- [ ] **Step 1: Start the backend**

Follow this repo's normal local-dev startup for the showcase backend (Postgres via `docker compose -f maia-showcase/compose.yaml up -d`, then run the showcase Spring Boot app). Confirm `MAIA_JOB_READ`/`MAIA_JOB_WRITE` authorities are available to the logged-in user, same as required for the existing jobs dashboard.

- [ ] **Step 2: Run some jobs to generate history data**

Via the existing Jobs Dashboard page's "Run..." button, trigger at least one job to completion (success) and, if possible, one that fails, so the history endpoint has more than just `RUNNING` rows to show.

- [ ] **Step 3: Start the showcase frontend**

Run (from `maia-showcase/maia-showcase-ui`): `npx ng serve` (its `package.json` `start` script is `ng serve`). Since it depends on `@maia/maia-jobs` via the built `dist/maia-jobs` (see Task 11's note), rebuild that first if it isn't already current: `npx ng build maia-jobs` (from `libs/maia-ui-workspace`).

- [ ] **Step 4: Walk through the acceptance criteria above in a browser**

Open the served app, navigate to `/jobs-history` via both entry points, and manually check every acceptance criterion listed above. Take note of and fix anything that doesn't match — this step is what actually validates the feature, not the earlier compile-only steps.

- [ ] **Step 5: No commit for this task** (verification only — if Step 4 surfaces bugs, fix them in the relevant earlier task's files and commit those fixes separately).

---

## Task dependency order

0 → 1, 2 (2 also needs 0) → 3
4 → 5, 6 (6 also needs 4) → 7 (needs 4, 5, 6) → 8 (needs 6) → 9 (needs 7, 8) → 10 (needs 9) → 11 (needs 9/10)
12 needs 3 and 11 (both backend and full frontend wiring complete)
