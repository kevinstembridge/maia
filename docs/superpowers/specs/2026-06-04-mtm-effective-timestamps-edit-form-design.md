# ManyToMany Effective Timestamps — Edit Form

**Date:** 2026-06-04
**Branch:** mtm_effective_timestamps
**Scope:** Generator change (edit form only; extends the create-form work in `2026-06-04-mtm-effective-timestamps-create-form-design.md`)

## Problem

The edit form for entities with timestamped many-to-many associations uses a chip-based UI. It shows
existing associations as chips (name only) and has no way to view or edit effective timestamps.
When saved, it calls `Instant.now()` on the backend for all re-created joins — discarding any
previously stored timestamps and making it impossible to set them.

## Solution

Mirror the create-form mini-form approach for the edit page. Existing joins are pre-populated from
`fetchForEdit` with their stored timestamps; the user can add, remove, or replace entries before
saving. The backend accepts the full join DTO (with timestamps) instead of bare IDs.

Non-timestamped associations remain chip-based and unchanged on the edit page.

---

## Layer 1: `EntityUpdateApiDef` — update request DTO

**File:** `EntityUpdateApiDef.kt`

Add `timestampedJoinRequestDtosByAssociation`, mirroring `EntityCreateApiDef`:

```kotlin
val timestampedJoinRequestDtosByAssociation: Map<ManyToManyEntityDef, RequestDtoDef> by lazy {
    entityDef.manyToManyAssociations
        .filter { it.entityDef.hasEffectiveTimestamps.value }
        .associateWith { m2m ->
            val otherSide = m2m.otherSideFrom(entityDef)
            RequestDtoDef(
                dtoBaseName = DtoBaseName("${otherSide.displayName.replace(" ", "")}Join"),
                packageName = entityDef.packageName,
                dtoFieldDefs = listOf(
                    RequestDtoFieldDef(ClassFieldDef.aClassField("${otherSide.fieldName}EntityId", FieldTypes.domainId).build(), null),
                    RequestDtoFieldDef(ClassFieldDef.aClassField("effectiveFrom", FieldTypes.instant).nullable().build(), null),
                    RequestDtoFieldDef(ClassFieldDef.aClassField("effectiveTo", FieldTypes.instant).nullable().build(), null)
                ),
                preAuthorizeExpression = null,
                moduleName = moduleName
            )
        }
}
```

In `dtoFields`, distinguish timestamped from non-timestamped M2M (same split as `EntityCreateApiDef`):

```kotlin
entityDef.manyToManyAssociations.map { m2m ->
    val otherSide = m2m.otherSideFrom(entityDef)
    if (m2m.entityDef.hasEffectiveTimestamps.value) {
        val joinDtoDef = timestampedJoinRequestDtosByAssociation[m2m]!!
        RequestDtoFieldDef(
            ClassFieldDef.aClassField("${otherSide.fieldName}Entities",
                FieldTypes.list(FieldTypes.requestDto(joinDtoDef))).nullable().build(), null
        )
    } else {
        RequestDtoFieldDef(
            ClassFieldDef.aClassField("${otherSide.fieldName}EntityIds",
                FieldTypes.list(FieldTypes.domainId)).nullable().build(), null
        )
    }
}
```

`timestampedJoinRequestDtosByAssociation` on `EntityUpdateApiDef` **delegates to the create API def**
rather than building new `RequestDtoDef` instances, to avoid rendering the same Kotlin/TypeScript
join DTO twice:

```kotlin
val timestampedJoinRequestDtosByAssociation: Map<ManyToManyEntityDef, RequestDtoDef> by lazy {
    entityDef.entityCrudApiDef?.createApiDef
        ?.timestampedJoinRequestDtosByAssociation
        ?: emptyMap()
}
```

If there is no create API def, this returns an empty map and no timestamped fields are generated for
the edit page.

---

## Layer 2: `FetchForEditDto` — include join timestamps

