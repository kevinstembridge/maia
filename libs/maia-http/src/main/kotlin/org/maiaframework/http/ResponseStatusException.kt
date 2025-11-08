package org.maiaframework.http

class ResponseStatusException(val statusCode: Int, val reason: String) : RuntimeException("responseStatus=$statusCode, reason=$reason")
