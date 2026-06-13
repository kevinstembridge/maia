# Join-Entity History Blotter Angular UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Angular UI generator produce a working, routable global history blotter page for join-entity history (`LeftToRightSimpleJoinEntity`), matching the backend's join-aware `EntityHistoryBlotterDef`.

**Architecture:** Branch 5 existing Angular history-blotter renderers on `def.isJoinEntityHistory` to drop `entityId`/`:id` route-param plumbing for join history. Fix `EntityCrudRoutesRenderer`/`AngularUiModuleGenerator` so an entity with only a history blotter (no CRUD pages) still gets a routes file. Manually wire the new route + nav entry into the showcase app shell.

**Tech Stack:** Kotlin code generator (maia-gen-generator), Angular 24, TypeScript.

**User Verification:** NO — no user sign-off requested; UI correctness is verified by the implementer via dev server + browser per CLAUDE.md's frontend-change guidance.

---

## Spec

See `docs/superpowers/specs/2026-06-13-join-history-blotter-angular-ui-design.md` for full design rationale.

## Reference: `EntityHistoryBlotterDef` (already implemented, do not modify)

Relevant properties on `def: EntityHistoryBlotterDef` (file:
`maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityHistoryBlotterDef.kt`):

- `def.isJoinEntityHistory: Boolean` — true for `LeftToRightSimpleJoinEntity`'s history.
- `def.routePath` — `"left-to-right-simple-join-history"` for join, `"<kebab>/history"` otherwise.
- `def.searchEndpointUrlForTypescript` — `"/api/left-to-right-simple-join/history/search"` for join (literal), `` "/api/<kebab>/${this.entityId}/history/search" `` otherwise (template-literal text, contains literal `${this.entityId}`).
- `def.blotterComponentNames`, `def.blotterPageComponentNames`, `def.datasourceClassName`, `def.serviceClassName`, `def.tsRowDtoClassName`, `def.blotterColumns`, `def.pageTitle`, `def.historyBlotterBaseName` — unchanged, used as before.

---

### Task 1: Branch the 5 Angular history-blotter renderers on `isJoinEntityHistory`

**Goal:** For join-entity history (`LeftToRightSimpleJoinEntity`), the generated page component, page HTML, blotter component, service, and ag-grid datasource no longer reference `entityId` / route params, and post to the global search URL. Non-join entity history (e.g. `UserHistoryBlotter`, `HistorySample`) generates byte-identical output to before.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterPageComponentRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterPageHtmlRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterComponentRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterServiceRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterAgGridDatasourceRenderer.kt`

**Acceptance Criteria:**
- [ ] `:maia-gen:maia-gen-generator:compileKotlin` succeeds
- [ ] After regenerating `:maia-showcase:maia-showcase-ui:maiaGeneration`, `git diff` shows changes ONLY in `LeftToRightSimpleJoinHistory*` generated files (page component, page html, blotter component, service, datasource) — all other generated `*HistoryBlotter*`/`*History*` files (e.g. `UserHistoryBlotter*`, `UserGroupHistoryBlotter*`, `HistorySample*`) are unchanged.
- [ ] Regenerated `left-to-right-simple-join-history-blotter-page.ts` has no `ActivatedRoute`/`toSignal`/`map` imports and an empty class body.
- [ ] Regenerated `left-to-right-simple-join-history-blotter-page.html` has `<app-left-to-right-simple-join-history-blotter />` with no `@if (entityId(); ...)` wrapper.
- [ ] Regenerated `left-to-right-simple-join-history-blotter.ts` has no `@Input() entityId`, no `OnInit`/`ngOnInit`.
- [ ] Regenerated `left-to-right-simple-join-history-blotter-service.ts` has `search(searchModel: any)` (no `entityId` param) posting to `` `/api/left-to-right-simple-join/history/search` ``.
- [ ] Regenerated `LeftToRightSimpleJoinHistoryBlotterAgGridDatasource.ts` has no `entityId`/`setEntityId`, posts to `` `/api/left-to-right-simple-join/history/search` ``.

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin :maia-showcase:maia-showcase-ui:maiaGeneration` → BUILD SUCCESSFUL, then `git diff --stat -- maia-showcase/maia-showcase-ui/src/generated` shows only the 5 `LeftToRightSimpleJoinHistory*`/`left-to-right-simple-join-history-*` files changed.

