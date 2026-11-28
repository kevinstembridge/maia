package org.maiaframework.gen.spec.definition


/**
 * A Hazelcast-backed declaration on a searchable DTO (ADR-0008). There is no version: the map is disposable and is
 * rebuilt from the database.
 *
 * @property autoRefresh whether the map should be refreshed automatically when source records change
 * @property population per-DTO override of the global population mode, or null to follow the global property
 */
data class HazelcastSpec(
    val autoRefresh: Boolean,
    val population: HazelcastPopulation? = null
)
