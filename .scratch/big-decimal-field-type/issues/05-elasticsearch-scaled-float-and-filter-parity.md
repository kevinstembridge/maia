# 05: Elasticsearch scaled_float mapping and filter parity

**What to build:** BigDecimal fields are indexed in Elasticsearch as `scaled_float` with a scaling factor of 10^scale taken from the field's own scale, so range queries and sorting work and values are exact to the declared scale. The shared `filterparity` scenarios run against Postgres and Elasticsearch and return identical rows (ADR-0005). The ES indexing limit (about 9.2×10^14 at scale 4) is documented in the Field type KDoc.

**Blocked by:** 04

**Status:** ready-for-agent

- [ ] New parameterised `EsDocMappingTypes` entry for `scaled_float` takes the scaling factor from the field's scale
- [ ] BigDecimal number-filter scenarios in `filterparity` pass on both Postgres and Elasticsearch with identical rows
- [ ] Field type KDoc states the 64-bit overflow limit
- [ ] Both backends run in Testcontainers in the full build
