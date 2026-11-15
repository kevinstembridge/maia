# BigDecimal fields travel as strings on the wire

`BigDecimal` fields (default precision 19, scale 4) are serialised as JSON strings, via `@JsonFormat(shape = STRING)` on generated DTO properties, and typed `string` in the generated TypeScript, unlike Int and Double which travel as numbers. A JS `number` cannot hold 19 digits exactly, and money-like values must not be silently rounded in the browser. The cost is that the UI converts to a number for ag-grid sorting and filtering (the server still filters exactly, so [ADR-0005](0005-ag-grid-filter-parity-across-backends.md) holds), and form inputs are text fields with a pattern validator.

Elasticsearch maps the field to `scaled_float` with a scaling factor of 10^scale, which is exact to the declared scale. Because `scaled_float` is stored as a signed 64-bit long, a value above about 9.2×10^14 at the default scale of 4 cannot be indexed, even though Postgres `NUMERIC(19,4)` allows it.
