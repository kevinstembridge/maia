package org.maiaframework.gen.spec.definition


/**
 * One Elasticsearch index version declared on a searchable DTO.
 *
 * @property version the index version number; each declared version yields its own EsDocDef
 * @property autoRefresh whether the index should be refreshed automatically when source records change
 */
data class EsVersionSpec(
    val version: Int,
    val autoRefresh: Boolean
)
