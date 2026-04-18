# maia-elasticsearch Spring Boot Starter — Design

## Goal

Introduce a proper Spring Boot starter for the maia-elasticsearch library, following the two-module pattern used by maia-props and maia-toggles. Move the existing `libs/maia-elasticsearch` module under a new `maia-elasticsearch-parent` directory alongside the two new modules.

## Module Structure

```
libs/maia-elasticsearch-parent/
  maia-elasticsearch/               ← moved from libs/maia-elasticsearch (no internal changes)
  maia-elasticsearch-autoconfigure/ ← new
  maia-elasticsearch-spring-boot-starter/ ← new
```

## maia-elasticsearch-autoconfigure

**`build.gradle.kts`**

- `api(project(":libs:maia-elasticsearch-parent:maia-elasticsearch"))`
- `implementation("org.springframework.boot:spring-boot-autoconfigure")`
- `implementation("org.springframework.boot:spring-boot-starter")`
- `annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor:3.5.6")`

**`MaiaElasticsearchAutoConfiguration`**

- `@AutoConfiguration(after = [ElasticsearchClientAutoConfiguration::class])`
- `@ConditionalOnClass(ElasticsearchClient::class)`
- Defines all 8 beans with `@ConditionalOnMissingBean`:
  - `EsIndexNameFactory`
  - `EsIndexActiveVersionManager(props, propsManager)`
  - `EsIndexNameOverrider(props, esIndexNameFactory)`
  - `EsIndexControlRegistry`
  - `ElasticIndexHelper(client)`
  - `ElasticIndexService(client, controlRegistry, esIndexNameFactory, esIndexActiveVersionManager)`
  - `EsSearchRequestFactory`
  - `EsSearchExecutor(esSearchRequestFactory, client)`

**`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`**

```
org.maiaframework.elasticsearch.MaiaElasticsearchAutoConfiguration
```

## maia-elasticsearch-spring-boot-starter

**`build.gradle.kts`**

- `api(project(":libs:maia-elasticsearch-parent:maia-elasticsearch"))`
- `api(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-autoconfigure"))`
- `implementation("org.springframework.boot:spring-boot-starter")`

No Kotlin source files.

## Changes to maia-elasticsearch

Remove `@Component` from all 8 bean classes (they become plain classes instantiated by the autoconfiguration):

- `ElasticIndexService`
- `EsIndexControlRegistry`
- `EsIndexNameFactory`
- `EsIndexActiveVersionManager`
- `ElasticIndexHelper`
- `EsSearchExecutor`
- `EsSearchRequestFactory`
- `EsIndexNameOverrider`

## settings.gradle.kts

Replace:
```
include("libs:maia-elasticsearch")
```
With:
```
include("libs:maia-elasticsearch-parent:maia-elasticsearch")
include("libs:maia-elasticsearch-parent:maia-elasticsearch-autoconfigure")
include("libs:maia-elasticsearch-parent:maia-elasticsearch-spring-boot-starter")
```

## Showcase Updates

**`maia-showcase/elasticsearch/build.gradle.kts`**
- `api(project(":libs:maia-elasticsearch"))` → `api(project(":libs:maia-elasticsearch-parent:maia-elasticsearch"))`

**`maia-showcase/service/build.gradle.kts`**
- `implementation(project(":libs:maia-elasticsearch"))` → `implementation(project(":libs:maia-elasticsearch-parent:maia-elasticsearch"))`

**`maia-showcase/app/build.gradle.kts`**
- Add `implementation(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-spring-boot-starter"))`

**`maia-showcase/app/src/main/kotlin/.../MaiaShowcaseAppConfiguration.kt`**
- Remove `"org.maiaframework.elasticsearch"` from `@ComponentScan`
