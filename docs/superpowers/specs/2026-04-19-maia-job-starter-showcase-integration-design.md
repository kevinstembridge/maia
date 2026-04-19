# maia-job-starter Showcase Integration Design

## Goal

Integrate `maia-job-starter` into the MaiaShowcase application so that the job framework is wired up, the `job_execution` table exists, and at least one `MaiaJob` implementation is registered.

## Changes

### 1. `maia-showcase/app/build.gradle.kts`
Add `implementation(project(":libs:maia-job-parent:maia-job-starter"))`. This pulls in `maia-job`, `maia-job-web`, `maia-job-autoconfigure`, and registers `MaiaJobAutoConfiguration` + `MaiaJobWebAutoConfiguration` via Spring Boot's autoconfigure mechanism.

### 2. `maia-showcase/service/build.gradle.kts`
Add `api(project(":libs:maia-job-parent:maia-job"))` so the service module can implement `MaiaJob`.

### 3. `maia-showcase/service/src/main/kotlin/org/maiaframework/showcase/jobs/ShowcaseSampleJob.kt`
A minimal `MaiaJob` implementation with `jobName = JobName("showcase-sample")` and a `description`. The `executeJob` method logs a message and records one processed item via `JobMetrics`. This gives the registry something concrete to expose.

### 4. `maia-showcase/dao/src/main/resources/db/migration/V007__create_jobs_schema_and_tables.sql`
Creates the `jobs` schema and `job_execution` table. DDL sourced from the generated file at `libs/maia-job-parent/maia-job-dao/src/generated/sql/create_entity_tables_jobs.sql`.

### 5. `maia-showcase/app/src/main/resources/application.yml`
Add `maia.jobs.api.base-path: /api`. Required by `MaiaJobEndpoint`'s `@RequestMapping("${maia.jobs.api.base-path}")`.

## Security

`MaiaJobEndpoint` uses method-level `@PreAuthorize("hasAuthority('SYS__OPS')")`. The showcase security config's catch-all `authenticated` rule covers the endpoints at the HTTP layer. No changes to `MaiaShowcaseSecurityConfiguration` are needed.

## No New Modules

The service module is the right home for hand-coded `MaiaJob` implementations. No new Gradle module is required.
