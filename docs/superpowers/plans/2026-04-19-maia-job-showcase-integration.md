# maia-job-starter Showcase Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire `maia-job-starter` into MaiaShowcase so the job framework boots, the `jobs` schema exists, and a sample job is registered.

**Architecture:** Add the starter to the app module, create a Flyway migration for the `jobs` schema/table, add a sample `MaiaJob` implementation in the service module, and configure the required `maia.jobs.api.base-path` property.

**Tech Stack:** Kotlin, Spring Boot autoconfigure, Flyway, PostgreSQL

**User Verification:** NO

---

## Files

| Action | Path |
|--------|------|
| Modify | `maia-showcase/app/build.gradle.kts` |
| Modify | `maia-showcase/service/build.gradle.kts` |
| Modify | `maia-showcase/app/src/main/resources/application.yml` |
| Create | `maia-showcase/service/src/main/kotlin/org/maiaframework/showcase/jobs/ShowcaseSampleJob.kt` |
| Create | `maia-showcase/dao/src/main/resources/db/migration/V007__create_jobs_schema_and_tables.sql` |

---

### Task 1: Add Flyway migration for jobs schema

**Goal:** Create the `jobs` schema and `job_execution` table so Flyway can migrate the showcase database.

**Files:**
- Create: `maia-showcase/dao/src/main/resources/db/migration/V007__create_jobs_schema_and_tables.sql`

**Acceptance Criteria:**
- [ ] File exists at the correct path
- [ ] DDL creates `jobs` schema, `job_execution` table, and `jobName_idx` index

**Verify:** `ls maia-showcase/dao/src/main/resources/db/migration/V007__create_jobs_schema_and_tables.sql` → file exists

**Steps:**

- [ ] **Step 1: Create the migration file**

```sql
-- maia-showcase/dao/src/main/resources/db/migration/V007__create_jobs_schema_and_tables.sql
CREATE SCHEMA IF NOT EXISTS jobs;

CREATE TABLE jobs.job_execution (
    completion_status text NULL,
    created_timestamp_utc timestamp(3) with time zone NOT NULL,
    end_timestamp_utc timestamp(3) with time zone NULL,
    error_message text NULL,
    id uuid NOT NULL,
    invoked_by text NOT NULL,
    job_name text NOT NULL,
    last_modified_timestamp_utc timestamp(3) with time zone NOT NULL,
    metrics jsonb NOT NULL,
    stack_trace text NULL,
    start_timestamp_utc timestamp(3) with time zone NOT NULL,
    PRIMARY KEY(id)
);
CREATE INDEX jobName_idx ON jobs.job_execution(job_name);
```

- [ ] **Step 2: Commit**

```bash
git add maia-showcase/dao/src/main/resources/db/migration/V007__create_jobs_schema_and_tables.sql
git commit -m "feat(showcase): add Flyway migration V007 for jobs schema"
```

```json:metadata
{"files": ["maia-showcase/dao/src/main/resources/db/migration/V007__create_jobs_schema_and_tables.sql"], "verifyCommand": "ls maia-showcase/dao/src/main/resources/db/migration/V007__create_jobs_schema_and_tables.sql", "acceptanceCriteria": ["File created at correct path", "Creates jobs schema, job_execution table, and jobName_idx index"], "requiresUserVerification": false}
```

---

### Task 2: Wire starter into app module and configure base path

**Goal:** Add `maia-job-starter` to the app's dependencies and set `maia.jobs.api.base-path` so `MaiaJobEndpoint` can bind its `@RequestMapping`.

**Files:**
- Modify: `maia-showcase/app/build.gradle.kts`
- Modify: `maia-showcase/app/src/main/resources/application.yml`

**Acceptance Criteria:**
- [ ] `maia-job-starter` dependency present in `app/build.gradle.kts`
- [ ] `maia.jobs.api.base-path: /api` present in `application.yml`

**Verify:** `grep -r "maia-job-starter" maia-showcase/app/build.gradle.kts` → match found

**Steps:**

- [ ] **Step 1: Add starter dependency to `maia-showcase/app/build.gradle.kts`**

Add after the `maia-props-starter` line:

```kotlin
    implementation(project(":libs:maia-job-parent:maia-job-starter"))
```

Full dependencies block after change (excerpt):
```kotlin
    implementation(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-starter"))
    implementation(project(":libs:maia-problem-parent:maia-problem-starter"))
    implementation(project(":libs:maia-props-parent:maia-props-starter"))
    implementation(project(":libs:maia-job-parent:maia-job-starter"))
    implementation(project(":libs:maia-webapp:maia-webapp-app"))
```

