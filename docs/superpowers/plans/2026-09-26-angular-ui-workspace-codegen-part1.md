# Angular UI Workspace Codegen Part 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire `AngularUiModuleGenerator` into `libs/maia-ui-workspace`'s build so it generates TypeScript DTO interfaces for `maia-elasticsearch`, `maia-props`, and `maia-jobs` from their backend specs, per project, alongside (not replacing) the existing hand-written models.

**Architecture:** A new, minimal Gradle module wrapping `libs/maia-ui-workspace` with three `JavaExec` tasks (one per spec), each writing into its own project's `src/generated/` (not a shared workspace-level folder, since `ng-packagr`/TS project references require sources to live under each project's own `src/`). A `@app/*` TS path alias per project points at that project's generated folder.

**Tech Stack:** Gradle (Kotlin DSL), the existing Maia code generator (`maia-gen-generator`), Angular CLI/`ng-packagr`, TypeScript project references.

**User Verification:** NO — no human sign-off was requested.

**IMPORTANT — explicit user instruction for this task, overriding the usual workflow:** No git worktree, no feature branch, no commits. All work happens directly on `main` in `/home/kevin/dev/code/maia`, left uncommitted when done. Do not run `git commit`, `git add`, or create a worktree/branch at any point in this plan.

---

## Design reference

`docs/superpowers/specs/2026-09-26-angular-ui-workspace-codegen-part1-design.md`

## Working directory for all commands

`/home/kevin/dev/code/maia` (backend/Gradle steps) and `/home/kevin/dev/code/maia/libs/maia-ui-workspace` (frontend verification steps)

---

### Task 1: Fix the elasticsearch spec's DTO naming collision

**Goal:** `indexStateDtoDef` in `MaiaElasticsearchSpec.kt` gets a distinct name (`"EsIndexState"`) instead of duplicating `managedIndexInfoDtoDef`'s name (`"ManagedEsIndexInfo"`), and the backend module still builds and regenerates correctly with two distinct classes.

**Files:**
- Modify: `../../../libs/maia-elasticsearch-parent/maia-elasticsearch-spec/src/main/kotlin/org/maiaframework/elasticsearch/spec/MaiaElasticsearchSpec.kt`

**Acceptance Criteria:**
- [ ] `indexStateDtoDef`'s `simpleResponseDto(...)` name argument is `"EsIndexState"`, not `"ManagedEsIndexInfo"`
- [ ] No other change to the file (field lists, package names, other DTO defs untouched)
- [ ] `:libs:maia-elasticsearch-parent:maia-elasticsearch` regenerates two distinct Kotlin classes, `ManagedEsIndexInfo.kt` and `EsIndexState.kt`, in `src/generated/kotlin/main/org/maiaframework/elasticsearch/index/model/`
- [ ] The module still builds and its existing tests still pass

**Verify:** `./gradlew :libs:maia-elasticsearch-parent:maia-elasticsearch:test` (from `/home/kevin/dev/code/maia`) → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Rename the DTO**

In `MaiaElasticsearchSpec.kt`, find:

```kotlin
    val indexStateDtoDef = simpleResponseDto("org.maiaframework.elasticsearch.index.model", "ManagedEsIndexInfo") {
        field("indexName", FieldTypes.string)
        field("managedIndexInfo", managedIndexInfoDtoDef)
        field("health", indexHealthDtoDef)
        field("exists", FieldTypes.boolean)
    }
```

and replace it with:

```kotlin
    val indexStateDtoDef = simpleResponseDto("org.maiaframework.elasticsearch.index.model", "EsIndexState") {
        field("indexName", FieldTypes.string)
        field("managedIndexInfo", managedIndexInfoDtoDef)
        field("health", indexHealthDtoDef)
        field("exists", FieldTypes.boolean)
    }
```

- [ ] **Step 2: Regenerate and verify the backend build**

Run: `./gradlew :libs:maia-elasticsearch-parent:maia-elasticsearch:test --rerun-tasks` (from `/home/kevin/dev/code/maia`) — use `--rerun-tasks` to force `maiaGeneration` to actually re-run rather than serve a stale cached result, since only the spec input changed in a way Gradle's up-to-date checks might not perfectly capture (the `inputs.files(...)` on that module's `maiaGeneration` task has a pre-existing, unrelated bug — it points at `maia-job-spec` paths, not its own spec file — so don't rely on incremental input tracking here).

