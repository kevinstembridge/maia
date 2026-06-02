# History Blotter Generator Feature

## Overview

Automatically generate a full history blotter stack for any entity with `recordVersionHistory = true` in the spec. The detail view page for such an entity gains a "History" button that navigates to a dedicated blotter page showing the audit trail for that specific entity instance.

No spec DSL changes required — the generator detects `withVersionHistory = true` on the `EntityDef` and emits all artifacts.

## Trigger

`EntityDef.withVersionHistory.value == true && !isHistoryEntity`

The existing `EntityDef.historyEntityDef` lazy val already constructs the history entity def (with fields: `version`, `changeType`, all original data fields, timestamps). The new `historyBlotterDef` lazy val on `EntityDef` constructs an `EntityHistoryBlotterDef` from it.

## Section 1: Spec Definition

### New class: `EntityHistoryBlotterDef` (`maia-gen-spec`)

Computed as a lazy val on `EntityDef`:

```kotlin
val historyBlotterDef: EntityHistoryBlotterDef? =
    if (withVersionHistory.value && !isHistoryEntity) EntityHistoryBlotterDef(this)
    else null
```

Derives from the entity:
- **Columns**: `version`, `changeType`, all non-FK non-PK data fields (e.g. `someString`, `someInt`), `lastModifiedTimestampUtc`. Excluded: `id` (in the page URL), `createdBy`, `lastModifiedBy` (raw FK IDs), `createdTimestampUtc` (entity creation time, not change time)
- **Backend names**: `HistorySampleHistoryBlotter*` pattern for Kotlin DTO, DAO, Repo, SearchService, Endpoint
- **Endpoint URLs**: `POST /api/history-sample/{entityId}/history/search` and `/count`
- **Frontend names**: Angular component names for page, blotter, service, datasource
- **Route path**: `history-sample/history` (`:id` appended by routes renderer)

### `ModelDef` addition

```kotlin
val entityHistoryBlotterDefs = entityDefs.mapNotNull { it.historyBlotterDef }
```

No changes to the showcase spec.

## Section 2: Backend

Seven new renderers in `maia-gen-generator/src/main/kotlin/.../renderers/`:

| Renderer | Output file |
|---|---|
| `EntityHistoryBlotterRowDtoRenderer` | `HistorySampleHistoryBlotterRowDto.kt` — Kotlin data class with blotter columns |
| `EntityHistoryBlotterRowMapperRenderer` | `HistorySampleHistoryBlotterRowDtoRowMapper.kt` |
| `EntityHistoryBlotterRowDtoMetaRenderer` | `HistorySampleHistoryBlotterRowDtoMeta.kt` — field→column mapping for `AgGridSearchModelConverter` |
| `EntityHistoryBlotterRowDtoDaoRenderer` | `HistorySampleHistoryBlotterRowDtoDao.kt` — SQL on `maia.history_sample_history WHERE id = :entityId AND {ag-grid clause}` |
| `EntityHistoryBlotterRowDtoRepoRenderer` | `HistorySampleHistoryBlotterRowDtoRepo.kt` |
| `EntityHistoryBlotterSearchServiceRenderer` | `HistorySampleHistoryBlotterRowDtoSearchService.kt` |
| `EntityHistoryBlotterSearchEndpointRenderer` | `HistorySampleHistoryBlotterSearchEndpoint.kt` |

The endpoint signature:
```kotlin
@PostMapping("/api/history-sample/{entityId}/history/search")
fun search(
    @PathVariable entityId: String,
    @RequestBody searchModel: AgGridSearchModel
): SearchResultPage<HistorySampleHistoryBlotterRowDto>
```

The DAO applies `WHERE id = :entityId` as a fixed filter before ag-grid dynamic clauses. Structure is otherwise identical to the existing `HistorySampleBlotterRowDtoDao`.

### Module generator wiring
- `DomainModuleGenerator` — DTO + meta + row mapper
- `RepoLayerModuleGenerator` — DAO + repo
- `ServiceLayerModuleGenerator` — search service
- `WebLayerModuleGenerator` — endpoint

## Section 3: Frontend

Seven new renderers in `maia-gen-generator/src/main/kotlin/.../renderers/ui/`:

