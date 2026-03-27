# Spring Security Annotations on Generated CRUD Service and Endpoint Methods

## Overview

Add `@PreAuthorize` annotations to generated CRUD service methods and CRUD endpoint methods, using the authorities already declared in the `crudApi` sections of the spec DSL.

## Background

The spec DSL supports per-operation authorities via `apis(defaultAuthority = ...)`:

```kotlin
crud {
    apis(defaultAuthority = partySpec.writeAuthority) {
        create()
        update()
        delete()
    }
}
```

Each of `EntityCreateApiDef`, `EntityUpdateApiDef`, and `EntityDeleteApiDef` already derives a `preAuthorizeExpression` from `crudApiDef.authority`. `@EnableMethodSecurity` is active via `libs/maia-webapp`. The `RequestDtoEndpointRenderer` already emits `@PreAuthorize` successfully, confirming Spring Boot 4 compatibility.

## Changes

### 1. `CrudServiceRenderer.kt`

Add a private helper:

```kotlin
private fun appendPreAuthorize(authority: Authority?) {
    authority?.let {
        addImportFor(Fqcns.SPRING_SECURITY_PRE_AUTHORIZE)
        appendLine("    @PreAuthorize(\"hasAuthority('$it')\")")
    }
}
```

Call before the `fun` declaration in:
- `render create by API` — `createApiDef.crudApiDef.authority`
- `render update function` — `updateApiDef.crudApiDef.authority`
- `render inline update function` — `updateApiDef.crudApiDef.authority`
- `render delete function` — `entityDef.entityCrudApiDef?.deleteApiDef?.crudApiDef?.authority`

**Not annotated:** `create(entity)` and `setFields()` — internal overloads not called from the web tier.

### 2. `CrudEndpointRenderer.kt`

Uncomment the body of the existing `appendPreAuthorize(crudApiDef)` helper (already called for create, update, delete). Add a call to it in `renderInlineEndpoint` using the update `crudApiDef`.

## Regeneration

```
./gradlew :maia-showcase:service:maiaGeneration :maia-showcase:web:maiaGeneration
```
