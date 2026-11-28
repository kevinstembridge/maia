package org.maiaframework.common


/**
 * Thrown when the set behind a Hazelcast-backed Blotter has more rows than the Searchable DTO's `maxRows` (ADR-0008).
 * The set is never truncated: population fails instead, and the web layer reports this message to the Blotter page.
 */
class BlotterTooLargeException(
    val searchableDtoName: String,
    val maxRows: Int
) : RuntimeException(
    "The Hazelcast-backed Searchable DTO $searchableDtoName has more rows than its maxRows limit of $maxRows, " +
            "so its Blotter cannot be loaded. Raise maxRows in the spec or narrow the Searchable DTO."
)
