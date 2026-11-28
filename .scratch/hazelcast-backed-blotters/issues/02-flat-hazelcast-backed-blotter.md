# 02: Flat Hazelcast-backed Blotter, end to end

**What to build:** A spec author declares `hazelcastEnabled(autoRefresh = true)` on a Searchable DTO with scalar fields only. At application start Maia fills a Hazelcast map with one document per row (eager population, from the database). A generated fetch-all GET endpoint returns the whole set, and the Blotter page is a Client-side blotter: AG Grid filters and sorts in the browser. The showcase has an example Blotter page. See spec.md and ADR-0008.

**Blocked by:** 01

**Status:** resolved

- [x] `hazelcastEnabled` is accepted on a Searchable DTO and has no version parameter.
- [x] Generation fails if a Searchable DTO declares both `hazelcastEnabled` and `elasticsearchEnabled`.
- [x] Generation fails if a Hazelcast-backed Blotter declares `infiniteScroll()` or paging options (spec-module unit tests).
- [x] The map and a compact serializer are registered in the generated `HazelcastConfig`, using the application's existing Hazelcast instance.
- [x] After startup, the fetch-all endpoint returns every row (black-box integration test).
- [ ] The showcase Blotter page loads the whole set and filters and sorts in the browser (Playwright test).
  Playwright not runnable in this environment (unsupported platform); test written and compiles.
- [x] No count endpoint and no server-side filter, sort or paging parameters are generated.
