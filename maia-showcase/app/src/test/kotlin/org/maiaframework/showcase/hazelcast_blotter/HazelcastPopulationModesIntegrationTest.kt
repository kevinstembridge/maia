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
import org.maiaframework.showcase.join.AlphaDao
import org.maiaframework.showcase.join.AlphaEntityTestBuilder
import org.maiaframework.showcase.join.BravoDao
import org.maiaframework.showcase.join.BravoEntityTestBuilder
import org.maiaframework.showcase.join.AlphaEntity
import org.maiaframework.showcase.join.AlphaEntityMeta
import org.maiaframework.showcase.join.BravoCreateRequestDto
import org.maiaframework.showcase.join.BravoCrudService
import org.maiaframework.showcase.join.BravoHzDto
import org.maiaframework.showcase.join.BravoHzDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoHzEagerDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoHzLazyDtoHazelcastRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.security.test.context.support.WithMockUser


/**
 * Black-box tests of the Hazelcast-backed Blotter population modes (ADR-0008): eager fills the map at application
 * start, lazy fills it on the first fetch, and a per-DTO override beats the global property
 * `maia.hazelcast-blotter.population`.
 *
 * The application context starts once for the whole test class, so a restart of the only node of a cluster is simulated
 * by clearing the cluster's Blotter state (a real restart loses it) and publishing the same [ContextRefreshedEvent] the
 * application context publishes when it has started. A repeated event with the state left as it is, and a start of a
 * node whose shape matches a populated map, leave that map alone. The mode is read when that event is
 * handled, so the global property is changed by adding a property source to the live environment.
 */
