# Jobs Dashboard UX Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the Jobs dashboard in `libs/maia-ui-workspace/projects/maia-jobs` with a card grid, a name filter, a single running/failed/idle status per job, elapsed running time, 15s auto-refresh polling, and a batched backend response that eliminates the current N+1 API call pattern.

**Architecture:** Same shape as the already-completed elastic-indices dashboard redesign: a pure-function module (`jobs-filtering.ts`) tested with plain Vitest, an NgRx signal store (`JobsDashboardStore`) that delegates to it and adds polling, a presentational card component, and a page component composing header/filter/state-priority content. On the backend, `JobStateResponseDto` gains a `recentlyFailedExecutions` field (via the `maia-job-spec` code-generation DSL, not a hand-written DTO), and the per-job N+1 query is replaced with one batched query grouped in the service layer.

**Tech Stack:** Kotlin/Spring Boot (backend, code-generated DTOs via the Maia Framework generator), Angular 21 (standalone components, signals, new control-flow syntax), `@ngrx/signals`, Angular Material, Tailwind, Vitest.

**User Verification:** NO — no human sign-off checkpoints were requested; verification is via automated build/tests plus the manual browser check in the final task.

**Important — read before starting Task 1:** The design spec (`docs/superpowers/specs/2026-09-13-jobs-dashboard-ux-design.md`) was amended twice while writing this plan, after discovering: (1) the response DTOs are code-generated with **alphabetically-ordered constructor parameters**, not declaration order — get this wrong and it won't compile; (2) naively batching the old per-job `PageRequest.of(0, 10, ...)` limit across all jobs would let one noisy job crowd out others, so the query fetches a bounded window (200 rows) and truncates to 10 per job in the service layer; (3) the frontend `JobExecutionState` model is currently **wrong** (fields that don't exist on the real backend DTO, and a misnamed field) — fixing it is required for the elapsed-time feature to work, not optional cleanup. All three are already reflected in the task code below — this note is just so you don't second-guess it as an error in the plan.

---

## Task 1: Backend — batch recently-failed executions into `JobState`

**Goal:** Add `recentlyFailedExecutions` to the generated `JobStateResponseDto`, replace the per-job N+1 query with one batched query, and remove the now-unused single-job endpoint.

**Files:**
- Modify: `libs/maia-job-parent/maia-job-spec/src/main/kotlin/org/maiaframework/job/spec/MaiaJobSpec.kt`
- Regenerate (do not hand-edit): `libs/maia-job-parent/maia-job-domain/src/generated/kotlin/main/org/maiaframework/job/JobStateResponseDto.kt`
- Modify: `libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/JobExecutionRepo.kt`
- Modify: `libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/MaiaJobService.kt`
- Modify: `libs/maia-job-parent/maia-job-web/src/main/kotlin/org/maiaframework/job/MaiaJobEndpoint.kt`

**Acceptance Criteria:**
- [ ] `JobStateResponseDto` has a `recentlyFailedExecutions: List<JobExecutionSummaryResponseDto>` field (generated, not hand-written).
- [ ] `MaiaJobService.getAllJobsCurrentState()` fetches recent failures for all jobs in a single query, groups by job name, and truncates to the 10 most recent per job.
- [ ] `JobExecutionRepo.recentFailedExecutions` takes `Iterable<JobName>` (not a single `JobName`), queries with `filters.jobName in jobNames`, and bounds the query with `PageRequest.of(0, 200, sort)`.
- [ ] `GET /job/recently_failed/{jobName}` and its backing service/endpoint methods are deleted.
- [ ] Everything compiles.

**Verify:** From the repo root (`/home/kevin/dev/code/maia`): `./gradlew :libs:maia-job-parent:maia-job-domain:maiaGeneration :libs:maia-job-parent:maia-job:compileKotlin :libs:maia-job-parent:maia-job-web:compileKotlin` → BUILD SUCCESSFUL, and `JobStateResponseDto.kt`'s constructor now includes `recentlyFailedExecutions` in alphabetical position.

**Steps:**

- [ ] **Step 1: Edit the spec to add the new field**

Replace the full contents of `libs/maia-job-parent/maia-job-spec/src/main/kotlin/org/maiaframework/job/spec/MaiaJobSpec.kt` with (note `jobExecutionSummaryDtoDef` has moved above `jobStateDtoDef`, since spec-DSL fields must reference an already-declared `val`, and `jobStateDtoDef` gained one new `field(...)` line):

```kotlin
@file:Suppress("MemberVisibilityCanBePrivate")

package org.maiaframework.job.spec


import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes

@Suppress("unused")
class MaiaJobSpec: AbstractSpec(appKey = AppKey("jobs"), defaultSchemaName = SchemaName("jobs")) {


    val readAuthority = authority("MAIA_JOB_READ")


    val writeAuthority = authority("MAIA_JOB_WRITE")


    val jobNameStringType = stringType("org.maiaframework.job.JobName") {
        provided()
    }

    val jobCompletionStatusEnumDef = enumDef("org.maiaframework.job.JobCompletionStatus") {
        provided()
    }

    val jobExecutionEntityDef = entity("org.maiaframework.job", "JobExecution") {
        daoHasSpringAnnotation = false
        field("jobName", jobNameStringType) {
            lengthConstraint(max = 100)
        }
        field("invokedBy", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
        field("startTimestamp", FieldTypes.instant)
        field("endTimestamp", FieldTypes.instant) {
            nullable()
            modifiableBySystem()
        }
        field("completionStatus", jobCompletionStatusEnumDef) {
            nullable()
            modifiableBySystem()
            lengthConstraint(max = 50)
        }
        field("metrics", FieldTypes.mapOfStringToAny()) {
            modifiableBySystem()
        }
        field("errorMessage", FieldTypes.string) {
            nullable()
            modifiableBySystem()
            lengthConstraint(max = 1000)
        }
        field("stackTrace", FieldTypes.string) {
            nullable()
            modifiableBySystem()
            lengthConstraint(max = 10_000)
        }
        field_lastModifiedTimestamp()
        index {
            indexName("jobName_idx")
            withFieldAscending("jobName")
        }
    }


    val runningJobStateDtoDef = simpleResponseDto("org.maiaframework.job", "RunningJobState") {
        field("id", FieldTypes.domainId)
        field("jobName", jobNameStringType)
        field("invokedBy", FieldTypes.string)
        field("startTimestamp", FieldTypes.instant)
        field("metrics", FieldTypes.mapOfStringToAny())
    }


    val jobExecutionSummaryDtoDef = simpleResponseDto("org.maiaframework.job", "JobExecutionSummary") {
        field("jobExecutionId", FieldTypes.domainId)
        field("jobName", jobNameStringType)
        field("startTimestamp", FieldTypes.instant)
        field("endTimestamp", FieldTypes.instant) {
            nullable()
        }
        field("errorMessage", FieldTypes.string) {
            nullable()
        }
    }


    val jobStateDtoDef = simpleResponseDto("org.maiaframework.job", "JobState") {
        field("jobName", jobNameStringType)
        field("description", FieldTypes.string) {
            nullable()
        }
        field("runningJobs", fieldListOf(runningJobStateDtoDef))
        field("recentlyFailedExecutions", fieldListOf(jobExecutionSummaryDtoDef))
    }


    val jobExecutionDetailDtoDef = simpleResponseDto("org.maiaframework.job", "JobExecutionDetail") {
        field("jobExecutionId", FieldTypes.domainId)
        field("jobName", jobNameStringType)
        field("startTimestamp", FieldTypes.instant)
        field("endTimestamp", FieldTypes.instant) {
            nullable()
        }
        field("errorMessage", FieldTypes.string) {
            nullable()
        }
        field("stackTrace", FieldTypes.string) {
            nullable()
        }
    }


}
```

