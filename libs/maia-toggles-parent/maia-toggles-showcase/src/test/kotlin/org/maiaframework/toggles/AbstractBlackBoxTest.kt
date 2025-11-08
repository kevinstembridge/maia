package org.maiaframework.toggles

import com.fasterxml.jackson.databind.ObjectMapper
import org.maiaframework.testing.postgresql.SingletonPostgresqlContainer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class AbstractBlackBoxTest {


    protected lateinit var mockMvc: MockMvc


    @Autowired
    private lateinit var context: WebApplicationContext


    @Autowired
    private lateinit var objectMapper: ObjectMapper


    @BeforeAll
    fun beforeAll() {

        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(this.context)
            .build()

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


    protected fun asJson(obj: Any): String {

        return this.objectMapper.writeValueAsString(obj)

    }


}
