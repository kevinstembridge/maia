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
