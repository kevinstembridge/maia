# 02: Elasticsearch Blotter shows a ref for a direct foreign key (tracer)

**What to build:** On an Elasticsearch-enabled Searchable DTO, a direct foreign-key field whose foreign Entity has an Entity ref becomes that Entity's RefDto, end to end: EsDoc object mapping (id, display name, shortcode), row mapper, Kotlin row DTO, generated TypeScript, and a Blotter cell that shows the display name as plain text. Conversion is automatic for ES-enabled DTOs only. Vehicle: add the `alpha` field to the existing Elasticsearch-enabled Bravo Searchable DTO and its Blotter. See spec.md and ADR-0007.

**Blocked by:** 01

**Status:** resolved

- [ ] A Showcase Playwright test, beside the existing Bravo CRUD Playwright test, creates an Alpha and a Bravo and sees the Alpha's display text in the ES Blotter's `alpha` column, not a uuid.
- [ ] The row DTO's `alpha` is `AlphaRefDto` and the TypeScript declares it as such; the SQL-backed Bravo Blotter is unchanged.
- [ ] A foreign key to an Entity without an Entity ref is not converted (generation does not fail).
- [ ] The feature doc for Elasticsearch-enabled Searchable DTOs gains a short section on ref fields and the version-bump consequence for existing indices.

## Comments

- Resolved on main (Maia) in the es-refs series, tip b28511da. Gap: no ES Blotter test shows createdBy/lastModifiedBy as a Party ref (no ES Searchable DTO in the Showcase exposes them).
