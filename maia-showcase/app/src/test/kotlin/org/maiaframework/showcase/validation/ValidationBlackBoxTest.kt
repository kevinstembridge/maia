package org.maiaframework.showcase.validation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user

class ValidationBlackBoxTest : AbstractBlackBoxTest() {


    @Test
    @Disabled
    fun `test all validators`() {

        // TODO Create a DTO that has invalid values for all validators

        val requestBody = """
            {
                "someBoolean": null
            
            }
        """.trimIndent()



        val csrfCookie = `fetch CSRF cookie`()

        assertThat(
            mockMvc.post().uri("/api/all_field_types/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("X-XSRF-TOKEN", csrfCookie.value)
                .with(user("nigel").roles("ADMIN"))
                .cookie(csrfCookie)
                .exchange()
        ).debug()
            .bodyJson().isEqualTo(
                "{}"
            )

    }



}