- [ ] **Step 2: Regenerate `maia-job-domain`**

Run (from `/home/kevin/dev/code/maia`): `./gradlew :libs:maia-job-parent:maia-job-domain:maiaGeneration`

Expected: BUILD SUCCESSFUL. Then read `libs/maia-job-parent/maia-job-domain/src/generated/kotlin/main/org/maiaframework/job/JobStateResponseDto.kt` and confirm the constructor is now (alphabetical by field name):

```kotlin
data class JobStateResponseDto(
    val description: String?,
    val jobName: JobName,
    val recentlyFailedExecutions: List<JobExecutionSummaryResponseDto>,
    val runningJobs: List<RunningJobStateResponseDto>
) {


}
```

If the order differs from this, STOP and report — do not proceed to Step 3 with a guessed constructor order; re-read the actual generated file and adjust Step 3's code to match exactly.

- [ ] **Step 3: Update `JobExecutionRepo` to batch the query**

Replace the full contents of `libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/JobExecutionRepo.kt` with:

```kotlin
package org.maiaframework.job

import org.maiaframework.common.ExceptionUtil
import org.maiaframework.domain.DomainId
import org.maiaframework.metrics.JobMetrics
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant

private const val RECENT_FAILURES_QUERY_LIMIT = 200

class JobExecutionRepo(private val jobExecutionDao: JobExecutionDao) {


    fun newJobExecution(
        jobInstanceId: DomainId,
        jobName: JobName,
        invokedBy: String
    ) {

        val now = Instant.now()

        val entity = JobExecutionEntity(
                null,
                now,
                null,
                null,
                jobInstanceId,
                invokedBy,
                jobName,
                now,
                emptyMap(),
                null,
                now)

        this.jobExecutionDao.insert(entity)

    }


    fun jobFailed(jobInstanceId: DomainId, jobMetrics: JobMetrics, e: Exception) {

        updateJobExecution(jobInstanceId, JobCompletionStatus.FAILED, jobMetrics, e)

    }


    fun jobCompleted(jobInstanceId: DomainId, jobMetrics: JobMetrics) {

        updateJobExecution(jobInstanceId, JobCompletionStatus.SUCCESS, jobMetrics)

    }


    private fun updateJobExecution(
        jobInstanceId: DomainId,
        completionStatus: JobCompletionStatus,
        jobMetrics: JobMetrics,
        e: Exception? = null
    ) {

        val updater = JobExecutionEntityUpdater.forPrimaryKey(jobInstanceId) {
            completionStatus(completionStatus)
            endTimestamp(Instant.now())
            metrics(jobMetrics.metricsReport())

            if (e != null) {
                errorMessage(e.message?.take(10_000))
                stackTrace(ExceptionUtil.stackTrace(e).take(10_000))
            }

        }

        this.jobExecutionDao.setFields(updater)

    }


    fun recentFailedExecutions(jobNames: Iterable<JobName>): List<JobExecutionEntity> {

        val jobNameList = jobNames.toList()

        if (jobNameList.isEmpty()) {
            return emptyList()
        }

        val filters = JobExecutionEntityFilters()
        val filter = filters.and(
                filters.jobName `in` jobNameList,
                filters.completionStatus eq JobCompletionStatus.FAILED
        )

        val sort = Sort.by(Sort.Order.desc(JobExecutionEntityMeta.endTimestamp))
        val pageRequest = PageRequest.of(0, RECENT_FAILURES_QUERY_LIMIT, sort)
        return this.jobExecutionDao.findAllBy(filter, pageRequest).toList()

    }


    fun findStacktraceForJob(jobExecutionId: DomainId): String? {

        return this.jobExecutionDao.findByPrimaryKey(jobExecutionId).stackTrace

    }


    fun findJobExecutionDetail(jobExecutionId: DomainId): JobExecutionEntity? {

        return this.jobExecutionDao.findByPrimaryKeyOrNull(jobExecutionId)

    }


}
```

- [ ] **Step 4: Update `MaiaJobService`**

Replace the full contents of `libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/MaiaJobService.kt` with:

