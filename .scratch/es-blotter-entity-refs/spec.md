Status: ready-for-agent

# Elasticsearch-backed Blotters show Entity refs for foreign-key columns

## Problem Statement

A Blotter backed by an Elasticsearch-enabled Searchable DTO shows each foreign-key column as a bare uuid, such as the `product` on a subscription plan row or the `createdBy` on any row. A user cannot tell which Product or Party a row refers to without opening the row. The detail and edit views of the same Entity already show an Entity ref (display name and shortcode), so the Blotter is inconsistent with the rest of the UI.

Separately, the generated TypeScript for every Blotter row DTO declares foreign-key fields as the RefDto type, although the Kotlin DTO and the JSON on the wire carry a plain uuid for SQL-backed Blotters. Code that trusted the TypeScript type would read properties that do not exist.

## Solution

When a Searchable DTO is Elasticsearch-enabled, every foreign-key field on it whose foreign Entity declares an Entity ref becomes that Entity's RefDto, in the EsDoc, the Blotter row DTO, and the generated TypeScript. The Blotter cell shows the ref's display name, and the column is filtered and sorted on the display name. The ref is stored in the index and kept fresh when the referenced Entity's display name or shortcode changes.

SQL-backed Blotters are unchanged: the foreign-key field stays a uuid, and the generated TypeScript now says so.

See ADR-0007 for the decision record and its relationship to ADR-0005.

## User Stories

1. As an end user viewing an Elasticsearch-backed Blotter, I want a foreign-key column to show the referenced record's display name, so that I can recognise the record without opening the row.
2. As an end user, I want the createdBy and lastModifiedBy columns to show a Party's display name, so that I can see who changed a record.
3. As an end user, I want to filter a ref column by typing part of the display name, so that I can find rows without knowing a uuid.
4. As an end user, I want to sort a ref column alphabetically by display name, so that related rows group sensibly.
5. As an end user, I want the Blotter to show the new display name after someone renames the referenced record, so that I am not looking at stale text.
6. As an end user, I want a row whose foreign key is empty to show an empty cell, not an error or the text "null", so that optional relationships read naturally.
7. As an end user, I want a ref column that is a path through another Entity (for example the creator of a related record) to behave the same as a direct one, so that I do not need to know how the column was declared.
8. As an end user of a SQL-backed Blotter, I want my existing foreign-key columns to keep working exactly as before, so that nothing regresses.
9. As an application developer, I want ref conversion to happen automatically when I enable Elasticsearch on a Searchable DTO, so that I do not have to declare each foreign-key column.
10. As an application developer, I want a foreign key to an Entity with no Entity ref to stay a uuid, so that enabling Elasticsearch never fails on such a field.
11. As an application developer, I want the Blotter row DTO to expose a nullable RefDto when the foreign key is nullable, so that my code handles absence in the type system.
12. As an application developer, I want the generated TypeScript row type to match what the server actually sends for each backend, so that the compiler catches wrong property access.
13. As an application developer, I want the index to refresh when a referenced Entity's display name or shortcode changes, so that I do not write refresh code by hand.
14. As an application developer with an existing live index, I want a clear way to roll out the changed mapping, so that I can migrate without downtime by declaring a new index version.
15. As a Maia maintainer, I want the Elasticsearch and SQL backends to keep identical filter behaviour for any given column type, so that ADR-0005 still holds.
16. As a Maia maintainer, I want the whole behaviour covered by one black-box test in the Showcase, so that regressions in generation, indexing, refresh and UI are caught together.
17. As a Maia maintainer, I want the exception to ADR-0005 recorded, so that future readers understand why the column type depends on the backend.

## Implementation Decisions

