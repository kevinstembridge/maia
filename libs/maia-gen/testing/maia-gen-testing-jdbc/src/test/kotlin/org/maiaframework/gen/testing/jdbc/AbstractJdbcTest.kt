package org.maiaframework.gen.testing.jdbc

import tools.jackson.databind.ObjectMapper
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.domain.party.FirstName
import org.maiaframework.domain.party.LastName
import org.maiaframework.gen.testing.sample.user.UserDao
import org.maiaframework.gen.testing.sample.user.UserEntity
import org.maiaframework.gen.testing.sample.user.UserEntityMeta
import org.maiaframework.jdbc.JdbcOps
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.postgresql.SingletonPostgresqlContainer
import org.junit.jupiter.api.BeforeEach
import org.maiaframework.domain.LifecycleState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.dao.DuplicateKeyException
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest(
    classes = [MaiaGenTestConfiguration::class],
    properties = ["debug=false"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@EnableAutoConfiguration
@Testcontainers
abstract class AbstractJdbcTest {


    @Autowired
    private lateinit var objectMapper: ObjectMapper


    @Autowired
    private lateinit var webAppContext: WebApplicationContext


    @Autowired
    private lateinit var jdbcOps: JdbcOps


    @Autowired
    private lateinit var userDao: UserDao


    protected lateinit var mockMvc: MockMvc


    protected lateinit var defaultUser: UserEntity


    protected fun asJson(any: Any): String = this.objectMapper.writeValueAsString(any)


    @BeforeEach
    fun configureMockMvcBeforeClass() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
//            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()

        this.defaultUser = getOrInsertDefaultCreatedByUser()

    }


    private fun getOrInsertDefaultCreatedByUser(): UserEntity {

        val existing = this.userDao.findByPrimaryKeyOrNull(Anys.defaultCreatedById)

        if (existing != null) {
            return existing
        }

        val user = UserEntity(
            Instant.now().truncatedTo(ChronoUnit.MILLIS),
            "Some display Name",
            EmailAddress("bogus@bogus.com"),
            "password",
            FirstName("Nigel"),
            Anys.defaultCreatedById,
            Instant.now(),
            LastName("Nigelson"),
            LifecycleState.ACTIVE,
            emptyList(),
            version = 1L
        )

        try {
            this.userDao.insert(user)
        } catch (e: DuplicateKeyException) {
            // Should never happen
        }

        return user

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
