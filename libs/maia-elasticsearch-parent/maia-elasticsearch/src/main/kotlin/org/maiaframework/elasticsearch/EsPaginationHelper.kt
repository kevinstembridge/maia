package org.maiaframework.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.elasticsearch.core.search.HitsMetadata
import org.springframework.stereotype.Component


@Component
class EsPaginationHelper(private val client: ElasticsearchClient) {


    fun <T> paginate(
        searchRequestFunc: (SearchRequest.Builder) -> Unit,
        documentClass: Class<T>,
        searchHitConsumer: (HitsMetadata<T>) -> Unit,
    ) {

        val searchRequestBuilder = SearchRequest.Builder()
        searchRequestFunc.invoke(searchRequestBuilder)
        val searchRequest = searchRequestBuilder.build()

        val searchResponse: SearchResponse<T> = client.search(searchRequest, documentClass)

        var hitsMetadata = searchResponse.hits()

        while (hitsMetadata != null && hitsMetadata.hits().isNotEmpty()) {

            searchHitConsumer.invoke(hitsMetadata)

            val lastHit = hitsMetadata.hits().last()

            val sortValues: List<FieldValue> = lastHit.sort()

            val newSearchRequestBuilder = SearchRequest.Builder()
            searchRequestFunc.invoke(newSearchRequestBuilder)
            val newSearchRequest = newSearchRequestBuilder.searchAfter(sortValues).build()

            hitsMetadata = client.search(newSearchRequest, documentClass).hits()

        }

    }


}
