package org.maiaframework.json

import tools.jackson.core.JacksonException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class JsonFacade(private val objectMapper: JsonMapper) {


    fun writeValueAsString(pojo: Any): String {

        try {
            return this.objectMapper.writeValueAsString(pojo)
        } catch (e: JacksonException) {
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
