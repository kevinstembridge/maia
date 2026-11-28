package org.maiaframework.hazelcast.refresh

import org.maiaframework.domain.refresh.RefreshCollector
import org.springframework.stereotype.Component


/**
 * The [RefreshCollector] of Hazelcast-backed Blotter maps (ADR-0004, ADR-0008). A failed refresh is logged, exactly as
 * an Elasticsearch refresh failure is, and never fails the write that raised it; the entry stays as it was until the
 * next refresh of that row or the next rebuild of the map.
 */
@Component
class HazelcastRefreshCollector : RefreshCollector(
    { 1000 },
    "Hazelcast",
    "the map entry is reconciled by the next refresh of the row or a rebuild"
)
