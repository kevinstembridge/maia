# 06: Path fields ending in a foreign key convert

**What to build:** A path field on an ES-enabled Searchable DTO whose terminal field is a foreign key to an Entity with an Entity ref (for example a related record's `createdBy`) becomes a ref, the same as a direct field. Path fields ending in a non-foreign-key field (such as `alpha.someString`) are unchanged. The index refresh must cover the extra hop. Needs a Showcase example.

**Blocked by:** 02

**Status:** resolved

- [ ] A Showcase Playwright test shows the display name of the terminal ref in the ES Blotter.
- [ ] Existing path fields ending in a non-foreign-key field still render as before.
- [ ] Editing the terminal referenced record's display field refreshes the Blotter (if cheap to add; otherwise note the gap).

## Comments
