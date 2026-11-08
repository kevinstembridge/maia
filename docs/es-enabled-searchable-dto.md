# Elasticsearch-enabled searchable DTOs

A `searchableDto` can be marked Elasticsearch-enabled. The generator then derives an EsDoc for it, generates the index plumbing and any blotter over it uses Elasticsearch as its data source. The index is kept fresh automatically: when a field of **any entity that contributes to the document** changes, the affected documents are rebuilt after the transaction commits.

## Usage

```kotlin
val bravoEsSearchableDtoDef = searchableDto(
    "org.maiaframework.showcase.join",
    "BravoEs",
    entityDef = bravoEntityDef,
    withGeneratedDto = WithGeneratedDto.TRUE,
) {
    elasticsearchEnabled(version = 1, autoRefresh = true)
    field("dtoStringFromAlpha", "alpha.someString")   // field of a referenced entity (FK path)
    field("dtoStringFromBravo", "someString")
    field("dtoIntFromBravo", "someInt")
    field("createdTimestamp")
}

val bravoEsBlotterDef = blotter(bravoEsSearchableDtoDef) {
    columnFromDto("id")          // required: ES-sourced blotters do not add the id column automatically
    columnFromDto("dtoStringFromAlpha")
    ...
}
```

* `elasticsearchEnabled(version, autoRefresh)` may be called once per version. Several versions can be live at the same time; every live version is refreshed. A blotter over the DTO uses the **highest** version.
* `autoRefresh = false` turns off the generated listeners for that version (the index is then refreshed only by the scheduled job or by code you write).
* `esMapping(EsDocMappingTypes.x)` inside a `field { }` overrides the default Elasticsearch mapping of a field.
* Many-to-many fields (`manyToManyField`) are supported and are indexed as `object` lists.

### Default mappings
Derived from `FieldType.elasticMappingType`, with two deliberate deviations: strings use `textAndKeyword` (text plus a `raw` keyword sub-field) and `Instant` uses `date`. The generated blotter field-name mapper filters (`term` query) and sorts a `textAndKeyword` column on `<field>.raw`, so filters match the exact value and sorting works; this applies to hand-built EsDocs with `textAndKeyword` fields as well. A field type with no default mapping (for example a collection) must declare `esMapping(...)`.

## What is generated (per ES-enabled DTO and version)

| Layer | Class |
|---|---|
| domain | EsDoc DTO `<Name>V<n>EsDoc` |
| dao / repo | `findByIds`, `findAllAsSequence`, `findRootIdsVia<Entity>` on the DTO DAO and repo |
| elasticsearch | `EsIndexMeta_v000n`, `EsIndexControl_v000n` |
| elastic-service | `<EsDoc>Repo`, `<Name>V<n>IndexService`, and one `…EsRefresh<Entity>CrudListener` per contributing entity |
| jobs | `Refresh<Name>V<n>IndexJob` (scheduled full reconcile) |
| blotter | ES search service, row mapper, field-name mappers, endpoint, TS (when a blotter is declared) |

Indices are created on demand, not at startup: the index service checks that the index exists before it writes (on every refresh and in `refreshIndex`) and creates it from the generated type mapping through the `EsIndexControl` if it does not, so Elasticsearch never auto-creates it with a dynamic mapping. An admin creating it first is still fine.

## How refresh works

1. A generated `CrudService` write fires the entity's `CrudListener` (`onXUpdated(before, after)`, `onXBeforeDelete`, `onXDeleted`, …).
2. The generated EsDoc listener compares only the fields the document actually reads (including join keys) and, when relevant, enlists the affected **root ids** with `EsRefreshCollector`.
3. Root ids of a changed referenced entity are found with a generated reverse-join query (`findRootIdsVia<Entity>`), aliased per hop, with discriminator predicates for hierarchy entities. For an update the query runs after commit (`enlistRefreshResolved`), so a slow or failing lookup cannot affect the caller's transaction. Lookups that must see the pre-change state (before a delete, and the old members of a many-to-many) run inside the transaction through `enlistRefreshNow`: a failure is logged and the refresh is left to the scheduled job, although a failed SQL statement in PostgreSQL still aborts the transaction.
4. `EsRefreshCollector` dedupes ids per index inside the transaction and, **after commit**, rebuilds the documents in chunks (`maia.elasticsearch.refresh.chunkSize`, default 1000) with one bulk upsert per chunk. Ids whose row no longer exists, or no longer matches the DTO's inner joins, have their document deleted.
5. A failure after commit is logged at error level and does not fail the caller (the database change stays committed); the scheduled `refreshIndex` job reconciles.

