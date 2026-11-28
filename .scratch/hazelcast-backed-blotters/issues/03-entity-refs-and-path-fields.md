# 03: Entity refs and path fields in the cached document

**What to build:** The cached document is the same rich, denormalized row as an Elasticsearch document (ADR-0007). A foreign key to an Entity with an Entity ref is a ref (id, display name, shortcode); a nullable foreign key gives a null ref; a foreign key to an Entity with no Entity ref stays a plain id; path fields such as `alpha.someString` are flattened into the document. The Blotter shows ref display names, and a ref column filters and sorts on its display name in the browser.

**Blocked by:** 02

**Status:** resolved

- [x] Showcase Hazelcast-backed DTOs cover a required ref, a nullable ref, a no-ref foreign key and a path field.
- [x] The fetch-all response carries refs, null refs and plain ids as described (black-box integration test).
- [x] The generated TypeScript row type follows the Kotlin type.
- [ ] In the browser, a ref column shows the display name and filters and sorts on it (Playwright test).

Playwright not runnable in this environment (unsupported platform); test written and compiles.