```kotlin
package org.maiaframework.job

import org.maiaframework.common.util.NamedThreadFactory
import org.maiaframework.metrics.JobMetrics
import org.maiaframework.domain.DomainId
import org.slf4j.LoggerFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.Instant
import java.util.concurrent.Executors

private const val MAX_RECENT_FAILURES_PER_JOB = 10


class MaiaJobService(
    private val maiaJobRegistry: MaiaJobRegistry,
    private val jobExecutionRepo: JobExecutionRepo
) {


    private val logger = LoggerFactory.getLogger(MaiaJobService::class.java)


    private val executor = Executors.newCachedThreadPool(NamedThreadFactory("maia-job"))


    private val runningJobs: MultiValueMap<JobName, RunningJob> = LinkedMultiValueMap()


    fun getAllJobsCurrentState(): List<JobStateResponseDto> {

        val allJobDescriptions = this.maiaJobRegistry.getAllJobDescriptions()
        val jobNames = allJobDescriptions.map { it.jobName }
        val recentFailuresByJobName = this.jobExecutionRepo.recentFailedExecutions(jobNames)
                .groupBy { it.jobName }

        return allJobDescriptions.map { jobDescription ->

            val runningJobStateDtos = getRunningJobsState(jobDescription.jobName)
            val recentFailureDtos = (recentFailuresByJobName[jobDescription.jobName] ?: emptyList())
                    .take(MAX_RECENT_FAILURES_PER_JOB)
                    .map { toJobExecutionSummaryDto(it) }

            JobStateResponseDto(
                    jobDescription.description,
                    jobDescription.jobName,
                    recentFailureDtos,
                    runningJobStateDtos)

        }

    }


    private fun getRunningJobsState(jobName: JobName): List<RunningJobStateResponseDto> {

        val runningJobs: List<RunningJob> = this.runningJobs[jobName] ?: emptyList()

        return runningJobs.map {
            RunningJobStateResponseDto(
                    it.id,
                    it.invokedBy,
                    it.jobName,
                    it.jobMetrics.metricsReport(),
                    it.startTimestamp
            )
        }

    }


    fun runJob(jobName: JobName, username: String): JobExecutionSummaryResponseDto {

        val jobInstanceId = DomainId.newId()
        val startTimestamp = Instant.now()
        runJobAsync(jobName, jobInstanceId, username, startTimestamp)
        return JobExecutionSummaryResponseDto(
                null,
                null,
                jobInstanceId,
                jobName,
                startTimestamp,
        )

    }


    private fun runJobAsync(
            jobName: JobName,
            jobInstanceId: DomainId,
            username: String,
            startTimestamp: Instant
    ) {

        this.executor.execute {

            logger.info("BEGIN: runJob('$jobName') with instanceId $jobInstanceId, invoked by $username")

            val jobMetrics = JobMetrics(jobName.value)

            initRunningJob(jobName, username, jobMetrics, jobInstanceId, startTimestamp)

            try {

                jobMetrics.timeInstanceOfJob {

                    val job = this.maiaJobRegistry.getJob(jobName)
                    job.executeJob(jobMetrics)

                }

                logger.info("Job completed ($jobName:$jobInstanceId)")

                this.jobExecutionRepo.jobCompleted(jobInstanceId, jobMetrics)

            } catch (e: Exception) {

                logger.error("Job $jobName with instanceId $jobInstanceId failed.", e)
                jobExecutionRepo.jobFailed(jobInstanceId, jobMetrics, e)

            } finally {

                val jobs = this.runningJobs[jobName]
                jobs?.removeIf { it.id == jobInstanceId }

                logger.info("job metrics for job $jobName with instanceId $jobInstanceId:\n${jobMetrics.getMetricsReportAsJson()}")

            }

        }

    }


    private fun initRunningJob(
        jobName: JobName,
        username: String,
        jobMetrics: JobMetrics,
        jobExecutionId: DomainId,
        startTimestamp: Instant
    ) {

        val runningJob = RunningJob(jobExecutionId, jobName, startTimestamp, username, jobMetrics)
        this.runningJobs[jobName] = runningJob
        this.jobExecutionRepo.newJobExecution(jobExecutionId, jobName, username)

    }


    private fun toJobExecutionSummaryDto(entity: JobExecutionEntity): JobExecutionSummaryResponseDto {

        return JobExecutionSummaryResponseDto(
                entity.endTimestamp,
                entity.errorMessage,
                entity.id,
                entity.jobName,
                entity.startTimestamp)

    }


    fun getJobExecutionDetailDto(jobExecutionId: DomainId): JobExecutionDetailResponseDto? {

        return getJobExecutionEntity(jobExecutionId)?.let {
            JobExecutionDetailResponseDto(
                    it.endTimestamp,
                    it.errorMessage,
                    it.id,
                    it.jobName,
                    it.stackTrace,
                    it.startTimestamp)
        }

    }


    fun getJobExecutionStacktrace(jobExecutionId: DomainId): String? {

        return getJobExecutionEntity(jobExecutionId)?.stackTrace

    }


    fun getJobExecutionEntity(jobExecutionId: DomainId): JobExecutionEntity? {

        return this.jobExecutionRepo.findJobExecutionDetail(jobExecutionId)

    }


}
```

(Removed: `getRecentFailures(jobName: JobName)`. Added: the `MAX_RECENT_FAILURES_PER_JOB` constant, the batched-and-grouped assembly in `getAllJobsCurrentState()`, and the extracted `toJobExecutionSummaryDto` helper reusing the exact same field mapping the old method used.)

- [ ] **Step 5: Remove the single-job endpoint**

In `libs/maia-job-parent/maia-job-web/src/main/kotlin/org/maiaframework/job/MaiaJobEndpoint.kt`, delete this method (lines 28–36 in the current file):

```kotlin
    @GetMapping("/job/recently_failed/{jobName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('MAIA_JOB_READ')")
    fun getRecentlyFailedExecutions(
        @PathVariable jobName: String
    ): List<JobExecutionSummaryResponseDto> {

        return this.jobService.getRecentFailures(JobName(jobName))

    }
```

Leave everything else in that file unchanged.

- [ ] **Step 6: Compile everything**

Run (from `/home/kevin/dev/code/maia`): `./gradlew :libs:maia-job-parent:maia-job:compileKotlin :libs:maia-job-parent:maia-job-web:compileKotlin`
Expected: BUILD SUCCESSFUL, no errors.

- [ ] **Step 7: Commit**

