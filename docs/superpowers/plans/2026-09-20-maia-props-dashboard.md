# Maia Props Dashboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a `maia-props` Angular dashboard for viewing/editing `maia-props-parent` property overrides, plus the REST/service backend layer it needs (which doesn't exist yet), end to end through a real showcase page.

**Architecture:** Backend: `PropsManager` moves from `maia-props-api` into a new `maia-props-service` module and gains env+override merge logic; a new `maia-props-web` module exposes it over REST. Frontend: a new `@maia/maia-props` Angular library (signal store + Material dialogs, mirroring `maia-elasticsearch`) is wired into `maia-showcase-ui`. Verified with a black-box JVM Playwright CRUD journey test, not unit tests.

**Tech Stack:** Kotlin, Spring Boot, Spring Security `@PreAuthorize`, Angular 21, `@ngrx/signals`, Angular Material, JVM Playwright.

**User Verification:** NO — no user verification requested for this work.

**Reference design doc:** `docs/superpowers/specs/2026-09-20-maia-props-dashboard-design.md`

---

## Task 1: `PropsSpec.kt` — authorities and response DTOs

**Goal:** Add the two authorities and the two response DTO defs the rest of the plan depends on, so the generator produces `PropertyResponseDto` / `PropertyHistoryItemResponseDto` into `maia-props-domain`.

**Files:**
- Modify: `libs/maia-props-parent/maia-props-spec/src/main/kotlin/org/maiaframework/props/spec/PropsSpec.kt`

**Acceptance Criteria:**
- [ ] `MAIA_PROPS_READ` and `MAIA_PROPS_WRITE` authorities declared
- [ ] `PropertyResponseDto` and `PropertyHistoryItemResponseDto` generated into `maia-props-domain/src/generated/kotlin/main/org/maiaframework/props/`

**Verify:** `./gradlew :libs:maia-props-parent:maia-props-domain:maiaGeneration :libs:maia-props-parent:maia-props-domain:compileKotlin` → BUILD SUCCESSFUL, and `ls libs/maia-props-parent/maia-props-domain/src/generated/kotlin/main/org/maiaframework/props/PropertyResponseDto.kt libs/maia-props-parent/maia-props-domain/src/generated/kotlin/main/org/maiaframework/props/PropertyHistoryItemResponseDto.kt` shows both files.

**Steps:**

- [ ] **Step 1: Edit `PropsSpec.kt`**

Replace the full file content:

```kotlin
@file:Suppress("MemberVisibilityCanBePrivate")

package org.maiaframework.props.spec


import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.flags.AllowFindAll
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.lang.FieldTypes

@Suppress("unused")
class PropsSpec : AbstractSpec(appKey = AppKey("maia_props"), defaultSchemaName = SchemaName("props")) {


    val readAuthority = authority("MAIA_PROPS_READ")


    val writeAuthority = authority("MAIA_PROPS_WRITE")


    val changeTypeEnumDef = enumDef("org.maiaframework.domain.ChangeType") {
        provided()
    }


    val propertyEntityDef = entity(
        "org.maiaframework.props", "Props",
        versioned = true,
        recordVersionHistory = true,
        deletable = Deletable.TRUE,
        allowFindAll = AllowFindAll.TRUE,
    ) {
        moduleName("sys_ops")
        tableName(name = "props")
        daoHasSpringAnnotation = false
        field("propertyName", FieldTypes.string) {
            primaryKey()
            lengthConstraint(max = 200)
        }
        field("propertyValue", FieldTypes.string) {
            modifiableBySystem()
            lengthConstraint(max = 2000)
        }
        field_lastModifiedByUsername()
        field_lastModifiedTimestamp()
        field("comment", FieldTypes.string) {
            nullable()
            lengthConstraint(max = 200)
        }
    }


    val propertyDtoDef = simpleResponseDto("org.maiaframework.props", "Property") {
        field("propertyName", FieldTypes.string)
        field("effectiveValue", FieldTypes.string) {
            nullable()
        }
        field("isOverridden", FieldTypes.boolean)
        field("environmentValue", FieldTypes.string) {
            nullable()
        }
        field("sourceName", FieldTypes.string) {
            nullable()
        }
        field("lastModifiedByUsername", FieldTypes.string) {
            nullable()
        }
        field("lastModifiedTimestamp", FieldTypes.instant) {
            nullable()
        }
        field("comment", FieldTypes.string) {
            nullable()
        }
    }


    val propertyHistoryItemDtoDef = simpleResponseDto("org.maiaframework.props", "PropertyHistoryItem") {
        field("propertyName", FieldTypes.string)
        field("propertyValue", FieldTypes.string)
        field("changeType", changeTypeEnumDef)
        field("lastModifiedByUsername", FieldTypes.string)
        field("lastModifiedTimestamp", FieldTypes.instant)
        field("comment", FieldTypes.string) {
            nullable()
        }
        field("version", FieldTypes.long)
    }


}
```

- [ ] **Step 2: Run generation and compile**

Run: `./gradlew :libs:maia-props-parent:maia-props-domain:maiaGeneration :libs:maia-props-parent:maia-props-domain:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add libs/maia-props-parent/maia-props-spec/src/main/kotlin/org/maiaframework/props/spec/PropsSpec.kt libs/maia-props-parent/maia-props-domain/src/generated
git commit -m "Add MAIA_PROPS authorities and Property/PropertyHistoryItem response DTOs to PropsSpec"
```

---

## Task 2: New `maia-props-service` module — move `PropsManager` (behavior unchanged)

**Goal:** Physically relocate `PropsManager` out of `maia-props-api` into a new `maia-props-service` module, with all dependent modules updated, and behavior identical to today (no merge logic yet — that's Task 3).

**Files:**
- Create: `libs/maia-props-parent/maia-props-service/build.gradle.kts`
- Create: `libs/maia-props-parent/maia-props-service/src/main/kotlin/org/maiaframework/props/PropsManager.kt` (moved, unchanged)
- Delete: `libs/maia-props-parent/maia-props-api/src/main/kotlin/org/maiaframework/props/PropsManager.kt`
- Modify: `settings.gradle.kts`
- Modify: `libs/maia-props-parent/maia-props-autoconfigure/build.gradle.kts`
- Modify: `libs/maia-props-parent/maia-props-autoconfigure/src/main/kotlin/org/maiaframework/props/MaiaPropsAutoConfiguration.kt`
- Modify: `libs/maia-elasticsearch-parent/maia-elasticsearch/build.gradle.kts`

**Acceptance Criteria:**
- [ ] `maia-props-api` contains only `Props.kt`
- [ ] `maia-props-service` compiles standalone and exposes `PropsManager` unchanged
- [ ] `maia-props-autoconfigure`, `maia-props-starter`, `maia-elasticsearch` all still compile

**Verify:** `./gradlew :libs:maia-props-parent:maia-props-service:compileKotlin :libs:maia-props-parent:maia-props-autoconfigure:compileKotlin :libs:maia-elasticsearch-parent:maia-elasticsearch:compileKotlin :libs:maia-props-parent:maia-props-starter:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Register the new module in `settings.gradle.kts`**

Find this block:

```kotlin
include("libs:maia-props-parent:maia-props-api")
include("libs:maia-props-parent:maia-props-autoconfigure")
include("libs:maia-props-parent:maia-props-dao")
include("libs:maia-props-parent:maia-props-domain")
include("libs:maia-props-parent:maia-props-repo")
include("libs:maia-props-parent:maia-props-spec")
include("libs:maia-props-parent:maia-props-starter")
```

Replace with:

```kotlin
include("libs:maia-props-parent:maia-props-api")
include("libs:maia-props-parent:maia-props-autoconfigure")
include("libs:maia-props-parent:maia-props-dao")
include("libs:maia-props-parent:maia-props-domain")
include("libs:maia-props-parent:maia-props-repo")
include("libs:maia-props-parent:maia-props-service")
include("libs:maia-props-parent:maia-props-spec")
include("libs:maia-props-parent:maia-props-starter")
```

**CORRECTION (found during execution):** do NOT add `include("libs:maia-props-parent:maia-props-web")` here — Gradle 9.6.1 refuses to configure an `include()` whose project directory doesn't exist yet ("Configuring project ':libs:maia-props-parent:maia-props-web' without an existing directory is not allowed"), and this was verified empirically. Leave `maia-props-web` out of `settings.gradle.kts` in this task; Task 4 adds that one `include(...)` line itself when it creates the module's directory.

- [ ] **Step 2: Create `libs/maia-props-parent/maia-props-service/build.gradle.kts`**

```kotlin

plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    implementation(project(":libs:maia-common"))
    api(project(":libs:maia-props-parent:maia-props-repo"))

}
```

- [ ] **Step 3: Move `PropsManager.kt`**

```bash
mkdir -p libs/maia-props-parent/maia-props-service/src/main/kotlin/org/maiaframework/props
git mv libs/maia-props-parent/maia-props-api/src/main/kotlin/org/maiaframework/props/PropsManager.kt libs/maia-props-parent/maia-props-service/src/main/kotlin/org/maiaframework/props/PropsManager.kt
```

Content stays exactly as it is today (no code changes in this task):

```kotlin
package org.maiaframework.props

import org.maiaframework.props.repo.PropsRepo

class PropsManager(private val propsRepo: PropsRepo) {


