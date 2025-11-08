package org.maiaframework.gen.search

import com.fasterxml.jackson.databind.ObjectMapper
import org.maiaframework.domain.search.SearchFieldConverter
import org.maiaframework.domain.search.SearchFieldNameConverter
import org.maiaframework.domain.search.mongo.SearchRequestParser
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant
import java.util.Date

class MongoPageableSearchRequestParserTest {

    private val dummySearchRequestFieldNameConverter = object: SearchFieldNameConverter {
        override fun convertFieldName(fieldName: String): String {
            return fieldName
        }
    }



    private val dummySearchFieldConverter = object: SearchFieldConverter {
        override fun convertValue(collectionFieldPath: String, inputValue: Any?): Any? {
            return when (collectionFieldPath) {
                "someInstant" -> Date.from(Instant.parse(inputValue as String))
                else -> inputValue
            }
        }

    }


    private val searchRequestParser = SearchRequestParser(this.dummySearchRequestFieldNameConverter, this.dummySearchFieldConverter)


    @org.testng.annotations.Test(dataProvider = "toReturnEmptySearchRequest")
    fun should_return_empty_bson_for_null_input(json: String) {

        val searchRequest = searchRequestParser.parseSearchJson(json)

        val actualBson = searchRequest.query
        val expectedBson = Document()

        assertThat(actualBson).isEqualTo(expectedBson)

        val actualPageable = searchRequest.pageable
        assertThat(actualPageable).isNull()

    }


    @org.testng.annotations.DataProvider(name = "toReturnEmptySearchRequest")
    fun provideInputForEmptyBson(): Array<Array<Any?>> {

        return arrayOf(
                arrayOf<Any?>(""),
                arrayOf<Any?>("{}")
        )

    }


    @org.testng.annotations.Test
    fun test_simple() {

        // GIVEN
        val json = MongoPageableSearchRequestParserTest.Companion.OBJECT_MAPPER.writeValueAsString(mapOf(
                "query" to mapOf(
                        "someField1" to "someValue1",
                        "someField2" to "someValue2",
                )
        ))

        val expectedBson = Document()
        expectedBson["someField1"] = "someValue1"
        expectedBson["someField2"] = "someValue2"

        // WHEN
        val actualBson = searchRequestParser.parseSearchJson(json.toString()).query

        // THEN
        assertThat(actualBson).isEqualTo(expectedBson)

    }


    @org.testng.annotations.Test
    fun test_with_nested_OR_operator() {

        // GIVEN
        val json = MongoPageableSearchRequestParserTest.Companion.OBJECT_MAPPER.writeValueAsString(mapOf(
                "query" to mapOf(
                        "someField1" to "someValue1",
                        "\$or" to mapOf("someField2" to "someValue2")
                )
        ))

        val expectedBson = Document()
        expectedBson["someField1"] = "someValue1"
        expectedBson["\$or"] = object : ArrayList<Document>() {
            init {
                add(Document("someField2", "someValue2"))
                add(Document("someField2", "someValue3"))
            }
        }
        expectedBson["\$or"] = Document("someField2", "someValue2")

        // WHEN
        val actualBson = searchRequestParser.parseSearchJson(json).query

        // THEN
        assertThat(actualBson).isEqualTo(expectedBson)

    }


    @org.testng.annotations.Test
    fun should_convert_field_names() {

        // GIVEN
        val rootNode = OBJECT_MAPPER.createObjectNode()
        rootNode.putObject("query")
                .put("someField1", "someValue1")
                .putArray("\$or")
                .add(OBJECT_MAPPER.createObjectNode().put("someField2", "someValue2"))
                .add(OBJECT_MAPPER.createObjectNode().put("someField3", "someValue3"))

        val expectedBson = Document()
        expectedBson["someField1_converted"] = "someValue1"
        expectedBson["\$or"] = listOf(
            Document("someField2_converted", "someValue2"),
            Document("someField3_converted", "someValue3")
        )

        // WHEN
        val searchRequestFieldNameConverter = object: SearchFieldNameConverter {
            override fun convertFieldName(fieldName: String): String {
                return "${fieldName}_converted"
            }
        }
        val fieldConvertingSearchRequestParser = SearchRequestParser(searchRequestFieldNameConverter, this.dummySearchFieldConverter)
        val actualBson = fieldConvertingSearchRequestParser.parseSearchJson(rootNode.toString()).query

        // THEN
        assertThat(actualBson).isEqualTo(expectedBson)

    }


    @org.testng.annotations.Test
    fun should_convert_Instant_to_Date() {

        // GIVEN
        val someInstant = Instant.now()
        val someInstantAsString = someInstant.toString()

        val someDate = Date.from(someInstant)

        val rootNode = OBJECT_MAPPER.createObjectNode()
        rootNode.putObject("query")
                .put("someInstant", someInstantAsString)

        val expectedBson = Document()
        expectedBson["someInstant"] = someDate

        // WHEN
        val actualBson = searchRequestParser.parseSearchJson(rootNode.toString()).query

        // THEN
        assertThat(actualBson).isEqualTo(expectedBson)

    }


    @org.testng.annotations.Test
    fun should_convert_date_range_search() {

        // GIVEN
        val fromTimestamp = Instant.now().minusSeconds(600)
        val toTimestamp = Instant.now().plusSeconds(600)

        val fromDate = Date.from(fromTimestamp)
        val toDate = Date.from(toTimestamp)

        val rootNode = OBJECT_MAPPER.createObjectNode()
        val queryNode = rootNode.putObject("query")
        queryNode.putObject("someInstant")
                .put("\$gt", fromTimestamp.toString())
        queryNode.putObject("someInstant")
                .put("\$lte", toTimestamp.toString())


        val expectedBson = Document()
        expectedBson["someInstant"] = Document("\$gt", fromDate)
        expectedBson["someInstant"] = Document("\$lte", toDate)

        // WHEN
        val actualBson = searchRequestParser.parseSearchJson(rootNode.toString()).query

        // THEN
        assertThat(actualBson).isEqualTo(expectedBson)

    }


    // TODO test support for the $in operator with an array


    @org.testng.annotations.Test
    fun should_convert_pageable() {

        // GIVEN

        val rootNode = OBJECT_MAPPER.createObjectNode()
        val expectedPageNumber = 3
        val expectedPageSize = 10

        rootNode.putObject("pageable")
                .put("page", expectedPageNumber)
                .put("size", expectedPageSize)
                .putArray("sort")
                .add(OBJECT_MAPPER.createObjectNode().put("someField1", "asc"))
                .add(OBJECT_MAPPER.createObjectNode().put("someField2", "desc"))

        val expectedSort = Sort.by(
            Sort.Order(Sort.Direction.ASC, "someField1"),
            Sort.Order(Sort.Direction.DESC, "someField2")
        )


        val expectedPageable = PageRequest.of(expectedPageNumber, expectedPageSize, expectedSort)

        // WHEN

        val actualPageable = this.searchRequestParser.parseSearchJson(rootNode.toString()).pageable ?: throw RuntimeException("Bugger!")

        // THEN
        assertThat(actualPageable).isEqualTo(expectedPageable)

    }


    companion object {

        private val OBJECT_MAPPER = ObjectMapper()

    }


}