**Steps:**

- [ ] **Step 1: Replace `EntityHistoryBlotterPageComponentRenderer.kt`**

Full new file content:

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class EntityHistoryBlotterPageComponentRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(def.packageName)


    init {
        addImport("@angular/core", "ChangeDetectionStrategy")
        addImport("@angular/core", "Component")
        addImport("@app/components/page-layout/page-layout", "PageLayout")
        addImport(TypescriptImport(
            def.blotterComponentNames.componentName,
            "@$genDir/${def.blotterComponentNames.componentNameKebab}"
        ))

        if (!def.isJoinEntityHistory) {
            addImport("@angular/core", "inject")
            addImport("@angular/core/rxjs-interop", "toSignal")
            addImport("@angular/router", "ActivatedRoute")
            addImport("rxjs", "map")
        }
    }


    override fun renderedFilePath(): String {
        return def.blotterPageComponentNames.componentRenderedFilePath
    }


    override fun renderSourceBody() {

        blankLine()
        blankLine()
        appendLine("@Component({")
        appendLine("    changeDetection: ChangeDetectionStrategy.OnPush,")
        appendLine("    imports: [PageLayout, ${def.blotterComponentNames.componentName}],")
        appendLine("    selector: '${def.blotterPageComponentNames.componentSelector}',")
        appendLine("    templateUrl: './${def.blotterPageComponentNames.htmlFileName}'")
        appendLine("})")
        appendLine("export class ${def.blotterPageComponentNames.componentName} {")

        if (def.isJoinEntityHistory) {

            appendLine("}")

        } else {

            blankLine()
            blankLine()
            appendLine("    private readonly route = inject(ActivatedRoute);")
            blankLine()
            blankLine()
            appendLine("    protected readonly entityId = toSignal(")
            appendLine("        this.route.paramMap.pipe(map(p => p.get('id')))")
            appendLine("    );")
            blankLine()
            blankLine()
            appendLine("}")

        }

    }


}
```

- [ ] **Step 2: Replace `EntityHistoryBlotterPageHtmlRenderer.kt`**

Full new file content:

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.AbstractSourceFileRenderer
import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.lang.text.StringFunctions


class EntityHistoryBlotterPageHtmlRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractSourceFileRenderer() {


    private val dataPageId = StringFunctions.toSnakeCase(def.historyBlotterBaseName)

    private val blotterSelector = def.blotterComponentNames.componentSelector


    override fun renderedFilePath(): String {
        return def.blotterPageComponentNames.htmlRenderedFilePath
    }


    override fun renderSource(): String {

        if (def.isJoinEntityHistory) {

            append("""
                |<app-page-layout pageTitle="${def.pageTitle}" dataPageId="${dataPageId}">
                |    <${blotterSelector} />
                |</app-page-layout>
                |""".trimMargin())

        } else {

            append("""
                |<app-page-layout pageTitle="${def.pageTitle}" dataPageId="${dataPageId}">
                |    @if (entityId(); as id) {
                |        <${blotterSelector} [entityId]="id" />
                |    }
                |</app-page-layout>
                |""".trimMargin())

        }

        return sourceCode.toString()

    }


}
```

- [ ] **Step 3: Replace `EntityHistoryBlotterComponentRenderer.kt`**

