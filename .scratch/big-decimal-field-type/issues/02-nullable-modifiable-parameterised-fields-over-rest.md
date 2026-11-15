# 02: Nullable, modifiable and parameterised fields over the REST API

**What to build:** `AllFieldTypesEntity` gains `someBigDecimalNullable` and `someBigDecimalModifiable` (at `(10, 2)`), carried through the service layer and Request and Response DTOs. Generated DTO properties carry `@JsonFormat(shape = STRING)` so values travel as JSON strings such as `"12.3400"` (ADR-0006). Test builder and the validation and security tests are updated for the new fields.

**Blocked by:** 01

**Status:** resolved

- [x] Nullable and modifiable BigDecimal fields are in the Showcase Spec, migration and regenerated code
- [x] Generated DTO properties serialise and deserialise BigDecimal as a JSON string
- [x] A black-box create, update and read through the web layer returns exact string values and honours the `(10, 2)` scale
- [x] `AllFieldTypesEntityTestBuilder`, the validation tests and the security tests cover the new fields and pass
