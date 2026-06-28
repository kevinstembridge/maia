# M2M System-Managed Effective Timestamps: Modify as Close+Reinsert

## Problem

When a many-to-many join entity has both system-managed effective timestamps and extra fields,
submitting an update with an existing `id` but changed extra field values currently does nothing.
The reconcile logic only handles two cases: close removed records, and insert new records (`id == null`).
Changed extra fields on an existing record are silently ignored.

## Behaviour

When a submitted join record has `id != null` and its extra fields differ from the stored record,
the system must close the old record's effective range and insert a new record starting from `Instant.now()`.

Change detection compares extra fields only (fields beyond left FK, right FK, effectiveFrom, effectiveTo).
The right-side entity ID is not compared — changing it via the UI means delete + add, not an in-place edit.

This logic is only generated when the join entity has extra fields (`extraReconcileArgs` non-empty)
and system-managed effective timestamps. No spec-level flag is required.

## Generator Change

All changes are in `CrudServiceRenderer.kt`, inside `render private join reconciliation functions`,
in the branch: `effectiveRangeDef?.dateType == TIMESTAMP && isDeletable && isSystemManaged`.

### Reconcile function (modified)

```
`reconcile X joins`(id, submitted):
  `close effectiveRange on removed X entities`(id, submitted)          // unchanged
  val modifiedDtos = `close effectiveRange on modified X entities`(id, submitted)  // NEW
  `insert added and replaced X entities`(id, submitted, modifiedDtos)  // renamed + new param
```

When `extraReconcileArgs` is empty, the middle step is skipped and the insert helper keeps its
current name and signature.

### `close effectiveRange on modified X entities` (new)

Only generated when `extraReconcileArgs` is non-empty.

```kotlin
private fun `close effectiveRange on modified X entities`(
    id: DomainId,
    submitted: List<XJoinDto>
): List<XJoinDto> {
    val existingById = this.xRepo.findEffectiveByThisSide(id).associateBy { it.id }
    return submitted.filter { it.id != null }.filter { joinDto ->
        val existing = existingById[joinDto.id!!]
            ?: throw this.maiaProblems.joinRecordNotFound("XEntity")
        // compare extra fields only
        existing.someField != joinDto.someField // one clause per extra field
    }.onEach { this.xRepo.closeEffectiveRange(it.id!!) }
}
```

The comparison is generated as one `!=` clause per extra field, ORed with `||`
(or expressed as `listOf(...).any { ... }` depending on what reads cleaner).

### `insert added and replaced X entities` (renamed from `insert added X entities`)

Only renamed/modified when `extraReconcileArgs` is non-empty; otherwise unchanged.

```kotlin
private fun `insert added and replaced X entities`(
    id: DomainId,
    submitted: List<XJoinDto>,
    modifiedDtos: List<XJoinDto>
) {
    val newJoins = (submitted.filter { it.id == null } + modifiedDtos).map { joinDto ->
        XEntity.newInstance(
            effectiveFrom = Instant.now(),
            effectiveTo = null,
            thisSideField = id,
            otherSideField = joinDto.otherSideEntityId,
            // extra fields...
        )
    }
    this.xRepo.bulkInsert(newJoins)
}
```

## Error Handling

A submitted record with `id != null` that does not appear in `existingById` (stale or invalid id)
throws `maiaProblems.joinRecordNotFound("XEntity")` — the same exception used in the
user-managed effective range path.

A closed record submitted with its old id also triggers `joinRecordNotFound` because
`findEffectiveBy${thisSideFieldNameCapitalized}` returns only currently-effective records.

## Testing

Test vehicle: `leftToRightManyToManyJoinEntityDef` in the showcase spec — it has
`withEffectiveTimestamps` (system-managed) and `someInt` as an extra field.

Test cases:
1. Create a join record with a known `someInt`. Submit update with same `id`, different `someInt`.
   Assert: old record has a closed `effectiveTo`; new record exists with `effectiveFrom ≈ now` and new `someInt`.
2. Submit update with same `id` and unchanged `someInt`.
   Assert: no new record created (change-detection no-op, old record still open).
