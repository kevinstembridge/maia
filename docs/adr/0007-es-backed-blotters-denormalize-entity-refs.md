# Elasticsearch-backed Blotters denormalize Entity refs into the index

When a Searchable DTO is Elasticsearch-enabled, every foreign-key field (direct, or the terminal field of a path) whose foreign Entity declares an Entity ref is typed as that Entity's RefDto in the EsDoc and in the Blotter row DTO, in Kotlin and in the generated TypeScript. The ref (id, display name, shortcode) is stored in the index as an object and kept fresh by the existing `esRefreshDependencies` machinery, as many-to-many refs already are. This keeps a Blotter to one Elasticsearch query, with no per-row lookup. A nullable foreign key gives a null ref. A foreign key to an Entity with no Entity ref stays a `DomainId`.

A ref column is filtered and sorted on the ref's display name (mapped as text plus keyword), not on the id. SQL-backed Blotters are unchanged: the foreign-key field stays a `DomainId`, and the generated TypeScript now says `string` for it, not a RefDto, which it wrongly declared before.

This is a deliberate exception to [ADR-0005](0005-ag-grid-filter-parity-across-backends.md). Parity still holds for any given column type, but the column type of a foreign key now depends on the backend. Giving SQL-backed Blotters refs would need joins in the DAO row mappers and was not chosen.

Converting an existing ES-enabled DTO changes its index mapping and row shape. Maia provides no migration; a consumer with a live index declares a new `elasticsearchEnabled(version = n)`.
