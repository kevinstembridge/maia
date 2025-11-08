package org.maiaframework.http

import com.fasterxml.jackson.databind.JsonNode
import org.maiaframework.json.JsonFacade
import org.apache.hc.client5.http.fluent.Request
import org.apache.hc.client5.http.fluent.Response
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.Header
import org.apache.hc.core5.http.NameValuePair
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.UncheckedIOException
import java.net.URI
import java.net.URL
import java.nio.charset.Charset


@Component
class HttpClientFacade(private val jsonFacade: JsonFacade) {


    fun get(uri: URI) {

        val response = execute(getRequestFor(uri))

        val httpResponse = response.returnResponse()
        val statusCode = httpResponse.code
        val reason = httpResponse.reasonPhrase

        if (statusCode >= 300) {
            throw ResponseStatusException(statusCode, reason)
        }

    }


    fun getString(url: URL, vararg headers: Header): String {

        try {
            val response = execute(Request.get(url.toExternalForm()), *headers)

            val httpResponse = response.returnResponse()
            val statusCode = httpResponse.code
            val reason = httpResponse.reasonPhrase

            if (statusCode >= 300) {
                throw ResponseStatusException(statusCode, reason)
            }

            return response.returnContent().toString()

        } catch (e: IOException) {
            throw RuntimeException("TODO handle this properly", e)
        }

    }


    fun <T> post(uri: URI, requestBodyJavaBean: Any, responseClass: Class<T>): T {

        return execute(postRequestFor(uri), requestBodyJavaBean, responseClass)

    }


    fun post(uri: URI) {

        try {
            val response = postWithJsonString(uri)
            val httpResponse = response.returnResponse()
            val statusCode = httpResponse.code
            val reason = httpResponse.reasonPhrase

            if (statusCode >= 300) {
                throw ResponseStatusException(statusCode, reason)
            }

        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    }


    fun postAndReturnString(uri: URI): String {

        return executeRequestAndReturnString(postRequestFor(uri))

    }


    fun post(uri: URI, requestJson: String): String {

        require(requestJson.isNotBlank()) { "requestJson must not be blank" }

        return executeRequestAndReturnString(postRequestFor(uri), requestJson)

    }


    fun post(uri: URI, requestJson: String, vararg headers: Header): String {

        require(requestJson.isNotBlank()) { "requestJson must not be blank" }

        return executeRequestAndReturnString(postRequestFor(uri), requestJson, *headers)

    }


    fun put(uri: URI, requestJson: String, vararg headers: Header): String {

        require(requestJson.isNotBlank()) { "requestJson must not be blank" }

        return executeRequestAndReturnString(putRequestFor(uri), requestJson, *headers)

    }


    fun postWithJsonFromPojo(uri: URI, requestBodyJavaBean: Any, vararg headers: Header): Response {

        val requestJson = this.jsonFacade.writeValueAsString(requestBodyJavaBean)
        return postWithJsonString(uri, requestJson, *headers)

    }


    fun putWithJsonFromPojo(uri: URI, requestBodyJavaBean: Any, vararg headers: Header): Response {

        val requestJson = this.jsonFacade.writeValueAsString(requestBodyJavaBean)
        return putWithJsonString(uri, requestJson, *headers)

    }


    fun postForm(uri: URI, charset: Charset, nameValuePairs: List<NameValuePair>): Response {

        try {
            return postRequestFor(uri).bodyForm(nameValuePairs, charset).execute()
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    }


    private fun execute(request: Request, vararg headers: Header): Response {

        return execute(request, null, *headers)

    }


    private fun <T> execute(request: Request, requestBodyJavaBean: Any, responseClass: Class<T>, vararg headers: Header): T {

        val requestJson = this.jsonFacade.writeValueAsString(requestBodyJavaBean)
        val responseString = executeRequestAndReturnString(request, requestJson, *headers)

        return this.jsonFacade.readValue(responseString, responseClass)

    }


    private fun executeRequestAndReturnString(request: Request, requestJson: String? = null, vararg headers: Header): String {

        try {
            val response = execute(request, requestJson, *headers)

            val httpResponse = response.returnResponse()
            val statusCode = httpResponse.code
            val reason = httpResponse.reasonPhrase

            if (statusCode >= 300) {
                throw ResponseStatusException(statusCode, reason)
            }

            return response.returnContent().toString()

        } catch (e: IOException) {
            throw RuntimeException("TODO handle this properly", e)
        }

    }


    fun getWithJsonString(uri: URI, requestJson: String? = null, vararg headers: Header): Response {

        return execute(getRequestFor(uri), requestJson, *headers)

    }


    fun postWithJsonString(uri: URI, requestJson: String? = null, vararg headers: Header): Response {

        return execute(postRequestFor(uri), requestJson, *headers)

    }


    fun putWithJsonString(uri: URI, requestJson: String? = null, vararg headers: Header): Response {

        return execute(putRequestFor(uri), requestJson, *headers)

    }


    fun deleteWithJsonString(uri: URI, requestJson: String? = null, vararg headers: Header): Response {

        return execute(deleteRequestFor(uri), requestJson, *headers)

    }


    fun getWithJsonNode(uri: URI, requestJson: JsonNode? = null, vararg headers: Header): Response {

        return execute(getRequestFor(uri), requestJson?.let( { this.jsonFacade.writeValueAsString(it) }), *headers)

    }


    fun postWithJsonNode(uri: URI, requestJson: JsonNode? = null, vararg headers: Header): Response {

        return execute(postRequestFor(uri), requestJson?.let( { this.jsonFacade.writeValueAsString(it) }), *headers)

    }


    fun putWithJsonNode(uri: URI, requestJson: JsonNode?, vararg headers: Header): Response {

        return execute(putRequestFor(uri), requestJson?.let( { this.jsonFacade.writeValueAsString(it) }), *headers)

    }


    fun deleteWithJsonNode(uri: URI, requestJson: JsonNode?, vararg headers: Header): Response {

        return execute(deleteRequestFor(uri), requestJson?.let( { this.jsonFacade.writeValueAsString(it) }), *headers)

    }


    private fun execute(request: Request, requestJson: String? = null, vararg headers: Header): Response {

        try {

            requestJson?.let { jsonBody -> request.bodyString(jsonBody, ContentType.APPLICATION_JSON) }

            for (header in headers) {
                request.addHeader(header)
            }

            return request.execute()

        } catch (e: IOException) {
            throw RuntimeException("TODO handle this properly", e)
        }

    }


    private fun getRequestFor(uri: URI): Request {

        return Request.get(uri)

    }


    private fun postRequestFor(uri: URI): Request {

        return Request.post(uri)

    }


    private fun putRequestFor(uri: URI): Request {

        return Request.put(uri)

    }


    private fun deleteRequestFor(uri: URI): Request {

        return Request.delete(uri)

    }


}
