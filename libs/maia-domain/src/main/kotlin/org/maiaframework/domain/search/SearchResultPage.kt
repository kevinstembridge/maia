package org.maiaframework.domain.search

data class SearchResultPage<T>(
    val results: List<T>,
    val totalResultCount: Long,
    val offset: Int,
    val limit: Int?
) {

    val firstResultIndex = offset + 1

    val lastResultIndex = offset + results.size

}
