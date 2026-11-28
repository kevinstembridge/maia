# 07: Rebuild service

**What to build:** A generated, injectable service per Hazelcast-backed Searchable DTO that clears its map and repopulates it from the database, so an operator can recover from a failed refresh without restarting. There is no REST endpoint or dashboard in v1.

**Blocked by:** 02

**Status:** resolved

- [x] The service exists for each Hazelcast-backed Searchable DTO.
- [ ] After a row is changed directly in the database, calling the service makes the fetch-all result match the database (black-box integration test).
- [ ] The service respects `maxRows` once ticket 06 has landed; until then it has no limit check.
