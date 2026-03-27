# Spring Security Annotations on Generated CRUD Methods Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Emit `@PreAuthorize` annotations on generated CRUD service methods and CRUD endpoint methods using the authorities declared in the spec's `crudApi` sections.

**Architecture:** Two renderer files in `maia-gen-generator` are modified: `CrudServiceRenderer` gains a private helper and calls it before each API-facing `fun`; `CrudEndpointRenderer` has its existing (commented-out) helper uncommented and its inline-endpoint renderer updated to pass the `CrudApiDef`. The showcase app is then regenerated.

**Tech Stack:** Kotlin, Spring Security `@PreAuthorize`, Maia code generator (renderer pattern), Gradle `maiaGeneration` task.

---

### Task 1: Add `@PreAuthorize` to generated service methods

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt`

The `simpleEntityDef` uses `partySpec.adminAuthority` (`"SYS__ADMIN"`), so after this change `SimpleCrudService.create(dto)` must have `@PreAuthorize("hasAuthority('SYS__ADMIN')")`.

- [ ] **Step 1: Add the `Authority` import and `appendPreAuthorize` helper**

In `CrudServiceRenderer.kt`, add to the import block (after the existing `Fqcns` import):

```kotlin
import org.maiaframework.gen.spec.definition.Authority
```

Add this private function at the bottom of the class, before the closing `}`:

```kotlin
    private fun appendPreAuthorize(authority: Authority?) {
        authority?.let {
            addImportFor(Fqcns.SPRING_SECURITY_PRE_AUTHORIZE)
            appendLine("    @PreAuthorize(\"hasAuthority('$it')\")")
        }
    }
```

- [ ] **Step 2: Annotate `create(createDto)`**

In `render create by API`, the current output block is:

```kotlin
        blankLine()
        blankLine()
        appendLine("    fun create(createDto: ${createApiDef.requestDtoDef.uqcn}): ${this.entityDef.entityUqcn} {")
```

Add the `appendPreAuthorize` call between the blank lines and the `appendLine`:

```kotlin
        blankLine()
        blankLine()
        appendPreAuthorize(createApiDef.crudApiDef.authority)
        appendLine("    fun create(createDto: ${createApiDef.requestDtoDef.uqcn}): ${this.entityDef.entityUqcn} {")
```

- [ ] **Step 3: Annotate `update(editDto)`**

In `render update function`, the current output block is:

```kotlin
            blankLine()
            blankLine()
            appendLine("    fun update(editDto: ${dtoDef.uqcn}) {")
```

Add the call between the blank lines and the `appendLine`:

```kotlin
            blankLine()
            blankLine()
            appendPreAuthorize(apiDef.crudApiDef.authority)
            appendLine("    fun update(editDto: ${dtoDef.uqcn}) {")
```

- [ ] **Step 4: Annotate inline update functions**

In `render inline update function`, the current output block is:

```kotlin
        blankLine()
        blankLine()
        appendLine("    fun update${fieldName.firstToUpper()}(editDto: $dtoUqcn) {")
```

Add the call. The inline function renderer doesn't receive the update `CrudApiDef` directly, so navigate via `entityDef`:

```kotlin
        blankLine()
        blankLine()
        appendPreAuthorize(this.entityDef.entityCrudApiDef?.updateApiDef?.crudApiDef?.authority)
        appendLine("    fun update${fieldName.firstToUpper()}(editDto: $dtoUqcn) {")
```

- [ ] **Step 5: Annotate `delete(...)`**

In `render delete function`, after the `isNotDeletable` early return the current output block is:

```kotlin
        blankLine()
        blankLine()
        appendLine("    fun delete($primaryKeyFieldNamesAndTypesCsv) {")
```

Add the call between the blank lines and the `appendLine`:

```kotlin
        blankLine()
        blankLine()
        appendPreAuthorize(this.entityDef.entityCrudApiDef?.deleteApiDef?.crudApiDef?.authority)
        appendLine("    fun delete($primaryKeyFieldNamesAndTypesCsv) {")
```

- [ ] **Step 6: Regenerate the service layer and inspect output**

```bash
./gradlew :maia-showcase:service:maiaGeneration
```

Open `maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/simple/SimpleCrudService.kt`.

Expected: `create(createDto)`, `update(editDto)`, and `delete(id)` each have `@PreAuthorize("hasAuthority('SYS__ADMIN')")` immediately before the `fun` keyword. The internal `create(entity: SimpleEntity)` and `setFields(updater)` have no annotation.

Also spot-check `AllFieldTypesCrudService.kt` (uses `writeAuthority = "WRITE"`): same three methods should have `@PreAuthorize("hasAuthority('WRITE')")`.

- [ ] **Step 7: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt
git add maia-showcase/service/src/generated/
git commit -m "feat: emit @PreAuthorize on generated CRUD service methods"
```

