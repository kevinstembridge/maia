# 05: Null foreign keys and foreign keys without an Entity ref

**What to build:** A nullable foreign key on an ES-enabled DTO gives a nullable ref: the row DTO field is `XRefDto?`, a null foreign key gives a null ref, and the Blotter cell is empty. A foreign key to an Entity with no Entity ref stays a uuid on the ES Blotter. The Showcase needs an entity with a nullable foreign key to an Entity with a ref (add one or make an existing foreign key nullable, keeping other tests working), and an example of a foreign key to an Entity without a ref if none exists.

**Blocked by:** 02

**Status:** resolved

- [ ] A Showcase Playwright test shows an empty cell, not an error or the text "null", for a row with no foreign-key value.
- [ ] A Showcase Playwright test shows a uuid for a foreign key to an Entity without an Entity ref on an ES Blotter.
- [ ] The TypeScript declares the nullable ref as optional/nullable.

## Comments