Full new file content:

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class EntityHistoryBlotterComponentRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(def.packageName)


    init {
        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/forms", "FormsModule")
        addImport("@angular/material/button", "MatButtonModule")
        addImport("@angular/material/icon", "MatIconModule")
        addImport("ag-grid-angular", "AgGridAngular")
        addImport("@app/themes/ag-grid-theme", "agGridTheme")
        addImport("ag-grid-community", "ColDef")
        addImport("ag-grid-community", "GridApi")
        addImport("ag-grid-community", "GridReadyEvent")
        addImport("ag-grid-community", "RowModelType")
        addImport(TypescriptImport(def.tsRowDtoClassName, "@$genDir/${def.tsRowDtoClassName}"))
        addImport(TypescriptImport(def.datasourceClassName, "@$genDir/${def.datasourceClassName}"))

        if (!def.isJoinEntityHistory) {
            addImport("@angular/core", "Input")
            addImport("@angular/core", "OnInit")
        }
    }


    override fun renderedFilePath(): String {
        return def.blotterComponentNames.componentRenderedFilePath
    }


    override fun renderSourceBody() {

        blankLine()
        blankLine()
        appendLine("@Component({")
        appendLine("    imports: [AgGridAngular, FormsModule, MatButtonModule, MatIconModule],")
        appendLine("    providers: [${def.datasourceClassName}],")
        appendLine("    selector: '${def.blotterComponentNames.componentSelector}',")
        appendLine("    templateUrl: './${def.blotterComponentNames.htmlFileName}'")
        appendLine("})")

        if (def.isJoinEntityHistory) {
            appendLine("export class ${def.blotterComponentNames.componentName} {")
        } else {
            appendLine("export class ${def.blotterComponentNames.componentName} implements OnInit {")
        }

        blankLine()
        blankLine()

        if (!def.isJoinEntityHistory) {
            appendLine("    @Input() entityId!: string;")
            blankLine()
            blankLine()
        }

        appendLine("    public columnDefs: ColDef[] = [")
        appendLine("        { field: 'id', headerName: 'ID', cellDataType: 'text', hide: true },")

        def.blotterColumns.forEach { fieldDef ->
            val fieldName = fieldDef.classFieldDef.classFieldName.value
            val headerName = fieldDef.classFieldDef.displayName?.value ?: fieldName
            val cellDataType = when (fieldDef.classFieldDef.fieldType) {
                is IntFieldType, is IntTypeFieldType, is IntValueClassFieldType,
                is LongFieldType, is LongTypeFieldType -> "number"
                else -> "text"
            }
            appendLine("        { field: '${fieldName}', headerName: '${headerName}', cellDataType: '${cellDataType}' },")
        }

        appendLine("    ];")
        blankLine()
        blankLine()
        appendLine("    public defaultColDef: ColDef = {")
        appendLine("        filter: true,")
        appendLine("        flex: 1,")
        appendLine("        floatingFilter: true,")
        appendLine("        minWidth: 100,")
        appendLine("        sortable: true")
        appendLine("    };")
        blankLine()
        blankLine()
        appendLine("    public rowBuffer = 0;")
        blankLine()
        blankLine()
        appendLine("    public rowSelection = {")
        appendLine("         mode: 'singleRow' as const,")
        appendLine("         checkboxes: false,")
        appendLine("    };")
        blankLine()
        blankLine()
        appendLine("    public agGridTheme = agGridTheme;")
        blankLine()
        blankLine()
        appendLine("    public rowModelType: RowModelType = 'infinite';")
        blankLine()
        blankLine()
        appendLine("    public cacheBlockSize = 100;")
        blankLine()
        blankLine()
        appendLine("    public cacheOverflowSize = 2;")
        blankLine()
        blankLine()
        appendLine("    public maxConcurrentDatasourceRequests = 1;")
        blankLine()
        blankLine()
        appendLine("    public infiniteInitialRowCount = 1000;")
        blankLine()
        blankLine()
        appendLine("    public maxBlocksInCache = 10;")
        blankLine()
        blankLine()
        appendLine("    public rowData!: ${def.tsRowDtoClassName}[];")
        blankLine()
        blankLine()
        appendLine("    private gridApi!: GridApi<${def.tsRowDtoClassName}>;")
        blankLine()
        blankLine()
        appendLine("    private readonly datasource = inject(${def.datasourceClassName});")
        blankLine()
        blankLine()

        if (!def.isJoinEntityHistory) {
            appendLine("    ngOnInit(): void {")
            appendLine("        this.datasource.setEntityId(this.entityId);")
            appendLine("    }")
            blankLine()
            blankLine()
        }

        appendLine("    onGridReady(params: GridReadyEvent<${def.tsRowDtoClassName}>): void {")
        blankLine()
        appendLine("        this.gridApi = params.api;")
        appendLine("        params.api?.setGridOption('datasource', this.datasource);")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("}")

    }


}
```

- [ ] **Step 4: Replace `EntityHistoryBlotterServiceRenderer.kt`**

Full new file content:

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class EntityHistoryBlotterServiceRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(def.packageName)


    init {
        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpHeaders")
        addImport("rxjs", "Observable")
        addImport("@maia/maia-ui", "SearchResultPage")
        addImport(TypescriptImport(def.tsRowDtoClassName, "@$genDir/${def.tsRowDtoClassName}"))
    }


    override fun renderedFilePath(): String {
        return def.blotterComponentNames.serviceRenderedFilePath
    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("@Injectable({providedIn: 'root'})")
        appendLine("export class ${def.serviceClassName} {")
        blankLine()
        appendLine("    private httpOptions = {")
        appendLine("        headers: new HttpHeaders({")
        appendLine("            'Content-Type': 'application/json'")
        appendLine("        })")
        appendLine("    };")
        blankLine()
        blankLine()
        appendLine("    private readonly http = inject(HttpClient);")
        blankLine()
        blankLine()

        if (def.isJoinEntityHistory) {

            appendLine("    public search(searchModel: any): Observable<SearchResultPage<${def.tsRowDtoClassName}>> {")
            blankLine()
            appendLine("        return this.http.post<SearchResultPage<${def.tsRowDtoClassName}>>(")
            appendLine("                `${def.searchEndpointUrlForTypescript}`,")
            appendLine("                searchModel,")
            appendLine("                this.httpOptions);")
            blankLine()
            appendLine("    }")

        } else {

            appendLine("    public search(entityId: string, searchModel: any): Observable<SearchResultPage<${def.tsRowDtoClassName}>> {")
            blankLine()
            appendLine("        return this.http.post<SearchResultPage<${def.tsRowDtoClassName}>>(")
            appendLine("                `/api/${def.entityDef.entityBaseName.toKebabCase()}/\${entityId}/history/search`,")
            appendLine("                searchModel,")
            appendLine("                this.httpOptions);")
            blankLine()
            appendLine("    }")

        }

        blankLine()
        blankLine()
        appendLine("}")

    }


}
```

