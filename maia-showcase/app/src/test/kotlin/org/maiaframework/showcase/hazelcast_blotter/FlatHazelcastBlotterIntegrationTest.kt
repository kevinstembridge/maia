package org.maiaframework.showcase.hazelcast_blotter

import com.hazelcast.core.HazelcastInstance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.join.AlphaCreateRequestDto
import org.maiaframework.showcase.join.AlphaCrudService
import org.maiaframework.showcase.join.AlphaEntity
import org.maiaframework.showcase.join.AlphaEntityMeta
import org.maiaframework.showcase.join.BravoCreateRequestDto
import org.maiaframework.showcase.join.BravoCrudService
import org.maiaframework.showcase.join.BravoEntity
import org.maiaframework.showcase.join.BravoHzDto
import org.maiaframework.showcase.join.BravoHzDtoHazelcastRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.TestSecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper


/**
 * Black-box tests of the flat Hazelcast-backed Blotter `BravoHz`: the map is filled from the database when the
 * application starts, and a single GET returns every row.
 *
 * The application context starts once for the whole test class, so the start-up population is exercised by publishing
 * the same [ContextRefreshedEvent] the application context publishes when it has started.
 */
@WithMockUser(authorities = ["WRITE"])
class FlatHazelcastBlotterIntegrationTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var applicationContext: ApplicationContext


    @Autowired
    private lateinit var hazelcastInstance: HazelcastInstance


    @Autowired
    private lateinit var jsonMapper: JsonMapper


    @Autowired
    private lateinit var alphaCrudService: AlphaCrudService


    @Autowired
    private lateinit var bravoCrudService: BravoCrudService


    @Autowired
    private lateinit var bravoHzRepo: BravoHzDtoHazelcastRepo


    @BeforeEach
    fun resetDatabase() {

        // cascades to bravo
        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        bravoHzRepo.populate()

    }


    @Test
    fun `application start populates the map and the fetch-all endpoint returns every row`() {

        val alpha = createAlpha()
        val bravos = listOf(
            createBravo(alpha, someString = "bravo-one", someInt = 1),
            createBravo(alpha, someString = "bravo-two", someInt = 2),
            createBravo(alpha, someString = "bravo-three", someInt = 3),
        )

        // the application context publishes this when it has started
        applicationContext.publishEvent(ContextRefreshedEvent(applicationContext))

        val rows = fetchAll()

        assertThat(rows).hasSize(3)
        assertThat(rows.map { it["id"].asString() }).containsExactlyInAnyOrderElementsOf(bravos.map { it.id.value })
        val byId = rows.associateBy { it["id"].asString() }
        assertThat(byId.getValue(bravos[1].id.value)["dtoStringFromBravo"].asString()).isEqualTo("bravo-two")
        assertThat(byId.getValue(bravos[1].id.value)["dtoIntFromBravo"].asInt()).isEqualTo(2)
        assertThat(byId.getValue(bravos[1].id.value)["createdTimestamp"].asString()).isNotBlank()

    }


    @Test
    fun `an empty table gives an empty set`() {

        applicationContext.publishEvent(ContextRefreshedEvent(applicationContext))

        assertThat(fetchAll()).isEmpty()

    }


    @Test
    fun `population rebuilds the map from the database and drops stale documents`() {

        val alpha = createAlpha()
        val kept = createBravo(alpha, someString = "kept")
        val deleted = createBravo(alpha, someString = "deleted")
        applicationContext.publishEvent(ContextRefreshedEvent(applicationContext))
        assertThat(fetchAll()).hasSize(2)

        bravoCrudService.delete(deleted.id)
        applicationContext.publishEvent(ContextRefreshedEvent(applicationContext))

        assertThat(fetchAll().map { it["id"].asString() }).containsExactly(kept.id.value)

    }


    @Test
    fun `the documents live in the application's Hazelcast instance`() {

        val alpha = createAlpha()
        val bravo = createBravo(alpha, someString = "in-hazelcast", someInt = 7)
        applicationContext.publishEvent(ContextRefreshedEvent(applicationContext))

        // what the application returns, through its own interface
        val row = fetchAll().single()
        assertThat(row["id"].asString()).isEqualTo(bravo.id.value)
        assertThat(row["dtoStringFromBravo"].asString()).isEqualTo("in-hazelcast")
        assertThat(row["dtoIntFromBravo"].asInt()).isEqualTo(7)

        // and where it is kept; polled because the test classes of this package share one Hazelcast instance
        assertThat(hazelcastInstance.config.mapConfigs).containsKey("bravo_hz_blotter")
        val map = hazelcastInstance.getMap<DomainId, BravoHzDto>("bravo_hz_blotter")
        val stored = eventually { map[bravo.id] }
        assertThat(stored.dtoStringFromBravo).isEqualTo("in-hazelcast")
        assertThat(stored.dtoIntFromBravo).isEqualTo(7)

    }


    @Test
    fun `there is no count endpoint and no server-side search endpoint`() {

        val mappings = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping::class.java)
            .handlerMethods.keys
            .filter { it.patternValues.any { p -> p.startsWith("/api/bravo-hz-blotter") } }

        assertThat(mappings.map { it.patternValues.single() to it.methodsCondition.methods.single().name })
            .containsExactly("/api/bravo-hz-blotter/find-all" to "GET")

    }


    private fun <T : Any> eventually(timeoutMillis: Long = 5_000, read: () -> T?): T {

        val deadline = System.nanoTime() + timeoutMillis * 1_000_000

        while (true) {
            read()?.let { return it }
            check(System.nanoTime() < deadline) { "Timed out after ${timeoutMillis}ms waiting for a value" }
            Thread.sleep(50)
        }

    }


    private fun fetchAll(): List<JsonNode> {

        val result = mockMvc.get().uri("/api/bravo-hz-blotter/find-all")
            .with(user("nigel").authorities(SimpleGrantedAuthority("WRITE")))
            .exchange()

        // MockMvc's security filter chain clears the thread's SecurityContext; restore the @WithMockUser one
        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext())

        assertThat(result.response.status).isEqualTo(200)

        return jsonMapper.readTree(result.response.contentAsString).toList()

    }


    private fun createAlpha(): AlphaEntity {

        return alphaCrudService.create(AlphaCreateRequestDto(someInt_raw = 1, someString_raw = "alpha"))

    }


    private fun createBravo(alpha: AlphaEntity, someString: String, someInt: Int = 1): BravoEntity {

        return bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = someInt, someString_raw = someString))

    }


}
