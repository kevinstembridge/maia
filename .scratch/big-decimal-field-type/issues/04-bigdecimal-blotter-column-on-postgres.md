# 04: BigDecimal blotter column on Postgres

**What to build:** A blotter user sees BigDecimal columns that sort numerically and support number filters (equals, greater than, range). The column uses the `number` cell data type with an `equals` default filter and converts the string to a number on the client for display, sort and filter. Filtering stays exact on the server through the Postgres backend.

**Blocked by:** 02

**Status:** ready-for-agent

- [ ] Blotter column definition for BigDecimal is `number` with an `equals` default filter, with client-side string-to-number conversion
- [ ] Sorting is numeric (9 sorts before 10)
- [ ] Number-filter scenarios on a BigDecimal column are added to `filterparity` and pass on the Postgres backend
- [ ] The Playwright blotter path through `AllFieldTypes` passes headless
