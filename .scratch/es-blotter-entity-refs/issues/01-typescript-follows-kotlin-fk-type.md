# 01: TypeScript follows the Kotlin foreign-key type

**What to build:** The generated TypeScript for a response DTO declares a foreign-key field as the Entity's RefDto only when the Kotlin field is a ref. Otherwise it declares `string`. Today every foreign-key field on a response DTO is declared as a RefDto, even where the Kotlin DTO and the JSON carry a plain uuid (SQL-backed Blotter rows). This is the prefactor for the rest of the feature (see spec.md and ADR-0007).

**Blocked by:** None (can start immediately)

**Status:** resolved

- [ ] The SQL-backed Bravo Blotter's `alpha` column is typed `string` in the generated TypeScript and still shows the uuid in the Showcase (Playwright check).
- [ ] Response DTOs that genuinely carry refs (fetch-for-edit, detail view) keep their RefDto types and existing Showcase tests still pass.
- [ ] The Showcase UI compiles with the generated TypeScript.