```bash
git add libs/maia-job-parent/maia-job-spec/src/main/kotlin/org/maiaframework/job/spec/MaiaJobSpec.kt \
        libs/maia-job-parent/maia-job-domain/src/generated \
        libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/JobExecutionRepo.kt \
        libs/maia-job-parent/maia-job/src/main/kotlin/org/maiaframework/job/MaiaJobService.kt \
        libs/maia-job-parent/maia-job-web/src/main/kotlin/org/maiaframework/job/MaiaJobEndpoint.kt
git commit -m "Batch recently-failed job execution queries into current_state response"
```

---

## Task 2: Frontend models + pure filtering/status/elapsed-time logic

**Goal:** Fix the `JobExecutionState` model to match the real backend DTO, add `recentlyFailedExecutions` to `JobState`, remove the now-dead `JobsApiService.getRecentlyFailedJobExecutions`, and add a unit-tested pure-function module for filtering, status derivation, count summaries, and elapsed-time formatting.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobExecutionState.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobState.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/services/jobs-api.service.ts`
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.ts`
- Test: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.spec.ts`

**Acceptance Criteria:**
- [ ] `JobExecutionState` has exactly `{ id, jobName, invokedBy, startTimestamp, metrics }` — matching `RunningJobStateResponseDto`, no invented fields.
- [ ] `JobState` has a `recentlyFailedExecutions: JobExecutionSummary[]` field.
- [ ] `JobsApiService.getRecentlyFailedJobExecutions` and the `/job/recently_failed/${jobName}` call are deleted.
- [ ] `filterAndSortByName` case-insensitively substring-matches `jobName`, sorted alphabetically.
- [ ] `deriveJobStatus` returns `'running'` when `runningJobs.length > 0` (regardless of failures), else `'failed'` when `recentlyFailedExecutions.length > 0`, else `'idle'`.
- [ ] `countByStatus` partitions a job list into running/failed/idle counts using `deriveJobStatus`.
- [ ] `buildJobCountSummary` formats `"N jobs"`/`"X of N jobs"` plus ordered (running, failed, idle) non-zero segments, singular `"job"` for a total of 1.
- [ ] `formatElapsed` formats `<60s` as `"Ns"`, `<1h` as `"Mm Ss"`, `>=1h` as `"Hh MMm"` (zero-padded minutes).

**Verify:** `npx ng test maia-jobs` (from `libs/maia-ui-workspace/`) → all tests in `jobs-filtering.spec.ts` pass.

**Steps:**

- [ ] **Step 1: Fix `JobExecutionState.ts`**

Replace the full contents of `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobExecutionState.ts` with:

```ts
export class JobExecutionState {
    id!: string;
    jobName!: string;
    invokedBy!: string;
    startTimestamp!: string;
    metrics!: any;
}
```

- [ ] **Step 2: Add the new field to `JobState.ts`**

Replace the full contents of `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobState.ts` with:

```ts
import {JobExecutionState} from './JobExecutionState';
import {JobExecutionSummary} from './JobExecutionSummary';

export class JobState {
    jobName!: string;
    jobDescription!: string;
    runningJobs!: JobExecutionState[];
    recentlyFailedExecutions!: JobExecutionSummary[];
}
```

- [ ] **Step 3: Remove the dead endpoint call from `JobsApiService`**

Replace the full contents of `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/services/jobs-api.service.ts` with:

```ts
import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {JobState} from '../models/JobState';
import {JobExecutionDetail} from '../models/JobExecutionDetail';
import {JOBS_API_BASE_URL} from './jobs-api-base-url.token';


@Injectable()
export class JobsApiService {


    private baseUrl = inject(JOBS_API_BASE_URL);


    constructor(private http: HttpClient) {}


    getJobsState(): Observable<JobState[]> {

        return this.http.get<JobState[]>(`${this.baseUrl}/jobs/current_state`).pipe(
            catchError(this.handleError<JobState[]>('getJobsState', []))
        );

    }


    getJobExecutionDetail(jobExecutionId: string): Observable<JobExecutionDetail> {

        return this.http.get<JobExecutionDetail>(`${this.baseUrl}/job/execution_detail/${jobExecutionId}`).pipe(
            catchError(this.handleError<JobExecutionDetail>('getJobExecutionDetail'))
        );

    }


    getStacktrace(jobExecutionId: string): Observable<any> {

        return this.http.get<any>(`${this.baseUrl}/job/execution_stacktrace/${jobExecutionId}`);

    }


    runJob(jobName: string): void {

        this.http.post(`${this.baseUrl}/job/run/${jobName}`, null).pipe(
            catchError(this.handleError('runJob'))
        ).subscribe();

    }


    private handleError<T>(operation = 'operation', result?: T) {

        return (error: any): Observable<T> => {
            console.error(error);
            return of(result as T);
        };

    }


}
```

- [ ] **Step 4: Write the failing test file**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.spec.ts`:

