package org.maiaframework.toggles

import tools.jackson.databind.json.JsonMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.maiaframework.testing.postgresql.SingletonPostgresqlContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class AbstractBlackBoxTest {


    protected lateinit var mockMvc: MockMvcTester


    @Autowired
    private lateinit var context: WebApplicationContext


    @Autowired
    private lateinit var jsonMapper: JsonMapper


    @BeforeAll
    fun beforeAll() {

        this.mockMvc = MockMvcTester.create(
            webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()
        )

    }


    protected fun asJson(obj: Any): String {

        return this.jsonMapper.writeValueAsString(obj)

    }


    companion object {


        @Container
        @ServiceConnection
        val postgresqlContainer = SingletonPostgresqlContainer.instance.also { it.start() }


        @DynamicPropertySource
        fun postgresqlProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl)
            registry.add("spring.datasource.password", postgresqlContainer::getPassword)
            registry.add("spring.datasource.username", postgresqlContainer::getUsername)
        }


    }


}
