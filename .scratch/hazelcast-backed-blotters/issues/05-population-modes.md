# 05: Population modes (eager and lazy)

**What to build:** Whether a Hazelcast-backed Blotter's map is filled at startup (eager, the default) or on first fetch (lazy) is a property, global with a per-Searchable-DTO override (for example `maia.hazelcast-blotter.population=eager|lazy`). The map is derived data and is rebuilt from the database on every startup. See ADR-0008.

**Blocked by:** 02

**Status:** resolved

- [x] With no property set, population is eager.
- [x] Setting the global property to lazy defers population to the first fetch, which then returns the full set.
- [x] A per-DTO override takes precedence over the global property.
- [x] A restart rebuilds the map from the database (black-box integration test).