```ts
import {describe, expect, it} from 'vitest';
import {
    buildJobCountSummary,
    countByStatus,
    deriveJobStatus,
    filterAndSortByName,
    formatElapsed
} from './jobs-filtering';
import {JobState} from '../models/JobState';
import {JobExecutionState} from '../models/JobExecutionState';
import {JobExecutionSummary} from '../models/JobExecutionSummary';


function runningExecution(overrides: Partial<JobExecutionState> = {}): JobExecutionState {
    return {
        id: 'exec-1',
        jobName: 'some-job',
        invokedBy: 'someone',
        startTimestamp: '2026-01-01T00:00:00.000Z',
        metrics: {},
        ...overrides
    } as JobExecutionState;
}


function failedExecution(overrides: Partial<JobExecutionSummary> = {}): JobExecutionSummary {
    return {
        jobExecutionId: 'exec-failed-1',
        jobName: 'some-job',
        startTimestamp: '2026-01-01T00:00:00.000Z',
        endTimestamp: '2026-01-01T00:05:00.000Z',
        errorMessage: 'boom',
        ...overrides
    } as JobExecutionSummary;
}


function jobState(overrides: Partial<JobState> & {jobName: string}): JobState {
    return {
        jobDescription: '',
        runningJobs: [],
        recentlyFailedExecutions: [],
        ...overrides
    } as JobState;
}


describe('jobs-filtering', () => {


    describe('filterAndSortByName()', () => {

        it('returns all jobs sorted alphabetically when nameFilter is empty', () => {
            const jobs = [jobState({jobName: 'zeta-job'}), jobState({jobName: 'alpha-job'})];
            expect(filterAndSortByName(jobs, '').map((it) => it.jobName)).toEqual(['alpha-job', 'zeta-job']);
        });

        it('matches jobName case-insensitively', () => {
            const jobs = [jobState({jobName: 'Nightly-Reconciliation'}), jobState({jobName: 'weekly-export'})];
            expect(filterAndSortByName(jobs, 'nightly').map((it) => it.jobName)).toEqual(['Nightly-Reconciliation']);
        });

        it('excludes jobs whose name does not contain the filter', () => {
            const jobs = [jobState({jobName: 'nightly-reconciliation'}), jobState({jobName: 'weekly-export'})];
            expect(filterAndSortByName(jobs, 'zzz')).toEqual([]);
        });

    });


    describe('deriveJobStatus()', () => {

        it('returns "idle" when there are no running jobs and no recent failures', () => {
            expect(deriveJobStatus(jobState({jobName: 'a'}))).toEqual('idle');
        });

        it('returns "failed" when there are recent failures and nothing running', () => {
            expect(deriveJobStatus(jobState({jobName: 'a', recentlyFailedExecutions: [failedExecution()]}))).toEqual('failed');
        });

        it('returns "running" when there are running executions', () => {
            expect(deriveJobStatus(jobState({jobName: 'a', runningJobs: [runningExecution()]}))).toEqual('running');
        });

        it('returns "running" (not "failed") when both running executions and recent failures are present', () => {
            const state = jobState({
                jobName: 'a',
                runningJobs: [runningExecution()],
                recentlyFailedExecutions: [failedExecution()]
            });
            expect(deriveJobStatus(state)).toEqual('running');
        });

    });


    describe('countByStatus()', () => {

        it('partitions jobs into running/failed/idle buckets using deriveJobStatus priority', () => {
            const jobs = [
                jobState({jobName: 'a', runningJobs: [runningExecution()]}),
                jobState({jobName: 'b', recentlyFailedExecutions: [failedExecution()]}),
                jobState({jobName: 'c'}),
                jobState({jobName: 'd', runningJobs: [runningExecution()], recentlyFailedExecutions: [failedExecution()]})
            ];
            expect(countByStatus(jobs)).toEqual({running: 2, failed: 1, idle: 1});
        });

    });


    describe('buildJobCountSummary()', () => {

        it('shows a plain count with ordered status segments when the filter does not narrow the set', () => {
            expect(buildJobCountSummary(8, 8, {running: 2, failed: 1, idle: 5}))
                .toEqual('8 jobs · 2 running · 1 failed · 5 idle');
        });

        it('shows "X of Y" when the filter narrows the set', () => {
            expect(buildJobCountSummary(3, 8, {running: 2, idle: 1}))
                .toEqual('3 of 8 jobs · 2 running · 1 idle');
        });

        it('omits status segments with a zero count', () => {
            expect(buildJobCountSummary(5, 5, {idle: 5, running: 0}))
                .toEqual('5 jobs · 5 idle');
        });

        it('uses singular "job" when the total is 1', () => {
            expect(buildJobCountSummary(1, 1, {idle: 1})).toEqual('1 job · 1 idle');
        });

        it('shows just the count label when there are no status counts', () => {
            expect(buildJobCountSummary(0, 0, {})).toEqual('0 jobs');
        });

    });


    describe('formatElapsed()', () => {

        it('formats durations under a minute as seconds', () => {
            const start = '2026-01-01T00:00:00.000Z';
            const now = new Date('2026-01-01T00:00:42.000Z').getTime();
            expect(formatElapsed(start, now)).toEqual('42s');
        });

        it('formats durations under an hour as minutes and seconds', () => {
            const start = '2026-01-01T00:00:00.000Z';
            const now = new Date('2026-01-01T00:04:12.000Z').getTime();
            expect(formatElapsed(start, now)).toEqual('4m 12s');
        });

        it('formats durations of an hour or more as hours and zero-padded minutes', () => {
            const start = '2026-01-01T00:00:00.000Z';
            const now = new Date('2026-01-01T01:03:00.000Z').getTime();
            expect(formatElapsed(start, now)).toEqual('1h 03m');
        });

    });


});
```

- [ ] **Step 5: Run the test to verify it fails**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-jobs`
Expected: FAIL — `jobs-filtering.ts` does not exist.

- [ ] **Step 6: Write the implementation**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.ts`:

```ts
import {JobState} from '../models/JobState';

export type JobStatus = 'running' | 'failed' | 'idle';

const STATUS_DISPLAY_ORDER: JobStatus[] = ['running', 'failed', 'idle'];


export function filterAndSortByName(jobs: JobState[], nameFilter: string): JobState[] {
    const normalizedFilter = nameFilter.toLowerCase();
    return jobs
        .filter((it) => it.jobName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.jobName.localeCompare(b.jobName));
}


export function deriveJobStatus(jobState: JobState): JobStatus {
    if (jobState.runningJobs.length > 0) {
        return 'running';
    }
    if (jobState.recentlyFailedExecutions.length > 0) {
        return 'failed';
    }
    return 'idle';
}


export function countByStatus(jobs: JobState[]): Record<string, number> {
    const counts: Record<string, number> = {};
    for (const job of jobs) {
        const status = deriveJobStatus(job);
        counts[status] = (counts[status] ?? 0) + 1;
    }
    return counts;
}


export function buildJobCountSummary(
    visibleCount: number,
    totalCount: number,
    statusCounts: Record<string, number>
): string {
    const jobWord = totalCount === 1 ? 'job' : 'jobs';
    const countLabel = visibleCount === totalCount
        ? `${totalCount} ${jobWord}`
        : `${visibleCount} of ${totalCount} ${jobWord}`;

    const statusSegments = STATUS_DISPLAY_ORDER
        .filter((status) => (statusCounts[status] ?? 0) > 0)
        .map((status) => `${statusCounts[status]} ${status}`);

    return statusSegments.length === 0
        ? countLabel
        : `${countLabel} · ${statusSegments.join(' · ')}`;
}


export function formatElapsed(startTimestamp: string, now: number): string {
    const elapsedMs = Math.max(0, now - new Date(startTimestamp).getTime());
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

- [ ] **Step 7: Run the test to verify it passes**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-jobs`
Expected: PASS — all `jobs-filtering` tests green.

