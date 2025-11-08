package org.maiaframework.webapp.http

import jakarta.servlet.http.HttpServletRequest
import org.maiaframework.domain.net.IpAddress

object HttpRequestUtil {


    fun getRemoteIpAddress(request: HttpServletRequest): IpAddress? {

        val rawXForwardedFor: String? = request.getHeader(RequestHeaderNames.X_FORWARDED_FOR)

        return if (rawXForwardedFor.isNullOrBlank()) {
            request.remoteAddr?.let { IpAddress(it) }
        } else {

            val ipAddressRaw = rawXForwardedFor.split(",".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                    .first()

            IpAddress(ipAddressRaw)
        }

    }


}