### 2a. New join-fetch DTO type

**Files:** `EntityDef.kt`, `FetchForEditDtoDef.kt`, `RowMapperFieldDef.kt`

`fetchForEditManyToManyFieldDefs` currently uses `FieldTypes.pkAndName(...)` for all M2M.
For timestamped associations, the field type must reference a new per-association inline DTO that
has `id`, `name`, `effectiveFrom?`, `effectiveTo?`.

Define a new `timestampedJoinFetchDtosByAssociation` property on `EntityDef` (or in
`FetchForEditDtoDef`):

```
{OtherSide}JoinFetchDto
    id            : DomainId
    name          : String
    effectiveFrom : Instant?
    effectiveTo   : Instant?
```

Example: `LeftJoinFetchDto` for the right-many entity.

`fetchForEditManyToManyFieldDefs` branches on `hasEffectiveTimestamps`:
- Timestamped → `${otherSide}Entities: List<{OtherSide}JoinFetchDto>`
- Non-timestamped → `${otherSide}Entities: List<{OtherSide}PkAndNameDto>` (unchanged)

### 2b. `ManyToManyRowMapperFieldDef` — timestamp flag

Add `withTimestamps: Boolean` to `ManyToManyRowMapperFieldDef` so the row mapper renderer can
distinguish which SQL and which DTO class to use.

### 2c. Row mapper SQL

**File:** `RowMapperRenderer.kt`

The `fetchLeft/RightEntities` sub-query currently selects:
```sql
select other.id, other.some_string
from maia.left_many other
join maia.left_to_right_many_to_many_join mtm on other.id = mtm.left_id
where mtm.right_id = :entityId
order by other.some_string
```

When `withTimestamps = true`, add two columns:
```sql
select other.id, other.some_string,
       mtm.effective_from, mtm.effective_to
from maia.left_many other
join maia.left_to_right_many_to_many_join mtm on other.id = mtm.left_id
where mtm.right_id = :entityId
order by other.some_string
```

The `{OtherSide}JoinFetchDtoRowMapper` reads the four columns and constructs the new DTO.

### 2d. New TypeScript interface

```typescript
// LeftJoinFetchDto.ts
export interface LeftJoinFetchDto {
    effectiveFrom?: string;
    effectiveTo?: string;
    id: string;
    name: string;
}
```

`RightManyFetchForEditDto.leftEntities` changes type from `ReadonlyArray<LeftManyPkAndNameDto>` to
`ReadonlyArray<LeftJoinFetchDto>`.

---

## Layer 3: `CrudServiceRenderer` — backend update

**File:** `CrudServiceRenderer.kt`

In `render update function`, for timestamped M2M change the mapping from:

```kotlin
val newLeftJoins = editDto.leftEntityIds.map { left ->
    JoinEntity.newInstance(effectiveFrom = Instant.now(), effectiveTo = null, right = id, left = left)
}
```

to:

```kotlin
val newLeftJoins = editDto.leftEntities.map { joinDto ->
    JoinEntity.newInstance(
        effectiveFrom = joinDto.effectiveFrom,
        effectiveTo   = joinDto.effectiveTo,
        right         = id,
        left          = joinDto.leftEntityId
    )
}
```

The `Instant.now()` import is removed from this code path. Non-timestamped path unchanged.

---

## Layer 4: Frontend renderers

### 4a. `AngularUiModuleGenerator`

Add `manyToManyTimestampedFieldsForEdit()`:

```kotlin
private fun manyToManyTimestampedFieldsForEdit(
    entityDef: EntityDef,
    associations: List<ManyToManyEntityDef>
): List<ManyToManyTimestampedFieldDef> {
    val updateApiDef = entityDef.entityCrudApiDef?.updateApiDef ?: return emptyList()
    return associations
        .filter { it.entityDef.hasEffectiveTimestamps.value }
        .mapNotNull { m2m ->
            val otherSide = m2m.otherSideFrom(entityDef)
            val typeaheadDef = typeaheadByEntityDef[otherSide.entityDef] ?: return@mapNotNull null
            val joinDtoDef = updateApiDef.timestampedJoinRequestDtosByAssociation[m2m] ?: return@mapNotNull null
            ManyToManyTimestampedFieldDef(entityDef, m2m, typeaheadDef, joinDtoDef)
        }
}
```

