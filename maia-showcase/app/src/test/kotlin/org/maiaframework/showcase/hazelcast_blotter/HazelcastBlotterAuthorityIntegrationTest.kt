package org.maiaframework.showcase.hazelcast_blotter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.TestSecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user


/**
 * Black-box tests of the optional authority on a Hazelcast-backed Blotter's fetch-all endpoint: `BravoHzSecured`
 * declares one, `BravoHz` does not.
 */
@WithMockUser(authorities = ["SOMETHING_ELSE"])
class HazelcastBlotterAuthorityIntegrationTest : AbstractBlackBoxTest() {


    @Test
    fun `a DTO with an authority refuses a user without it`() {

        assertThat(statusOf("/api/bravo-hz-secured-blotter/find-all", "SOMETHING_ELSE")).isEqualTo(403)

    }


    @Test
    fun `a DTO with an authority serves a user with it`() {

        assertThat(statusOf("/api/bravo-hz-secured-blotter/find-all", "WRITE")).isEqualTo(200)

    }


    @Test
    fun `a DTO with no authority serves any authenticated user`() {

        assertThat(statusOf("/api/bravo-hz-blotter/find-all", "SOMETHING_ELSE")).isEqualTo(200)

    }


    private fun statusOf(url: String, authority: String): Int {

        val result = mockMvc.get().uri(url)
            .with(user("nigel").authorities(SimpleGrantedAuthority(authority)))
            .exchange()

        // MockMvc's security filter chain clears the thread's SecurityContext; restore the @WithMockUser one
        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext())

        return result.response.status

    }


}