- [ ] **Step 5: Replace `EntityHistoryBlotterAgGridDatasourceRenderer.kt`**

Full new file content:

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class EntityHistoryBlotterAgGridDatasourceRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(def.packageName)


    init {
        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("ag-grid-community", "IDatasource")
        addImport("ag-grid-community", "IGetRowsParams")
        addImport("@maia/maia-ui", "SearchResultPage")
        addImport(TypescriptImport(def.tsRowDtoClassName, "@$genDir/${def.tsRowDtoClassName}"))
    }


    override fun renderedFilePath(): String {
        return "$genDir/${def.datasourceClassName}.ts"
    }


    override fun renderSourceBody() {

        blankLine()
        blankLine()
        appendLine("@Injectable()")
        appendLine("export class ${def.datasourceClassName} implements IDatasource {")
        blankLine()
        blankLine()
        appendLine("    rowCount?: number = undefined;")
        blankLine()
        blankLine()

        if (!def.isJoinEntityHistory) {
            appendLine("    private entityId!: string;")
            blankLine()
            blankLine()
        }

        appendLine("    private readonly http = inject(HttpClient);")
        blankLine()
        blankLine()

        if (!def.isJoinEntityHistory) {
            appendLine("    setEntityId(id: string): void {")
            appendLine("        this.entityId = id;")
            appendLine("    }")
            blankLine()
            blankLine()
        }

        appendLine("    getRows(params: IGetRowsParams): void {")
        blankLine()
        appendLine("        this.http.post<SearchResultPage<${def.tsRowDtoClassName}>>(")
        appendLine("            `${def.searchEndpointUrlForTypescript}`,")
        appendLine("            params")
        appendLine("        ).subscribe({")
        appendLine("           next: searchResultPage => params.successCallback(searchResultPage.results, searchResultPage.totalResultCount)")
        appendLine("        });")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("}")

    }


}
```

- [ ] **Step 6: Compile, regenerate, and verify diff**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin :maia-showcase:maia-showcase-ui:maiaGeneration
git status --porcelain -- maia-showcase/maia-showcase-ui/src/generated
```