In `renderEntityEditPages()`:
- Replace `manyToManyChipFieldsForEdit(...)` with two calls: one for chip fields (non-timestamped only) and one for timestamped fields
- Pass `timestampedFields` to both `AngularReactiveFormComponentRenderer` and `EntityEditReactiveFormHtmlRenderer`
- Include timestamped field service class names in `providerServices`

### 4b. `EntityEditReactiveFormHtmlRenderer`

Add `timestampedFields: List<ManyToManyTimestampedFieldDef> = emptyList()` parameter and pass to
the parent `AbstractCrudReactiveFormHtmlRenderer`. No other changes needed — the parent class
already knows how to render timestamped fields via `renderManyToManyTimestampedFields()`.

### 4c. `AngularReactiveFormComponentRenderer` — edit ngOnInit pre-population

The renderer already handles timestamped fields for the create form. For the edit form
(`FormPurpose.edit`), after `formGroup.patchValue(...)` and before `loading.set(false)`, emit a
mapping block for each timestamped field:

```typescript
this.${field.joinsFieldName} = dto.${field.requestDtoFieldName}?.map(e => ({
    entityId: e.id,
    entityName: e.name,
    effectiveFrom: e.effectiveFrom ? new Date(e.effectiveFrom) : null,
    effectiveTo: e.effectiveTo ? new Date(e.effectiveTo) : null,
})) ?? [];
```

`dto.${field.requestDtoFieldName}` corresponds to `dto.leftEntities` — the `{OtherSide}JoinFetchDto`
list returned by `fetchForEdit`.

The `${field.joinsFieldName}` (`leftJoins`) declaration, `showLeftJoinForm`, `confirmAdd*`,
`cancelAdd*`, `remove*`, and `displayWith*` methods are already emitted for all timestamped fields
regardless of form purpose. Only the pre-population block is edit-specific.

---

## Resulting generated output (example)

### `RightManyFetchForEditDto.kt`
```kotlin
data class RightManyFetchForEditDto(
    val createdTimestampUtc: Instant,
    val id: DomainId,
    val leftEntities: List<LeftJoinFetchDto>,  // was List<LeftManyPkAndNameDto>
    val someInt: Int,
    val someString: String
)
```

### `RightManyUpdateRequestDto.kt`
```kotlin
// leftEntityIds field replaced by:
@param:JsonProperty("leftEntities") private val leftEntities_raw: List<LeftJoinRequestDto>?
val leftEntities: List<LeftJoinRequestDto> by lazy { leftEntities_raw ?: emptyList() }
```

### `right-many-entity-edit-form.ts` (key diff)
- Chip-based fields (`selectedLeftEntities`, `leftEntitySearchControl`, `leftEntityInput`) removed
- Mini-form fields (`leftJoins`, `showLeftJoinForm`, `addLeftJoinEntityControl`, etc.) added
- `ngOnInit` maps `dto.leftEntities` into `leftJoins` on fetch success
- `onSubmit` uses `leftJoins.map(j => ({ leftEntityId: j.entityId, ... }))` instead of `selectedLeftEntities.map(e => e.id)`

### `right-many-entity-edit-form.html` (key diff)
- `<mat-chip-grid>` section replaced by join-entries list + mini form (same structure as create)

---

## Out of scope

- Validation that `effectiveTo` > `effectiveFrom`
- Duplicate entries with different date ranges (same entity, different effective periods)
- Inline edit of timestamps for an existing join entry (add/remove only)