- [ ] **Step 8: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobExecutionState.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/models/JobState.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/services/jobs-api.service.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-filtering.spec.ts
git commit -m "Fix JobExecutionState model, add recentlyFailedExecutions, add pure jobs-filtering logic"
```

---

## Task 3: `JobsDashboardStore` with polling

**Goal:** Add an NgRx signal store wiring the pure functions from Task 2 to fetched job data, with 15s polling split from manual retry so one never disturbs the other.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-dashboard-store.ts`

**Acceptance Criteria:**
- [ ] State: `jobStates: JobState[]`, `isLoading: boolean`, `error: string | null`, `nameFilter: string`, `now: number`.
- [ ] Computed: `visibleJobStates` (name-filtered + sorted via `filterAndSortByName`), `statusCounts` (via `countByStatus` on `visibleJobStates`), `countSummary` (via `buildJobCountSummary`).
- [ ] `startPolling()`: fetches immediately, then every 15 seconds, forever, via `timer(0, 15000)`.
- [ ] `retryFetch()`: a one-shot fetch, independent of the polling timer — calling it does not reset the 15s schedule.
- [ ] No `distinctUntilChanged()` anywhere in the store (known pitfall from the sibling elastic-indices dashboard — silently swallows repeated void triggers).
- [ ] `onNameFilterChanged(value: string)`: direct `patchState`, no debounce.

**Verify:** `npx ng build maia-jobs` (from `libs/maia-ui-workspace/`) → builds with no TypeScript errors.

**Steps:**

- [ ] **Step 1: Create the store**

Create `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-dashboard-store.ts`:

```ts
import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap, timer} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {JobState} from '../models/JobState';
import {JobsApiService} from '../services/jobs-api.service';
import {buildJobCountSummary, countByStatus, filterAndSortByName} from './jobs-filtering';

const POLL_INTERVAL_MS = 15_000;

type JobsDashboardState = {
    jobStates: JobState[];
    isLoading: boolean;
    error: string | null;
    nameFilter: string;
    now: number;
};

const initialState: JobsDashboardState = {
    jobStates: [],
    isLoading: false,
    error: null,
    nameFilter: '',
    now: Date.now(),
};

export const JobsDashboardStore = signalStore(

    withState(initialState),

    withComputed(({jobStates, nameFilter}) => {
        const visibleJobStates = computed<JobState[]>(() =>
            filterAndSortByName(jobStates(), nameFilter())
        );
        const statusCounts = computed<Record<string, number>>(() =>
            countByStatus(visibleJobStates())
        );
        const countSummary = computed<string>(() =>
            buildJobCountSummary(visibleJobStates().length, jobStates().length, statusCounts())
        );
        return {visibleJobStates, statusCounts, countSummary};
    }),

    withMethods((store, jobsService = inject(JobsApiService)) => {

        const fetchOnce = rxMethod<void>(
            pipe(
                tap(() => patchState(store, {isLoading: true, now: Date.now()})),
                switchMap(() =>
                    jobsService.getJobsState().pipe(
                        tapResponse({
                            next: (jobStates) => patchState(store, {jobStates, isLoading: false, error: null}),
                            error: (err) => {
                                patchState(store, {isLoading: false, error: 'Failed to load jobs.'});
                                console.error(err);
                            },
                        })
                    )
                )
            )
        );

        return {

            startPolling: rxMethod<void>(
                pipe(
                    switchMap(() => timer(0, POLL_INTERVAL_MS).pipe(tap(() => fetchOnce())))
                )
            ),

            retryFetch(): void {
                fetchOnce();
            },

            onNameFilterChanged(value: string): void {
                patchState(store, {nameFilter: value});
            },

        };

    })

);
```

- [ ] **Step 2: Verify it compiles**

