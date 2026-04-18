package org.maiaframework.elasticsearch.results

import co.elastic.clients.elasticsearch.core.SearchResponse


data class IndexSearchResults<T>(
    val totalHits: TotalHits,
    val hits: List<T>,
    val firstResultIndex: Int,
    val lastResultIndex: Int
) {


    companion object {

        fun <T> empty(): IndexSearchResults<T> {
            return IndexSearchResults(TotalHits.EMPTY, emptyList(), 0, 0)
        }

        fun <T> from(
            searchResponse: SearchResponse<T>,
            startIndex: Int,
        ): IndexSearchResults<T> {

            val hitsMetadata = searchResponse.hits()
            val totalHits = TotalHits.from(hitsMetadata)
            val hits = hitsMetadata.hits().mapNotNull { it.source() }
            val firstResultIndex = calculateFirstResultIndex(startIndex, totalHits)
            val lastResultIndex = calculateLastResultIndex(startIndex, hits.size)

            return IndexSearchResults(totalHits, hits, firstResultIndex, lastResultIndex)

        }


        fun <ESDOC, T> from(
            searchResponse: SearchResponse<ESDOC>,
            startIndex: Int,
            searchResultMapper: (ESDOC) -> T
        ): IndexSearchResults<T> {

            val searchHits = searchResponse.hits()
            val totalHits = TotalHits.from(searchHits)
            val searchResults = searchResponse.hits().hits().mapNotNull { it.source()?.let(searchResultMapper) }

            val firstResultIndex = calculateFirstResultIndex(startIndex, totalHits)
            val lastResultIndex = calculateLastResultIndex(startIndex, searchHits.hits().size)

            return IndexSearchResults(totalHits, searchResults, firstResultIndex, lastResultIndex)

        }


        private fun calculateFirstResultIndex(from: Int, totalHits: TotalHits): Int {

            val totalHitCount = totalHits.count

            return minOf(from + 1, totalHitCount)

        }


        private fun calculateLastResultIndex(from: Int, hitCount: Int): Int {

            return from + hitCount

        }


    }


}
