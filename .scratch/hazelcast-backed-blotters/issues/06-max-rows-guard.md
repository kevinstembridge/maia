# 06: maxRows guard

**What to build:** Each Hazelcast-backed Searchable DTO has a configurable `maxRows` (default 10,000). If the set exceeds it, eager startup population or the lazy fetch fails with a clear message naming the Searchable DTO and the limit. The fetch never returns a truncated set, and the Blotter page shows the error rather than an empty grid.

**Blocked by:** 05

**Status:** ready-for-agent

- [ ] `maxRows` can be set per Searchable DTO in the spec and defaults to 10,000.
- [ ] Exceeding it fails eager population with the explicit message (black-box integration test).
- [ ] Exceeding it fails a lazy fetch with the same message and returns no rows (black-box integration test).
- [ ] The Blotter page shows the error message (Playwright test).