    fun setProperty(
            propertyName: String,
            propertyValue: String,
            username: String,
            comment: String?
    ) {

        this.propsRepo.setPropertyOverride(
                propertyName,
                propertyValue,
                username,
                comment
        )

    }


    fun removeProperty(propertyName: String, username: String, comment: String?) {

        this.propsRepo.removePropertyOverride(
                propertyName,
                username,
                comment
        )

    }


}
```

- [ ] **Step 4: Update `maia-props-autoconfigure/build.gradle.kts`**

Find:

```kotlin
dependencies {

    api(project(":libs:maia-props-parent:maia-props-api"))
    api(project(":libs:maia-props-parent:maia-props-repo"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-flyway")

    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor:3.5.6")

}
```

Replace with:

```kotlin
dependencies {

    api(project(":libs:maia-props-parent:maia-props-api"))
    api(project(":libs:maia-props-parent:maia-props-repo"))
    api(project(":libs:maia-props-parent:maia-props-service"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-flyway")

    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor:3.5.6")

}
```

- [ ] **Step 5: Update `MaiaPropsAutoConfiguration.kt`'s `propsManager` bean**

Find:

```kotlin
    @Bean
    @ConditionalOnMissingBean
    fun propsManager(propsRepo: PropsRepo): PropsManager {

        return PropsManager(propsRepo)

    }
```

Replace with (no behavior change yet — `Environment` param added in Task 3):

```kotlin
    @Bean
    @ConditionalOnMissingBean
    fun propsManager(propsRepo: PropsRepo): PropsManager {

        return PropsManager(propsRepo)

    }
```

(No change needed here yet — leave as-is. `PropsManager`'s constructor is unchanged in this task, so this bean method is unchanged too. This step exists only to confirm you checked it; move on.)

- [ ] **Step 6: Update `maia-elasticsearch/build.gradle.kts`**

Find the line:

```kotlin
    api(project(":libs:maia-props-parent:maia-props-api"))
```

Replace with:

```kotlin
    api(project(":libs:maia-props-parent:maia-props-api"))
    api(project(":libs:maia-props-parent:maia-props-service"))
```

- [ ] **Step 7: Compile everything touched**

Run: `./gradlew :libs:maia-props-parent:maia-props-service:compileKotlin :libs:maia-props-parent:maia-props-api:compileKotlin :libs:maia-props-parent:maia-props-autoconfigure:compileKotlin :libs:maia-elasticsearch-parent:maia-elasticsearch:compileKotlin :libs:maia-props-parent:maia-props-starter:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add settings.gradle.kts libs/maia-props-parent/maia-props-service libs/maia-props-parent/maia-props-api libs/maia-props-parent/maia-props-autoconfigure libs/maia-elasticsearch-parent/maia-elasticsearch/build.gradle.kts
git commit -m "Move PropsManager from maia-props-api into new maia-props-service module"
```

---

## Task 3: `PropsManager` — env/override merge logic

**Goal:** Add the merge logic and history/DTO methods to `PropsManager`, now living in `maia-props-service`.

**Files:**
- Modify: `libs/maia-props-parent/maia-props-service/src/main/kotlin/org/maiaframework/props/PropsManager.kt`
- Modify: `libs/maia-props-parent/maia-props-autoconfigure/src/main/kotlin/org/maiaframework/props/MaiaPropsAutoConfiguration.kt`

**Acceptance Criteria:**
- [ ] `PropsManager.getAllProperties()` returns one `PropertyResponseDto` per property name found in any `EnumerablePropertySource` or in the DB overrides
- [ ] DB override always wins over environment value when both exist
- [ ] `PropsManager.getPropertyHistory(name)` returns `PropertyHistoryItemResponseDto` list
- [ ] `PropsManager.setProperty(...)` returns the resulting `PropertyResponseDto`
- [ ] `propsManager` bean now injects `Environment`

**Verify:** `./gradlew :libs:maia-props-parent:maia-props-service:compileKotlin :libs:maia-props-parent:maia-props-autoconfigure:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Rewrite `PropsManager.kt`**

```kotlin
package org.maiaframework.props

import org.maiaframework.props.repo.PropsRepo
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.EnumerablePropertySource

class PropsManager(
    private val propsRepo: PropsRepo,
    private val environment: ConfigurableEnvironment
) {


    fun getAllProperties(): List<PropertyResponseDto> {

        val overridesByName = this.propsRepo.getAllProperties().associateBy { it.propertyName }
        val sourceNameByPropertyName = enumerateEnvironmentPropertyNames()

        val allPropertyNames = (sourceNameByPropertyName.keys + overridesByName.keys).toSortedSet()

        return allPropertyNames.map { propertyName ->
            toPropertyResponseDto(propertyName, overridesByName[propertyName], sourceNameByPropertyName[propertyName])
        }

    }


    private fun enumerateEnvironmentPropertyNames(): Map<String, String> {

        val sourceNameByPropertyName = mutableMapOf<String, String>()

        this.environment.propertySources
            .filterIsInstance<EnumerablePropertySource<*>>()
            .forEach { propertySource ->
                propertySource.propertyNames.forEach { propertyName ->
                    sourceNameByPropertyName.putIfAbsent(propertyName, propertySource.name)
                }
            }

        return sourceNameByPropertyName

    }


    private fun toPropertyResponseDto(
        propertyName: String,
        override: org.maiaframework.props.PropsEntity?,
        environmentSourceName: String?
    ): PropertyResponseDto {

        val environmentValue = this.environment.getProperty(propertyName)

        return if (override != null) {
            PropertyResponseDto(
                comment = override.comment,
                effectiveValue = override.propertyValue,
                environmentValue = environmentValue,
                isOverridden = true,
                lastModifiedByUsername = override.lastModifiedByUsername,
                lastModifiedTimestamp = override.lastModifiedTimestamp,
                propertyName = propertyName,
                sourceName = "DB override",
            )
        } else {
            PropertyResponseDto(
                comment = null,
                effectiveValue = environmentValue,
                environmentValue = environmentValue,
                isOverridden = false,
                lastModifiedByUsername = null,
                lastModifiedTimestamp = null,
                propertyName = propertyName,
                sourceName = environmentSourceName,
            )
        }

    }


    fun getPropertyHistory(propertyName: String): List<PropertyHistoryItemResponseDto> {

        return this.propsRepo.getPropertyHistory(propertyName).map {
            PropertyHistoryItemResponseDto(
                changeType = it.changeType,
                comment = it.comment,
                lastModifiedByUsername = it.lastModifiedByUsername,
                lastModifiedTimestamp = it.lastModifiedTimestamp,
                propertyName = it.propertyName,
                propertyValue = it.propertyValue,
                version = it.version,
            )
        }

    }


    fun setProperty(
        propertyName: String,
        propertyValue: String,
        username: String,
        comment: String?
    ): PropertyResponseDto {

        this.propsRepo.setPropertyOverride(
                propertyName,
                propertyValue,
                username,
                comment
        )

        val override = this.propsRepo.getPropertyOrNull(propertyName)
            ?: throw IllegalStateException("Expected a property override to exist for '$propertyName' immediately after setting it.")

        return toPropertyResponseDto(propertyName, override, null)

    }


    fun removeProperty(propertyName: String, username: String, comment: String?) {

        this.propsRepo.removePropertyOverride(
                propertyName,
                username,
                comment
        )

    }


}
```

Check the generated `PropertyResponseDto`/`PropertyHistoryItemResponseDto` constructors before running this step — they're generated from Task 1's spec and will use named-parameter construction with alphabetically-ordered fields (matching the pattern seen in `PropsEntity.kt`'s generated `newInstance`). If the generated constructor parameter order or names differ from what's used above, fix the call sites to match the actual generated file at `libs/maia-props-parent/maia-props-domain/src/generated/kotlin/main/org/maiaframework/props/PropertyResponseDto.kt` — read that file first if compilation fails on this step.

- [ ] **Step 2: Update the `propsManager` bean to inject `Environment`**

In `libs/maia-props-parent/maia-props-autoconfigure/src/main/kotlin/org/maiaframework/props/MaiaPropsAutoConfiguration.kt`, find:

```kotlin
    @Bean
    @ConditionalOnMissingBean
    fun propsManager(propsRepo: PropsRepo): PropsManager {

        return PropsManager(propsRepo)

    }
```

Replace with:

```kotlin
    @Bean
    @ConditionalOnMissingBean
    fun propsManager(propsRepo: PropsRepo, environment: org.springframework.core.env.ConfigurableEnvironment): PropsManager {

        return PropsManager(propsRepo, environment)

    }
```

- [ ] **Step 3: Compile**

Run: `./gradlew :libs:maia-props-parent:maia-props-service:compileKotlin :libs:maia-props-parent:maia-props-autoconfigure:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add libs/maia-props-parent/maia-props-service/src/main/kotlin/org/maiaframework/props/PropsManager.kt libs/maia-props-parent/maia-props-autoconfigure/src/main/kotlin/org/maiaframework/props/MaiaPropsAutoConfiguration.kt
git commit -m "Add Environment/override merge logic to PropsManager"
```

---

## Task 4: New `maia-props-web` module and `MaiaPropsEndpoint`

**Goal:** Expose `PropsManager` over REST, mirroring `maia-job-web`.

**Files:**
- Create: `libs/maia-props-parent/maia-props-web/build.gradle.kts`
- Create: `libs/maia-props-parent/maia-props-web/src/main/kotlin/org/maiaframework/props/MaiaPropsEndpoint.kt`
- Create: `libs/maia-props-parent/maia-props-web/src/main/kotlin/org/maiaframework/props/SetPropertyRequestDto.kt`
- Modify: `libs/maia-props-parent/maia-props-starter/build.gradle.kts`
- Modify: `settings.gradle.kts` (add `include("libs:maia-props-parent:maia-props-web")` — Task 2 deliberately deferred this to here, since Gradle 9.6.1 refuses `include()` for a directory that doesn't exist yet; add this line as the FIRST step, before creating the module's files)

**Acceptance Criteria:**
- [ ] `maia-props-web` compiles and runs its own `maiaGeneration` task against `PropsApplicationSpec` using `WebLayerModuleGeneratorKt`
- [ ] `MaiaPropsEndpoint` is in package `org.maiaframework.props` (required — this is the package already whitelisted in `MaiaShowcaseAppConfiguration`'s `@ComponentScan`, confirmed in Task 5; a sub-package would NOT be scanned)
- [ ] `maia-props-starter` now aggregates the web module so consumers (like `maia-showcase`) get the endpoint for free

**Verify:** `./gradlew :libs:maia-props-parent:maia-props-web:compileKotlin :libs:maia-props-parent:maia-props-starter:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Create `libs/maia-props-parent/maia-props-web/build.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val maiagen by configurations.creating

plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    implementation(kotlin("reflect"))

    api(project(":libs:maia-common"))
    api(project(":libs:maia-props-parent:maia-props-service"))
    api(project(":libs:maia-props-parent:maia-props-domain"))
    api(project(":libs:maia-webapp:maia-webapp-domain"))

    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-web")

    maiagen(project(":libs:maia-props-parent:maia-props-spec"))
    maiagen(project(":maia-gen:maia-gen-generator"))

}


sourceSets {
    main {
        java.srcDir("src/generated/kotlin/main")
        resources.srcDir("src/generated/resources/main")
    }
    test {
        java.srcDir("src/generated/kotlin/test")
        java.srcDir("src/generated/resources/test")
    }
}


tasks {
    clean {
        delete("src/generated")
    }
}


tasks.register<JavaExec>("maiaGeneration") {

    group = BasePlugin.BUILD_GROUP
    inputs.files(file("../maia-props-spec/src/main/kotlin/org/maiaframework/props/spec/PropsApplicationSpec"), file("../maia-props-spec/src/main/kotlin/org/maiaframework/props/spec/PropsSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/resources/main")
    outputs.dir("src/generated/kotlin/test")
    outputs.dir("src/generated/resources/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.WebLayerModuleGeneratorKt")
    args("applicationSpecClassName=org.maiaframework.props.spec.PropsApplicationSpec")

}


tasks.withType<KotlinCompile>() {
    dependsOn("maiaGeneration")
}


tasks.withType<ProcessResources>() {
    dependsOn("maiaGeneration")
}
```

- [ ] **Step 2: Create `SetPropertyRequestDto.kt`**

```kotlin
package org.maiaframework.props

data class SetPropertyRequestDto(
    val propertyValue: String,
    val comment: String?
)
```

- [ ] **Step 3: Create `MaiaPropsEndpoint.kt`**

```kotlin
package org.maiaframework.props

import org.maiaframework.webapp.domain.auth.CurrentUserHolder
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping($$"${maia.props.web.base-url:/api/ops}")
class MaiaPropsEndpoint(private val propsManager: PropsManager) {


    @GetMapping("/props", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('MAIA_PROPS_READ')")
    fun getAllProperties(): List<PropertyResponseDto> {

        return this.propsManager.getAllProperties()

    }


    @GetMapping("/props/{propertyName}/history", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('MAIA_PROPS_READ')")
    fun getPropertyHistory(
        @PathVariable propertyName: String
    ): List<PropertyHistoryItemResponseDto> {

        return this.propsManager.getPropertyHistory(propertyName)

    }


    @PostMapping("/props/{propertyName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('MAIA_PROPS_WRITE')")
    fun setProperty(
        @PathVariable propertyName: String,
        @RequestBody request: SetPropertyRequestDto
    ): PropertyResponseDto {

        val username = CurrentUserHolder.currentUsernameOrNull ?: "unknown"
        return this.propsManager.setProperty(propertyName, request.propertyValue, username, request.comment)

    }


    @DeleteMapping("/props/{propertyName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MAIA_PROPS_WRITE')")
    fun removeProperty(
        @PathVariable propertyName: String,
        @RequestParam(required = false) comment: String?
    ) {

        val username = CurrentUserHolder.currentUsernameOrNull ?: "unknown"
        this.propsManager.removeProperty(propertyName, username, comment)

    }


}
```

- [ ] **Step 4: Update `maia-props-starter/build.gradle.kts`**

Find:

```kotlin
dependencies {

    api(project(":libs:maia-props-parent:maia-props-autoconfigure"))
    api(project(":libs:maia-props-parent:maia-props-api"))
    implementation("org.springframework.boot:spring-boot-starter")

}
```

Replace with:

```kotlin
dependencies {

    api(project(":libs:maia-props-parent:maia-props-autoconfigure"))
    api(project(":libs:maia-props-parent:maia-props-api"))
    api(project(":libs:maia-props-parent:maia-props-web"))
    implementation("org.springframework.boot:spring-boot-starter")

}
```

- [ ] **Step 5: Compile**

Run: `./gradlew :libs:maia-props-parent:maia-props-web:compileKotlin :libs:maia-props-parent:maia-props-starter:compileKotlin`
Expected: BUILD SUCCESSFUL. If the generated web-layer scaffolding from `WebLayerModuleGeneratorKt` conflicts with the hand-written `MaiaPropsEndpoint.kt`/`SetPropertyRequestDto.kt` (e.g. the generator also emits a file at the same path), inspect `libs/maia-props-parent/maia-props-web/src/generated/kotlin/main/` and rename/adjust the hand-written files to avoid the collision — check what `maia-job-web`'s generated output contains for comparison (`ls libs/maia-job-parent/maia-job-web/src/generated/kotlin/main/org/maiaframework/job/`) since that module has the identical generator wired the same way and already coexists with `MaiaJobEndpoint.kt`.

- [ ] **Step 6: Commit**

```bash
git add libs/maia-props-parent/maia-props-web libs/maia-props-parent/maia-props-starter/build.gradle.kts
git commit -m "Add maia-props-web module with MaiaPropsEndpoint REST controller"
```

---

## Task 5: Register showcase-side authorities, full build, confirm props endpoint is live

**Goal:** Make the showcase app's own generated `Authority` enum aware of `MAIA_PROPS_READ`/`MAIA_PROPS_WRITE` (needed by Task 12's test), build the full backend, and confirm `MaiaShowcaseApplication` starts with the new endpoint registered.

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcasePartySpec.kt`

**Acceptance Criteria:**
- [ ] Full backend build succeeds
- [ ] `MaiaPropsEndpoint` bean is present in the showcase app's Spring context
- [ ] The generated `Authority` enum in `maia-showcase/domain` contains `MAIA_PROPS_READ` and `MAIA_PROPS_WRITE`

**Verify:** `./gradlew build -x test` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add the two authorities to `MaiaShowcasePartySpec.kt`**

The showcase app's generated `Authority` enum (`maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/Authority.kt`, rendered by `AuthorityEnumRenderer`) is populated from authorities declared directly in `maia-showcase/spec`'s own spec classes — it does **not** automatically pick up authorities declared in `maia-job-spec`, `maia-elasticsearch-spec`, or `maia-props-spec`. Confirm this by noting `MaiaShowcasePartySpec.kt` already redeclares `authority("MAIA_JOB_READ")`, `authority("MAIA_JOB_WRITE")`, `authority("MAIA_ELASTICSEARCH_SYS_OPS_READ")`, `authority("MAIA_ELASTICSEARCH_SYS_OPS_WRITE")` — each a duplicate string of the identical authority already declared in its owning feature's own spec, purely so the showcase app's own generated enum (used for UI/admin purposes) knows about it. `@PreAuthorize` itself only checks the raw string, so this redeclaration doesn't affect runtime security — it's only for the generated catalog enum.

Find in `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcasePartySpec.kt`:

```kotlin
    val jobWriteAuthority = authority("MAIA_JOB_WRITE")


    val jobReadAuthority = authority("MAIA_JOB_READ")


    val writeAuthority = authority("WRITE") {
```

Replace with:

```kotlin
    val jobWriteAuthority = authority("MAIA_JOB_WRITE")


    val jobReadAuthority = authority("MAIA_JOB_READ")


    val propsWriteAuthority = authority("MAIA_PROPS_WRITE")


    val propsReadAuthority = authority("MAIA_PROPS_READ")


    val writeAuthority = authority("WRITE") {
```

- [ ] **Step 2: Full backend build**

Run: `./gradlew build -x test`
Expected: BUILD SUCCESSFUL across all modules, including `maia-showcase:app`.

- [ ] **Step 3: Confirm the generated showcase `Authority` enum picked up the new authorities**

Read `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/Authority.kt` and confirm it now contains `MAIA_PROPS_READ` and `MAIA_PROPS_WRITE` entries.

- [ ] **Step 4: Confirm `org.maiaframework.props` is in the showcase `@ComponentScan`**

Read `maia-showcase/app/src/main/kotlin/org/maiaframework/showcase/config/MaiaShowcaseAppConfiguration.kt` and confirm the `@ComponentScan(basePackages = [...])` list includes `"org.maiaframework.props"`. It already does as of this plan being written — if it has been removed or changed, add it back:

```kotlin
@ComponentScan(basePackages = [
    "org.maiaframework.json",
    "org.maiaframework.webapp",
    "org.maiaframework.props",
    "org.maiaframework.hazelcast"
])
```

If Step 4 required a fix to `MaiaShowcaseAppConfiguration.kt`, include it in Step 5's commit below; otherwise it's just a confirmation, nothing to add.

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcasePartySpec.kt maia-showcase/domain/src/generated
git commit -m "Register MAIA_PROPS_READ/WRITE in showcase's generated Authority enum"
```

---

## Task 6: Scaffold the `maia-props` Angular library project

**Goal:** Create the `@maia/maia-props` ng-packagr project shell (no feature code yet), registered in the workspace.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-props/package.json`
- Create: `libs/maia-ui-workspace/projects/maia-props/ng-package.json`
- Create: `libs/maia-ui-workspace/projects/maia-props/tsconfig.lib.json`
- Create: `libs/maia-ui-workspace/projects/maia-props/tsconfig.lib.prod.json`
- Create: `libs/maia-ui-workspace/projects/maia-props/tsconfig.spec.json`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/public-api.ts` (empty placeholder export, replaced in Task 9)
- Modify: `libs/maia-ui-workspace/angular.json`
- Modify: `libs/maia-ui-workspace/tsconfig.json`

**Acceptance Criteria:**
- [ ] `ng build maia-props` succeeds from `libs/maia-ui-workspace`
- [ ] Project registered the same way `maia-elasticsearch` is

**Verify:** `cd libs/maia-ui-workspace && npx ng build maia-props` → build succeeds

**Steps:**

- [ ] **Step 1: Read the reference files first**

Read `libs/maia-ui-workspace/projects/maia-elasticsearch/package.json`, `ng-package.json`, `tsconfig.lib.json`, `tsconfig.lib.prod.json`, `tsconfig.spec.json` to confirm exact current content (Angular/peer-dependency versions drift over time — copy from what's actually there, not from what's shown below if it differs).

- [ ] **Step 2: Create `package.json`**

```json
{
  "name": "@maia/maia-props",
  "version": "0.0.1",
  "peerDependencies": {
    "@angular/common": "^21.1.0",
    "@angular/core": "^21.1.0",
    "@angular/material": "^21.1.0",
    "@angular/cdk": "^21.1.0"
  },
  "dependencies": {
    "tslib": "^2.3.0"
  },
  "sideEffects": false
}
```

(Match the exact peer dependency version numbers from `maia-elasticsearch/package.json` read in Step 1 if they differ from the above.)

- [ ] **Step 3: Create `ng-package.json`**

```json
{
  "$schema": "../../node_modules/ng-packagr/ng-package.schema.json",
  "dest": "../../dist/maia-props",
  "lib": {
    "entryFile": "src/public-api.ts"
  }
}
```

- [ ] **Step 4: Create `tsconfig.lib.json`, `tsconfig.lib.prod.json`, `tsconfig.spec.json`**

Copy the exact content of `maia-elasticsearch`'s three tsconfig files verbatim (they contain no project-specific paths, only shared compiler settings) into the same three filenames under `libs/maia-ui-workspace/projects/maia-props/`.

- [ ] **Step 5: Create a placeholder `src/public-api.ts`**

```typescript
export const MAIA_PROPS_PLACEHOLDER = true;
```

(Replaced with real exports in Task 9.)

- [ ] **Step 6: Register the project in `libs/maia-ui-workspace/angular.json`**

Read the `"maia-elasticsearch"` entry in `angular.json` first to copy its exact shape (build/test target options, `tsConfig` paths, `projectType`, `prefix`). Add a new top-level entry `"maia-props"` under `"projects"` with the same structure, substituting `maia-elasticsearch` → `maia-props` in every path (`"root": "projects/maia-props"`, `"sourceRoot": "projects/maia-props/src"`, `"tsConfig": "projects/maia-props/tsconfig.lib.json"`, etc.).

- [ ] **Step 7: Register the project in `libs/maia-ui-workspace/tsconfig.json`**

Find the `references` array entries for `maia-elasticsearch`:

```json
{
  "path": "./projects/maia-elasticsearch/tsconfig.lib.json"
},
{
  "path": "./projects/maia-elasticsearch/tsconfig.spec.json"
},
```

Add equivalent entries for `maia-props` immediately after them:

```json
{
  "path": "./projects/maia-props/tsconfig.lib.json"
},
{
  "path": "./projects/maia-props/tsconfig.spec.json"
},
```

- [ ] **Step 8: Build**

Run: `cd libs/maia-ui-workspace && npx ng build maia-props`
Expected: build succeeds, output appears under `libs/maia-ui-workspace/dist/maia-props`.

- [ ] **Step 9: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props libs/maia-ui-workspace/angular.json libs/maia-ui-workspace/tsconfig.json
git commit -m "Scaffold maia-props Angular library project"
```

---

## Task 7: Models and API service

**Goal:** TypeScript DTOs matching the backend response shapes, and the HTTP service that calls the four `MaiaPropsEndpoint` routes.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/models/PropertyResponseDto.ts`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/models/PropertyHistoryItemResponseDto.ts`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/services/props-api-base-url.token.ts`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/services/props-api.service.ts`

**Acceptance Criteria:**
- [ ] DTO field names/types match `PropertyResponseDto`/`PropertyHistoryItemResponseDto` from Task 1/3 exactly
- [ ] Service methods cover all four endpoint routes from Task 4

**Verify:** `cd libs/maia-ui-workspace && npx ng build maia-props` → build succeeds

**Steps:**

- [ ] **Step 1: Create `PropertyResponseDto.ts`**

```typescript
export interface PropertyResponseDto {
    propertyName: string;
    effectiveValue: string | null;
    isOverridden: boolean;
    environmentValue: string | null;
    sourceName: string | null;
    lastModifiedByUsername: string | null;
    lastModifiedTimestamp: string | null;
    comment: string | null;
}
```

- [ ] **Step 2: Create `PropertyHistoryItemResponseDto.ts`**

```typescript
export type ChangeType = 'CREATE' | 'UPDATE' | 'DELETE';

export interface PropertyHistoryItemResponseDto {
    propertyName: string;
    propertyValue: string;
    changeType: ChangeType;
    lastModifiedByUsername: string;
    lastModifiedTimestamp: string;
    comment: string | null;
    version: number;
}
```

- [ ] **Step 3: Create `props-api-base-url.token.ts`**

```typescript
import {InjectionToken} from '@angular/core';


export const PROPS_API_BASE_URL = new InjectionToken<string>(
    'propsApiBaseUrl',
    { factory: () => '/api/ops' }
);
```

- [ ] **Step 4: Create `props-api.service.ts`**

```typescript
import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PropertyResponseDto} from '../models/PropertyResponseDto';
import {PropertyHistoryItemResponseDto} from '../models/PropertyHistoryItemResponseDto';
import {PROPS_API_BASE_URL} from './props-api-base-url.token';


@Injectable()
export class PropsApiService {


    private baseUrl = inject(PROPS_API_BASE_URL);


    constructor(private http: HttpClient) {}


    getAllProperties(): Observable<PropertyResponseDto[]> {

        return this.http.get<PropertyResponseDto[]>(`${this.baseUrl}/props`);

    }


    getPropertyHistory(propertyName: string): Observable<PropertyHistoryItemResponseDto[]> {

        return this.http.get<PropertyHistoryItemResponseDto[]>(`${this.baseUrl}/props/${encodeURIComponent(propertyName)}/history`);

    }


    setProperty(propertyName: string, propertyValue: string, comment: string | null): Observable<PropertyResponseDto> {

        return this.http.post<PropertyResponseDto>(`${this.baseUrl}/props/${encodeURIComponent(propertyName)}`, {
            propertyValue,
            comment,
        });

    }


    removeProperty(propertyName: string, comment: string | null): Observable<void> {

        const params = comment ? {comment} : {};
        return this.http.delete<void>(`${this.baseUrl}/props/${encodeURIComponent(propertyName)}`, {params});

    }


}
```

- [ ] **Step 5: Build**

Run: `cd libs/maia-ui-workspace && npx ng build maia-props`
Expected: build succeeds (these files aren't exported from `public-api.ts` yet, but `ng build` type-checks the whole project directory, not just exported files).

- [ ] **Step 6: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/models libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/services
git commit -m "Add maia-props models and PropsApiService"
```

---

## Task 8: Signal store and filtering helpers

**Goal:** `PropsDashboardStore` (state + fetch/filter/mutate) and its pure filtering helpers, with a unit test for the filtering logic (the one place this codebase does hand-write unit tests, per `elastic-indices-filtering.spec.ts` / `jobs-filtering.spec.ts`).

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.ts`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-filtering.spec.ts`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state/props-dashboard-store.ts`

**Acceptance Criteria:**
- [ ] `filterProperties` filters by case-insensitive name substring and, when `overriddenOnly` is true, further restricts to `isOverridden === true`
- [ ] `PropsDashboardStore` fetches on demand, exposes `visibleProperties`, and has methods to patch state after a successful write/remove without a full refetch

**Verify:** `cd libs/maia-ui-workspace && npx ng test maia-props --watch=false` → filtering spec passes

**Steps:**

- [ ] **Step 1: Write the failing filtering test — `props-dashboard-filtering.spec.ts`**

```typescript
import {filterProperties} from './props-dashboard-filtering';
import {PropertyResponseDto} from '../models/PropertyResponseDto';

function aProperty(overrides: Partial<PropertyResponseDto> = {}): PropertyResponseDto {
    return {
        propertyName: 'server.port',
        effectiveValue: '3200',
        isOverridden: false,
        environmentValue: '3200',
        sourceName: 'applicationConfig',
        lastModifiedByUsername: null,
        lastModifiedTimestamp: null,
        comment: null,
        ...overrides,
    };
}

describe('filterProperties', () => {

    it('returns all properties when nameFilter is empty and overriddenOnly is false', () => {
        const properties = [aProperty({propertyName: 'a'}), aProperty({propertyName: 'b'})];
        expect(filterProperties(properties, '', false).length).toBe(2);
    });

    it('filters case-insensitively by name substring', () => {
        const properties = [aProperty({propertyName: 'server.port'}), aProperty({propertyName: 'maia.props.web.base-url'})];
        const result = filterProperties(properties, 'PROPS', false);
        expect(result.length).toBe(1);
        expect(result[0].propertyName).toBe('maia.props.web.base-url');
    });

    it('restricts to overridden properties when overriddenOnly is true', () => {
        const properties = [
            aProperty({propertyName: 'a', isOverridden: true}),
            aProperty({propertyName: 'b', isOverridden: false}),
        ];
        const result = filterProperties(properties, '', true);
        expect(result.length).toBe(1);
        expect(result[0].propertyName).toBe('a');
    });

    it('sorts results by property name ascending', () => {
        const properties = [aProperty({propertyName: 'zebra'}), aProperty({propertyName: 'alpha'})];
        const result = filterProperties(properties, '', false);
        expect(result.map(p => p.propertyName)).toEqual(['alpha', 'zebra']);
    });

});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd libs/maia-ui-workspace && npx ng test maia-props --watch=false`
Expected: FAIL — `props-dashboard-filtering` module not found.

- [ ] **Step 3: Implement `props-dashboard-filtering.ts`**

```typescript
import {PropertyResponseDto} from '../models/PropertyResponseDto';


export function filterProperties(
    properties: PropertyResponseDto[],
    nameFilter: string,
    overriddenOnly: boolean
): PropertyResponseDto[] {

    const normalizedFilter = nameFilter.trim().toLowerCase();

    return properties
        .filter(p => !overriddenOnly || p.isOverridden)
        .filter(p => normalizedFilter === '' || p.propertyName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.propertyName.localeCompare(b.propertyName));

}


export function countOverridden(properties: PropertyResponseDto[]): number {

    return properties.filter(p => p.isOverridden).length;

}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd libs/maia-ui-workspace && npx ng test maia-props --watch=false`
Expected: PASS (4 tests)

- [ ] **Step 5: Implement `props-dashboard-store.ts`**

```typescript
import {patchState, signalStore, withComputed, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';
import {rxMethod} from '@ngrx/signals/rxjs-interop';
import {pipe, tap} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {tapResponse} from '@ngrx/operators';
import {PropertyResponseDto} from '../models/PropertyResponseDto';
import {PropsApiService} from '../services/props-api.service';
import {countOverridden, filterProperties} from './props-dashboard-filtering';

type PropsDashboardState = {
    properties: PropertyResponseDto[];
    isLoading: boolean;
    error: string | null;
    nameFilter: string;
    overriddenOnly: boolean;
};

const initialState: PropsDashboardState = {
    properties: [],
    isLoading: false,
    error: null,
    nameFilter: '',
    overriddenOnly: false,
};

export const PropsDashboardStore = signalStore(

    withState(initialState),

    withComputed(({properties, nameFilter, overriddenOnly}) => {
        const visibleProperties = computed<PropertyResponseDto[]>(() =>
            filterProperties(properties(), nameFilter(), overriddenOnly())
        );
        const overriddenCount = computed<number>(() =>
            countOverridden(properties())
        );
        return {visibleProperties, overriddenCount};
    }),

    withMethods((store, propsService = inject(PropsApiService)) => ({

        fetchAllProperties: rxMethod<void>(
            pipe(
                tap(() => patchState(store, {isLoading: true})),
                switchMap(() =>
                    propsService.getAllProperties().pipe(
                        tapResponse({
                            next: (properties) => patchState(store, {properties, isLoading: false, error: null}),
                            error: (err) => {
                                patchState(store, {isLoading: false, error: 'Failed to load properties.'});
                                console.error(err);
                            },
                        })
                    )
                )
            )
        ),

        retryFetch(): void {
            this.fetchAllProperties();
        },

        onNameFilterChanged(value: string): void {
            patchState(store, {nameFilter: value});
        },

        onOverriddenOnlyToggled(value: boolean): void {
            patchState(store, {overriddenOnly: value});
        },

        applyPropertyUpdate(updated: PropertyResponseDto): void {
            const properties = store.properties().filter(p => p.propertyName !== updated.propertyName);
            patchState(store, {properties: [...properties, updated]});
        },

        applyPropertyRemoval(propertyName: string): void {
            patchState(store, {properties: store.properties().filter(p => p.propertyName !== propertyName)});
        },

    }))

);
```

- [ ] **Step 6: Build**

Run: `cd libs/maia-ui-workspace && npx ng build maia-props`
Expected: build succeeds

- [ ] **Step 7: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/state
git commit -m "Add PropsDashboardStore and filtering helpers with tests"
```

---

## Task 9: Dialogs

**Goal:** The three Material dialogs — edit (add/edit), remove, history.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/dialogs/edit-property-dialog/edit-property-dialog.ts`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/dialogs/edit-property-dialog/edit-property-dialog.html`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/dialogs/remove-override-dialog/remove-override-dialog.ts`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/dialogs/remove-override-dialog/remove-override-dialog.html`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/dialogs/property-history-dialog/property-history-dialog.ts`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/dialogs/property-history-dialog/property-history-dialog.html`

**Acceptance Criteria:**
- [ ] `EditPropertyDialog` takes `{propertyName: string | null, currentValue: string | null}` as `MAT_DIALOG_DATA`; when `propertyName` is null the name field is editable (add flow), otherwise read-only (edit flow); closes with `{propertyName, propertyValue, comment} | undefined`
- [ ] `RemoveOverrideDialog` takes `{propertyName: string}`, closes with `{comment: string | null} | undefined`
- [ ] `PropertyHistoryDialog` takes `{propertyName: string, historyItems: PropertyHistoryItemResponseDto[]}` and renders them, no API calls of its own (the page component fetches history before opening it, consistent with dumb/presentational dialogs elsewhere in this codebase)

**Verify:** `cd libs/maia-ui-workspace && npx ng build maia-props` → build succeeds

**Steps:**

- [ ] **Step 1: `edit-property-dialog.ts`**

```typescript
import {Component, Inject} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';

export interface EditPropertyDialogData {
    propertyName: string | null;
    currentValue: string | null;
}

export interface EditPropertyDialogResult {
    propertyName: string;
    propertyValue: string;
    comment: string | null;
}

@Component({
    selector: 'maia-edit-property-dialog',
    templateUrl: './edit-property-dialog.html',
    imports: [ReactiveFormsModule, MatDialogTitle, MatDialogContent, MatDialogActions, MatFormFieldModule, MatInputModule, MatButtonModule]
})
export class EditPropertyDialog {

    readonly isAdding = this.data.propertyName === null;

    readonly form = this.formBuilder.group({
        propertyName: this.formBuilder.control(this.data.propertyName ?? '', Validators.required),
        propertyValue: this.formBuilder.control(this.data.currentValue ?? '', Validators.required),
        comment: this.formBuilder.control(''),
    });

    constructor(
        public dialogRef: MatDialogRef<EditPropertyDialog>,
        @Inject(MAT_DIALOG_DATA) public data: EditPropertyDialogData,
        private formBuilder: FormBuilder
    ) {}

    onSubmit() {

        if (this.form.invalid) {
            return;
        }

        const value = this.form.getRawValue();

        const result: EditPropertyDialogResult = {
            propertyName: value.propertyName!,
            propertyValue: value.propertyValue!,
            comment: value.comment || null,
        };

        this.dialogRef.close(result);

    }

    onCancel(): void {
        this.dialogRef.close();
    }

}
```

- [ ] **Step 2: `edit-property-dialog.html`**

```html
<h2 mat-dialog-title>{{isAdding ? 'Add Property Override' : 'Edit Property Override'}}</h2>
<form [formGroup]="form" (ngSubmit)="onSubmit()">
    <mat-dialog-content>
        <mat-form-field appearance="fill">
            <mat-label>Property name</mat-label>
            <input matInput name="propertyName" formControlName="propertyName" [readonly]="!isAdding">
        </mat-form-field>
        <mat-form-field appearance="fill">
            <mat-label>Value</mat-label>
            <input matInput name="propertyValue" formControlName="propertyValue">
        </mat-form-field>
        <mat-form-field appearance="fill">
            <mat-label>Comment (optional)</mat-label>
            <input matInput name="comment" formControlName="comment">
        </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions>
        <button mat-button type="button" (click)="onCancel()">Cancel</button>
        <button mat-flat-button type="submit" [disabled]="form.invalid">Submit</button>
    </mat-dialog-actions>
</form>
```

- [ ] **Step 3: `remove-override-dialog.ts`**

```typescript
import {Component, Inject} from '@angular/core';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';

export interface RemoveOverrideDialogData {
    propertyName: string;
}

export interface RemoveOverrideDialogResult {
    comment: string | null;
}

@Component({
    selector: 'maia-remove-override-dialog',
    templateUrl: './remove-override-dialog.html',
    imports: [ReactiveFormsModule, MatDialogTitle, MatDialogContent, MatDialogActions, MatFormFieldModule, MatInputModule, MatButtonModule]
})
export class RemoveOverrideDialog {

    readonly form = this.formBuilder.group({
        comment: this.formBuilder.control(''),
    });

    constructor(
        public dialogRef: MatDialogRef<RemoveOverrideDialog>,
        @Inject(MAT_DIALOG_DATA) public data: RemoveOverrideDialogData,
        private formBuilder: FormBuilder
    ) {}

    onConfirm() {

        const result: RemoveOverrideDialogResult = {
            comment: this.form.getRawValue().comment || null,
        };

        this.dialogRef.close(result);

    }

    onCancel(): void {
        this.dialogRef.close();
    }

}
```

- [ ] **Step 4: `remove-override-dialog.html`**

```html
<h2 mat-dialog-title>Remove Override</h2>
<form [formGroup]="form" (ngSubmit)="onConfirm()">
    <mat-dialog-content>
        <p>Remove the override for <strong>{{data.propertyName}}</strong>? It will revert to its environment-resolved value, if any.</p>
        <mat-form-field appearance="fill">
            <mat-label>Comment (optional)</mat-label>
            <input matInput name="comment" formControlName="comment">
        </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions>
        <button mat-button type="button" (click)="onCancel()">Cancel</button>
        <button mat-flat-button type="submit" color="warn">Remove</button>
    </mat-dialog-actions>
</form>
```

- [ ] **Step 5: `property-history-dialog.ts`**

```typescript
import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {PropertyHistoryItemResponseDto} from '../../models/PropertyHistoryItemResponseDto';

export interface PropertyHistoryDialogData {
    propertyName: string;
    historyItems: PropertyHistoryItemResponseDto[];
}

@Component({
    selector: 'maia-property-history-dialog',
    templateUrl: './property-history-dialog.html',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule]
})
export class PropertyHistoryDialog {

    constructor(
        public dialogRef: MatDialogRef<PropertyHistoryDialog>,
        @Inject(MAT_DIALOG_DATA) public data: PropertyHistoryDialogData
    ) {}

    onClose(): void {
        this.dialogRef.close();
    }

}
```

- [ ] **Step 6: `property-history-dialog.html`**

```html
<h2 mat-dialog-title>History for {{data.propertyName}}</h2>
<mat-dialog-content>
    <table data-testid="property-history-table">
        <thead>
        <tr>
            <th>Change</th>
            <th>Value</th>
            <th>By</th>
            <th>When</th>
            <th>Comment</th>
        </tr>
        </thead>
        <tbody>
        @for (item of data.historyItems; track item.version) {
            <tr [attr.data-testid]="'property-history-row-' + item.version">
                <td>{{item.changeType}}</td>
                <td>{{item.propertyValue}}</td>
                <td>{{item.lastModifiedByUsername}}</td>
                <td>{{item.lastModifiedTimestamp}}</td>
                <td>{{item.comment}}</td>
            </tr>
        } @empty {
            <tr>
                <td colspan="5">No history yet.</td>
            </tr>
        }
        </tbody>
    </table>
</mat-dialog-content>
<mat-dialog-actions>
    <button mat-button (click)="onClose()">Close</button>
</mat-dialog-actions>
```

- [ ] **Step 7: Build**

Run: `cd libs/maia-ui-workspace && npx ng build maia-props`
Expected: build succeeds

- [ ] **Step 8: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/dialogs
git commit -m "Add edit/remove/history dialogs for maia-props"
```

---

## Task 10: Dashboard page and `public-api.ts`

**Goal:** The `PropsDashboardPage` component wiring the store, table, filters, and the three dialogs together; finalize `public-api.ts`.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html`
- Create: `libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.scss`
- Modify: `libs/maia-ui-workspace/projects/maia-props/src/public-api.ts`

**Acceptance Criteria:**
- [ ] `<maia-props-dashboard-page>` fetches on init, renders a filterable/toggleable table, and supports add/edit/remove/history actions
- [ ] Every actionable element has a stable Playwright hook: `data-testid="property-row-<name>"` on rows, `aria-label="Edit <name>"` / `"Remove <name>"` / `"History <name>"` on the row action buttons, `aria-label="Add override"` on the add button

**Verify:** `cd libs/maia-ui-workspace && npx ng build maia-props` → build succeeds

**Steps:**

- [ ] **Step 1: `props-dashboard-page.ts`**

```typescript
import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatTableModule} from '@angular/material/table';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSlideToggleModule, MatSlideToggleChange} from '@angular/material/slide-toggle';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {PropsApiService} from './services/props-api.service';
import {PropsDashboardStore} from './state/props-dashboard-store';
import {PropertyResponseDto} from './models/PropertyResponseDto';
import {EditPropertyDialog, EditPropertyDialogData, EditPropertyDialogResult} from './dialogs/edit-property-dialog/edit-property-dialog';
import {RemoveOverrideDialog, RemoveOverrideDialogData, RemoveOverrideDialogResult} from './dialogs/remove-override-dialog/remove-override-dialog';
import {PropertyHistoryDialog, PropertyHistoryDialogData} from './dialogs/property-history-dialog/property-history-dialog';


@Component({
    selector: 'maia-props-dashboard-page',
    templateUrl: './props-dashboard-page.html',
    styleUrl: './props-dashboard-page.scss',
    imports: [MatTableModule, MatFormFieldModule, MatInputModule, MatSlideToggleModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
    providers: [PropsApiService, PropsDashboardStore]
})
export class PropsDashboardPage implements OnInit {


    readonly store = inject(PropsDashboardStore);

    readonly displayedColumns = ['propertyName', 'effectiveValue', 'isOverridden', 'sourceName', 'lastModifiedByUsername', 'lastModifiedTimestamp', 'actions'];


    constructor(
        private propsService: PropsApiService,
        private dialog: MatDialog
    ) {}


    ngOnInit() {
        this.store.fetchAllProperties();
    }


    onNameFilterInput(event: Event) {
        this.store.onNameFilterChanged((event.target as HTMLInputElement).value);
    }


    onOverriddenOnlyToggled(change: MatSlideToggleChange) {
        this.store.onOverriddenOnlyToggled(change.checked);
    }


    onAddOverride() {

        const data: EditPropertyDialogData = {propertyName: null, currentValue: null};
        const dialogRef = this.dialog.open(EditPropertyDialog, {width: '480px', data});

        dialogRef.afterClosed().subscribe((result: EditPropertyDialogResult | undefined) => {
            if (result) {
                this.propsService.setProperty(result.propertyName, result.propertyValue, result.comment).subscribe(updated => {
                    this.store.applyPropertyUpdate(updated);
                });
            }
        });

    }


    onEdit(row: PropertyResponseDto) {

        const data: EditPropertyDialogData = {propertyName: row.propertyName, currentValue: row.effectiveValue};
        const dialogRef = this.dialog.open(EditPropertyDialog, {width: '480px', data});

        dialogRef.afterClosed().subscribe((result: EditPropertyDialogResult | undefined) => {
            if (result) {
                this.propsService.setProperty(result.propertyName, result.propertyValue, result.comment).subscribe(updated => {
                    this.store.applyPropertyUpdate(updated);
                });
            }
        });

    }


    onRemove(row: PropertyResponseDto) {

        const data: RemoveOverrideDialogData = {propertyName: row.propertyName};
        const dialogRef = this.dialog.open(RemoveOverrideDialog, {width: '480px', data});

        dialogRef.afterClosed().subscribe((result: RemoveOverrideDialogResult | undefined) => {
            if (result) {
                this.propsService.removeProperty(row.propertyName, result.comment).subscribe(() => {
                    this.store.retryFetch();
                });
            }
        });

    }


    onHistory(row: PropertyResponseDto) {

        this.propsService.getPropertyHistory(row.propertyName).subscribe(historyItems => {
            const data: PropertyHistoryDialogData = {propertyName: row.propertyName, historyItems};
            this.dialog.open(PropertyHistoryDialog, {width: '600px', data});
        });

    }


}
```

- [ ] **Step 2: `props-dashboard-page.html`**

```html
<div class="props-dashboard-toolbar">
    <mat-form-field appearance="fill">
        <mat-label>Filter by name</mat-label>
        <input matInput (input)="onNameFilterInput($event)">
    </mat-form-field>
    <mat-slide-toggle (change)="onOverriddenOnlyToggled($event)">Overridden only</mat-slide-toggle>
    <button mat-flat-button aria-label="Add override" (click)="onAddOverride()">Add override</button>
</div>

@if (store.isLoading()) {
    <mat-progress-spinner mode="indeterminate" diameter="32" />
} @else if (store.error()) {
    <p>{{store.error()}} <button mat-button (click)="store.retryFetch()">Retry</button></p>
} @else {
    <table mat-table [dataSource]="store.visibleProperties()" data-testid="props-dashboard-table">

        <ng-container matColumnDef="propertyName">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let row">{{row.propertyName}}</td>
        </ng-container>

        <ng-container matColumnDef="effectiveValue">
            <th mat-header-cell *matHeaderCellDef>Effective Value</th>
            <td mat-cell *matCellDef="let row" class="property-value-cell">{{row.effectiveValue}}</td>
        </ng-container>

        <ng-container matColumnDef="isOverridden">
            <th mat-header-cell *matHeaderCellDef>Overridden</th>
            <td mat-cell *matCellDef="let row" class="property-overridden-badge">{{row.isOverridden ? 'Overridden' : 'Default'}}</td>
        </ng-container>

        <ng-container matColumnDef="sourceName">
            <th mat-header-cell *matHeaderCellDef>Source</th>
            <td mat-cell *matCellDef="let row">{{row.sourceName}}</td>
        </ng-container>

        <ng-container matColumnDef="lastModifiedByUsername">
            <th mat-header-cell *matHeaderCellDef>Last Modified By</th>
            <td mat-cell *matCellDef="let row">{{row.lastModifiedByUsername}}</td>
        </ng-container>

        <ng-container matColumnDef="lastModifiedTimestamp">
            <th mat-header-cell *matHeaderCellDef>Last Modified</th>
            <td mat-cell *matCellDef="let row">{{row.lastModifiedTimestamp}}</td>
        </ng-container>

        <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let row">
                <button mat-icon-button [attr.aria-label]="'Edit ' + row.propertyName" (click)="onEdit(row)">
                    <mat-icon>edit</mat-icon>
                </button>
                <button mat-icon-button [attr.aria-label]="'Remove ' + row.propertyName" [disabled]="!row.isOverridden" (click)="onRemove(row)">
                    <mat-icon>delete</mat-icon>
                </button>
                <button mat-icon-button [attr.aria-label]="'History ' + row.propertyName" (click)="onHistory(row)">
                    <mat-icon>history</mat-icon>
                </button>
            </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;" [attr.data-testid]="'property-row-' + row.propertyName"></tr>

    </table>
}
```

- [ ] **Step 3: `props-dashboard-page.scss`**

```scss
.props-dashboard-toolbar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
}

.property-value-cell {
    max-width: 320px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
```

- [ ] **Step 4: Finalize `public-api.ts`**

```typescript
export * from './lib/props-dashboard/models/PropertyResponseDto';
export * from './lib/props-dashboard/models/PropertyHistoryItemResponseDto';
export * from './lib/props-dashboard/services/props-api-base-url.token';
export * from './lib/props-dashboard/services/props-api.service';
export * from './lib/props-dashboard/dialogs/edit-property-dialog/edit-property-dialog';
export * from './lib/props-dashboard/dialogs/remove-override-dialog/remove-override-dialog';
export * from './lib/props-dashboard/dialogs/property-history-dialog/property-history-dialog';
export * from './lib/props-dashboard/props-dashboard-page';
```

- [ ] **Step 5: Build**

Run: `cd libs/maia-ui-workspace && npx ng build maia-props`
Expected: build succeeds

- [ ] **Step 6: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.ts libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.html libs/maia-ui-workspace/projects/maia-props/src/lib/props-dashboard/props-dashboard-page.scss libs/maia-ui-workspace/projects/maia-props/src/public-api.ts
git commit -m "Add PropsDashboardPage and finalize maia-props public API"
```

---

## Task 11: Showcase wiring

**Goal:** Route + page wrapper in `maia-showcase-ui`, with `dataPageId` set (required for the Playwright `assertOnPage()` check in Task 12 — `job-history-page.ts` and `elastic-indices-page.ts` both currently omit this, which is why neither has ever had a Playwright test written against it; don't repeat that gap here).

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/props-dashboard/props-dashboard-page.ts`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`
- Modify: `maia-showcase/maia-showcase-ui/tsconfig.json`

**Acceptance Criteria:**
- [ ] Navigating to `/props-dashboard` in the showcase app renders the dashboard
- [ ] `data-page-id="props_dashboard"` is present on the page

**Verify:** `cd maia-showcase/maia-showcase-ui && npx ng build` → build succeeds

**Steps:**

- [ ] **Step 1: Create the wrapper page**

```typescript
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@maia/maia-ui';
import {PropsDashboardPage as MaiaPropsDashboardPageComponent} from '@maia/maia-props';

@Component({
    imports: [PageLayout, MaiaPropsDashboardPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <maia-page-layout pageTitle="Property Overrides" dataPageId="props_dashboard">
            <maia-props-dashboard-page />
        </maia-page-layout>
    `
})
export class PropsDashboardPage {}
```

- [ ] **Step 2: Add the route**

In `app.routes.ts`, find the closing of the routes array (the same place `jobs-dashboard`/`jobs-history` routes were added — read the file first to find the exact end-of-array location) and add:

```typescript
    {
        path: 'props-dashboard',
        loadComponent: () =>
            import('./pages/props-dashboard/props-dashboard-page').then(
                (m) => m.PropsDashboardPage,
            ),
    },
```

- [ ] **Step 3: Add the tsconfig path mapping**

In `maia-showcase/maia-showcase-ui/tsconfig.json`, find:

```json
        "@maia/maia-elasticsearch": [
            "../../libs/maia-ui-workspace/dist/maia-elasticsearch"
        ],
```

Add immediately after (matching the exact bracket/comma style already there):

```json
        "@maia/maia-props": [
            "../../libs/maia-ui-workspace/dist/maia-props"
        ],
```

- [ ] **Step 4: Build the workspace lib, then the showcase UI**

Run: `cd libs/maia-ui-workspace && npx ng build maia-props && cd ../../maia-showcase/maia-showcase-ui && npx ng build`
Expected: both build successfully

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/pages/props-dashboard maia-showcase/maia-showcase-ui/src/app/app.routes.ts maia-showcase/maia-showcase-ui/tsconfig.json
git commit -m "Wire props-dashboard page and route into maia-showcase-ui"
```

---

## Task 12: Playwright CRUD journey test

**Goal:** Black-box end-to-end verification of the whole feature — add, edit, view history, remove — including the env/override merge precedence, using a real `application.yml`-backed property (`maia.problems.type_prefix`, currently `"showcase_"`) so revert-on-remove is meaningfully checked.

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/PropsDashboardPage.kt`
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/EditPropertyDialogPage.kt`
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RemoveOverrideDialogPage.kt`
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/PropertyHistoryDialogPage.kt`
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/props/PropsCrudPlaywrightTest.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`

**Acceptance Criteria:**
- [ ] Test logs in as an admin user granted `MAIA_PROPS_READ`/`MAIA_PROPS_WRITE` (in addition to the standard `Authority.WRITE` the shared `initAdminUserFixture()` grants — this test builds its own fixture rather than modifying the shared helper, to avoid touching every other Playwright test)
- [ ] Test adds an override on `maia.problems.type_prefix`, asserts it shows as overridden with the new value
- [ ] Test edits the override to a different value, asserts updated
- [ ] Test opens history and asserts 2 entries (CREATE, UPDATE) are visible
- [ ] Test removes the override, asserts the row reverts to showing `showcase_` and "Default" (not overridden)

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.props.PropsCrudPlaywrightTest"` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Read `RightManyCrudPlaywrightTest.kt` and its page objects first**

Read `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt` end to end to confirm current exact class shape, imports, and `@BeforeAll`/`@Test` annotations in use, since `AbstractPlaywrightTest`/`AbstractBlackBoxTest` may have shifted slightly since this plan was written. Match that shape. Confirmed as of this plan: no `@TestInstance` annotation needed (inherited from the base class), plain `@BeforeAll fun setUp()` and `` @Test fun `crud journey`() `` — the sketch in Step 7 already matches this.

Also read `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/Authority.kt`. This file is generated by `AuthorityEnumRenderer`, aggregating every `authority(...)` declared across every spec the showcase app pulls in (already contains `MAIA_JOB_READ`, `MAIA_JOB_WRITE`, `MAIA_ELASTICSEARCH_SYS_OPS_READ/WRITE`, `READ`, `WRITE` as of this plan). After Task 1 and a full build (Task 5), it will also contain `MAIA_PROPS_READ` and `MAIA_PROPS_WRITE` — confirm they're present before writing Step 7; if the full `./gradlew build -x test` from Task 5 already ran, they will be. `AbstractPlaywrightTest.kt`'s own `initAdminUserFixture()` uses this generated enum unqualified (`Authority.WRITE.name`) because it lives in the same package (`org.maiaframework.showcase`) as the generated file — `PropsCrudPlaywrightTest.kt` lives in a subpackage, so it needs an explicit import, shown in Step 7.

- [ ] **Step 2: Create `PropsDashboardPage.kt`**

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper

class PropsDashboardPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/props-dashboard",
    "props_dashboard"
) {


    fun filterByName(nameFilter: String) {
        page.locator("[data-testid='props-dashboard-table']").waitFor()
        page.getByRole(AriaRole.TEXTBOX).first().fill(nameFilter)
    }


    fun clickAddOverride() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add override")).click()
    }


    fun clickEditFor(propertyName: String) {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Edit $propertyName")).click()
    }


    fun clickRemoveFor(propertyName: String) {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Remove $propertyName")).click()
    }


    fun clickHistoryFor(propertyName: String) {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("History $propertyName")).click()
    }


    fun assertRowShowsValue(propertyName: String, expectedValue: String) {
        val row = page.locator("[data-testid='property-row-$propertyName']")
        row.waitFor()
        page.waitForFunction(
            "(propertyName) => { const row = document.querySelector(`[data-testid='property-row-\${propertyName}']`); " +
            "return row && row.textContent && row.textContent.includes(arguments[1]); }",
            propertyName
        )
    }


    fun assertRowIsOverridden(propertyName: String, overridden: Boolean) {
        val row = page.locator("[data-testid='property-row-$propertyName']")
        row.waitFor()
        val expectedText = if (overridden) "Overridden" else "Default"
        page.waitForFunction(
            "([propertyName, expectedText]) => { const row = document.querySelector(`[data-testid='property-row-\${propertyName}']`); " +
            "return row && row.textContent && row.textContent.includes(expectedText); }",
            listOf(propertyName, expectedText)
        )
    }


}
```

- [ ] **Step 3: Create `EditPropertyDialogPage.kt`**

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

class EditPropertyDialogPage(private val page: Page) {


    fun fillForm(propertyName: String? = null, propertyValue: String, comment: String? = null) {

        if (propertyName != null) {
            val nameInput = page.locator("input[name='propertyName']")
            nameInput.clear()
            nameInput.fill(propertyName)
        }

        val valueInput = page.locator("input[name='propertyValue']")
        valueInput.clear()
        valueInput.fill(propertyValue)

        if (comment != null) {
            val commentInput = page.locator("input[name='comment']")
            commentInput.clear()
            commentInput.fill(comment)
        }

    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit")).click()
    }


}
```

- [ ] **Step 4: Create `RemoveOverrideDialogPage.kt`**

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

class RemoveOverrideDialogPage(private val page: Page) {


    fun clickRemoveButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Remove").setExact(true)).click()
    }


}
```

- [ ] **Step 5: Create `PropertyHistoryDialogPage.kt`**

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole

class PropertyHistoryDialogPage(private val page: Page) {


    fun assertHistoryRowCount(expectedCount: Int) {
        assertThat(page.locator("[data-testid^='property-history-row-']")).hasCount(expectedCount)
    }


    fun assertHistoryContainsChangeType(changeType: String) {
        assertThat(page.locator("[data-testid='property-history-table']")).containsText(changeType)
    }


    fun clickClose() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Close")).click()
    }


}
```

- [ ] **Step 6: Register the new page objects in `AbstractPlaywrightTest.kt`**

Add these `protected lateinit var` declarations alongside the existing ones (e.g. near `usersViewPage`):

```kotlin
    protected lateinit var propsDashboardPage: PropsDashboardPage


    protected lateinit var editPropertyDialogPage: EditPropertyDialogPage


    protected lateinit var removeOverrideDialogPage: RemoveOverrideDialogPage


    protected lateinit var propertyHistoryDialogPage: PropertyHistoryDialogPage
```

Add the corresponding imports next to the other `org.maiaframework.showcase.testing.pages.*` imports:

```kotlin
import org.maiaframework.showcase.testing.pages.PropsDashboardPage
import org.maiaframework.showcase.testing.pages.EditPropertyDialogPage
import org.maiaframework.showcase.testing.pages.RemoveOverrideDialogPage
import org.maiaframework.showcase.testing.pages.PropertyHistoryDialogPage
```

Add the corresponding instantiations inside `initPlaywrightPage()`, alongside the existing ones:

```kotlin
        propsDashboardPage = PropsDashboardPage(page, urlHelper)
        editPropertyDialogPage = EditPropertyDialogPage(page)
        removeOverrideDialogPage = RemoveOverrideDialogPage(page)
        propertyHistoryDialogPage = PropertyHistoryDialogPage(page)
```

- [ ] **Step 7: Create `PropsCrudPlaywrightTest.kt`**

Match the exact base class / annotation style confirmed in Step 1. This sketch assumes the same shape as `RightManyCrudPlaywrightTest` (JUnit 5, `AbstractPlaywrightTest`, `@BeforeAll`/`@Test`) — adjust to match what Step 1 actually found:

```kotlin
package org.maiaframework.showcase.props

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.maiaframework.props.PropsManager
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.Authority
import org.springframework.beans.factory.annotation.Autowired

private const val TEST_PROPERTY_NAME = "maia.problems.type_prefix"
private const val ORIGINAL_ENVIRONMENT_VALUE = "showcase_"

class PropsCrudPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var propsManager: PropsManager


    @BeforeAll
    fun setUpPropsFixtures() {

        fixtures.resetDatabaseState()

        // Uses the generated org.maiaframework.showcase.Authority enum (aggregated from every
        // spec's authority() declarations, including PropsSpec's MAIA_PROPS_READ/WRITE from Task 1)
        // — not a raw string, matching how AbstractPlaywrightTest.initAdminUserFixture() does it.
        adminUser = fixtures.aUser(
            loginMailVerified = true,
            { b -> b.copy(authorities = listOf(
                org.maiaframework.domain.auth.Authority(Authority.WRITE.name),
                org.maiaframework.domain.auth.Authority(Authority.MAIA_PROPS_READ.name),
                org.maiaframework.domain.auth.Authority(Authority.MAIA_PROPS_WRITE.name),
            )) }
        )

        try {
            propsManager.removeProperty(TEST_PROPERTY_NAME, "test-setup", null)
        } catch (_: Exception) {
            // no pre-existing override — fine
        }

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(propsDashboardPage)

        // 1. Add an override on a property that already has a real environment value
        propsDashboardPage.filterByName(TEST_PROPERTY_NAME)
        propsDashboardPage.clickEditFor(TEST_PROPERTY_NAME)
        editPropertyDialogPage.fillForm(propertyValue = "test_override_", comment = "playwright add")
        editPropertyDialogPage.clickSubmitButton()
        propsDashboardPage.assertRowIsOverridden(TEST_PROPERTY_NAME, true)
        propsDashboardPage.assertRowShowsValue(TEST_PROPERTY_NAME, "test_override_")

        // 2. Edit the override
        propsDashboardPage.clickEditFor(TEST_PROPERTY_NAME)
        editPropertyDialogPage.fillForm(propertyValue = "test_override_edited_", comment = "playwright edit")
        editPropertyDialogPage.clickSubmitButton()
        propsDashboardPage.assertRowShowsValue(TEST_PROPERTY_NAME, "test_override_edited_")

        // 3. History shows both changes
        propsDashboardPage.clickHistoryFor(TEST_PROPERTY_NAME)
        propertyHistoryDialogPage.assertHistoryContainsChangeType("CREATE")
        propertyHistoryDialogPage.assertHistoryContainsChangeType("UPDATE")
        propertyHistoryDialogPage.clickClose()

        // 4. Remove reverts to the environment value
        propsDashboardPage.clickRemoveFor(TEST_PROPERTY_NAME)
        removeOverrideDialogPage.clickRemoveButton()
        propsDashboardPage.assertRowIsOverridden(TEST_PROPERTY_NAME, false)
        propsDashboardPage.assertRowShowsValue(TEST_PROPERTY_NAME, ORIGINAL_ENVIRONMENT_VALUE)

    }


}
```

Note: the `UserEntityTestBuilder` import above may be unnecessary depending on what Step 1's read reveals about the exact `aUser(...)` fixture-configurer lambda signature (`(UserEntityTestBuilder) -> UserEntityTestBuilder`, matching `initAdminUserFixture()` in `AbstractPlaywrightTest.kt`) — remove the import if the compiler flags it unused.

- [ ] **Step 8: Run the test**

Run: `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.props.PropsCrudPlaywrightTest"`
Expected: BUILD SUCCESSFUL. This requires the local Postgres/pgAdmin stack running (`docker compose -f maia-showcase/compose.yaml up -d`) and the showcase UI built (`cd libs/maia-ui-workspace && npx ng build maia-props && cd ../../maia-showcase/maia-showcase-ui && npx ng build`) beforehand, matching how other Playwright tests in this suite are run.

If it fails, debug via `superpowers-extended-cc:systematic-debugging` rather than guessing — likely failure points, in order of likelihood: (a) selector mismatches between the Kotlin page objects and the actual rendered Angular DOM (Material component internals sometimes wrap `aria-label` differently than expected — inspect with the Playwright trace at `playwright-trace.zip`), (b) the generated `PropertyResponseDto` constructor parameter order from Task 1/3 not matching the hand-written `PropsManager` code, (c) `MaiaPropsEndpoint` not being picked up by component scan (re-check Task 5).

- [ ] **Step 9: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/PropsDashboardPage.kt maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/EditPropertyDialogPage.kt maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RemoveOverrideDialogPage.kt maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/PropertyHistoryDialogPage.kt maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/props/PropsCrudPlaywrightTest.kt maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt
git commit -m "Add black-box Playwright CRUD journey test for props dashboard"
```

---

## Post-plan check

After Task 12 passes, run the full backend + frontend build once more to catch anything the per-task verifies missed:

```bash
./gradlew build -x test
cd libs/maia-ui-workspace && npx ng build maia-props && npx ng test maia-props --watch=false
cd ../../maia-showcase/maia-showcase-ui && npx ng build
```
