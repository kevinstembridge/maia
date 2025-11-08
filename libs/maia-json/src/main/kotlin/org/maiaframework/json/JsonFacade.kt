package org.maiaframework.json

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class JsonFacade(private val objectMapper: ObjectMapper) {


    fun writeValueAsString(pojo: Any): String {

        try {
            return this.objectMapper.writeValueAsString(pojo)
        } catch (e: JsonProcessingException) {
            throw JsonRuntimeException(e)
        }

    }


    fun <T> readValue(string: String, responseClass: Class<T>): T {

        try {
            return this.objectMapper.readValue(string, responseClass)
        } catch (e: IOException) {
            throw JsonRuntimeException(e)
        }

    }


    @Suppress("UNCHECKED_CAST")
    fun readObjectAsMap(json: String): Map<String, Any?> {

        try {
            return this.objectMapper.readValue(json, HashMap::class.java) as Map<String, Any?>
        } catch (e: IOException) {
            throw JsonRuntimeException(e)
        }

    }


    fun readTree(json: String): JsonNode {

        try {
            return this.objectMapper.readTree(json)
        } catch (e: IOException) {
            throw JsonRuntimeException(e)
        }

    }


}
