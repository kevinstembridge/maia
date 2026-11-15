# 01: BigDecimal Field type persists to Postgres

**What to build:** A Spec author can declare `FieldTypes.bigDecimal` (default precision 19, scale 4) or `FieldTypes.bigDecimal(precision, scale)` and get an exact `NUMERIC(p,s)` column that round-trips through the generated domain, DAO and repo layers, with Int-parity coverage (same `TODO("YAGNI?")` branches as Int). Showcase gets `someBigDecimal` on `AllFieldTypesEntity` via the Showcase Spec, plus a Flyway migration. Generated code is regenerated, never hand-edited. See `.scratch/big-decimal-field-type/spec.md`.

**Blocked by:** None (can start immediately).

**Status:** resolved

- [x] `FieldTypes.bigDecimal` property and `FieldTypes.bigDecimal(precision, scale)` function exist; the Field type is not a singleton
- [x] Spec building fails clearly unless `precision >= 1` and `0 <= scale <= precision`
- [x] Spec-level test in `maia-gen-spec` covers the default 19,4, the `NUMERIC(p,s)` string and the validation failures
- [x] `ResultSetAdapter` has `readBigDecimal` and `readBigDecimalOrNull`; JDBC compatible type is `decimal`
- [x] `someBigDecimal` generated on `AllFieldTypesEntity`; migration added; module builds
- [x] `AllFieldTypesDaoTest` inserts, updates and reads back a BigDecimal including scale on a real Postgres
