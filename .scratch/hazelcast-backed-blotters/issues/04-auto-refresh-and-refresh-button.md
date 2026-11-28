# 04: Auto-refresh and the refresh button

**What to build:** With `autoRefresh = true`, the map is updated after the owning transaction commits whenever a row, or any record its document draws on (a referenced Entity, a path-field source), is created, updated or deleted. This reuses the post-commit CRUD notifications (ADR-0004) and the shared refresh plan. The Blotter has a refresh button that re-fetches the set; the grid is a snapshot as of fetch time, with no live push. A failed refresh is logged as ES refresh failures are.

**Blocked by:** 03

**Status:** ready-for-agent

- [ ] Creating, updating and deleting a row changes the fetch-all result (black-box integration test).
- [ ] Renaming a referenced Entity updates the ref display name in the cached rows, including where the foreign key is the only link (as in the ES `BravoRefOnlyEs` scenario).
- [ ] Changing a foreign key to a different Entity updates the row's ref.
- [ ] In the browser, an edit is visible after pressing the refresh button (Playwright test).
- [ ] A refresh failure is logged and does not fail the originating write.