| Change | Result |
|---|---|
| Root created / deleted | its document is created / removed |
| Root updated | refreshed if a field the document reads changed; always for a DTO with a many-to-many field |
| Referenced entity updated | all roots that reach it are refreshed if a field the document reads changed (one bulk call for many roots) |
| Referenced entity deleted | roots are captured before the delete and re-evaluated after commit |
| Many-to-many other side updated | roots resolved twice: before the change (old members) and after commit (new members) |
| Transaction rolled back | nothing is written to Elasticsearch |

## Rules checked at generation time

* The root entity needs a single `DomainId` primary key named `id`.
* Every entity on a path and every many-to-many other-side entity needs `crud { }` (so a `CrudListener` exists) and a single `DomainId` primary key. Many-to-many join entities are exempt.
* `crud { withCrudListener(false) }` leaves out an entity's CrudListener and CrudNotifier (they are generated by default). An entity on an ES-enabled DTO's path, and an entity with a typeahead, needs the listener: generation fails with a message naming the entity if it is turned off.
* Two EsDocs with the same index base name and version are rejected.
* A hand-built `esDoc { }` **must declare `autoRefresh(false)`** (you refresh it yourself); `autoRefresh(true)` is rejected because only ES-enabled searchable DTOs have a join model to refresh from.

## Known limitations

* Changes that raise no `CrudListener` event are only reconciled by the scheduled `refreshIndex` job: importers, direct DAO writes, join rows edited outside a CrudService (many-to-many join rows are written straight to the join repo), and effective-dated membership that changes by the clock passing.
* `refreshIndex` streams `findAllAsSequence` inside a read-only transaction. No JDBC fetch size is set, so PostgreSQL may buffer a large result set in memory. A DTO with many-to-many fields runs one extra query per row.
* A document reached through a foreign key from the root back to itself is not supported.
* The many-to-many "resolve before the change" step is only proven necessary for hard-deleted join rows; the integration test uses an effective-dated join entity, where rows are closed rather than deleted.
* Two refreshes of the same document racing (transaction A reads the database, B commits and refreshes, then A writes) can briefly leave an older state in the index; the scheduled job corrects it.
* The before-delete lookup of a row with a very large number of referencing roots loads all their ids inside the transaction.
* Pre-existing ES blotter behaviour (not changed here): multi-column sort keeps only the last column; columns mapped `text`, `searchAsYouType` or `object` can be sorted in the grid but Elasticsearch rejects the sort.

## Contract change

`CrudListener.on<E>Updated` now receives `(before: <E>Entity, after: <E>Entity)` instead of the primary key, and there is a new default no-op `on<E>BeforeDelete(entity)`. The event now fires only when the update changed a row (`count > 0`); an update that matched no row (for example a stale `version`) raises no event. The generated `CrudService.setFields` loads the entity before and after the update for every entity that has listeners. Hand-written listener implementations must be updated.

## Operating notes

* `maia.elasticsearch.refresh.chunkSize` (default 1000) controls the after-commit refresh chunking.
* `<indexServiceName>.bulkUpsert.chunkSize` and `<indexServiceName>.bulkDelete.chunkSize` (service class name with a lower-case first letter, e.g. `bravoEsV1IndexService.bulkUpsert.chunkSize`) control the scheduled job.
* Run the `Refresh<Name>V<n>IndexJob` regularly; it is the safety net for everything listed under limitations.

## Tests

`maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/es/EsDocAutoRefreshIntegrationTest.kt` exercises the behaviour against real PostgreSQL and Elasticsearch (Testcontainers). Renderer and spec behaviour is covered by tests in `maia-gen-spec` and `maia-gen-generator`; the collector by `EsRefreshCollectorTest`.
