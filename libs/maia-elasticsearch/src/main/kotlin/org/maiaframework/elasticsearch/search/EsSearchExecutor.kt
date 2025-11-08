package org.maiaframework.elasticsearch.search

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.maiaframework.elasticsearch.index.EsIndexName
import org.maiaframework.elasticsearch.results.IndexSearchResults
import org.maiaframework.domain.search.AgGridSearchModel
import org.springframework.stereotype.Component

@Component
class EsSearchExecutor(
    private val esSearchRequestFactory: EsSearchRequestFactory,
    private val esClient: ElasticsearchClient
) {


    fun <T> search(
        searchModel: AgGridSearchModel,
        indexName: EsIndexName,
        fieldNameMapper: (String) -> String,
        documentClass: Class<T>
    ): IndexSearchResults<T> {

        val searchRequest = this.esSearchRequestFactory.buildSearchRequest(searchModel, fieldNameMapper, indexName)

        val searchResponse = this.esClient.search(searchRequest, documentClass)

        return IndexSearchResults.from(searchResponse, searchModel.startRow)

    }


    fun <ESDOC, SEARCH_RESULT> search(
        searchModel: AgGridSearchModel,
        indexName: EsIndexName,
        fieldNameMapper: (String) -> String,
        documentClass: Class<ESDOC>,
        docMapper: (ESDOC) -> SEARCH_RESULT
    ): IndexSearchResults<SEARCH_RESULT> {

        val searchRequest = this.esSearchRequestFactory.buildSearchRequest(searchModel, fieldNameMapper, indexName)

        val searchResponse = this.esClient.search(searchRequest, documentClass)

        return IndexSearchResults.from(
            searchResponse,
            searchModel.startRow,
            docMapper
        )

    }


    fun count(
        searchModel: AgGridSearchModel,
        indexName: EsIndexName,
        fieldNameMapper: (String) -> String
    ): Long {

        val searchRequest = this.esSearchRequestFactory.buildCountRequest(searchModel, fieldNameMapper, indexName)
        return this.esClient.count(searchRequest).count()

    }


}