Run (from `libs/maia-ui-workspace/`): `npx ng build maia-jobs`
Expected: builds with no TypeScript errors. (No component references this store yet — that's Task 5 — so this just confirms the store file itself is well-typed.)

- [ ] **Step 3: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/state/jobs-dashboard-store.ts
git commit -m "Add JobsDashboardStore with 15s polling and manual retry"
```

---

## Task 4: Card component redesign

**Goal:** Replace `JobStateComponent`'s per-card API call and unstyled markup with a pure presentational card: status dot, inline running rows with elapsed time, and a collapsed-by-default failures disclosure.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/components/job-state/job-state.component.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/components/job-state/job-state.component.html`

**Acceptance Criteria:**
- [ ] No `OnInit`, `DestroyRef`, or `JobsApiService` dependency — the component only reads its `jobState`/`now` inputs.
- [ ] Status dot color: blue for `'running'`, red for `'failed'`, green for `'idle'` (via `deriveJobStatus`), with an `aria-label` (no visible text label).
- [ ] One row per entry in `runningJobs`, each showing `"Running for " + formatElapsed(execution.startTimestamp, now())` and the existing "Metrics" button.
- [ ] When not running: a "Not currently running" line.
- [ ] When `recentlyFailedExecutions.length > 0`: a closed-by-default toggle ("1 recent failure" / "`N` recent failures") that expands to the existing per-failure list (error message + "stacktrace" button).
- [ ] "Run..." button and its `(click)` handler unchanged in behavior.

**Verify:** `npx ng build maia-jobs` (from `libs/maia-ui-workspace/`) → builds with no errors.

**Steps:**

- [ ] **Step 1: Replace the component class**

Replace the full contents of `job-state.component.ts` with:

```ts
import {Component, computed, input, output, signal} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {JobState} from '../../models/JobState';
import {JobExecutionState} from '../../models/JobExecutionState';
import {deriveJobStatus, formatElapsed, JobStatus} from '../../state/jobs-filtering';

const STATUS_COLORS: Record<JobStatus, string> = {
    running: '#2196f3',
    failed: '#d32f2f',
    idle: '#4caf50',
};

const STATUS_LABELS: Record<JobStatus, string> = {
    running: 'Running',
    failed: 'Failed',
    idle: 'Idle',
};

@Component({
    selector: 'maia-job-state',
    templateUrl: './job-state.component.html',
    imports: [MatButtonModule]
})
export class JobStateComponent {


    jobState = input.required<JobState>();

    now = input.required<number>();


    runJob = output<JobState>();

    displayStackTrace = output<string>();

    displayJobMetrics = output<JobExecutionState>();


    failuresExpanded = signal(false);


    status = computed<JobStatus>(() => deriveJobStatus(this.jobState()));

    statusColor = computed<string>(() => STATUS_COLORS[this.status()]);

    statusLabel = computed<string>(() => STATUS_LABELS[this.status()]);

    runningRows = computed(() =>
        this.jobState().runningJobs.map((execution) => ({
            execution,
            elapsedLabel: formatElapsed(execution.startTimestamp, this.now())
        }))
    );


    onRun() {
        this.runJob.emit(this.jobState());
    }


    onDisplayStackTrace(jobExecutionId: string) {
        this.displayStackTrace.emit(jobExecutionId);
    }


    onDisplayJobMetrics(jobExecution: JobExecutionState) {
        this.displayJobMetrics.emit(jobExecution);
    }


    onToggleFailures() {
        this.failuresExpanded.set(!this.failuresExpanded());
    }


}
```

- [ ] **Step 2: Replace the template**

Replace the full contents of `job-state.component.html` with:

```html
<div class="flex flex-col gap-2 rounded-lg border border-gray-300 p-3.5">
    <div class="flex items-center justify-between gap-2">
        <span class="font-medium">{{ jobState().jobName }}</span>
        <span
            class="inline-block h-2.5 w-2.5 shrink-0 rounded-full"
            [style.background-color]="statusColor()"
            role="img"
            [attr.aria-label]="statusLabel()">
        </span>
    </div>

    @if (jobState().jobDescription) {
        <p class="text-sm text-gray-500">{{ jobState().jobDescription }}</p>
    }

    @if (runningRows().length > 0) {
        @for (row of runningRows(); track row.execution.id) {
            <div class="flex items-center justify-between rounded bg-blue-50 px-2.5 py-1.5 text-xs text-blue-800">
                <span>Running for {{ row.elapsedLabel }}</span>
                <button mat-flat-button (click)="onDisplayJobMetrics(row.execution)">Metrics</button>
            </div>
        }
    } @else {
        <p class="text-xs text-gray-400">Not currently running</p>
    }

    @if (jobState().recentlyFailedExecutions.length > 0) {
        <button type="button" class="text-left text-xs text-red-700" (click)="onToggleFailures()">
            {{ failuresExpanded() ? '▾' : '▸' }}
            {{ jobState().recentlyFailedExecutions.length === 1 ? '1 recent failure' : jobState().recentlyFailedExecutions.length + ' recent failures' }}
        </button>
        @if (failuresExpanded()) {
            <ul class="flex flex-col gap-1">
                @for (execution of jobState().recentlyFailedExecutions; track execution.jobExecutionId) {
                    <li class="flex items-center justify-between gap-2 text-xs">
                        <span>{{ execution.errorMessage }}</span>
                        <button mat-flat-button type="button" (click)="onDisplayStackTrace(execution.jobExecutionId)">
                            stacktrace
                        </button>
                    </li>
                }
            </ul>
        }
    }

    <div class="flex justify-end">
        <button mat-flat-button (click)="onRun()" color="primary">Run...</button>
    </div>
</div>
```

- [ ] **Step 3: Verify it compiles**

Run (from `libs/maia-ui-workspace/`): `npx ng build maia-jobs`
Expected: builds with no errors. (The parent page still passes the old props at this point — Task 5 updates it — so this build only confirms the card component itself is well-typed in isolation; a full-app build happens in Task 5/6.)

- [ ] **Step 4: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/components/job-state/job-state.component.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/components/job-state/job-state.component.html
git commit -m "Redesign job card: status dot, elapsed running time, collapsed failures"
```

---

## Task 5: Page layout — header, filter row, states, grid

**Goal:** Rebuild `jobs-dashboard-page.component.ts`/`.html` to use `JobsDashboardStore`, with a header/count-summary, an always-visible filter row, and stale-while-revalidate-aware loading/error/empty/grid states.

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.html`

**Acceptance Criteria:**
- [ ] Header shows a title and `store.countSummary()`.
- [ ] Filter row (`matInput`, labeled, placeholder "Filter jobs...") wired to `store.onNameFilterChanged(...)`, always visible.
- [ ] `ngOnInit` calls `store.startPolling()` (not a one-shot fetch).
- [ ] Full-page spinner shows only when `isLoading() && jobStates().length === 0`.
- [ ] Full-page error banner (with `role="alert"` and a "Retry" button calling `store.retryFetch()`) shows only when `error() && jobStates().length === 0`.
- [ ] Once any data has loaded, the grid renders `visibleJobStates()` and keeps rendering through subsequent poll ticks/errors (no blanking).
- [ ] Empty message ("No jobs match your filter.") shows when data has loaded but `visibleJobStates().length === 0`.
- [ ] Grid passes `[jobState]`, `[now]="store.now()"`, and the three existing outputs to `<maia-job-state>`.
- [ ] `onRunJob`/`onDisplayStackTrace`/`onDisplayJobMetricsDialog` dialog-handling logic unchanged.

**Verify:** `npx ng build maia-jobs` (from `libs/maia-ui-workspace/`) → builds with no errors.

**Steps:**

- [ ] **Step 1: Replace the component class**

Replace the full contents of `jobs-dashboard-page.component.ts` with:

```ts
import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatButtonModule} from '@angular/material/button';
import {JobState} from './models/JobState';
import {JobExecutionState} from './models/JobExecutionState';
import {JobsApiService} from './services/jobs-api.service';
import {JobsDashboardStore} from './state/jobs-dashboard-store';
import {JobStateComponent} from './components/job-state/job-state.component';
import {JobMetricsDialogComponent} from './dialogs/job-metrics-dialog/job-metrics-dialog.component';
import {RunJobDialogComponent} from './dialogs/run-job-dialog/run-job-dialog.component';
import {StacktraceDialogComponent} from './dialogs/stacktrace-dialog/stacktrace-dialog.component';


@Component({
    imports: [JobStateComponent, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule, MatButtonModule],
    providers: [JobsApiService, JobsDashboardStore],
    selector: 'maia-jobs-dashboard-page',
    templateUrl: './jobs-dashboard-page.component.html'
})
export class JobsDashboardPageComponent implements OnInit {


    readonly store = inject(JobsDashboardStore);


    constructor(
        private jobsService: JobsApiService,
        private dialog: MatDialog
    ) {}


    ngOnInit() {
        this.store.startPolling();
    }


    onFilterInput(event: Event) {
        this.store.onNameFilterChanged((event.target as HTMLInputElement).value);
    }


    onRunJob(jobState: JobState) {

      const dialogRef = this.dialog.open(RunJobDialogComponent, {data: jobState});
        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.jobsService.runJob(jobState.jobName);
            }
        });

    }


    onDisplayStackTrace(jobExecutionId: string) {

        this.jobsService.getStacktrace(jobExecutionId).subscribe(res => {
            this.dialog.open(StacktraceDialogComponent, {data: res.stacktrace});
        });

    }


    onDisplayJobMetricsDialog(jobExecutionState: JobExecutionState) {

        this.dialog.open(JobMetricsDialogComponent, {data: jobExecutionState.metrics});

    }


}
```

- [ ] **Step 2: Replace the template**

Replace the full contents of `jobs-dashboard-page.component.html` with:

```html
<div class="flex items-center justify-between">
    <h2 class="text-lg font-medium">Jobs</h2>
    <span class="text-sm text-gray-500">{{ store.countSummary() }}</span>
</div>

<div class="mt-2 flex items-center gap-4 border-b border-gray-300 pb-2.5">
    <mat-form-field subscriptSizing="dynamic" class="max-w-[240px] flex-1">
        <mat-label>Filter jobs</mat-label>
        <input matInput placeholder="Filter jobs..." (input)="onFilterInput($event)">
    </mat-form-field>
</div>

@if (store.isLoading() && store.jobStates().length === 0) {
    <div class="flex justify-center py-10">
        <mat-spinner diameter="40"></mat-spinner>
    </div>
} @else if (store.error() && store.jobStates().length === 0) {
    <div class="mt-5 flex items-center justify-between rounded border border-red-300 bg-red-50 p-3.5 text-red-800" role="alert">
        <span>{{ store.error() }}</span>
        <button mat-flat-button color="primary" (click)="store.retryFetch()">Retry</button>
    </div>
} @else if (store.visibleJobStates().length === 0) {
    <p class="mt-5 text-gray-500">No jobs match your filter.</p>
} @else {
    <div class="mt-5 grid grid-cols-[repeat(auto-fill,minmax(260px,1fr))] gap-3">
        @for (jobState of store.visibleJobStates(); track jobState.jobName) {
            <maia-job-state
                [jobState]="jobState"
                [now]="store.now()"
                (runJob)="onRunJob($event)"
                (displayStackTrace)="onDisplayStackTrace($event)"
                (displayJobMetrics)="onDisplayJobMetricsDialog($event)">
            </maia-job-state>
        }
    </div>
}
```

- [ ] **Step 3: Verify it compiles**

Run (from `libs/maia-ui-workspace/`): `npx ng build maia-jobs`
Expected: builds with no errors.

- [ ] **Step 4: Verify existing tests still pass**

Run (from `libs/maia-ui-workspace/`): `npx ng test maia-jobs`
Expected: PASS — all `jobs-filtering` tests from Task 2 still green (no new tests added in this task).

- [ ] **Step 5: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.ts \
        libs/maia-ui-workspace/projects/maia-jobs/src/lib/jobs-dashboard/jobs-dashboard-page.component.html
git commit -m "Rebuild jobs dashboard page with polling store, filter bar, and stale-while-revalidate states"
```

---

## Task 6: Manual verification in the showcase app

**Goal:** Confirm the redesigned dashboard and the batched backend endpoint behave correctly end-to-end in the running showcase app.

**Files:** None (verification only).

**Acceptance Criteria:**
- [ ] `GET /jobs/current_state` returns `recentlyFailedExecutions` per job (each capped at 10, most recent first); `GET /job/recently_failed/{jobName}` no longer exists (404).
- [ ] The `/jobs` (or wherever this page is routed in the showcase app) route loads without console errors.
- [ ] Grid renders responsively.
- [ ] Filter narrows the grid and updates the count summary (X of Y form).
- [ ] A running job shows elapsed time that increases roughly every 15s.
- [ ] A job with both a running execution and a past failure shows a blue (not red) dot; the failure is visible via the collapsed toggle.
- [ ] The page keeps auto-refreshing without user action, and the grid does not blank/flicker on each poll.
- [ ] Stopping the backend before first load shows the error banner with a working Retry.
- [ ] Stopping the backend *after* data has loaded does NOT blank the grid; restarting the backend causes the next poll to silently refresh the data.

**Verify:** Manual browser check — this task has no automated test; it exercises Tasks 1–5 together.

**Steps:**

- [ ] **Step 1: Build the backend and frontend libraries**

From `/home/kevin/dev/code/maia`: `./gradlew :libs:maia-job-parent:maia-job-web:build -x test`
From `libs/maia-ui-workspace/`: `npx ng build maia-jobs`

- [ ] **Step 2: Start the showcase app and exercise the page**

Follow this project's normal process for running `maia-showcase` locally (backend + frontend). Navigate to the jobs dashboard route and walk through each Acceptance Criteria item above. If a full local job-execution backend isn't available, focus on what can be exercised: page load, filter, empty/loading/error states (the error path is easy to trigger by pointing the base-URL token at an unreachable host), and static rendering of whatever job data is available.

- [ ] **Step 3: Note and fix any discrepancy**

If any Acceptance Criteria item fails, fix it in the relevant Task's files (1–5) and re-verify. Commit fixes separately, referencing what was wrong:

```bash
git add <fixed files>
git commit -m "Fix <specific defect found during manual verification>"
```

- [ ] **Step 4: Stop any local processes started for verification**
