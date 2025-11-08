package org.maiaframework.webapp.infra

import org.maiaframework.webapp.http.HttpRequestUtil
import org.maiaframework.webapp.http.RequestHeaderNames
import jakarta.servlet.http.HttpServletRequest
import org.maiaframework.domain.net.IpAddress
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class HttpRequestUtilTest {


    @Test
    @Throws(Exception::class)
    fun testGetRemoteIpAddress_should_pick_first_from_x_forwarded_for_list() {

        val requestMock = Mockito.mock(HttpServletRequest::class.java)
        val expectedIpAddress = "someIpAddress"

        Mockito.`when`(requestMock.getHeader(RequestHeaderNames.X_FORWARDED_FOR)).thenReturn("$expectedIpAddress, someOtherIpAddress")

        val actual = HttpRequestUtil.getRemoteIpAddress(requestMock)

        Assertions.assertThat(actual).isEqualTo(IpAddress(expectedIpAddress))

    }


    @Test
    @Throws(Exception::class)
    fun testGetRemoteIpAddress_should_fall_back_to_remoteAddr() {

        val requestMock = Mockito.mock(HttpServletRequest::class.java)
        val expectedIpAddress = "someRemoteAddress"

        Mockito.`when`(requestMock.remoteAddr).thenReturn(expectedIpAddress)

        val actual = HttpRequestUtil.getRemoteIpAddress(requestMock)

        Assertions.assertThat(actual).isEqualTo(IpAddress(expectedIpAddress))

    }


    @Test
    @Throws(Exception::class)
    fun testGetRemoteIpAddress_should_return_empty() {

        val requestMock = Mockito.mock(HttpServletRequest::class.java)
        val actual = HttpRequestUtil.getRemoteIpAddress(requestMock)
        Assertions.assertThat(actual).isNull()

    }


}
