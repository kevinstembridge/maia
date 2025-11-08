package org.maiaframework.gen

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testng.annotations.BeforeClass


@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
class AbstractIntegrationTest: AbstractTestNGSpringContextTests() {


    private val objectMapper = ObjectMapper()

    @Autowired
    private lateinit var webAppContext: WebApplicationContext

    protected lateinit var mockMvc: MockMvc


    protected fun json(map: Map<String, Any?>): String = this.objectMapper.writeValueAsString(map)


    @BeforeClass
    fun configureMockMvcBeforeClass() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()

    }


}
