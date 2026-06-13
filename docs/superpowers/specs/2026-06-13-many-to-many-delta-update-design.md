# Delta-based many-to-many join updates (design)

## Problem

`CrudServiceRenderer`'s generated `update()` function for entities with
many-to-many associations currently deletes **all** existing join rows and
recreates them from the edit DTO (see
`maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyCrudService.kt`
lines ~99-112).

For joins with `hasEffectiveTimestamps` (e.g. `LeftToRightManyToManyJoin`,
`UserGroupMembership`), this means every `update()` call:

- assigns a fresh `DomainId` and `createdTimestampUtc` to every join row,
  even ones the user didn't touch
- writes a delete + insert pair to the history table for every unchanged
  association, obscuring what actually changed

## Scope

Two independent sub-cases, both in `CrudServiceRenderer`'s update():

1. **Many-to-many without `hasEffectiveTimestamps`** (edit DTO field is
   `otherSideEntityIds: List<DomainId>`) — set-diff, no DTO change.
2. **Many-to-many with `hasEffectiveTimestamps`** (edit DTO field is a list
   of join DTOs, e.g. `LeftJoinRequestDto` with `leftEntityId`,
   `effectiveFrom`, `effectiveTo`) — id-based delta, requires adding a
   nullable `id` field to the join request DTO.

Out of scope:

- The commented-out TODO block for `hasEffectiveTimestamps && !isDeletable`
  (currently dead code, untouched by this change).
- Changing which fields are editable on an existing join row beyond
  `effectiveFrom`/`effectiveTo` — `otherSideEntityId` is treated as
  immutable for existing rows (to change which entity an association points
  to, remove it and add a new one).

## Design

### 1. Non-effective-timestamps case — set-diff

In `CrudServiceRenderer`'s update(), for `manyToManyAssociations` where
`!entityDef.hasEffectiveTimestamps.value`, replace delete-all/recreate with:

```kotlin
val existingJoins = this.${joinRepoFieldName}.findBy${thisSideFieldNameCapitalized}(id)
val existingIds = existingJoins.map { it.${otherSideFieldName} }.toSet()
val desiredIds = editDto.${otherSideDtoFieldName}.toSet()

existingJoins.filter { it.${otherSideFieldName} !in desiredIds }.forEach {
    this.${joinRepoFieldName}.deleteByPrimaryKey(it.id)
}

val newJoins = (desiredIds - existingIds).map { otherSideId ->
    ${joinEntityClass}.newInstance($thisSideFieldName = id, $otherSideFieldName = otherSideId)
}
this.${joinRepoFieldName}.bulkInsert(newJoins)
```

No DTO or Angular changes are needed for this case.

### 2. Effective-timestamps case — id-based delta

#### 2a. Request DTO gets a nullable `id`

In `EntityCreateApiDef.kt` (`timestampedJoinRequestDtosByAssociation`, shared
by the update API), add a new field to the join `RequestDtoDef`'s
`dtoFieldDefs`, first in the list:

```kotlin
RequestDtoFieldDef(
    ClassFieldDef.aClassField("id", FieldTypes.domainId).nullable().build(),
    null
),
```

This flows through automatically (no renderer changes) to:

- `LeftJoinRequestDto.kt`: `val id: DomainId?`
- `LeftJoinRequestDto.ts`: `id?: string`

`null` means "new association"; a populated value means "this is join row
`id`, as previously returned by `LeftJoinFetchDto.id`".

#### 2b. CrudServiceRenderer update() — id-based delta

For `manyToManyAssociations` where `entityDef.hasEffectiveTimestamps.value &&
entityDef.isDeletable`, replace the current `append("""..."""trimMargin())`
block with (converted to `appendLine`/helper calls so the conditional/loop
structure can be generated):

```kotlin
val existingJoinsById = this.${joinRepoFieldName}.findBy${thisSideFieldNameCapitalized}(id).associateBy { it.id }
val submittedIds = editDto.${otherSideDtoFieldName}.mapNotNull { it.id }.toSet()

existingJoinsById.keys.filterNot { it in submittedIds }.forEach {
    this.${joinRepoFieldName}.deleteByPrimaryKey(it)
}

val newJoins = editDto.${otherSideDtoFieldName}.filter { it.id == null }.map { joinDto ->
    ${joinEntityClass}.newInstance(
        effectiveFrom = joinDto.effectiveFrom,
        effectiveTo = joinDto.effectiveTo,
        $thisSideFieldName = id,
        $otherSideFieldName = joinDto.${otherSideFieldName}EntityId
    )
}
this.${joinRepoFieldName}.bulkInsert(newJoins)

editDto.${otherSideDtoFieldName}.filter { it.id != null }.forEach { joinDto ->
    val existing = existingJoinsById[joinDto.id]
        ?: throw this.maiaProblems.<TBD: stale/conflict problem — see Open Questions>

    if (existing.effectiveFrom != joinDto.effectiveFrom || existing.effectiveTo != joinDto.effectiveTo) {
        this.${joinRepoFieldName}.setFields(
            ${joinEntityClass}Updater.forPrimaryKey(joinDto.id) {
                effectiveFrom(joinDto.effectiveFrom)
                effectiveTo(joinDto.effectiveTo)
            }
        )
    }
}
```

### 3. Angular form changes

`AngularReactiveFormComponentRenderer.kt` — thread `id` through the
many-to-many join entry model in 3 spots:

1. Join entry type (~line 384-389): add `id: string | null`
2. Fetch→form mapping (~line 752-757): `id: e.id`
3. "Add join" handler (~line 837-842): new entries get `id: null`
4. Form→submit mapping (~line 964-968): `id: j.id`

No other Angular logic changes — add/remove/edit UX is unchanged; only the
round-tripped `id` changes server-side behavior.

## Testing

- Existing RightMany CRUD e2e Playwright test should pass unchanged
  (add/remove associations).
- New test on `RightManyCrudService.update()` (or equivalent): edit only
  `effectiveFrom`/`effectiveTo` of an existing join → assert the join row's
  `id` and `createdTimestampUtc` are unchanged, and exactly one history row
  (UPDATE) is written, not a DELETE+CREATE pair.
- Test: mixed submission (unchanged existing, changed existing, new, and one
  existing omitted) → assert correct inserts/updates/deletes.
- Test: non-effective-timestamps many-to-many set-diff — unchanged
  associations keep their `id`/`createdTimestampUtc`.

## Open Questions

- Which `MaiaProblems` method (existing or new) should be thrown when a
  submitted join `id` no longer exists (stale/concurrent edit)? Plan phase
  should grep `MaiaProblems` for an existing optimistic-lock/conflict
  problem, or define a new one.