Expected: BUILD SUCCESSFUL. The `git status` output should show only these files as modified:
- `.../many-to-many/left-to-right-simple-join-history-blotter-page.ts`
- `.../many-to-many/left-to-right-simple-join-history-blotter-page.html`
- `.../many-to-many/left-to-right-simple-join-history-blotter.ts`
- `.../many-to-many/left-to-right-simple-join-history-blotter-service.ts`
- `.../many-to-many/LeftToRightSimpleJoinHistoryBlotterAgGridDatasource.ts`

If any other `*History*` generated file shows as modified, compare it against `git diff` for that file — it must be byte-identical (no diff). If there IS a diff for a non-join file, find and fix the discrepancy in the corresponding renderer before proceeding.

- [ ] **Step 7: Build the showcase UI to confirm it still compiles**

```bash
cd maia-showcase/maia-showcase-ui && npx tsc --noEmit -p tsconfig.app.json
```

Expected: no TypeScript errors.

- [ ] **Step 8: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterPageComponentRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterPageHtmlRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterComponentRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterServiceRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterAgGridDatasourceRenderer.kt \
        maia-showcase/maia-showcase-ui/src/generated
git commit -m "feat: generate entityId-free Angular history blotter for join entities"
```

---

### Task 2: Generate a routes file for entities with only a history blotter

**Goal:** `LeftToRightSimpleJoinEntity` (which has no blotter/view/create/edit pages) gets a generated `left-to-right-simple-join-routes.ts` containing its (join-style, no `:id`) history route. Non-join entity history routes keep their `/:id` suffix. All other entities' routes files are unchanged.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityCrudRoutesRenderer.kt:114-124` (the `renderHistoryRoute` method)
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt:613-640` (the `renderEntityCrudRoutes` method)

**Acceptance Criteria:**
- [ ] `:maia-gen:maia-gen-generator:compileKotlin` succeeds
- [ ] After regenerating `:maia-showcase:maia-showcase-ui:maiaGeneration`, a new file `.../many-to-many/left-to-right-simple-join-routes.ts` is generated, exporting `leftToRightSimpleJoinRoutes` with a single route entry: `path: 'left-to-right-simple-join-history'` (no `:id`), `loadComponent` importing `./left-to-right-simple-join-history-blotter-page`.
- [ ] All previously-existing `*-routes.ts` files are unchanged (`git diff` empty for them) — in particular `user-routes.ts` still has `path: 'user/history/:id'` (or equivalent `${routePath}/:id`).

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin :maia-showcase:maia-showcase-ui:maiaGeneration` → BUILD SUCCESSFUL, then `git status --porcelain -- maia-showcase/maia-showcase-ui/src/generated` shows exactly one new file: `.../many-to-many/left-to-right-simple-join-routes.ts`.

**Steps:**

- [ ] **Step 1: Update `renderHistoryRoute` in `EntityCrudRoutesRenderer.kt`**

Replace the existing method (lines 114-124):

```kotlin
    private fun renderHistoryRoute(def: EntityHistoryBlotterDef) {

        append("""
            |    {
            |        path: '${def.routePath}/:id',
            |        loadComponent: () =>
            |            import('./${def.blotterPageComponentNames.componentNameKebab}').then(m => m.${def.blotterPageComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }
```

with:

