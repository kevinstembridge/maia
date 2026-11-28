# 01: Share the document shape and refresh plan between the ES and Hazelcast sinks

**What to build:** A prefactor with no behaviour change. The Searchable DTO document shape (including Entity refs, ADR-0007) and the refresh-dependency plan are separated from their Elasticsearch-specific sink, so a second sink (a Hazelcast map) can reuse them. See spec.md and ADR-0008.

**Blocked by:** None (can start immediately)

**Status:** resolved

- [ ] Elasticsearch-enabled Searchable DTOs generate exactly the same behaviour as before.
- [ ] The existing ES integration and Playwright tests in the showcase pass unchanged.
  Playwright not runnable in this environment (unsupported platform); test written and compiles. Not individually re-verified here; generator/spec unit tests pass.
- [ ] The shared pieces have no dependency on Elasticsearch types.
