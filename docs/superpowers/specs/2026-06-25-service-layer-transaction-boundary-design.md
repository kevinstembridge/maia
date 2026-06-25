# Service Layer Transaction Boundary

**Date:** 2026-06-25

## Problem

`CrudServiceRenderer` generates service classes annotated only with `@Component`. Mutating operations (`create`, `update`, `delete`) can span multiple DB writes with no transaction boundary — a failure mid-operation leaves partial state with no rollback.

## Design

Method-level `@Transactional` annotations added entirely within `CrudServiceRenderer`. No class-level or `EntityDef`/`ClassDef` changes.

### Infrastructure

Add to `Fqcns.kt`:
```kotlin
val SPRING_TRANSACTIONAL = Fqcn.valueOf("org.springframework.transaction.annotation.Transactional")
```

### Renderer helper

Add to `CrudServiceRenderer`:
```kotlin
private fun appendTransactional(readOnly: Boolean = false) {
    addImportFor(Fqcns.SPRING_TRANSACTIONAL)
    if (readOnly) {
        appendLine("    @Transactional(readOnly = true)")
    } else {
        appendLine("    @Transactional")
    }
}
```

Parallel to the existing `appendPreAuthorize` helper.

### Method annotations

| Render function | Generated method | Annotation |
|---|---|---|
| `render create by DTO` | `create(createDto)` | `@Transactional` |
| `render the create function` | `create(entity)` | `@Transactional` |
| `render existsBy functions for unique indexes` | `existsBy*` | `@Transactional(readOnly = true)` |
| `render the fetchForEdit function` | `fetchForEdit` | `@Transactional(readOnly = true)` |
| `render the update function` | `update` | `@Transactional` |
| `render inline update function` | `updateXxx` | `@Transactional` |
| `render the setFields function` | `setFields` | `@Transactional` |
| `render the delete function` | `delete` | `@Transactional` |

### Files changed

- `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/Fqcns.kt` — add `SPRING_TRANSACTIONAL`
- `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt` — add helper, call before each method

## Black-box test

**Class:** `LeftManyTransactionBlackBoxTest : AbstractBlackBoxTest()`  
**Location:** `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/`

### Scenario

POST `/api/left-many/create` with a valid `someString`/`someInt` but a non-existent UUID in `rightSimpleEntityIds`. The service inserts the `left_many` entity row, then attempts to insert the `left_to_right_simple` join row, which fails with a FK violation (no matching row in `right_many`). With `@Transactional` in place, both inserts are rolled back.

### Setup

- `@Autowired private lateinit var leftManyDao: LeftManyDao`
- `@BeforeEach`: truncate `left_many` table (via `truncateTable(LeftManyEntityMeta.SCHEMA_AND_TABLE_NAME)`) so each test starts clean

### Test method

```
fun `nothing is persisted when a join insert fails`()
  POST /api/left-many/create  { someString, someInt, rightSimpleEntityIds: [<random-uuid-not-in-db>] }
  assert response is 5xx
  assert leftManyDao.findAll() is empty
```

### Why FK violation

`left_to_right_simple` has no unique constraint on `(left_simple_id, right_simple_id)`, so a duplicate-ID approach would silently insert two rows. A non-existent `rightSimpleEntityId` triggers a FK constraint on the join insert — after the entity row is already written — which is the right point in the call stack to demonstrate rollback.
