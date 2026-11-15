Status: ready-for-agent

# BigDecimal Field type

## Problem Statement

A Spec author who needs to model money, rates or other exact decimal quantities has no suitable Field type. Int and Long cannot hold fractions, and Double is binary floating point and silently loses exactness. Authors must either store decimals as strings or accept rounding errors, and the Generator gives them no typed column, form, blotter or search support for either workaround.

## Solution

Add a BigDecimal Field type, declared in a Spec as `FieldTypes.bigDecimal` (default precision 19, scale 4) or `FieldTypes.bigDecimal(precision, scale)`. The Generator then produces an exact `NUMERIC(p,s)` column, a `BigDecimal` property throughout the Kotlin layers, a string-typed field in the TypeScript DTOs and forms, a numeric blotter column, an exact Elasticsearch mapping and Hazelcast serialization, with the same coverage as Int.

## User Stories

1. As a Spec author, I want to declare a field with `FieldTypes.bigDecimal`, so that I get an exact decimal without choosing a precision.
2. As a Spec author, I want the default to be precision 19 and scale 4, so that typical monetary values work with no configuration.
3. As a Spec author, I want to pass `FieldTypes.bigDecimal(precision, scale)`, so that I can model rates or quantities needing different digits.
4. As a Spec author, I want a nullable BigDecimal field, so that optional amounts are supported like optional Ints.
5. As a Spec author, I want a modifiable BigDecimal field, so that amounts can be edited after creation.
6. As a Spec author, I want generation to fail with a clear message when precision is below 1 or scale is outside 0..precision, so that I find a bad declaration before it reaches the database.
7. As a Spec author, I want the Generator to emit a `NUMERIC(p,s)` column, so that the database enforces the declared precision and scale.
8. As a Spec author, I want the generated DAO to read and write BigDecimal values including nulls, so that persistence works with no custom code.
9. As a Spec author, I want the generated Entity, Request DTOs and Response DTOs to use `BigDecimal`, so that the Kotlin layers keep exactness.
10. As a Spec author, I want the same coverage as Int, so that BigDecimal works wherever a plain Int field works today.
11. As an API client, I want BigDecimal values sent and received as JSON strings such as `"12.3400"`, so that no precision is lost in transit.
12. As a browser user, I want values shown exactly as stored, so that I never see a rounded amount.
13. As an Angular developer, I want BigDecimal fields typed `string` in generated TypeScript DTOs, so that the compiler stops me treating them as lossy numbers.
14. As an end user, I want a text input on create and edit forms, so that I can type an exact value without browser number-input rounding.
15. As an end user, I want the form to reject input with too many integer or decimal digits, so that I get immediate feedback instead of a server error.
16. As an end user, I want a new form to default to `0` for required BigDecimal fields, so that it behaves like Int fields.
17. As an end user, I want a nullable BigDecimal field to start empty, so that I can leave an optional amount unset.
18. As a blotter user, I want BigDecimal columns to sort numerically, so that 9 sorts before 10.
19. As a blotter user, I want number filters (equals, greater than, range) on BigDecimal columns, so that I can find rows by amount.
20. As a blotter user, I want filter results to be the same whether the Blotter is served from Postgres or Elasticsearch, so that the backend never changes what I see.
21. As a blotter user, I want filtering to be exact on the server, so that a client-side conversion never drops or adds rows.
22. As a search user, I want BigDecimal fields indexed in Elasticsearch as `scaled_float` with scaling factor 10^scale, so that range queries and sorting work and values are exact to the declared scale.
23. As an operator, I want to know the Elasticsearch indexing limit (about 9.2×10^14 at scale 4), so that I understand why a very large value fails to index while Postgres accepts it.
24. As an operator, I want BigDecimal values cached in Hazelcast using its native decimal type, so that cached entities keep exactness without string conversion.
25. As a Framework maintainer, I want `ResultSetAdapter.readBigDecimal` and `readBigDecimalOrNull`, so that generated row mappers read decimals the same way they read ints.
26. As a Framework maintainer, I want a Hazelcast `DECIMAL` compatible type, so that the serializer renderer has a native mapping to target.
27. As a Framework maintainer, I want unsupported combinations to keep the same `TODO("YAGNI?")` as Int, so that behaviour stays consistent and gaps are visible.
28. As a Framework maintainer, I want `FieldTypes.bigDecimal` to coexist as a property and a function, so that the default reads like `FieldTypes.int`.
29. As a Framework maintainer, I want a Field type entry in `GLOSSARY.md` and ADR-0006, so that the wire-format decision is not undone by accident.
30. As a Showcase maintainer, I want `someBigDecimal`, `someBigDecimalNullable` and `someBigDecimalModifiable` (at `(10, 2)`) on `AllFieldTypesEntity`, so that the Showcase exercises default, nullable and parameterised forms.
31. As a Showcase maintainer, I want a Flyway migration for the new columns, so that a fresh database matches the generated code.
32. As a test author, I want the `AllFieldTypes` test builder and page objects to cover the new fields, so that existing CRUD tests exercise BigDecimal automatically.

