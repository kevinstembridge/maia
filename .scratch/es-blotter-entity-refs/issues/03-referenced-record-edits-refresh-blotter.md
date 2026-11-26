# 03: Referenced record edits refresh the Blotter

**What to build:** When the display name or shortcode of a referenced Entity changes, the ES Blotter shows the new text without any hand-written refresh code. The refresh-dependency model registers the foreign Entity for a ref field and watches its display-name and shortcode fields, as it does for many-to-many refs.

**Blocked by:** 02

**Status:** resolved

- [ ] A Showcase Playwright test edits an Alpha's display field and the Bravo ES Blotter row shows the new text.
- [ ] Changes to Alpha fields that are not part of the ref do not trigger a refresh of the index.
- [ ] Deleting or changing the Alpha does not leave a stale ref in the Blotter.

## Comments