```kotlin
    private fun renderHistoryRoute(def: EntityHistoryBlotterDef) {

        val path = if (def.isJoinEntityHistory) def.routePath else "${def.routePath}/:id"

        append("""
            |    {
            |        path: '$path',
            |        loadComponent: () =>
            |            import('./${def.blotterPageComponentNames.componentNameKebab}').then(m => m.${def.blotterPageComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }
```

- [ ] **Step 2: Update `renderEntityCrudRoutes` in `AngularUiModuleGenerator.kt`**

Replace the existing method (lines 613-640):

```kotlin
    private fun renderEntityCrudRoutes() {

        val blotterPageByEntity = this.modelDef.blotterPageDefs
            .mapNotNull { pageDef -> pageDef.blotterDef.blotterSourceDef.rootEntityDef?.let { it to pageDef } }
            .toMap()

        val viewPageByEntity = this.modelDef.entityDetailViewDefs
            .associateBy { it.entityDef }

        val createPageByEntity = this.modelDef.entityCreatePageDefs
            .associateBy { it.entityDef }

        val editPageByEntity = this.modelDef.entityEditPageDefs
            .associateBy { it.entityDef }

        val allEntities = (blotterPageByEntity.keys + viewPageByEntity.keys + createPageByEntity.keys + editPageByEntity.keys).toSet()

        allEntities.forEach { entityDef ->
            EntityCrudRoutesRenderer(
                entityDef = entityDef,
                blotterPageDef = blotterPageByEntity[entityDef],
                entityDetailViewDef = viewPageByEntity[entityDef],
                entityCreatePageDef = createPageByEntity[entityDef],
                entityEditPageDef = editPageByEntity[entityDef],
            ).renderToDir(this.typescriptOutputDir)
        }

    }
```

with:

```kotlin
    private fun renderEntityCrudRoutes() {

        val blotterPageByEntity = this.modelDef.blotterPageDefs
            .mapNotNull { pageDef -> pageDef.blotterDef.blotterSourceDef.rootEntityDef?.let { it to pageDef } }
            .toMap()

        val viewPageByEntity = this.modelDef.entityDetailViewDefs
            .associateBy { it.entityDef }

        val createPageByEntity = this.modelDef.entityCreatePageDefs
            .associateBy { it.entityDef }

        val editPageByEntity = this.modelDef.entityEditPageDefs
            .associateBy { it.entityDef }

        val historyOnlyEntities = this.modelDef.entityHistoryBlotterDefs
            .map { it.entityDef }
            .toSet()

        val allEntities = (blotterPageByEntity.keys + viewPageByEntity.keys + createPageByEntity.keys + editPageByEntity.keys + historyOnlyEntities).toSet()

        allEntities.forEach { entityDef ->
            EntityCrudRoutesRenderer(
                entityDef = entityDef,
                blotterPageDef = blotterPageByEntity[entityDef],
                entityDetailViewDef = viewPageByEntity[entityDef],
                entityCreatePageDef = createPageByEntity[entityDef],
                entityEditPageDef = editPageByEntity[entityDef],
            ).renderToDir(this.typescriptOutputDir)
        }

    }
```

- [ ] **Step 3: Compile, regenerate, and verify diff**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin :maia-showcase:maia-showcase-ui:maiaGeneration
git status --porcelain -- maia-showcase/maia-showcase-ui/src/generated
```

Expected: BUILD SUCCESSFUL. Output shows exactly one new (`??` or `A`) file:
`maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-to-right-simple-join-routes.ts`

Read that file and confirm it contains:

```ts
export const leftToRightSimpleJoinRoutes: Routes = [
    {
        path: 'left-to-right-simple-join-history',
        loadComponent: () =>
            import('./left-to-right-simple-join-history-blotter-page').then(m => m.LeftToRightSimpleJoinHistoryBlotterPage),
    },
];
```

(exact import path / component name come from the existing `def.blotterPageComponentNames` — verify they match the page component generated in Task 1).

If any previously-existing `-routes.ts` file (e.g. `user-routes.ts`, `left-many-routes.ts`) shows a diff, investigate — it must remain unchanged.

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityCrudRoutesRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt \
        maia-showcase/maia-showcase-ui/src/generated
git commit -m "feat: generate routes file for entities with only a history blotter"
```