## Implementation Decisions

- BigDecimal is a Field type declared in a Spec. It is not a singleton: each instance carries its precision and scale. `FieldTypes.bigDecimal` is the default (19, 4) and `FieldTypes.bigDecimal(precision, scale)` creates other variants.
- Only the raw Field type is added. There is no Microtype (`bigDecimalType`) or Value class equivalent; they can follow when a Spec needs one.
- Spec building validates `precision >= 1` and `0 <= scale <= precision`, and nothing more.
- Coverage mirrors Int: every place Int is handled gets a BigDecimal branch, and wherever Int is `TODO("YAGNI?")` BigDecimal is too.
- Database: column type `NUMERIC(precision,scale)`; JDBC compatible type `decimal`. `ResultSetAdapter` in the JDBC library gains `readBigDecimal` and `readBigDecimalOrNull`.
- Wire format: JSON string. Generated DTO properties carry `@JsonFormat(shape = STRING)`; no global ObjectMapper change. See ADR-0006.
- TypeScript: the type is `string`. The reactive form uses a plain text input with a generated pattern validator allowing at most `precision - scale` integer digits and `scale` decimal digits. Default form value `"0"`; nullable fields start empty.
- Blotter: column uses the `number` cell data type with an `equals` default filter, like Int, converting the string to a number on the client for display, sort and filter. The server performs the real filtering, so ADR-0005 filter parity holds.
- Elasticsearch: new parameterised mapping type `scaled_float` with scaling factor 10^scale. It stores a signed 64-bit long, so values above about 9.2×10^14 at scale 4 cannot be indexed even though Postgres accepts them. This is documented in the Field type KDoc and ADR-0006; no validation rejects the default.
- Hazelcast: new `HazelcastCompatibleType.DECIMAL` using Compact serialization's native decimal read and write in the serializer renderer.
- Showcase: add `someBigDecimal` (default), `someBigDecimalNullable` and `someBigDecimalModifiable` (`(10, 2)`) to `AllFieldTypesEntity` through the Showcase Spec, regenerate, and add a Flyway migration for the three columns.
- Generated code is never hand-edited; changes are made in the Spec and the Generator, then regenerated.
- Glossary: Field type entry added. ADR-0006 records the string wire format and the `scaled_float` limit.

## Testing Decisions

- A good test asserts external behaviour (persisted and returned values, filter results, rendered form behaviour, generation failure for bad declarations) and not generated text or renderer internals.
- Seam 1, the main one: the Showcase round trip through `AllFieldTypesEntity` against real Postgres and Elasticsearch in Testcontainers.
  - `AllFieldTypesDaoTest` covers insert, update and read-back, nullable values, and scale handling.
  - The `filterparity` scenarios gain number-filter cases on a BigDecimal column, asserting identical rows from Postgres and Elasticsearch (ADR-0005).
  - The existing Playwright CRUD tests cover the text input, pattern validation, string transport and the blotter column through the updated `AllFieldTypesCreatePage`, `AllFieldTypesEditPage` and `AllFieldTypesEntityTestBuilder`. No new Playwright scenarios.
  - The validation and security tests that touch the Int fields are updated for the new fields.
- Seam 2: a small test in `maia-gen-spec` for `FieldTypes.bigDecimal`: the default 19,4, the `NUMERIC(p,s)` string, and the precision and scale validation failures.
- No per-renderer unit tests: they would assert generated text and let renderers drift from behaviour.
- Prior art: `AllFieldTypesDaoTest`, the `filterparity` tests, the Playwright page objects for `AllFieldTypes`, and the spec definition tests in `maia-gen-spec` (for example `SearchableDtoEsDocTest`).
- Full verification is `./gradlew clean build -Dplaywright.headless=true`.

## Out of Scope

- Microtype or Value class wrappers for BigDecimal.
- BigDecimal inside lists, sets or maps (same `TODO("YAGNI?")` as Int where it applies).
- Currency or unit modelling.
- Rejecting precision above 18 or otherwise handling the `scaled_float` overflow range.
- Exact (non-number) client-side arithmetic in the UI; the client converts to a number for display, sort and filter only.
- New Playwright scenarios.
- A global ObjectMapper change for non-generated classes.

## Further Notes

- The Showcase Spec lives in `maia-showcase/spec`; the generated `AllFieldTypesEntity.kt` under `src/generated/` is output and must not be edited.
- Related decisions: ADR-0005 (filter parity across backends) and ADR-0006 (BigDecimal on the wire as a string).