| Renderer | Output file |
|---|---|
| `EntityHistoryBlotterRowDtoTypescriptRenderer` | `HistorySampleHistoryBlotterRowDto.ts` — TS interface |
| `EntityHistoryBlotterServiceRenderer` | `history-sample-history-blotter-service.ts` |
| `EntityHistoryBlotterAgGridDatasourceRenderer` | `HistorySampleHistoryBlotterAgGridDatasource.ts` |
| `EntityHistoryBlotterComponentRenderer` | `history-sample-history-blotter.ts` |
| `EntityHistoryBlotterHtmlRenderer` | `history-sample-history-blotter.html` |
| `EntityHistoryBlotterPageComponentRenderer` | `history-sample-history-blotter-page.ts` |
| `EntityHistoryBlotterPageHtmlRenderer` | `history-sample-history-blotter-page.html` |

### Key pattern: component-scoped datasource

The datasource is `@Injectable()` (no `providedIn: 'root'`), declared in the blotter component's `providers` array. This gives each blotter instance its own datasource with its own `entityId`.

The blotter component receives `entityId` as `@Input()`, calls `datasource.setEntityId(entityId)` on `ngOnInit`, then sets the datasource on the grid:

```typescript
@Component({
    providers: [HistorySampleHistoryBlotterAgGridDatasource],
    ...
})
export class HistorySampleHistoryBlotter {
    @Input() entityId!: string;
    private readonly datasource = inject(HistorySampleHistoryBlotterAgGridDatasource);

    ngOnInit(): void {
        this.datasource.setEntityId(this.entityId);
    }

    onGridReady(params: GridReadyEvent): void {
        params.api.setGridOption('datasource', this.datasource);
    }
}
```

The page component reads `:id` from the route and passes it as `[entityId]` input to the blotter:

```html
<app-page-layout ...>
    @if (entityId(); as id) {
        <app-history-sample-history-blotter [entityId]="id" />
    }
</app-page-layout>
```

### Module generator wiring
`AngularUiModuleGenerator` gains a `renderEntityHistoryBlotters()` method iterating `modelDef.entityHistoryBlotterDefs`.

## Section 4: Navigation

### Modified renderers

**`EntityDetailViewPageHtmlRenderer`** — adds "History" button when `entityDef.historyBlotterDef != null`:

```html
<button matButton aria-label="History" (click)="onHistoryClicked()">
    <mat-icon>history</mat-icon>
    History
</button>
```

**`EntityDetailViewPageComponentRenderer`** — adds `onHistoryClicked()`:

```typescript
onHistoryClicked(): void {
    const id = this.entityId();
    if (id) {
        this.router.navigate(['/history-sample/history', id]);
    }
}
```

**`EntityCrudRoutesRenderer`** — adds history route when `entityDef.historyBlotterDef != null`:

```typescript
{
    path: 'history-sample/history/:id',
    loadComponent: () =>
        import('./history-sample-history-blotter-page').then(m => m.HistorySampleHistoryBlotterPage),
},
```

`app.routes.ts` in the showcase needs no changes — `historySampleRoutes` already spreads into it.

## File Summary

**`maia-gen-spec` (3 files):**
- New: `EntityHistoryBlotterDef.kt`
- Modified: `EntityDef.kt` — add `historyBlotterDef` lazy val
- Modified: `ModelDef.kt` — add `entityHistoryBlotterDefs`

**`maia-gen-generator` — new renderers (14 files):**
- Backend (7): `EntityHistoryBlotterRowDtoRenderer`, `EntityHistoryBlotterRowMapperRenderer`, `EntityHistoryBlotterRowDtoMetaRenderer`, `EntityHistoryBlotterRowDtoDaoRenderer`, `EntityHistoryBlotterRowDtoRepoRenderer`, `EntityHistoryBlotterSearchServiceRenderer`, `EntityHistoryBlotterSearchEndpointRenderer`
- Frontend (7): `EntityHistoryBlotterRowDtoTypescriptRenderer`, `EntityHistoryBlotterServiceRenderer`, `EntityHistoryBlotterAgGridDatasourceRenderer`, `EntityHistoryBlotterComponentRenderer`, `EntityHistoryBlotterHtmlRenderer`, `EntityHistoryBlotterPageComponentRenderer`, `EntityHistoryBlotterPageHtmlRenderer`

**`maia-gen-generator` — modified renderers (3 files):**
- `EntityDetailViewPageHtmlRenderer.kt`
- `EntityDetailViewPageComponentRenderer.kt`
- `EntityCrudRoutesRenderer.kt`

**`maia-gen-generator` — modified module generators (5 files):**
- `AngularUiModuleGenerator.kt`
- `WebLayerModuleGenerator.kt`
- `ServiceLayerModuleGenerator.kt`
- `RepoLayerModuleGenerator.kt`
- `DomainModuleGenerator.kt`

**Showcase:** Zero changes — regenerate after `maia-gen` changes.
