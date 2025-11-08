package org.maiaframework.webtesting

import org.springframework.core.env.Environment
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap


class UrlHelper(private val env: Environment) {


    fun url(
            url: String,
            pathParams: List<String> = emptyList(),
            queryParams: MultiValueMap<String, Any> = LinkedMultiValueMap()
    ): String {

        val urlWithHostAndPort = urlWithHostAndPort(url)
        val pathParamsFormatted = formatPathParams(pathParams)
        val queryParamsFormatted = formatQueryParams(queryParams)
        return "$urlWithHostAndPort$pathParamsFormatted$queryParamsFormatted"

    }


    private fun urlWithHostAndPort(url: String): String {

        val urlWithHostAndPort = if (url.startsWith("/")) {
            val port: String = this.env.getProperty("local.server.port", "8080")
            "http://localhost:$port$url"
        } else {
            url
        }

        return urlWithHostAndPort

    }


    private fun formatPathParams(pathParams: List<String>): String {

        return if (pathParams.isEmpty()) {
            ""
        } else {
            pathParams.joinToString(prefix = "/", separator = "/")
        }

    }


    private fun formatQueryParams(queryParams: MultiValueMap<String, Any>): String {

        val queryParamPairs = queryParams.map { it.toPair() }

        val flatMap: List<Pair<String, Any>> = queryParamPairs.flatMap { pair ->
            val paramName = pair.first
            val paramValues = pair.second
            paramValues.map { Pair(paramName, it) }
        }

        return if (flatMap.isEmpty()) {
            return ""
        } else {
            flatMap.joinToString(prefix = "?", separator = "=")
        }

    }


}