- **Which fields convert.** On an Elasticsearch-enabled Searchable DTO, a field converts when its type is a foreign key and the foreign Entity has an Entity ref (a declared display name field). This applies to direct fields and to the terminal field of a path. Path fields ending in a non-foreign-key field are unchanged. A foreign key to an Entity without an Entity ref stays a `DomainId`.
- **Automatic, ES-only.** No per-field opt-in. Searchable DTOs that are not Elasticsearch-enabled are unaffected.
- **Field type.** The converted field's type becomes the Entity ref type already used by the fetch-for-edit and detail-view DTOs. Nullability follows the entity field. A null foreign key, or one that no longer resolves, gives a null ref.
- **EsDoc and mapping.** The EsDoc field is an object holding id, display name and, where declared, shortcode. The display name is mapped as text with a keyword sub-field so it can be filtered and sorted. The existing many-to-many ref field is the precedent for object-typed refs in the index.
- **Index refresh.** The existing refresh-dependency model registers the foreign Entity as a dependency of the root and watches its display-name and shortcode fields, as it does for many-to-many refs. Direct and path foreign keys both need this. Existing reverse-join and bulk-refresh behaviour is reused.
- **Row mapping.** The generated EsDoc-to-row mapper passes the ref object through; no lookup per row.
- **Blotter filter and sort.** A ref column filters and sorts on the display name as a text column. The ID `equals` filter currently used for ref-typed columns is not used for these columns. The filter must behave the same as other text columns on the Elasticsearch backend.
- **Blotter UI.** A ref column renders the display name as plain text. It replaces the uuid cell renderer. No link to the referenced record.
- **TypeScript renderer.** Foreign-key fields on response DTOs render as the RefDto type only when the Kotlin field is a ref. Otherwise they render as `string`. This fixes the current mismatch on SQL-backed Blotter rows and any other response DTO built without refs. Other response DTOs that genuinely carry refs (fetch-for-edit, detail view) keep their RefDto types.
- **Existing indices.** No migration support. A consumer with a live index declares a new Elasticsearch version of the Searchable DTO.
- **ADR and glossary.** ADR-0007 records the decision and amends ADR-0005. The Entity ref glossary entry is extended to cover Elasticsearch-backed Blotter columns.
- **Scope.** All changes are in Maia (spec model, generators, Showcase). Mahana is not changed by this spec.

## Testing Decisions

- A good test here observes behaviour through the running Showcase app and the browser, not generated source text or internal types. Assertions are about what a user sees and what filtering and sorting return.
- **One seam: the Showcase black-box Playwright test**, run against real Postgres and Elasticsearch in Testcontainers, sitting beside the existing Bravo CRUD Playwright test. The existing Elasticsearch-enabled Bravo Searchable DTO and its Blotter are the vehicle, with the `alpha` foreign key added as a field. Alpha already declares an Entity ref.
- The test covers:
  - The `alpha` column shows the Alpha's display text, not a uuid.
  - After editing the Alpha's display field, the Blotter shows the new text (index refresh).
  - Filtering the column by display-name text returns the expected rows; sorting orders by display name.
  - A row with no foreign-key value shows an empty cell. The Showcase needs an entity with a nullable foreign key to an Entity with a ref for this; add one or make an existing foreign key nullable, keeping other tests working.
  - A foreign key to an Entity without an Entity ref stays a uuid on the Elasticsearch Blotter (needs a Showcase example if none exists).
  - The SQL-backed Blotter over the same Entity still shows the uuid.
  - The createdBy/lastModifiedBy columns show a Party-style ref if the Showcase has a Party-like Entity ref for them; otherwise this is covered by the Alpha case.
- The generated TypeScript type is not tested directly. The Showcase UI compiles it, so a wrong declaration fails the build or the page.
- No separate generator unit tests for this behaviour, per the one-seam preference.
- Prior art: the existing Showcase Playwright tests, in particular the join (Bravo) and many-to-many (LeftMany) suites, and the shared filter scenarios from ADR-0005 for Elasticsearch filter behaviour.

## Out of Scope

- Converting SQL-backed Blotters to carry refs (would need DAO row-mapper joins).
- Links from a ref cell to the referenced record's view page.
- Any change in mahana, including enabling Elasticsearch on the SubscriptionPlan Searchable DTO.
- Migration tooling for existing Elasticsearch indices.
- Filtering a ref column by id.
- Per-field opt-in or opt-out of ref conversion.

## Further Notes

- The existing feature doc for Elasticsearch-enabled Searchable DTOs should gain a short section on ref fields and the version-bump consequence.
- Unverified before implementation: whether the Elasticsearch filter code can filter a ref column's display-name sub-field without a new filter type, and which Showcase Entities already provide the nullable-foreign-key and no-Entity-ref cases above.
