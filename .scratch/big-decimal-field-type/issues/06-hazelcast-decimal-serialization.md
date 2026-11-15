# 06: Hazelcast DECIMAL serialization

**What to build:** An Entity with BigDecimal fields can be cached in Hazelcast and read back with exact values. A new `HazelcastCompatibleType.DECIMAL` maps to Compact serialization's native decimal read and write in the serializer renderer, with Int-parity coverage.

**Blocked by:** 01

**Status:** ready-for-agent

- [ ] `HazelcastCompatibleType.DECIMAL` exists and `BigDecimalFieldType` uses it
- [ ] Generated Hazelcast serializers read and write BigDecimal, including nullable, with Compact's native decimal methods
- [ ] A cache round trip on an Entity with BigDecimal fields returns exact values including scale
