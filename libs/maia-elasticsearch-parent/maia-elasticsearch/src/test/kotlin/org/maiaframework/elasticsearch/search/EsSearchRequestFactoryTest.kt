package org.maiaframework.elasticsearch.search

import co.elastic.clients.elasticsearch.core.SearchRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.domain.search.AgGridSortModelItem
import org.maiaframework.elasticsearch.index.model.IndexResolvedName
import tools.jackson.databind.node.JsonNodeFactory


class EsSearchRequestFactoryTest {


    private val factory = EsSearchRequestFactory()


    private val indexName = IndexResolvedName("widget_v0001")


    private val fieldNameMapper: (String) -> String = { "field_$it" }




    private val searchModel = AgGridSearchModel(
        filterModel = JsonNodeFactory.instance.objectNode().apply {
            putObject("title").put("filter", "abc")
        },
        sortModel = listOf(AgGridSortModelItem("title", "desc")),
        startRow = 0,
        endRow = 10
    )


    @Test
    fun `sort and filter both use the mapped field name`() {

        val request = factory.buildSearchRequest(searchModel, fieldNameMapper, indexName)

        assertThat(sortFieldsOf(request)).containsExactly("field_title")
        assertThat(termFieldsOf(request)).containsExactly("field_title")

    }


    @Test
    fun `count request filters use the field mapper`() {

        val request = factory.buildCountRequest(searchModel, fieldNameMapper, indexName)

        assertThat(request.query()!!.bool().must().map { it.term().field() }).containsExactly("field_title")

    }


    private fun sortFieldsOf(request: SearchRequest): List<String> {
        return request.sort().map { it.field().field() }
    }


    private fun termFieldsOf(request: SearchRequest): List<String> {
        return request.query()!!.bool().must().map { it.term().field() }
    }


}
