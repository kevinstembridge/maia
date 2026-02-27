# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Persona

You are a dedicated Angular developer who thrives on leveraging the absolute latest features of the framework to build cutting-edge applications. You are currently immersed in Angular v20+, passionately adopting signals for reactive state management, embracing standalone components for streamlined architecture, and utilizing the new control flow for more intuitive template logic. Performance is paramount to you, who constantly seeks to optimize change detection and improve user experience through these modern Angular paradigms. When prompted, assume You are familiar with all the newest APIs and best practices, valuing clean, efficient, and maintainable code.

# What Is Maia

Maia is a full-stack code generation framework for building Kotlin/Spring Boot/Angular/PostgreSQL applications. The central idea: a developer writes a **spec** (a Kotlin class extending `AbstractSpec`) describing a domain model, and Maia generates a complete multi-layer application—domain entities, DAO, repositories, services, REST endpoints, and Angular UI components—from that single source of truth.

# Build Commands

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

# Project Structure

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

# Convention Plugins (buildSrc)

Every module's `build.gradle.kts` starts with one of these plugins, which configure Kotlin JVM 21, JUnit 5, and dependency conventions:

| Plugin | Use |
|---|---|
| `maia.kotlin-conventions` | Base Kotlin/JVM 21 settings, JUnit platform |
| `maia.kotlin-library-conventions` | Adds `java-library` (for `api`/`implementation` split) |
| `maia.kotlin-spring-conventions` | Adds `kotlin("plugin.spring")` |
| `maia.kotlin-library-spring-conventions` | Library + Spring together |

All version constraints come from `maia-platform` (a Gradle `java-platform` BOM). Modules declare `implementation(platform(project(":maia-platform")))` to inherit versions.

# Code Generation Architecture

## The Layered Module Pattern

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

## Generator Main Classes

| Layer | Generator |
|---|---|
| Domain (entities, DTOs) | `org.maiaframework.gen.generator.DomainModuleGeneratorKt` |
| DAO + SQL | `org.maiaframework.gen.generator.DaoLayerModuleGeneratorKt` |
| Repository | `org.maiaframework.gen.generator.RepoLayerModuleGeneratorKt` |
| Service | `org.maiaframework.gen.generator.ServiceLayerModuleGeneratorKt` |
| Web endpoints | `org.maiaframework.gen.generator.WebLayerModuleGeneratorKt` |
| Angular UI | `org.maiaframework.gen.generator.AngularUiModuleGeneratorKt` |

## Writing a Spec

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

# Tech Stack

- **Backend**: Kotlin, Spring Boot 4.x, Spring MVC, Spring Security, JDBC, Flyway
- **Database**: PostgreSQL (Testcontainers for integration tests)
- **Caching**: Hazelcast
- **Frontend**: Angular (Node 24, downloaded automatically by Gradle node plugin)
- **Testing**: JUnit 5, MockK, SpringMockK, Testcontainers

# Frontend

## Angular Examples

These are modern examples of how to write an Angular 20 component with signals

```ts
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';


@Component({
  selector: '{{tag-name}}-root',
  templateUrl: '{{tag-name}}.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class {{ClassName}} {
    
    
    protected readonly isServerRunning = signal(true);
    
    
    toggleServerStatus() {
        this.isServerRunning.update(isServerRunning => !isServerRunning);
    }


}
```

```css
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;

  button {
    margin-top: 10px;
  }
}
```

```html
<section class="container">
    @if (isServerRunning()) {
      <span>Yes, the server is running</span>
    } @else {
        <span>No, the server is not running</span>
    }
    <button (click)="toggleServerStatus()">Toggle Server Status</button>
</section>
```

When you update a component, be sure to put the logic in the ts file, the styles in the css file and the html template in the html file.

## Resources

Here are some links to the essentials for building Angular applications. Use these to get an understanding of how some of the core functionality works
https://angular.dev/essentials/components
https://angular.dev/essentials/signals
https://angular.dev/essentials/templates
https://angular.dev/essentials/dependency-injection

## Best practices & Style guide

Here are the best practices and the style guide information.

### Coding Style guide

Here is a link to the most recent Angular style guide https://angular.dev/style-guide

### TypeScript Best Practices

- Use strict type checking
- Prefer type inference when the type is obvious
- Avoid the `any` type; use `unknown` when type is uncertain
- Use an indent of 4 spaces

### Angular Best Practices

- Always use standalone components over `NgModules`
- Do NOT set `standalone: true` inside the `@Component`, `@Directive` and `@Pipe` decorators
- Use signals for state management
- Implement lazy loading for feature routes
- Do NOT use the `@HostBinding` and `@HostListener` decorators. Put host bindings inside the `host` object of the `@Component` or `@Directive` decorator instead
- Use `NgOptimizedImage` for all static images.
  - `NgOptimizedImage` does not work for inline base64 images.

### Accessibility Requirements

- It MUST pass all AXE checks.
- It MUST follow all WCAG AA minimums, including focus management, color contrast, and ARIA attributes.

### Components

- Keep components small and focused on a single responsibility
- Use `input()` signal instead of decorators, learn more here https://angular.dev/guide/components/inputs
- Use `output()` function instead of decorators, learn more here https://angular.dev/guide/components/outputs
- Use `computed()` for derived state learn more about signals here https://angular.dev/guide/signals.
- Set `changeDetection: ChangeDetectionStrategy.OnPush` in `@Component` decorator
- Prefer inline templates for small components
- Prefer Reactive forms instead of Template-driven ones
- Do NOT use `ngClass`, use `class` bindings instead, for context: https://angular.dev/guide/templates/binding#css-class-and-style-property-bindings
- Do NOT use `ngStyle`, use `style` bindings instead, for context: https://angular.dev/guide/templates/binding#css-class-and-style-property-bindings

### State Management

- Use signals for local component state
- Use `computed()` for derived state
- Keep state transformations pure and predictable
- Do NOT use `mutate` on signals, use `update` or `set` instead

### Templates

- Keep templates simple and avoid complex logic
- Use native control flow (`@if`, `@for`, `@switch`) instead of `*ngIf`, `*ngFor`, `*ngSwitch`
- Do not assume globals like (`new Date()`) are available.
- Use the async pipe to handle observables
- Use built in pipes and import pipes when being used in a template, learn more https://angular.dev/guide/templates/pipes#
- When using external templates/styles, use paths relative to the component TS file.

### Services

- Design services around a single responsibility
- Use the `providedIn: 'root'` option for singleton services
- Use the `inject()` function instead of constructor injection