---

### Task 3: Wire the global join-history page into the showcase app shell

**Goal:** `/left-to-right-simple-join-history` is reachable from the nav menu and renders the global history blotter grid.

**Files:**
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.html`

**Acceptance Criteria:**
- [ ] `app.routes.ts` imports and spreads `leftToRightSimpleJoinRoutes`
- [ ] `app.html` has a nav menu button linking to `/left-to-right-simple-join-history`, gated by `hasReadAuthority()`
- [ ] Dev server starts; navigating to `/left-to-right-simple-join-history` renders the page with the ag-grid history blotter (columns: ID hidden, Change Type, Created At, Left, Right); clicking the new nav menu item navigates there

**Verify:** `cd maia-showcase/maia-showcase-ui && npx tsc --noEmit -p tsconfig.app.json` → no errors. Then start the dev server and check in a browser (see Step 4).

**Steps:**

- [ ] **Step 1: Add the import to `app.routes.ts`**

In `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`, after the existing line:

```ts
import {leftManyRoutes} from '@app/gen-components/org/maiaframework/showcase/many-to-many/left-many-routes';
```

add a new line directly below it:

```ts
import {leftToRightSimpleJoinRoutes} from '@app/gen-components/org/maiaframework/showcase/many-to-many/left-to-right-simple-join-routes';
```

- [ ] **Step 2: Spread the routes into the `routes` array**

In the same file, in the `routes` array, after the existing line:

```ts
    ...leftManyRoutes,
```

add a new line directly below it:

```ts
    ...leftToRightSimpleJoinRoutes,
```

- [ ] **Step 3: Add a nav menu entry to `app.html`**

In `maia-showcase/maia-showcase-ui/src/app/app.html`, the menu has a block for "Left Many to Many" followed by a block for "Right Many to Many":

```html
        @if (hasReadAuthority()) {
            <button mat-menu-item routerLink="/left-many-blotter">
                <mat-icon>dialpad</mat-icon>
                <span>Left Many to Many</span>
            </button>
        }
        @if (hasReadAuthority()) {
            <button mat-menu-item routerLink="/right-many-blotter">
                <mat-icon>dialpad</mat-icon>
                <span>Right Many to Many</span>
            </button>
        }
```

Insert a new block between them, so the result reads:

```html
        @if (hasReadAuthority()) {
            <button mat-menu-item routerLink="/left-many-blotter">
                <mat-icon>dialpad</mat-icon>
                <span>Left Many to Many</span>
            </button>
        }
        @if (hasReadAuthority()) {
            <button mat-menu-item routerLink="/left-to-right-simple-join-history">
                <mat-icon>history</mat-icon>
                <span>Left-Right Join History</span>
            </button>
        }
        @if (hasReadAuthority()) {
            <button mat-menu-item routerLink="/right-many-blotter">
                <mat-icon>dialpad</mat-icon>
                <span>Right Many to Many</span>
            </button>
        }
```

- [ ] **Step 4: Type-check, then verify in a browser**

```bash
cd maia-showcase/maia-showcase-ui && npx tsc --noEmit -p tsconfig.app.json
```

Expected: no errors.

Then start the dev stack (Postgres via `docker compose -f maia-showcase/compose.yaml up -d`, the Spring Boot app, and `ng serve` for `maia-showcase-ui` — follow the existing showcase README/dev workflow for exact commands), log in, open the hamburger menu, click "Left-Right Join History", and confirm:
- URL is `/left-to-right-simple-join-history`
- The page title reads "LeftToRightSimpleJoin History"
- An ag-grid renders with columns Change Type, Created At, Left, Right (ID column hidden)
- The grid loads data (or an empty grid with no errors if no history rows exist yet)

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/app.routes.ts maia-showcase/maia-showcase-ui/src/app/app.html
git commit -m "feat: add global join-entity history blotter to showcase nav and routes"
```
