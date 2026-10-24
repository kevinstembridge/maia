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


    @Test
    fun `contains filter becomes a case-insensitive wildcard query`() {

        val request = factory.buildSearchRequest(containsModel("awai"), fieldNameMapper, indexName)

        val wildcard = request.query()!!.bool().must().single().wildcard()
        assertThat(wildcard.field()).isEqualTo("field_title")
        assertThat(wildcard.value()).isEqualTo("*awai*")
        assertThat(wildcard.caseInsensitive()).isTrue()

    }


    @Test
    fun `contains filter escapes wildcard characters in the user input`() {

        val request = factory.buildSearchRequest(containsModel("a*b?c\\d"), fieldNameMapper, indexName)

        assertThat(request.query()!!.bool().must().single().wildcard().value()).isEqualTo("*a\\*b\\?c\\\\d*")

    }


    @Test
    fun `contains filter is applied to count requests too`() {

        val request = factory.buildCountRequest(containsModel("awai"), fieldNameMapper, indexName)

        assertThat(request.query()!!.bool().must().single().wildcard().value()).isEqualTo("*awai*")

    }


    private fun containsModel(filter: String) = AgGridSearchModel(
        filterModel = JsonNodeFactory.instance.objectNode().apply {
            putObject("title").put("filterType", "text").put("type", "contains").put("filter", filter)
        },
        sortModel = emptyList(),
        startRow = 0,
        endRow = 10
    )


    private fun sortFieldsOf(request: SearchRequest): List<String> {
        return request.sort().map { it.field().field() }
    }


    private fun termFieldsOf(request: SearchRequest): List<String> {
        return request.query()!!.bool().must().map { it.term().field() }
    }


}
