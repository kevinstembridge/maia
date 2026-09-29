# Angular UI workspace codegen — Part 1: generator infrastructure

## Goal
Introduce the Maia code generator into `libs/maia-ui-workspace`'s build so `AngularUiModuleGenerator` produces TypeScript DTO interfaces for `maia-elasticsearch`, `maia-props`, and `maia-jobs` from their backend specs — the same generator already wired into `maia-showcase-ui`, applied to a multi-project Angular library workspace instead of a single flat app.

## Scope note
This is Part 1 of 2. Part 1 only wires up generation and confirms it produces correct output alongside the existing hand-written models (which stay in place, untouched, still in use). Part 2 (separate spec/plan cycle, deferred until Part 1's real generated files exist to plan against) swaps `maia-elasticsearch`'s Angular consumers over to the generated types. `maia-ui` has no backend spec and gets nothing generated for it.

## Working style for this task (explicit user instruction)
No worktree, no feature branch, no commits. All work happens directly on `main` in `/home/kevin/dev/code/maia`, left uncommitted.

## Fix: the spec naming collision
`../../../libs/maia-elasticsearch-parent/maia-elasticsearch-spec/src/main/kotlin/org/maiaframework/elasticsearch/spec/MaiaElasticsearchSpec.kt` currently declares two `simpleResponseDto`s with the identical name `"ManagedEsIndexInfo"`:

```kotlin
val managedIndexInfoDtoDef = simpleResponseDto("org.maiaframework.elasticsearch.index.model", "ManagedEsIndexInfo") { ... }
val indexStateDtoDef = simpleResponseDto("org.maiaframework.elasticsearch.index.model", "ManagedEsIndexInfo") { ... }  // bug: same name
```

This collides at the Kotlin level too (two backend classes named `ManagedEsIndexInfo` would render to the same file path). Rename `indexStateDtoDef`'s name to `"EsIndexState"`.

## Why not the workspace-level `@app/*` convention `maia-showcase-ui` uses as-is
`maia-showcase-ui` is a single flat Angular application with one `@app/*` alias pointing at a workspace-root `src/generated/typescript/main/app/*`. `maia-ui-workspace` uses TypeScript project references (`tsconfig.json` → each project's own `tsconfig.lib.json`/`tsconfig.spec.json`, each with `"include": ["src/**/*.ts"]` scoped to that project's own `src/` tree) and builds each library independently via `ng-packagr`. A file outside a project's own `src/` won't be picked up by that project's build. So each of the three target projects gets its own generation task writing into its own `projects/<lib>/src/generated/...`, with its own `@app/*` path alias — same internal generator conventions, redirected per project rather than shared workspace-wide.

## New Gradle module: `libs/maia-ui-workspace`
Not currently a registered Gradle project at all. Add:
- `settings.gradle.kts`: `include("libs:maia-ui-workspace")`
- `libs/maia-ui-workspace/build.gradle.kts`: no plugins needed (there's no Kotlin/JVM code here, and `JavaExec` is a core Gradle task type requiring no plugin). No Gradle Node plugin either — this task doesn't ask Gradle to drive `npm`/`ng`; verification stays on the same `npx ng build`/`npx ng test` commands used throughout this project's existing workflow.

Three configurations + `JavaExec` tasks, one per target project, plus a convenience aggregate:

| Task | Spec module dependency | `applicationSpecClassName` | `generatedSourceDir` |
|---|---|---|---|
| `maiaGenerationElasticsearch` | `:libs:maia-elasticsearch-parent:maia-elasticsearch-spec` | `org.maiaframework.elasticsearch.spec.MaiaElasticsearchApplicationSpec` | `projects/maia-elasticsearch/src/generated` |
| `maiaGenerationProps` | `:libs:maia-props-parent:maia-props-spec` | `org.maiaframework.props.spec.PropsApplicationSpec` | `projects/maia-props/src/generated` |
| `maiaGenerationJob` | `:libs:maia-job-parent:maia-job-spec` | `org.maiaframework.job.spec.MaiaJobApplicationSpec` | `projects/maia-jobs/src/generated` |
| `maiaGeneration` | — | — | `dependsOn` all three above |

Each task's `classpath` = a dedicated `maiagen*` configuration depending on `:maia-gen:maia-gen-generator` + that task's own spec module only (not all three specs on every task's classpath). Main class for all three: `org.maiaframework.gen.generator.AngularUiModuleGeneratorKt`.

## Generated output
Committed to git, matching this repo's existing convention for every other `src/generated` directory (backend included). The internal path structure inside each `src/generated/typescript/main/` is fixed by the generator itself (`app/gen-components/<package-as-dirs>/<ClassName>.ts`) — not something a build script can redirect without changing `maia-gen-spec`'s `ClassDef`/`RequestDtoDef`, which is out of scope here.

## Per-project TS path alias
Add to each of the three projects' `tsconfig.lib.json` and `tsconfig.spec.json`:
```json
"paths": {
    "@app/*": ["src/generated/typescript/main/app/*"]
}
```
None of these six files currently declare a `paths` key, so this is a pure addition, not a merge with existing entries. `maia-ui`'s root-level `"maia-ui": ["./dist/maia-ui"]` mapping is unaffected (different files).

## Verification approach
Given this is the first time this generator has targeted a multi-project TS-project-references workspace, some of the tsconfig/path-resolution behavior is genuinely untested territory for this codebase. The plan directs empirical verification (a real `npx ng build`/`npx ng test` run per affected project) rather than treating every tsconfig detail as certain in advance — if the alias doesn't resolve as expected, adjust minimally and record what actually worked.

## Out of scope
- Swapping any consumer code over to the generated types (Part 2).
- Touching `maia-gen-spec`'s hardcoded output-path convention.
- `maia-ui` (no backend spec, nothing to generate).
- Any change to `maia-showcase-ui`'s existing, working setup.