@WithMockUser(authorities = ["WRITE"])
class HazelcastPopulationModesIntegrationTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var applicationContext: ApplicationContext


    @Autowired
    private lateinit var hazelcastInstance: HazelcastInstance


    @Autowired
    private lateinit var alphaCrudService: AlphaCrudService


    @Autowired
    private lateinit var alphaDao: AlphaDao


    @Autowired
    private lateinit var bravoDao: BravoDao


    @Autowired
    private lateinit var bravoCrudService: BravoCrudService


    @Autowired
    private lateinit var bravoHzRepo: BravoHzDtoHazelcastRepo


    @Autowired
    private lateinit var lazyRepo: BravoHzLazyDtoHazelcastRepo


    @Autowired
    private lateinit var eagerRepo: BravoHzEagerDtoHazelcastRepo


    @BeforeEach
    fun resetDatabase() {

        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        clearGlobalPopulation()

    }


    @AfterEach
    fun restoreGlobalDefault() {

        clearGlobalPopulation()

        // leave every map as the default (eager) start-up leaves it, for the test classes that share this context
        restart()

    }


    @Test
    fun `with no property set population is eager`() {

        createBravos(3)

        restart()

        assertThat(mapSize("bravo_hz_blotter")).isEqualTo(3)
        assertThat(bravoHzRepo.findAll()).hasSize(3)

    }


    @Test
    fun `lazy defers population to the first fetch which returns the full set`() {

        setGlobalPopulation("lazy")
        createBravos(3)

        restart()

        assertThat(mapSize("bravo_hz_blotter")).isZero()
        assertThat(bravoHzRepo.findAll()).hasSize(3)
        assertThat(mapSize("bravo_hz_blotter")).isEqualTo(3)

    }


    @Test
    fun `lazy populates once not on every fetch`() {

        setGlobalPopulation("lazy")
        createBravos(2)
        restart()
        assertThat(bravoHzRepo.findAll()).hasSize(2)

        insertBravoWithoutNotification()

        assertThat(bravoHzRepo.findAll()).hasSize(2)

    }


    @Test
    fun `a restart rebuilds a lazy map from the database`() {

        setGlobalPopulation("lazy")
        createBravos(2)
        restart()
        assertThat(bravoHzRepo.findAll()).hasSize(2)

        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        createBravos(1)
        restart()

        assertThat(bravoHzRepo.findAll()).hasSize(1)

    }


    @Test
    fun `a restart rebuilds an eager map from the database`() {

        createBravos(2)
        restart()
        assertThat(bravoHzRepo.findAll()).hasSize(2)

        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        createBravos(4)
        restart()

        assertThat(bravoHzRepo.findAll()).hasSize(4)

    }


    @Test
    fun `a repeated eager start-up event leaves a populated same-shape map alone`() {

        createBravos(2)
        restart()
        insertBravoWithoutNotification()

        publishStartUpEvent()

        assertThat(bravoHzRepo.findAll()).hasSize(2)

    }


    @Test
    fun `a repeated lazy start-up event does not wipe a populated same-shape map`() {

        setGlobalPopulation("lazy")
        createBravos(2)
        restart()
        assertThat(bravoHzRepo.findAll()).hasSize(2)
        insertBravoWithoutNotification()

        publishStartUpEvent()

        assertThat(mapSize("bravo_hz_blotter")).isEqualTo(2)
        assertThat(bravoHzRepo.findAll()).hasSize(2)

    }


    @Test
    fun `a per-dto lazy override beats an eager global default`() {

        createBravos(2)

        restart()

        assertThat(mapSize("bravo_hz_blotter")).isEqualTo(2)
        assertThat(mapSize("bravo_hz_lazy_blotter")).isZero()
        assertThat(lazyRepo.findAll()).hasSize(2)

    }


    @Test
    fun `a per-dto eager override beats a lazy global setting`() {

        setGlobalPopulation("lazy")
        createBravos(2)

        restart()

        assertThat(mapSize("bravo_hz_blotter")).isZero()
        assertThat(mapSize("bravo_hz_eager_blotter")).isEqualTo(2)
        assertThat(eagerRepo.findAll()).hasSize(2)

    }


    @Test
    fun `an invalid population value fails start-up naming the property and the valid values`() {

        setGlobalPopulation("sometimes")

        assertThatThrownBy { restart() }
            .hasStackTraceContaining("maia.hazelcast-blotter.population")
            .hasStackTraceContaining("sometimes")
            .hasStackTraceContaining("eager|lazy")

    }


    /** A restart of the only node of a cluster: the cluster's memory is gone, then the start-up event is handled. */
    private fun restart() {

        HazelcastBlotterTestState.clearAll(hazelcastInstance)
        publishStartUpEvent()

    }


    /** The event the application context publishes when it has started, with the cluster's state as it is. */
    private fun publishStartUpEvent() {

        applicationContext.publishEvent(ContextRefreshedEvent(applicationContext))

    }


    private fun mapSize(name: String): Int {

        return hazelcastInstance.getMap<DomainId, BravoHzDto>(name).size

    }


    private fun setGlobalPopulation(value: String) {

        (applicationContext.environment as ConfigurableEnvironment).propertySources
            .addFirst(MapPropertySource(PROPERTY_SOURCE_NAME, mapOf("maia.hazelcast-blotter.population" to value)))

    }


    private fun clearGlobalPopulation() {

        (applicationContext.environment as ConfigurableEnvironment).propertySources.remove(PROPERTY_SOURCE_NAME)

    }


    private fun createBravos(count: Int) {

        val alpha: AlphaEntity = alphaCrudService.create(AlphaCreateRequestDto(someInt_raw = 1, someString_raw = "alpha"))

        repeat(count) {
            bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = it, someString_raw = "bravo-$it"))
        }

    }


    /** Straight through the DAOs, so no CrudListener event refreshes the map: only a population can show the row. */
    private fun insertBravoWithoutNotification() {

        val alpha = AlphaEntityTestBuilder().build().also { alphaDao.insert(it) }
        bravoDao.insert(BravoEntityTestBuilder(alphaId = alpha.id).build())

    }


    private companion object {

        const val PROPERTY_SOURCE_NAME = "hazelcast-population-test"

    }


}