- [ ] **Step 2: Add `maia.jobs.api.base-path` to `maia-showcase/app/src/main/resources/application.yml`**

Add under the `maia:` key:

```yaml
maia:
  jobs:
    api:
      base-path: /api
  problems:
    type_prefix: "showcase_"
```

- [ ] **Step 3: Commit**

```bash
git add maia-showcase/app/build.gradle.kts maia-showcase/app/src/main/resources/application.yml
git commit -m "feat(showcase): add maia-job-starter dependency and configure jobs api base path"
```

```json:metadata
{"files": ["maia-showcase/app/build.gradle.kts", "maia-showcase/app/src/main/resources/application.yml"], "verifyCommand": "grep 'maia-job-starter' maia-showcase/app/build.gradle.kts", "acceptanceCriteria": ["maia-job-starter in app/build.gradle.kts", "maia.jobs.api.base-path: /api in application.yml"], "requiresUserVerification": false}
```

---

### Task 3: Add sample MaiaJob in service module

**Goal:** Create `ShowcaseSampleJob` so the `MaiaJobRegistry` has at least one concrete job to expose.

**Files:**
- Modify: `maia-showcase/service/build.gradle.kts`
- Create: `maia-showcase/service/src/main/kotlin/org/maiaframework/showcase/jobs/ShowcaseSampleJob.kt`

**Acceptance Criteria:**
- [ ] `maia-job` api dependency present in `service/build.gradle.kts`
- [ ] `ShowcaseSampleJob` implements `MaiaJob` with `jobName = JobName("showcase-sample")`
- [ ] `executeJob` logs a message and increments an `itemCount` counter

**Verify:** `./gradlew :maia-showcase:service:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add `maia-job` api dependency to `maia-showcase/service/build.gradle.kts`**

Add after the existing `api(project(":maia-showcase:elasticsearch"))` line:

```kotlin
    api(project(":libs:maia-job-parent:maia-job"))
```

- [ ] **Step 2: Create `ShowcaseSampleJob.kt`**

```kotlin
package org.maiaframework.showcase.jobs

import org.maiaframework.job.JobName
import org.maiaframework.job.MaiaJob
import org.maiaframework.metrics.JobMetrics
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ShowcaseSampleJob : MaiaJob {

    private val log = LoggerFactory.getLogger(javaClass)

    override val jobName = JobName("showcase-sample")

    override val description = "Sample job demonstrating the maia-job framework"

    override fun executeJob(jm: JobMetrics) {
        log.info("ShowcaseSampleJob executing")
        jm.getOrCreateCounter("itemCount").inc(1L)
    }

}
```

- [ ] **Step 3: Compile to verify**

Run: `./gradlew :maia-showcase:service:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/service/build.gradle.kts maia-showcase/service/src/main/kotlin/org/maiaframework/showcase/jobs/ShowcaseSampleJob.kt
git commit -m "feat(showcase): add ShowcaseSampleJob MaiaJob implementation"
```

```json:metadata
{"files": ["maia-showcase/service/build.gradle.kts", "maia-showcase/service/src/main/kotlin/org/maiaframework/showcase/jobs/ShowcaseSampleJob.kt"], "verifyCommand": "./gradlew :maia-showcase:service:compileKotlin", "acceptanceCriteria": ["maia-job api dep in service/build.gradle.kts", "ShowcaseSampleJob implements MaiaJob with jobName showcase-sample", "executeJob logs and increments itemCount counter"], "requiresUserVerification": false}
```

---

### Task 4: Full build verification

**Goal:** Confirm the showcase app compiles and all tests pass with the new dependencies wired in.

**Files:** (no new files)

**Acceptance Criteria:**
- [ ] `./gradlew :maia-showcase:app:build` succeeds

**Verify:** `./gradlew :maia-showcase:app:build` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Run full showcase app build**

Run: `./gradlew :maia-showcase:app:build`
Expected: BUILD SUCCESSFUL

If it fails with a missing class, check that `maia-job-starter` was added correctly to `app/build.gradle.kts`.
If it fails with `maia.jobs.api.base-path` unresolved, check `application.yml`.

```json:metadata
{"files": [], "verifyCommand": "./gradlew :maia-showcase:app:build", "acceptanceCriteria": ["./gradlew :maia-showcase:app:build succeeds"], "requiresUserVerification": false}
```
