Status: ready-for-agent

# Hazelcast-backed Blotters

See [ADR-0008](../../docs/adr/0008-hazelcast-backed-blotters-are-client-side-and-rebuilt-from-the-database.md), [ADR-0007](../../docs/adr/0007-es-backed-blotters-denormalize-entity-refs.md) and the glossary terms **Client-side blotter** and **Hazelcast-backed Blotter**.

## Problem Statement

A spec author can back a Blotter with SQL or with Elasticsearch. Both are server-side row models: every filter, sort and page change is a round trip, and Elasticsearch needs an index, a version and a cluster. For a small data set (a few thousand rows of reference data, configuration or a short worklist) that is heavy. The author wants the rich, denormalized row of an Elasticsearch document, kept fresh automatically, but delivered to the browser in one go so that AG Grid can filter and sort instantly on the client. Maia already runs Hazelcast in many applications, so no new infrastructure should be needed.

## Solution

A Searchable DTO can be declared `hazelcastEnabled(autoRefresh = true)`. Maia then keeps a Hazelcast map holding one rich document per row, with the same shape as an Elasticsearch document, including Entity refs for foreign keys (ADR-0007). The map is refreshed after commit whenever any entity the document draws on changes. The Blotter over that Searchable DTO is a Client-side blotter: the generated page calls one fetch-all endpoint and AG Grid filters and sorts the whole set in the browser. The user sees a snapshot as of fetch time, with a refresh button to re-fetch. The map is derived from the database, so it is filled at startup (or lazily, by property) and can be rebuilt by a generated service.

## User Stories

1. As a spec author, I want to declare `hazelcastEnabled(autoRefresh = true)` on a Searchable DTO, so that its Blotter is backed by Hazelcast without any change to the `blotter()` definition.
2. As a spec author, I want generation to fail if a Searchable DTO declares both `hazelcastEnabled` and `elasticsearchEnabled`, so that a DTO has exactly one backing.
3. As a spec author, I want generation to fail if a Hazelcast-backed Blotter declares `infiniteScroll()` or paging options, so that I find out at generation time that they do not apply.
4. As a spec author, I want the Hazelcast-backed Blotter to use the same `columnFromDto` columns as any other Blotter, so that I do not learn a new column DSL.
5. As a spec author, I want a Searchable DTO's path fields (such as `alpha.someString`) flattened into the cached document, so that the Blotter needs no joins at fetch time.
6. As a spec author, I want a foreign key to an Entity with an Entity ref to appear as a ref (id, display name, shortcode) in the row, so that users see names, not uuids.
7. As a spec author, I want a foreign key to an Entity with no Entity ref to stay a plain id, so that behaviour matches the Elasticsearch-backed Blotter.
8. As a spec author, I want a nullable foreign key to give a null ref, so that rows with no target render cleanly.
9. As a spec author, I want to optionally name an authority on the Searchable DTO, so that the fetch-all endpoint, which returns the whole set in one call, is restricted to users holding it.
10. As a spec author, I want to set `maxRows` per Searchable DTO (default 10,000), so that a data set that outgrows the design fails loudly.
11. As a spec author, I want a per-DTO override of the population mode, so that a large, rarely used DTO can be lazy while the rest are eager.
12. As an application operator, I want one global property to choose eager or lazy population (default eager), so that I can trade startup time against first-fetch time.
13. As an application operator, I want eager population to run at startup, so that the first user does not wait.
14. As an application operator, I want startup population to fail with a clear message naming the Searchable DTO and the limit if the set exceeds `maxRows`, so that I know the DTO is unsuitable.
15. As an application operator, I want a lazy fetch that exceeds `maxRows` to fail with the same explicit error and never return a truncated set, so that no one acts on incomplete data.
16. As an application operator, I want the map rebuilt from the database on every startup, so that a new document shape or a stale map after a restart needs no migration.
17. As an application operator, I want a generated, injectable rebuild service that clears and repopulates a map, so that I can recover from a failed refresh without restarting.
18. As an application operator, I want the maps to live in the application's existing Hazelcast instance and be registered through the generated `HazelcastConfig`, so that nothing new needs provisioning.
19. As an application operator, I want a failed refresh to be logged in the same way as a failed Elasticsearch refresh, so that I have one way to monitor both.
20. As an end user, I want the Blotter to load the whole set once, so that filtering and sorting respond instantly.
21. As an end user, I want to filter and sort in the browser on every column, including a ref column on its display name, so that I can find a row by the name I recognise.
22. As an end user, I want a refresh button on the Blotter, so that I can see changes made since I loaded the page.
23. As an end user, I want a changed related record (for example a renamed Alpha) to show its new name in the Blotter after I refresh, so that the grid is never permanently stale.
24. As an end user, I want a created, updated or deleted row to appear, change or disappear after a refresh, so that the grid matches the data.
25. As an end user, I want a clear error message if the data set is too large to display, so that I know it is a configuration problem, not an empty result.
26. As an end user without the required authority, I want the fetch to be refused, so that data I may not see is not sent to my browser.
27. As a framework maintainer, I want the document shape, ref handling and refresh-dependency plan shared with the Elasticsearch path, so that the two backends do not drift.
28. As a framework maintainer, I want the Hazelcast documents to use compact serialization registered in the generated `HazelcastConfig`, so that the map follows the existing Hazelcast conventions.
29. As a framework maintainer, I want the showcase to include a Hazelcast-backed Blotter with a direct ref, a nullable ref, a no-ref foreign key and a path field, so that every feature has a reference example and a test vehicle.

