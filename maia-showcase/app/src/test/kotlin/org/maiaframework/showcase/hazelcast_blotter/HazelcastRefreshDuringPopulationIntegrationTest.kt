package org.maiaframework.showcase.hazelcast_blotter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.join.AlphaCreateRequestDto
import org.maiaframework.showcase.join.AlphaCrudService
import org.maiaframework.showcase.join.AlphaEntityMeta
import org.maiaframework.showcase.join.BravoCreateRequestDto
import org.maiaframework.showcase.join.BravoCrudService
import org.maiaframework.showcase.join.BravoHzDto
import org.maiaframework.showcase.join.BravoHzDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoHzDtoRepo
import org.maiaframework.showcase.join.BravoUpdate_someStringRequestDto
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * Black-box tests of a write that commits while the Hazelcast map of `BravoHz` is being (re)populated: the refresh
 * must not be lost, otherwise the population's older snapshot would leave a deleted row (a ghost) or a stale value
 * in the map. The population is held mid-stream by a spy on the Searchable DTO repo, which stands in for a slow
 * database read.
 */
@WithMockUser(authorities = ["WRITE"])
class HazelcastRefreshDuringPopulationIntegrationTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var alphaCrudService: AlphaCrudService


    @Autowired
    private lateinit var bravoCrudService: BravoCrudService


    @MockitoSpyBean
    private lateinit var dtoRepo: BravoHzDtoRepo


    @Autowired
    private lateinit var hazelcastRepo: BravoHzDtoHazelcastRepo


    private val executor = Executors.newSingleThreadExecutor()


    @BeforeEach
    fun resetDatabase() {

        Mockito.reset(dtoRepo)
        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        hazelcastRepo.populate()

    }


    @AfterEach
    fun restore() {

        Mockito.reset(dtoRepo)
        hazelcastRepo.populate()

    }


    @Test
    fun `a row deleted while the map is being populated is not left in the map`() {

        val alpha = alphaCrudService.create(AlphaCreateRequestDto(someInt_raw = 1, someString_raw = "alpha"))
        val doomed = bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = 1, someString_raw = "doomed"))
        bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = 2, someString_raw = "kept"))

        val population = populateHeldMidStream()

        bravoCrudService.delete(doomed.id)

        population.finish()

        assertThat(hazelcastRepo.findAll().map { it.dtoStringFromBravo }).containsExactly("kept")

    }


    @Test
    fun `a row updated while the map is being populated shows the new value`() {

        val alpha = alphaCrudService.create(AlphaCreateRequestDto(someInt_raw = 1, someString_raw = "alpha"))
        val bravo = bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = 1, someString_raw = "before"))

        val population = populateHeldMidStream()

        bravoCrudService.updateSomeString(BravoUpdate_someStringRequestDto(id_raw = bravo.id, someString_raw = "after"))

        population.finish()

        assertThat(hazelcastRepo.findAll().map { it.dtoStringFromBravo }).containsExactly("after")

    }


    private class HeldPopulation(
        private val release: CountDownLatch,
        private val done: java.util.concurrent.Future<*>
    ) {

        fun finish() {

            release.countDown()
            done.get(30, TimeUnit.SECONDS)

        }

    }


    /**
     * Starts a population whose database read has delivered its (pre-write) snapshot but not yet finished, and
     * returns once it is in that state.
     */
    private fun populateHeldMidStream(): HeldPopulation {

        val streaming = CountDownLatch(1)
        val release = CountDownLatch(1)

        Mockito.doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val real = invocation.callRealMethod() as Sequence<BravoHzDto>
            val snapshot = real.toList()
            sequence {
                yieldAll(snapshot)
                streaming.countDown()
                check(release.await(30, TimeUnit.SECONDS)) { "population was never released" }
            }
        }.`when`(dtoRepo).findAllAsSequence()

        val done = executor.submit { hazelcastRepo.populate() }

        assertThat(streaming.await(30, TimeUnit.SECONDS)).isTrue()

        return HeldPopulation(release, done)

    }


}
