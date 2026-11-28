package org.maiaframework.gen.spec.definition


/**
 * A Hazelcast-backed declaration on a searchable DTO (ADR-0008). There is no version: the map is disposable and is
 * rebuilt from the database.
 *
 * @property autoRefresh whether the map should be refreshed automatically when source records change
 * @property population per-DTO override of the global population mode, or null to follow the global property
 * @property maxRows the most rows the map may hold; a database set larger than this fails population instead of being truncated
 */
data class HazelcastSpec(
    val autoRefresh: Boolean,
    val population: HazelcastPopulation? = null,
    val maxRows: Int = DEFAULT_MAX_ROWS
) {

    companion object {

        const val DEFAULT_MAX_ROWS = 10_000

    }

}
