# 08: Optional authority on the fetch-all endpoint

**What to build:** A spec author can name an authority on a Hazelcast-backed Searchable DTO. The fetch-all endpoint, which returns the whole set in one call, then requires it, following the entity-history Blotter's `@PreAuthorize(hasAuthority(...))` pattern. With no authority declared, the endpoint is open to any authenticated user, as the ES endpoints are today.

**Blocked by:** 02

**Status:** ready-for-agent

- [ ] A DTO with an authority refuses a user without it and serves a user with it (black-box integration test).
- [ ] A DTO with no authority serves any authenticated user.
- [ ] No new authorization mechanism is introduced.
