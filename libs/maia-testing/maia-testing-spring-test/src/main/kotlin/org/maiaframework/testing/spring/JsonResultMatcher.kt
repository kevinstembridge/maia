package org.maiaframework.testing.spring

import com.fasterxml.jackson.databind.ObjectMapper
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.comparator.JSONComparator
import org.springframework.test.web.servlet.ResultMatcher
import java.nio.charset.StandardCharsets

class JsonResultMatcher(private val jsonComparator: JSONComparator) {


    private val objectMapper = ObjectMapper()


    fun asJson(expectedResponseBody: Any): ResultMatcher {

        val expectedJson = this.objectMapper.writeValueAsString(expectedResponseBody)

        return ResultMatcher { result ->
            val actualJson = result.response.getContentAsString(StandardCharsets.UTF_8)
            JSONAssert.assertEquals(expectedJson, actualJson, this.jsonComparator)
        }

    }


}
