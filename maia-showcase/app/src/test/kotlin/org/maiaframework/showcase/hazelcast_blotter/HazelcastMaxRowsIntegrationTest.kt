package org.maiaframework.showcase.hazelcast_blotter

import com.hazelcast.core.HazelcastInstance
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.join.AlphaCreateRequestDto
import org.maiaframework.showcase.join.AlphaCrudService
import org.maiaframework.showcase.join.AlphaEntityMeta
import org.maiaframework.showcase.join.BravoCreateRequestDto
import org.maiaframework.showcase.join.BravoCrudService
import org.maiaframework.showcase.join.BravoHzCappedDto
import org.maiaframework.showcase.join.BravoHzCappedDtoHazelcastRebuildService
import org.maiaframework.showcase.join.BravoHzCappedDtoHazelcastRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.TestSecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import tools.jackson.databind.json.JsonMapper


/**
 * Black-box tests of the `maxRows` guard of the Hazelcast-backed Blotter `BravoHzCapped` (limit 20): a set larger
 * than the limit fails population with a message naming the Searchable DTO and the limit, never a truncated set.
 */
@WithMockUser(authorities = ["WRITE"])
class HazelcastMaxRowsIntegrationTest : AbstractBlackBoxTest() {


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
    private lateinit var cappedRepo: BravoHzCappedDtoHazelcastRepo


    @Autowired
    private lateinit var rebuildService: BravoHzCappedDtoHazelcastRebuildService


    @BeforeEach
    fun resetDatabase() {

        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        clearGlobalPopulation()

    }


    @AfterEach
    fun restoreState() {

        clearGlobalPopulation()
        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        restart()

    }


    @Test
    fun `a set exactly at the limit is populated`() {

        createBravos(20)

        restart()

        assertThat(cappedRepo.findAll()).hasSize(20)

    }


    @Test
    fun `eager population fails with a message naming the dto and the limit when the set exceeds it`() {

        createBravos(21)

        assertThatThrownBy { restart() }
            .hasStackTraceContaining("BravoHzCappedDto")
            .hasStackTraceContaining("maxRows")
            .hasStackTraceContaining("20")

        assertThat(mapSize()).isZero()

    }


    @Test
    fun `a lazy fetch over the limit returns an error with the same message and no rows, and retries on the next fetch`() {

        setGlobalPopulation("lazy")
        createBravos(21)
        restart()

        val failed = fetchAll()

        assertThat(failed.status).isGreaterThanOrEqualTo(400)
        assertThat(failed.body).contains("BravoHzCappedDto").contains("maxRows").contains("20")
        assertThat(failed.body).doesNotContain("dtoStringFromBravo")
        assertThat(mapSize()).isZero()

        jdbcOps.update("delete from ${org.maiaframework.showcase.join.BravoEntityMeta.SCHEMA_AND_TABLE_NAME} where some_string = 'bravo-0'")

        val retried = fetchAll()

        assertThat(retried.status).isEqualTo(200)
        assertThat(jsonMapper.readTree(retried.body).toList()).hasSize(20)

    }


    @Test
    fun `rebuild respects the limit`() {

        createBravos(21)

        assertThatThrownBy { rebuildService.rebuild() }
            .hasStackTraceContaining("BravoHzCappedDto")
            .hasStackTraceContaining("20")

        assertThat(mapSize()).isZero()

    }


    private class Response(val status: Int, val body: String)


    private fun fetchAll(): Response {

        val result = mockMvc.get().uri("/api/bravo-hz-capped-blotter/find-all")
            .with(user("nigel").authorities(SimpleGrantedAuthority("WRITE")))
            .exchange()

        // MockMvc's security filter chain clears the thread's SecurityContext; restore the @WithMockUser one
        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext())

        return Response(result.response.status, result.response.contentAsString)

    }


    private fun restart() {

        applicationContext.publishEvent(ContextRefreshedEvent(applicationContext))

    }


    private fun mapSize(): Int {

        return hazelcastInstance.getMap<DomainId, BravoHzCappedDto>("bravo_hz_capped_blotter").size

    }


    private fun setGlobalPopulation(value: String) {

        (applicationContext.environment as ConfigurableEnvironment).propertySources
            .addFirst(MapPropertySource(PROPERTY_SOURCE_NAME, mapOf("maia.hazelcast-blotter.population" to value)))

    }


    private fun clearGlobalPopulation() {

        (applicationContext.environment as ConfigurableEnvironment).propertySources.remove(PROPERTY_SOURCE_NAME)

    }


    private fun createBravos(count: Int) {

        val alpha = alphaCrudService.create(AlphaCreateRequestDto(someInt_raw = 1, someString_raw = "alpha"))

        repeat(count) {
            bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = it, someString_raw = "bravo-$it"))
        }

    }


    private companion object {

        const val PROPERTY_SOURCE_NAME = "hazelcast-max-rows-test"

    }


}
