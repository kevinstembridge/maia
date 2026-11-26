# 04: Filter and sort a ref column on the display name

**What to build:** A ref column in an ES Blotter filters and sorts on the ref's display name as a text column (mapped text plus keyword), not on the id. Establish first whether the Elasticsearch filter code can address the display-name sub-field without a new filter type; filter semantics must match other text columns (ADR-0005).

**Blocked by:** 02

**Status:** resolved

- [ ] A Showcase Playwright test filters the `alpha` column by display-name text and gets the expected rows.
- [ ] The same test sorts the column and sees rows ordered by display name.
- [ ] The id `equals` filter is no longer used for ref columns.

## Comments
