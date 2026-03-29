package org.maiaframework.showcase

import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.maiaframework.domain.LifecycleState
import org.maiaframework.domain.party.FirstName
import org.maiaframework.domain.party.LastName
import org.maiaframework.jdbc.JdbcOps
import org.maiaframework.showcase.testing.fixtures.Fixtures
import org.maiaframework.showcase.user.UserDao
import org.maiaframework.showcase.user.UserEntity
import org.maiaframework.showcase.user.UserEntityMeta
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.postgresql.SingletonPostgresqlContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.security.web.FilterChainProxy
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.security.web.csrf.CsrfTokenRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest(
//    classes = [MaiaShowcaseTestConfiguration::class],
    properties = ["debug=false"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("test")
@EnableAutoConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class AbstractBlackBoxTest {


    @Autowired
    private lateinit var jsonMapper: JsonMapper


    @Autowired
    private lateinit var webAppContext: WebApplicationContext


    @Autowired
    protected lateinit var jdbcOps: JdbcOps


    @Autowired
    private lateinit var userDao: UserDao


    @Autowired
    protected lateinit var fixtures: Fixtures


    protected lateinit var mockMvc: MockMvcTester


    protected lateinit var defaultUser: UserEntity


    protected fun asJson(any: Any): String = this.jsonMapper.writeValueAsString(any)


    @BeforeEach
    fun configureMockMvcBeforeClass() {

        this.mockMvc = MockMvcTester.from(webAppContext) { builder: DefaultMockMvcBuilder ->
            builder.apply<DefaultMockMvcBuilder>(springSecurity()).build()
        }

        this.defaultUser = getOrInsertDefaultCreatedByUser()

    }


    private fun getOrInsertDefaultCreatedByUser(): UserEntity {

        val existing = this.userDao.findByPrimaryKeyOrNull(Anys.defaultCreatedById)

        if (existing != null) {
            return existing
        }

        val user = UserEntity(
            emptyList(),
            createdById = null,
            Instant.now().truncatedTo(ChronoUnit.MILLIS),
            "Some display Name",
            "password",
            FirstName("Nigel"),
            Anys.defaultCreatedById,
            lastModifiedById = null,
            Instant.now(),
            LastName("Nigelson"),
            LifecycleState.ACTIVE,
            version = 1L
        )

        try {
            this.userDao.insert(user)
        } catch (_: DuplicateKeyException) {
            // Should never happen
        }

        return user

    }


    protected fun assertThat_POST(
        path: String,
        requestBody: String,
    ): MvcTestResultAssert {

        val csrfCookie = `fetch CSRF cookie`()

        return assertThat(
            mockMvc.post().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("X-XSRF-TOKEN", csrfCookie.value)
                .with(user("nigel").roles("ADMIN"))
                .cookie(csrfCookie)
                .exchange()
        ).debug()

    }


    protected fun `fetch CSRF cookie`(): Cookie {

        val result = mockMvc.get().uri("/csrf").exchange()
        return result.response.cookies.first { it.name == "XSRF-TOKEN" }

    }


    protected fun deleteParties() {

        this.jdbcOps.update("delete from ${UserEntityMeta.SCHEMA_AND_TABLE_NAME} where id != '${Anys.defaultCreatedById}'")

    }


    companion object {

        @Container
        @ServiceConnection
        val postgresql = SingletonPostgresqlContainer.instance

    }


}
