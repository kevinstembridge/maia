# Timeline Blotter Design

## Problem

Display the change history of a `LeftManyEntity` — including both its own field changes and its many-to-many join add/remove events — in a single unified chronological blotter page.

The entity (`leftManyEntityDef`) has `recordVersionHistory = true`, and its join to `rightManyEntityDef` uses a `simpleManyToManyEntity` with `effectiveRange(managedBy = EffectiveRangeManagedBy.SYSTEM)`. Currently no UI exists to show these two streams of events together.

## Approach

A new `timelineBlotter()` DSL concept parallel to the existing `EntityHistoryBlotter` stack. The generator produces a UNION SQL DAO, Spring endpoint, and Angular ag-grid page — all fully generated from the spec.

## Spec DSL

```kotlin
// Step 1: entity must have a timestamp for change events
// Add to leftManyEntityDef:
field_lastModifiedTimestampUtc()

// Step 2: declare the timeline blotter
val leftManyTimelineBlotterDef = timelineBlotter(
    entityDef = leftManyEntityDef,
    joinDefs = listOf(leftToRightSystemManagedEffectiveRangeEntityDef)
) {
    joinDisplayField(
        joinDef = leftToRightSystemManagedEffectiveRangeEntityDef,
        fromEntityDef = rightManyEntityDef,
        fieldName = "someString"
    )
}

val leftManyTimelineBlotterPageDef = blotterPage(leftManyTimelineBlotterDef)
```

`joinDisplayField()` tells the generator to JOIN onto the right entity's table and include a human-readable display name column alongside the raw FK.

## Unified DTO Schema

```kotlin
data class LeftManyTimelineBlotterRowDto(
    val eventTimestamp: Instant,
    val eventType: String,                        // ENTITY_CHANGE | JOIN_ADDED | JOIN_REMOVED
    val changeType: ChangeType?,                  // entity events only: CREATE | UPDATE | DELETE
    val version: Long?,                           // entity events only
    val someInt: Int?,                            // entity events only — one field per entity history field
    val someString: String?,                      // entity events only
    val rightSystemEffectiveId: DomainId?,        // join events only — named from join's right FK field name
    val rightSystemEffectiveDisplayName: String?, // join events only — from joinDisplayField()
)
```

For entities with multiple join defs, each join adds its own pair of nullable FK + display name fields.

## UNION SQL

The DAO wraps three UNION ALL arms in a subquery so ag-grid's dynamic WHERE clause can filter across all three:

```sql
SELECT * FROM (
  -- Arm 1: entity field changes
  SELECT
    lmh.last_modified_timestamp_utc AS event_timestamp,
    'ENTITY_CHANGE'                  AS event_type,
    lmh.change_type, lmh.version, lmh.some_int, lmh.some_string,
    NULL::uuid  AS right_system_effective_id,
    NULL::text  AS right_system_effective_display_name
  FROM maia.left_many_history lmh
  WHERE lmh.id = :entityId

  UNION ALL

  -- Arm 2: join added (lower bound of effective_range)
  SELECT
    lower(ltrse.effective_range), 'JOIN_ADDED',
    NULL, NULL, NULL, NULL,
    ltrse.right_system_effective_id, rm.some_string
  FROM maia.left_to_right_system_effective ltrse
  JOIN maia.right_many rm ON rm.id = ltrse.right_system_effective_id
  WHERE ltrse.left_system_effective_id = :entityId

  UNION ALL

  -- Arm 3: join removed (upper bound of effective_range, non-null only)
  SELECT
    upper(ltrse.effective_range), 'JOIN_REMOVED',
    NULL, NULL, NULL, NULL,
    ltrse.right_system_effective_id, rm.some_string
  FROM maia.left_to_right_system_effective ltrse
  JOIN maia.right_many rm ON rm.id = ltrse.right_system_effective_id
  WHERE ltrse.left_system_effective_id = :entityId
    AND upper(ltrse.effective_range) IS NOT NULL
) AS timeline
$whereClause
ORDER BY event_timestamp DESC
$offsetAndLimitClause
```

## Backend Stack (generated per entity)

| File | Layer | Purpose |
|---|---|---|
| `*TimelineBlotterRowDto` | domain | Kotlin data class |
| `*TimelineBlotterRowDtoMeta` | domain | Field→column mapping for AgGridSearchModelConverter |
| `*TimelineBlotterRowDtoRowMapper` | dao | JDBC ResultSet → DTO |
| `*TimelineBlotterRowDtoDao` | dao | UNION SQL search + count |
| `*TimelineBlotterRowDtoRepo` | repo | Exception wrapping |
| `*TimelineBlotterRowDtoSearchService` | service | Delegates to repo |
| `*TimelineBlotterSearchEndpoint` | web | `POST /api/{entity-path}/{entityId}/timeline/search` + `/count` |

## Angular Stack (generated per entity)

| File | Purpose |
|---|---|
| `*TimelineBlotterRowDto.ts` | TypeScript DTO interface |
| `*TimelineBlotterService.ts` | HTTP calls to endpoint |
| `*TimelineBlotterDatasource.ts` | ag-grid infinite row model datasource (entityId via @Input) |
| `*TimelineBlotterComponent.ts/html` | ag-grid blotter; columns: eventTimestamp, eventType, changeType, version, entity fields, join display fields |
| `*TimelineBlotterPageComponent.ts/html` | Page wrapper; reads entityId from route params |

Route: `/left-many/timeline/:entityId`
Navigation: "View Timeline" link added to the entity detail view (generated by `EntityDetailViewHtmlRenderer` when a timeline blotter def is present for the entity).

## Generator Changes

**New spec files** (`maia-gen-spec`):
- `TimelineBlotterJoinDef.kt`
- `TimelineBlotterDef.kt`
- `TimelineBlotterDefBuilder.kt`
- Update: `ModelDef.kt`, `ApplicationModelDef.kt`, `AbstractSpec.kt`

**New renderers** (`maia-gen-generator`):
- 7 Kotlin renderers (domain/dao/repo/service/web)
- 7 Angular renderers (TS DTO, service, datasource, component, HTML, page component, page HTML)

**Module generator updates**: `DomainModuleGenerator`, `DaoLayerModuleGenerator`, `RepoLayerModuleGenerator`, `ServiceLayerModuleGenerator`, `WebLayerModuleGenerator`, `AngularUiModuleGenerator`

**Entity detail view update**: `EntityDetailViewHtmlRenderer` (or equivalent) — add "View Timeline" link when entity has a timeline blotter def.

## Constraints

- Entity must declare `field_lastModifiedTimestampUtc()` for entity change events to be timestamped.
- Join must be a `simpleManyToManyEntity` with `effectiveRange(managedBy = EffectiveRangeManagedBy.SYSTEM)`.
- `entityId` is a required path variable (endpoint is per-entity, not global).
