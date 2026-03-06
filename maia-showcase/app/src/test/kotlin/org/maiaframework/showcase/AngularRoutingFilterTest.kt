package org.maiaframework.showcase

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


class AngularRoutingFilterTest : AbstractBlackBoxTest() {


    @Test
    fun `when angular navigation request then permit forward to root`() {

        mockMvc.get("/some-angular-route") {
            header("Sec-Fetch-Mode", "navigate")
        }.andExpect {
            status { isOk() }
        }

    }


    @Test
    fun `test any other route should be denied for anonymous`() {

        mockMvc.perform(MockMvcRequestBuilders.get("/some-other-route"))
            .andExpect(status().isForbidden)

    }


}
