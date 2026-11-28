package org.maiaframework.gen.spec.definition


/**
 * A Hazelcast-backed declaration on a searchable DTO (ADR-0008). There is no version: the map is disposable and is
 * rebuilt from the database.
 *
 * @property autoRefresh whether the map should be refreshed automatically when source records change
 * @property authority if set, the fetch-all endpoint requires it; if null, any authenticated user may call it
 */
data class HazelcastSpec(
    val autoRefresh: Boolean,
    val authority: AuthorityDef? = null
)