---

### Task 2: Fix `@PreAuthorize` in the CRUD endpoint renderer

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudEndpointRenderer.kt`

- [ ] **Step 1: Uncomment the `appendPreAuthorize` body**

Find the current (commented-out) implementation:

```kotlin
    private fun appendPreAuthorize(crudApiDef: CrudApiDef) {

        // TODO find out if we can do this in Spring Boot 3
//        crudApiDef.authority?.let { authority ->
//            addImportFor(Fqcns.SPRING_SECURITY_PRE_AUTHORIZE)
//            appendLine("    @PreAuthorize(\"hasAuthority('$authority')\")")
//        }

    }
```

Replace with:

```kotlin
    private fun appendPreAuthorize(crudApiDef: CrudApiDef) {

        crudApiDef.authority?.let { authority ->
            addImportFor(Fqcns.SPRING_SECURITY_PRE_AUTHORIZE)
            appendLine("    @PreAuthorize(\"hasAuthority('$authority')\")")
        }

    }
```

- [ ] **Step 2: Thread `CrudApiDef` through to `renderInlineEndpoint`**

The caller `render inline endpoints` already has the `updateApiDef`. Update the two functions to pass the `CrudApiDef`:

Change `render inline endpoints` from:

```kotlin
    private fun `render inline endpoints`() {

        this.entityCrudApiDef.updateApiDef?.let { apiDef ->
            apiDef.inlineEditDtoDefs.forEach { `render inline endpoint`(it) }
        }

    }
```

To:

```kotlin
    private fun `render inline endpoints`() {

        this.entityCrudApiDef.updateApiDef?.let { apiDef ->
            apiDef.inlineEditDtoDefs.forEach { `render inline endpoint`(it, apiDef.crudApiDef) }
        }

    }
```

Change the `renderInlineEndpoint` signature and add the annotation call. The current function:

```kotlin
    private fun renderInlineEndpoint(dtoDef: InlineEditDtoDef) {

        addImportFor(Fqcns.JAKARTA_VALIDATION_VALID)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_PUT_MAPPING)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)

        val dtoUqcn = dtoDef.uqcn
        val fieldName = dtoDef.fieldDef.classFieldDef.classFieldName

        blankLine()
        blankLine()
        appendLine("    @PutMapping(\"/api/${this.entityDef.entityBaseName.toSnakeCase()}/inline/${fieldName.toSnakeCase()}\", produces = [MediaType.APPLICATION_JSON_VALUE])")
        appendLine("    fun update${fieldName.firstToUpper()}(@RequestBody @Valid editDto: $dtoUqcn) {")
        blankLine()
        appendLine("        this.crudService.update${fieldName.firstToUpper()}(editDto)")
        blankLine()
        appendLine("    }")

    }
```

Replace with:

```kotlin
    private fun renderInlineEndpoint(dtoDef: InlineEditDtoDef, crudApiDef: CrudApiDef) {

        addImportFor(Fqcns.JAKARTA_VALIDATION_VALID)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_PUT_MAPPING)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)

        val dtoUqcn = dtoDef.uqcn
        val fieldName = dtoDef.fieldDef.classFieldDef.classFieldName

        blankLine()
        blankLine()
        appendLine("    @PutMapping(\"/api/${this.entityDef.entityBaseName.toSnakeCase()}/inline/${fieldName.toSnakeCase()}\", produces = [MediaType.APPLICATION_JSON_VALUE])")
        appendPreAuthorize(crudApiDef)
        appendLine("    fun update${fieldName.firstToUpper()}(@RequestBody @Valid editDto: $dtoUqcn) {")
        blankLine()
        appendLine("        this.crudService.update${fieldName.firstToUpper()}(editDto)")
        blankLine()
        appendLine("    }")

    }
```

- [ ] **Step 3: Regenerate the web layer and inspect output**

```bash
./gradlew :maia-showcase:web:maiaGeneration
```

Open `maia-showcase/web/src/generated/kotlin/main/org/maiaframework/showcase/simple/SimpleCrudEndpoint.kt`.

Expected: `create`, `update`, and `deleteById` each have `@PreAuthorize("hasAuthority('SYS__ADMIN')")` between the `@PostMapping`/`@PutMapping`/`@DeleteMapping` annotation and the `fun` keyword.

Also check an entity with inline edits (e.g. `AllFieldTypesCrudEndpoint.kt`) — inline update methods should also have `@PreAuthorize("hasAuthority('WRITE')")`.

- [ ] **Step 4: Build the full showcase to confirm compilation**

```bash
./gradlew :maia-showcase:build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudEndpointRenderer.kt
git add maia-showcase/web/src/generated/
git commit -m "feat: emit @PreAuthorize on generated CRUD endpoint methods"
```
