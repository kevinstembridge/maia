# System-Managed Effective Timestamps Design

## Context

Many-to-many join entities with `withEffectiveTimestamps(managedBy = SYSTEM)` currently only affect the UI (date-picker inputs are suppressed). The service layer does not implement the intended SYSTEM semantics — both `effectiveFrom` and `effectiveTo` are stored as `null` on insert, and removal hard-deletes the record. This design implements the full SYSTEM-managed behavior in the generator.

## Scope

- TIMESTAMP + SYSTEM-managed joins only (`effectiveRangeDef.managedBy == SYSTEM && dateType == TIMESTAMP`)
- `hasSingleEffectiveRecord = true` handling (close previous record on re-add) is deferred to a future concern
- LOCAL_DATE + SYSTEM-managed is also deferred

## Intended Semantics

- **Add join**: `effectiveFrom = Instant.now()`, `effectiveTo = null`
- **Remove join**: soft-delete — set `effectiveTo = Instant.now()` (record stays in DB as historical data)
- **Edit form**: shows only currently-effective joins (`effective_range @> current_timestamp`)
- **Parent entity delete**: FK guard ignores soft-deleted (historical) records

## Generator Changes

### `CrudServiceRenderer.kt`

All changes are gated on `managedBy == SYSTEM && dateType == TIMESTAMP`.

**Create path** — insert with system-set timestamps instead of DTO values:
```
effectiveFrom = Instant.now()   // was: joinDto.effectiveFrom
effectiveTo = null              // was: joinDto.effectiveTo
```

**Update read** — load only currently-effective records for the remove-detection loop:
```
findEffectiveBy${Side}(id)      // was: findBy${Side}(id)
```

**Update remove** — soft-delete instead of hard-delete:
```kotlin
this.${joinRepo}.setFields(
    ${JoinEntity}Updater.forPrimaryKey(it) {
        effectiveTo(Instant.now())
    }
)
// was: this.${joinRepo}.deleteByPrimaryKey(it)
```

**Update add** — same as create path: `effectiveFrom = Instant.now()`, `effectiveTo = null`.

**Update existing loop** — skipped entirely for SYSTEM-managed (no date fields editable by the user; the loop would always be a no-op).

**Delete FK guard** — ignore soft-deleted records:
```kotlin
if (this.${joinRepo}.findEffectiveBy${Side}(pk).isNotEmpty()) { ... }
// was: this.${joinRepo}.existsBy${Side}(pk)
```

`findEffectiveBy${Side}` already exists in the generated Repo (wraps the DAO's `effective_range @> current_timestamp` query). No new DAO method needed.

### `RowMapperRenderer.kt`

In `fetchRightEntitiesJoinFetchDtos`, when `managedBy == SYSTEM && dateType == TIMESTAMP`, add to the WHERE clause:
```sql
and mtm.effective_range @> current_timestamp
```

This prevents soft-deleted join entries from reappearing in the edit form.

## Regenerated Output (maia-showcase)

After running `maiaGeneration`:

- `LeftManyCrudService.kt`: create/update use `Instant.now()`, soft-delete on remove, `findEffectiveByLeft` in update loop and delete guard
- `LeftManyFetchForEditDtoRowMapper.kt`: `fetchRightEntitiesJoinFetchDtos` adds effective range filter

## Test Additions

`ManyToManyEffectiveRangeCrudPlaywrightTest` gets two new `@Autowired` fields and two DB assertion blocks:

**After create:**
```kotlin
val beforeCreate = Instant.now()
leftManyCreatePage.clickSubmitButton()
leftManyViewPage.assertOnPage()

val leftId = leftManyDao.findAllAsSequence().toList().single().id
val joinAfterCreate = leftToRightManyToManyJoinDao.findByLeft(leftId).single()
assertThat(joinAfterCreate.effectiveFrom).isBetween(beforeCreate, Instant.now())
assertThat(joinAfterCreate.effectiveTo).isNull()
```

**After remove + edit submit:**
```kotlin
val beforeRemove = Instant.now()
leftManyEditPage.clickSubmitButton()
leftManyViewPage.assertOnPage()

val joinAfterRemove = leftToRightManyToManyJoinDao.findByLeft(leftId).single()
assertThat(joinAfterRemove.effectiveTo).isBetween(beforeRemove, Instant.now())
```

`findByLeft` (not `findEffectiveByLeft`) is used in the assertions to retrieve the soft-deleted record.
