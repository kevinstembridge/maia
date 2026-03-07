package org.maiaframework.showcase

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus


class AngularRoutingFilterTest : AbstractBlackBoxTest() {


    @Test
    fun `when angular navigation request then permit forward to root`() {

        assertThat(
            mockMvc.get().uri("/some-angular-route")
                .header("Sec-Fetch-Mode", "navigate")
                .exchange()
        ).hasStatusOk()

    }


    @Test
    fun `test any other route should be denied for anonymous`() {

        assertThat(mockMvc.get().uri("/some-other-route").exchange())
            .hasStatus(HttpStatus.FORBIDDEN)

    }


}
