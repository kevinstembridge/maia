# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What Is Maia

Maia is a full-stack code generation framework for building Kotlin/Spring Boot/Angular/PostgreSQL applications. The central idea: a developer writes a **spec** (a Kotlin class extending `AbstractSpec`) describing a domain model, and Maia generates a complete multi-layer application—domain entities, DAO, repositories, services, REST endpoints, and Angular UI components—from that single source of truth.

## Build Commands

```bash
# Build everything
./gradlew build

# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :maia-showcase:app:test

# Run a single test class
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.SomeTest"

# Run code generation for a specific module (outputs to src/generated/)
./gradlew :maia-showcase:web:maiaGeneration

# Delete generated sources for a module
./gradlew :maia-showcase:web:clean

# Build the Angular showcase UI
./gradlew :maia-showcase:maia-showcase-ui:buildAngularApp

# Start local infrastructure (PostgreSQL on 5433, pgAdmin on 8889)
docker compose -f maia-showcase/compose.yaml up -d
```

## Project Structure

```
maia/
├── buildSrc/                  Convention plugins (see below)
├── maia-platform/             BOM — central dependency version management
├── maia-gen/
│   ├── maia-gen-spec/         Spec DSL: AbstractSpec, entity/enum/type builders, FieldTypes
│   ├── maia-gen-library/      Runtime support library for generated code
│   └── maia-gen-generator/    The generator itself — produces Kotlin, SQL, TypeScript, HTML
├── libs/                      Reusable framework libraries (maia-common, maia-jdbc, maia-http, etc.)
│   └── maia-ui-workspace/     Shared Angular library (auth, search models, utilities)
├── maia-showcase/             Reference application demonstrating all generator capabilities
│   ├── spec/                  MaiaShowcaseSpec.kt — the single source of truth
│   ├── domain/                Generated: entities, DTOs, validators, filters
│   ├── dao/                   Generated: DAO layer + SQL Flyway scripts
│   ├── repo/                  Generated: Hazelcast-backed repository implementations
│   ├── service/               Generated: business logic services
│   ├── web/                   Generated: Spring MVC REST endpoints
│   ├── app/                   Spring Boot application entry point
│   └── maia-showcase-ui/      Generated: Angular TypeScript/HTML/SCSS components
└── plugins/
    └── maia-gradle-plugin/    Gradle plugin that wires up maiaGeneration tasks
```

## Convention Plugins (buildSrc)

Every module's `build.gradle.kts` starts with one of these plugins, which configure Kotlin JVM 21, JUnit 5, and dependency conventions:

| Plugin | Use |
|---|---|
| `maia.kotlin-conventions` | Base Kotlin/JVM 21 settings, JUnit platform |
| `maia.kotlin-library-conventions` | Adds `java-library` (for `api`/`implementation` split) |
| `maia.kotlin-spring-conventions` | Adds `kotlin("plugin.spring")` |
| `maia.kotlin-library-spring-conventions` | Library + Spring together |

All version constraints come from `maia-platform` (a Gradle `java-platform` BOM). Modules declare `implementation(platform(project(":maia-platform")))` to inherit versions.

## Code Generation Architecture

### The Layered Module Pattern

Each domain feature follows a strict layering. For example, `libs/maia-toggles-parent/`:

```
spec → domain → dao → repo → service → endpoints (→ app)
```

Each layer module:
1. Declares `val maiagen by configurations.creating` in its `build.gradle.kts`
2. Adds the generator and spec JAR to `maiagen` dependencies
3. Registers a `maiaGeneration` JavaExec task pointing to the appropriate generator main class
4. Adds `src/generated/kotlin/main` and `src/generated/resources/main` to its source sets
5. Makes `KotlinCompile` depend on `maiaGeneration`

### Generator Main Classes

| Layer | Generator |
|---|---|
| Domain (entities, DTOs) | `org.maiaframework.gen.generator.DomainModuleGeneratorKt` |
| DAO + SQL | `org.maiaframework.gen.generator.DaoLayerModuleGeneratorKt` |
| Repository | `org.maiaframework.gen.generator.RepoLayerModuleGeneratorKt` |
| Service | `org.maiaframework.gen.generator.ServiceLayerModuleGeneratorKt` |
| Web endpoints | `org.maiaframework.gen.generator.WebLayerModuleGeneratorKt` |
| Angular UI | `org.maiaframework.gen.generator.AngularUiModuleGeneratorKt` |

### Writing a Spec

Specs extend `AbstractSpec` and use a Kotlin DSL to declare the model:

```kotlin
class MySpec : AbstractSpec(AppKey("myapp")) {
    val statusEnum = enumDef("com.example.enums.Status") {
        withTypescript(withEnumSelectionOptions = true)
        value("ACTIVE") { displayName = "Active" }
    }

    val myEntity = entity("com.example", "MyEntity") {
        field("name", FieldTypes.string) { notNullable() }
        field("status", statusEnum) {}
        crud { apis { create(); update(); delete() } }
    }
}
```

**Never manually edit files under `src/generated/`** — they are overwritten on every generation run. To change generated output, modify either the spec or the generator.

## Tech Stack

- **Backend**: Kotlin, Spring Boot 4.x, Spring MVC, Spring Security, JDBC, Flyway
- **Database**: PostgreSQL (Testcontainers for integration tests)
- **Caching**: Hazelcast
- **Frontend**: Angular (Node 24, downloaded automatically by Gradle node plugin)
- **Testing**: JUnit 5, MockK, SpringMockK, Testcontainers