## Implementation Decisions

- **Declaration:** `hazelcastEnabled(autoRefresh = true)` inside `searchableDto {}`, mirroring `elasticsearchEnabled`, with an optional required authority, an optional `maxRows` (default 10,000) and an optional per-DTO population override. No version parameter (the map is disposable; ADR-0008). `blotter()` is unchanged.
- **Validation (spec module):** SQL, Elasticsearch and Hazelcast are mutually exclusive on one Searchable DTO; a Hazelcast-backed Blotter rejects `infiniteScroll()` and paging options.
- **Document model:** the same rich, denormalized row as the Elasticsearch document, including Entity refs (ADR-0007) and flattened path fields. The existing refresh-dependency plan is reused; only the sink differs, a Hazelcast map keyed by the row id instead of an Elasticsearch index.
- **Refresh:** driven by the post-commit CRUD notifications (ADR-0004), with `autoRefresh` behaving as it does for Elasticsearch. A failed refresh is logged, with the same handling as the Elasticsearch listener. No new retry mechanism.
- **Population:** eager at startup by default; lazy on first fetch by property. The property is global with a per-DTO override (for example `maia.hazelcast-blotter.population=eager|lazy`). Eager population clears and rebuilds the map. Rows are read through the SQL path, converted to documents, and the count is checked against `maxRows` before the map is considered populated.
- **Size guard:** exceeding `maxRows` fails population (eager) or the fetch (lazy) with an explicit message naming the Searchable DTO and the limit. It never truncates. The Blotter page shows the error.
- **Endpoint:** one generated GET returning all rows, with no filter, sort or page parameters. If an authority is declared, it is enforced with `@PreAuthorize(hasAuthority(...))`, following the entity-history Blotter endpoint pattern. Otherwise it is open to any authenticated user, as the Elasticsearch endpoints are today. There is no count endpoint.
- **Rebuild service:** a generated injectable service per Hazelcast-backed Searchable DTO that clears and repopulates its map. No REST endpoint or dashboard in v1.
- **Hazelcast instance:** the application's existing instance. The generated `HazelcastConfig` registers the map config and a compact serializer for the document. An application using this feature needs Hazelcast on its classpath.
- **Frontend:** the generated Blotter uses AG Grid's client-side row model, with a refresh control that re-fetches. The generated TypeScript row type follows the Kotlin type (ref fields typed as the RefDto), as for the Elasticsearch-backed Blotter. No server-side filter model; ADR-0005 is not engaged.
- **Showcase:** add Hazelcast-backed Searchable DTOs, Blotters and Blotter pages over the existing Alpha/Bravo/Delta join entities, covering a required ref, a nullable ref, a no-ref foreign key and a path field. Every Blotter gets a page.
- **Glossary and ADR:** already written (`Client-side blotter`, `Hazelcast-backed Blotter`, ADR-0008).

## Testing Decisions

A good test exercises external behaviour only: what a spec author can declare and what a user or caller sees. It does not assert on generated source text or on map internals.

Seams (agreed with the user):

1. **Showcase black-box integration test** (extends `AbstractBlackBoxTest`): drive the generated CRUD services, then call the fetch-all endpoint or service. Covers eager and lazy population, auto-refresh when a referenced entity changes (including create, update and delete of rows), ref denormalization (direct, nullable, no-ref, path field), the `maxRows` failure on both population modes, the rebuild service and the authority check. Prior art: `EsDocAutoRefreshIntegrationTest`.
2. **Showcase Playwright test** (extends `AbstractPlaywrightTest`, one page per scenario): the Blotter loads the whole set, filters and sorts client-side including on a ref column's display name, the refresh button picks up an edit, and the `maxRows` error is shown. Prior art: `BravoEsBlotterPlaywrightTest`, with its page objects.
3. **Spec validation unit tests in the spec module**: mutual exclusion of the three backings, rejection of `infiniteScroll()` and paging. Prior art: `EsRefreshDependencyDefTest`.

No renderer-level tests beyond the showcase build compiling the generated code.

## Out of Scope

- Live push of changes to an open Blotter (a later feature may reuse the CRUD notifications).
- An operations dashboard or REST endpoint for rebuilding (as with the Elastic Indices dashboard).
- Server-side filtering, sorting or paging for Hazelcast-backed Blotters, and any server-side filter model.
- Migration of existing Searchable DTOs between backings.
- Provisioning or configuring a Hazelcast cluster; the existing instance is used as is.
- Giving SQL-backed Blotters Entity refs.

## Further Notes

- `cacheable {}` on an Entity is an unrelated per-Entity read cache; avoid the term "cached blotter".
- The refresh-dependency plan currently lives in the Elasticsearch generation code; implementation should extract what is shared rather than copy it.
- Suggested slicing: (1) spec DSL, validation and the Hazelcast map plus fetch-all endpoint for a flat DTO; (2) Client-side blotter page generation; (3) refs and path fields; (4) auto-refresh; (5) population modes, `maxRows` and the rebuild service; (6) authority.