Expected: `BUILD SUCCESSFUL`.

Then inspect `libs/maia-elasticsearch-parent/maia-elasticsearch/src/generated/kotlin/main/org/maiaframework/elasticsearch/index/model/` and confirm both `ManagedEsIndexInfo.kt` and `EsIndexState.kt` now exist as separate files with the expected field lists (`ManagedEsIndexInfo`: `indexName`/`description`/`isActiveVersion`; `EsIndexState`: `indexName`/`managedIndexInfo`/`health`/`exists`).

- [ ] **Step 3: Check for any hand-written backend reference to the old generated name**

Run: `grep -rn "ManagedEsIndexInfo" libs/maia-elasticsearch-parent --include="*.kt" | grep -v "/generated/" | grep -v maia-elasticsearch-spec` (from `/home/kevin/dev/code/maia`) to check whether any hand-written Kotlin code referenced the old (buggy, shared) generated class name in a way that could now be ambiguous or wrong. If anything shows up, read it and confirm it's referring to the correct one of the two now-distinct classes (most likely it's fine — the rename only affects `indexStateDtoDef`'s output file/class name, and any existing hand-written code was necessarily already dealing with whichever one of the two identically-named-but-differently-shaped classes actually got compiled last/won the file collision — read the affected code and use judgment; if genuinely unsure, report it rather than guessing).

- [ ] **Step 4: Do NOT commit**

Leave these changes uncommitted, per explicit instruction. Do not run `git add` or `git commit`.

---

### Task 2: Register `libs/maia-ui-workspace` as a Gradle module with generation tasks

**Goal:** `libs/maia-ui-workspace` becomes a registered Gradle project with three `JavaExec` tasks that invoke `AngularUiModuleGeneratorKt` against the elasticsearch, props, and job specs, each writing into that project's own `src/generated/` directory, plus a convenience aggregate task.

**Files:**
- Modify: `settings.gradle.kts`
- Create: `libs/maia-ui-workspace/build.gradle.kts`

**Acceptance Criteria:**
- [ ] `settings.gradle.kts` includes `libs:maia-ui-workspace`
- [ ] `libs/maia-ui-workspace/build.gradle.kts` defines three `maiagen*` configurations (elasticsearch/props/job), each depending only on `:maia-gen:maia-gen-generator` plus its own spec project
- [ ] Three `JavaExec` tasks (`maiaGenerationElasticsearch`, `maiaGenerationProps`, `maiaGenerationJob`), each with the correct `applicationSpecClassName` and `generatedSourceDir` args, and correct `outputs.dir(...)` declarations
- [ ] An aggregate `maiaGeneration` task that `dependsOn` all three
- [ ] Running the aggregate task produces real, non-empty output under each of the three projects' `src/generated/typescript/main/app/gen-components/...` directories, with file names/fields matching what each spec declares (spot-check against the known field lists — e.g. `Property.ts` for props with `propertyName`/`effectiveValue`/`isSensitive`/etc., matching the fields on `PropsSpec.kt`'s `propertyDtoDef` as of the current spec state)
- [ ] No `plugins {}` block referencing Kotlin/JVM conventions, no Gradle Node plugin — this module needs neither

**Verify:** `./gradlew :libs:maia-ui-workspace:maiaGeneration` (from `/home/kevin/dev/code/maia`) → `BUILD SUCCESSFUL`, and the generated files described above genuinely exist on disk with sensible content

**Steps:**

- [ ] **Step 1: Register the Gradle project**

In `settings.gradle.kts`, find a sensible existing `include(...)` line near other `libs:` entries (e.g. right after the last `libs:maia-props-parent:...` line) and add:

```kotlin
include("libs:maia-ui-workspace")
```

- [ ] **Step 2: Create the build script**

Create `libs/maia-ui-workspace/build.gradle.kts`:

```kotlin
val maiagenElasticsearch by configurations.creating
val maiagenProps by configurations.creating
val maiagenJob by configurations.creating

dependencies {

    maiagenElasticsearch(project(":maia-gen:maia-gen-generator"))
    maiagenElasticsearch(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-spec"))

    maiagenProps(project(":maia-gen:maia-gen-generator"))
    maiagenProps(project(":libs:maia-props-parent:maia-props-spec"))

    maiagenJob(project(":maia-gen:maia-gen-generator"))
    maiagenJob(project(":libs:maia-job-parent:maia-job-spec"))

}

tasks.register<JavaExec>("maiaGenerationElasticsearch") {
    group = "maia generation"
    outputs.dir("projects/maia-elasticsearch/src/generated")
    classpath = configurations["maiagenElasticsearch"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.AngularUiModuleGeneratorKt")
    args(
        "applicationSpecClassName=org.maiaframework.elasticsearch.spec.MaiaElasticsearchApplicationSpec",
        "generatedSourceDir=projects/maia-elasticsearch/src/generated"
    )
}

tasks.register<JavaExec>("maiaGenerationProps") {
    group = "maia generation"
    outputs.dir("projects/maia-props/src/generated")
    classpath = configurations["maiagenProps"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.AngularUiModuleGeneratorKt")
    args(
        "applicationSpecClassName=org.maiaframework.props.spec.PropsApplicationSpec",
        "generatedSourceDir=projects/maia-props/src/generated"
    )
}

tasks.register<JavaExec>("maiaGenerationJob") {
    group = "maia generation"
    outputs.dir("projects/maia-jobs/src/generated")
    classpath = configurations["maiagenJob"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.AngularUiModuleGeneratorKt")
    args(
        "applicationSpecClassName=org.maiaframework.job.spec.MaiaJobApplicationSpec",
        "generatedSourceDir=projects/maia-jobs/src/generated"
    )
}

tasks.register("maiaGeneration") {
    group = "maia generation"
    dependsOn("maiaGenerationElasticsearch", "maiaGenerationProps", "maiaGenerationJob")
}
```

- [ ] **Step 3: Run the generation task and inspect output**

Run: `./gradlew :libs:maia-ui-workspace:maiaGeneration` (from `/home/kevin/dev/code/maia`)
Expected: `BUILD SUCCESSFUL`

Then run: `find libs/maia-ui-workspace/projects/*/src/generated -type f` (from `/home/kevin/dev/code/maia`) and report every file produced. For each project, open at least one generated file and confirm its fields match the corresponding spec (e.g. `libs/maia-ui-workspace/projects/maia-elasticsearch/src/generated/typescript/main/app/gen-components/org/maiaframework/elasticsearch/index/model/EsIndexState.ts` should have `indexName: string`, `managedIndexInfo: ManagedEsIndexInfo`, `health: EsIndexHealth`, `exists: boolean`).

If any of the three tasks fails (e.g. a spec that doesn't produce valid output, or a reflective-instantiation error), report the exact error — do not silently skip a failing task; this step must confirm all three specs generate successfully before moving to Task 3.

- [ ] **Step 4: Do NOT commit**

Leave these changes (and the newly generated files) uncommitted, per explicit instruction. Do not run `git add` or `git commit`.

---

### Task 3: Wire the `@app/*` path alias per project and verify existing builds still pass

**Goal:** Each of the three projects (`maia-elasticsearch`, `maia-props`, `maia-jobs`) can resolve `@app/*` imports to its own generated folder, and the existing Angular build/test suite for each still passes with the new generated files present (even though nothing imports them yet).

**Files:**
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/tsconfig.lib.json`
- Modify: `libs/maia-ui-workspace/projects/maia-elasticsearch/tsconfig.spec.json`
- Modify: `libs/maia-ui-workspace/projects/maia-props/tsconfig.lib.json`
- Modify: `libs/maia-ui-workspace/projects/maia-props/tsconfig.spec.json`
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/tsconfig.lib.json`
- Modify: `libs/maia-ui-workspace/projects/maia-jobs/tsconfig.spec.json`

**Acceptance Criteria:**
- [ ] Each of the six files gets a `"paths": {"@app/*": ["src/generated/typescript/main/app/*"]}` entry added to `compilerOptions` (none of them currently have a `paths` key, so this is a pure addition — verify that assumption still holds before editing each file, in case it's changed since this plan was written)
- [ ] `npx ng build maia-elasticsearch`, `npx ng build maia-props`, `npx ng build maia-jobs` all still succeed
- [ ] `npx ng test maia-elasticsearch`, `npx ng test maia-props`, `npx ng test maia-jobs` all still pass with the same test counts as before this change
- [ ] A real, throwaway-in-the-editor-only check confirms the alias actually resolves: temporarily add a line importing one generated type (e.g. `import {EsIndexState} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/EsIndexState';`) into a scratch/test file, confirm `ng build`/`tsc` doesn't report a module-resolution error, then remove that scratch import before finishing (Part 1 doesn't consume the generated types yet — that's Part 2)

**Verify:** `npx ng build maia-elasticsearch && npx ng build maia-props && npx ng build maia-jobs` (from `libs/maia-ui-workspace`) → all succeed; `npx ng test maia-elasticsearch && npx ng test maia-props && npx ng test maia-jobs` → all pass, same counts as the pre-change baseline

**Steps:**

- [ ] **Step 1: Record the pre-change baseline**

Run `npx ng test maia-elasticsearch`, `npx ng test maia-props`, `npx ng test maia-jobs` (from `libs/maia-ui-workspace`) and note the passing test counts for each, before making any tsconfig changes, so you have something concrete to compare against afterward.

- [ ] **Step 2: Read each of the six tsconfig files and confirm no existing `paths` key**

For each of the six files listed in Files above, read it and confirm `compilerOptions` has no `"paths"` entry today (this plan was written against that assumption — if one now exists, add `"@app/*"` as an additional entry inside it rather than overwriting).

- [ ] **Step 3: Add the path alias to each file**

For each `tsconfig.lib.json`, add inside `compilerOptions`:

```json
"paths": {
    "@app/*": [
        "src/generated/typescript/main/app/*"
    ]
}
```

For each `tsconfig.spec.json`, add the same `"paths"` entry inside its own `compilerOptions`.

(Match each file's existing JSON formatting/indentation style — these are hand-formatted JSONC-ish files with comments at the top in some cases; preserve that.)

- [ ] **Step 4: Empirically verify the alias resolves**

For ONE project first (suggest `maia-elasticsearch`, since Task 1/2 already confirmed its `EsIndexState` output content): temporarily add a throwaway import of a generated type into an existing file in that project (or a new scratch `.ts` file under its `src/` — anywhere `tsconfig.lib.json`'s `include: ["src/**/*.ts"]` would pick it up), for example:

```ts
import {EsIndexState} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/EsIndexState';
```

Run `npx ng build maia-elasticsearch` (from `libs/maia-ui-workspace`). If it fails with a module-resolution error on `@app/*`, the path alias isn't being picked up correctly — investigate why (common causes: the alias needs to be on `baseUrl`-relative paths matching Angular's resolution, or `ng-packagr`'s build might need `paths` declared differently than a plain `tsc` build would — read the actual error message and adjust the `paths` value accordingly, this is exactly the "verify empirically, don't assume" step flagged in the design). Once it resolves successfully, remove the throwaway import (Part 1 doesn't keep any real usage of the generated types) and confirm the build still succeeds without it.

Repeat the same throwaway-import check for `maia-props` and `maia-jobs` (each against one real DTO you can see in that project's own generated output from Task 2) to confirm the alias works consistently across all three, then remove those throwaway imports too.

- [ ] **Step 5: Run the full verify suite**

Run, from `libs/maia-ui-workspace`:
```bash
npx ng build maia-elasticsearch
npx ng build maia-props
npx ng build maia-jobs
npx ng test maia-elasticsearch
npx ng test maia-props
npx ng test maia-jobs
```

Confirm all six succeed, and the three test runs show the same passing counts recorded in Step 1 (no regressions from the new generated files sitting unused under `src/`).

- [ ] **Step 6: Do NOT commit**

Leave all changes uncommitted, per explicit instruction. Do not run `git add` or `git commit`. Report the final `git status --short` output so the user can see exactly what changed.

---

## Self-Review Notes

- **Spec coverage:** the naming-collision fix, the new Gradle module, the per-project generation tasks, and the per-project path aliases all map to Tasks 1-3. `maia-ui` is correctly excluded (no backend spec). Part 2 (consumer swap) is explicitly out of scope, deferred to a follow-up cycle.
- **Placeholder scan:** none found — all steps contain complete code, except where the design doc and this plan both explicitly call for empirical verification (the tsconfig path-resolution behavior) rather than assuming untested tooling behavior — that's a deliberate, flagged exception, not a placeholder.
- **Type/naming consistency:** `EsIndexState` (Task 1) is the exact name spot-checked in Task 2's verification and referenced in Task 3's throwaway import example — consistent throughout.
- **User verification requirement scan:** the original request doesn't ask for human sign-off on the outcome — answer is NO. However, the user gave an explicit, binding process instruction (no worktree, no commits, work directly on main) that every task's final step encodes as a hard "do NOT commit" instruction, and Task 3's final step reports `git status` so the user can see the resulting diff themselves.
