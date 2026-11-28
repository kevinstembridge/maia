package org.maiaframework.showcase.hazelcast_blotter

import com.hazelcast.config.Config
import com.hazelcast.config.YamlConfigBuilder
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.common.BlotterTooLargeException
import org.maiaframework.domain.DomainId
import org.maiaframework.hazelcast.population.PopulationLockLostException
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.hazelcast.HazelcastConfig
import org.maiaframework.showcase.join.AlphaCreateRequestDto
import org.maiaframework.showcase.join.AlphaCrudService
import org.maiaframework.showcase.join.AlphaDao
import org.maiaframework.showcase.join.AlphaEntityMeta
import org.maiaframework.showcase.join.AlphaEntityTestBuilder
import org.maiaframework.showcase.join.BravoCreateRequestDto
import org.maiaframework.showcase.join.BravoCrudService
import org.maiaframework.showcase.join.BravoDao
import org.maiaframework.showcase.join.BravoEntity
import org.maiaframework.showcase.join.BravoEntityMeta
import org.maiaframework.showcase.join.BravoEntityTestBuilder
import org.maiaframework.showcase.join.BravoHzCappedDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoHzCappedDtoRepo
import org.maiaframework.showcase.join.BravoHzDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoHzDtoRepo
import org.maiaframework.showcase.join.BravoHzLazyDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoHzLazyDtoRepo
import org.maiaframework.showcase.join.BravoUpdate_someStringRequestDto
import org.mockito.AdditionalAnswers
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.mock.env.MockEnvironment
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.PlatformTransactionManager
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread


/**
 * Black-box tests of Hazelcast-backed Blotter population on a cluster of two members (ADR-0008). The application's own
 * embedded member is "node 1"; the test starts a second member in the same JVM, joined to the same cluster, and gives
 * it its own generated repo ("node 2") over the same database. Both repos share one IMap. The protocol itself is
 * tested once, in the maia-hazelcast library; these tests check the generated repos wire it up correctly.
 */
@WithMockUser(authorities = ["WRITE"])
class HazelcastClusterPopulationIntegrationTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var node1: HazelcastInstance

    @Autowired
    private lateinit var hazelcastConfig: HazelcastConfig

    @Autowired
    private lateinit var dtoRepo: BravoHzDtoRepo

    @Autowired
    private lateinit var lazyDtoRepo: BravoHzLazyDtoRepo

    @Autowired
    private lateinit var cappedDtoRepo: BravoHzCappedDtoRepo

    @Autowired
    private lateinit var springCappedRepo: BravoHzCappedDtoHazelcastRepo

    @Autowired
    private lateinit var environment: Environment

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @Autowired
    private lateinit var alphaCrudService: AlphaCrudService

    @Autowired
    private lateinit var bravoCrudService: BravoCrudService

    @Autowired
    private lateinit var alphaDao: AlphaDao

    @Autowired
    private lateinit var bravoDao: BravoDao

    private var node2: HazelcastInstance? = null

    private var executor = Executors.newCachedThreadPool()

    private val spies = mutableListOf<BravoHzDtoRepo>()

    private lateinit var repo1: BravoHzDtoHazelcastRepo

    private var membersBefore = 0


    @BeforeEach
    fun reset() {

        executor = Executors.newCachedThreadPool { runnable -> Thread(runnable).apply { isDaemon = true } }
        spies.clear()
        repo1 = repoOn(node1)

        resetDatabaseAndMaps()

    }


    @AfterEach
    fun cleanUp() {

        try {
            executor.shutdownNow()
        } finally {
            try {
                node2?.let { if (it.lifecycleService.isRunning) it.shutdown() }
            } finally {
                node2 = null
                resetDatabaseAndMaps()
            }
        }

    }


    private fun resetDatabaseAndMaps() {

        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        HazelcastBlotterTestState.clearAll(node1)

    }


    @Test
    fun `a node that joins a populated map leaves it intact and both nodes serve the full set throughout`() {

        createBravos(3)
        repo1.populate()

        val member2 = startSecondMember()
        val joiner = repoOn(member2)
        Mockito.clearInvocations(*spies.toTypedArray())

        val failures = CopyOnWriteArrayList<String>()
        val running = AtomicBoolean(true)
        val watcher = thread(isDaemon = true) {
            try {
                while (running.get()) {
                    val sizes = listOf(repo1.findAll().size, joiner.findAll().size)
                    if (sizes != listOf(3, 3)) failures.add("sizes were $sizes")
                    Thread.sleep(1)
                }
            } catch (e: Throwable) {
                failures.add(e.toString())
            }
        }

        joiner.onContextRefreshed()

        running.set(false)
        watcher.join(30_000)

        assertThat(failures).isEmpty()
        assertThat(populations()).isZero()
        assertThat(repo1.findAll()).hasSize(3)
        assertThat(joiner.findAll()).hasSize(3)

    }


    @Test
    fun `nodes that start together populate once and both serve the complete set`() {

        createBravos(3)
        val repo2 = repoOn(startSecondMember())
        val held = holdFirstPopulation()

        val first = executor.submit { repo1.onContextRefreshed() }
        held.awaitStreaming()
        val second = executor.submit { repo2.onContextRefreshed() }

        assertThat(isDoneWithin(second, 1_000)).isFalse()

        held.release()
        first.get(30, TimeUnit.SECONDS)
        second.get(30, TimeUnit.SECONDS)

        assertThat(populations()).isEqualTo(1)
        assertThat(repo1.findAll()).hasSize(3)
        assertThat(repo2.findAll()).hasSize(3)

    }


    @Test
    fun `a rebuild queued behind another population still runs and reflects the database as of the call`() {

        createBravos(2)
        val repo2 = repoOn(startSecondMember())
        val held = holdFirstPopulation()

        val first = executor.submit { repo1.populate() }
        held.awaitStreaming()
        val queued = executor.submit { repo2.populate() }
        assertThat(isDoneWithin(queued, 500)).isFalse()
        insertBravoWithoutNotification()

        held.release()
        first.get(30, TimeUnit.SECONDS)
        queued.get(30, TimeUnit.SECONDS)

        assertThat(populations()).isEqualTo(2)
        assertThat(repo1.findAll()).hasSize(3)
        assertThat(repo2.findAll()).hasSize(3)

    }


    @Test
    fun `a node whose shape stamp differs from the one in the map rebuilds it, and one with the same stamp leaves it alone`() {

        createBravos(2)
        repo1.populate()
        control("bravo_hz_blotter").set(SHAPE_STAMP, "a-different-shape")
        insertBravoWithoutNotification()
        val joiner = repoOn(startSecondMember())
        Mockito.clearInvocations(*spies.toTypedArray())

        joiner.onContextRefreshed()

        assertThat(populations()).isEqualTo(1)
        assertThat(joiner.findAll()).hasSize(3)
        assertThat(control("bravo_hz_blotter").get(SHAPE_STAMP)).isNotEqualTo("a-different-shape")

        insertBravoWithoutNotification()
        repo1.onContextRefreshed()
        joiner.onContextRefreshed()

        assertThat(populations()).isEqualTo(1)
        assertThat(repo1.findAll()).hasSize(3)

    }


    @Test
    fun `a lazy node whose map another node filled serves it without repopulating and applies a refresh made on either node`() {

        val bravos = createBravos(2)
        val lazySpy1 = delegatingTo(lazyDtoRepo)
        val lazySpy2 = delegatingTo(lazyDtoRepo)
        val lazy1 = BravoHzLazyDtoHazelcastRepo(
            dtoRepo = lazySpy1,
            environment = environment,
            hazelcastInstance = node1,
            transactionManager = transactionManager
        )
        val lazy2 = BravoHzLazyDtoHazelcastRepo(
            dtoRepo = lazySpy2,
            environment = environment,
            hazelcastInstance = startSecondMember(),
            transactionManager = transactionManager
        )

        assertThat(lazy1.findAll()).hasSize(2)

        // node 2 never fetched, never populated: it serves what node 1 filled
        assertThat(lazy2.findAll().map { it.dtoStringFromBravo }).containsExactlyInAnyOrder("bravo-0", "bravo-1")
        Mockito.verify(lazySpy1, Mockito.times(1)).findAllAsSequence()
        Mockito.verify(lazySpy2, Mockito.never()).findAllAsSequence()

        // a refresh made on the node that did not populate
        setSomeStringWithoutNotification(bravos[0].id, "changed-via-node-2")
        lazy2.refreshByIds(listOf(bravos[0].id))
        assertThat(lazy1.findAll().map { it.dtoStringFromBravo }).containsExactlyInAnyOrder("changed-via-node-2", "bravo-1")

        // a refresh made on the node that did
        setSomeStringWithoutNotification(bravos[1].id, "changed-via-node-1")
        lazy1.refreshByIds(listOf(bravos[1].id))
        assertThat(lazy2.findAll().map { it.dtoStringFromBravo }).containsExactlyInAnyOrder("changed-via-node-2", "changed-via-node-1")
        Mockito.verify(lazySpy1, Mockito.times(1)).findAllAsSequence()
        Mockito.verify(lazySpy2, Mockito.never()).findAllAsSequence()

    }


    @Test
    fun `a delete committed on another node while a population runs leaves no ghost row`() {

        val bravos = createBravos(2)
        val repo2 = repoOn(startSecondMember())
        val held = holdFirstPopulation()

        val population = executor.submit { repo1.populate() }
        held.awaitStreaming()

        jdbcOps.update("delete from ${BravoEntityMeta.SCHEMA_AND_TABLE_NAME} where id = '${bravos[0].id.value}'")
        repo2.deleteById(bravos[0].id)

        held.release()
        population.get(30, TimeUnit.SECONDS)

        assertThat(repo1.findAll().map { it.dtoStringFromBravo }).containsExactly("bravo-1")
        assertThat(repo2.findAll().map { it.dtoStringFromBravo }).containsExactly("bravo-1")

    }


    @Test
    fun `a refresh committed on another node while a population runs is reflected afterwards`() {

        val bravos = createBravos(2)
        val repo2 = repoOn(startSecondMember())
        val held = holdFirstPopulation()

        val population = executor.submit { repo2.populate() }
        held.awaitStreaming()

        bravoCrudService.updateSomeString(BravoUpdate_someStringRequestDto(id_raw = bravos[0].id, someString_raw = "after"))

        held.release()
        population.get(30, TimeUnit.SECONDS)

        assertThat(repo1.findAll().map { it.dtoStringFromBravo }).containsExactlyInAnyOrder("after", "bravo-1")
        assertThat(repo2.findAll().map { it.dtoStringFromBravo }).containsExactlyInAnyOrder("after", "bravo-1")

    }


    @Test
    fun `a refresh committed while the changed rows are being replayed is not lost`() {

        val bravos = createBravos(2)
        val repo2 = repoOn(startSecondMember())
        val held = holdFirstPopulation()
        val replaying = CountDownLatch(1)
        val proceed = CountDownLatch(1)
        val replaySpy = spies.last()
        Mockito.doAnswer { invocation ->
            replaying.countDown()
            check(proceed.await(30, TimeUnit.SECONDS)) { "replay was never released" }
            dtoRepo.findByIds(invocation.getArgument<Collection<DomainId>>(0))
        }.`when`(replaySpy).findByIds(Mockito.anyCollection())

        val population = executor.submit { repo2.populate() }
        held.awaitStreaming()
        bravoCrudService.updateSomeString(BravoUpdate_someStringRequestDto(id_raw = bravos[0].id, someString_raw = "during-streaming"))
        held.release()
        assertThat(replaying.await(30, TimeUnit.SECONDS)).isTrue()

        // the replay is re-reading from the database: a refresh now is neither blocked nor lost
        bravoCrudService.updateSomeString(BravoUpdate_someStringRequestDto(id_raw = bravos[1].id, someString_raw = "during-replay"))
        proceed.countDown()
        population.get(30, TimeUnit.SECONDS)

        assertThat(repo1.findAll().map { it.dtoStringFromBravo }).containsExactlyInAnyOrder("during-streaming", "during-replay")
        assertThat(repo2.findAll().map { it.dtoStringFromBravo }).containsExactlyInAnyOrder("during-streaming", "during-replay")

    }


    @Test
    fun `viewers on other nodes keep getting the previous complete set while a rebuild runs`() {

        createBravos(2)
        repo1.populate()
        val repo2 = repoOn(startSecondMember())
        insertBravoWithoutNotification()

        val held = holdFirstPopulation()
        val rebuild = executor.submit { repo2.populate() }
        held.awaitStreaming()

        assertThat(repo1.findAll()).hasSize(2)
        assertThat(repo2.findAll()).hasSize(2)

        held.release()
        rebuild.get(30, TimeUnit.SECONDS)

        assertThat(repo1.findAll()).hasSize(3)
        assertThat(repo2.findAll()).hasSize(3)

    }


    @Test
    fun `viewers keep getting the previous complete set while a rebuild fails, and after it`() {

        createBravos(2)
        repo1.populate()
        val repo2 = repoOn(startSecondMember())
        insertBravoWithoutNotification()

        val held = holdFirstPopulation(failWith = IllegalStateException("transient database error"))
        val rebuild = executor.submit { repo2.populate() }
        held.awaitStreaming()
        assertThat(repo1.findAll()).hasSize(2)

        held.release()
        assertThatThrownBy { rebuild.get(30, TimeUnit.SECONDS) }
            .isInstanceOf(ExecutionException::class.java)
            .hasRootCauseMessage("transient database error")

        assertThat(control("bravo_hz_blotter").get(POPULATED)).isEqualTo(true)
        assertThat(repo1.findAll()).hasSize(2)
        assertThat(repo2.findAll()).hasSize(2)

        repo2.populate()

        assertThat(repo1.findAll()).hasSize(3)

    }


    @Test
    fun `when the populating node leaves the cluster mid-population the map is reported unpopulated and another node populates it`() {

        createBravos(2)
        repo1.populate()
        val member2 = startSecondMember()
        val repo2 = repoOn(member2)
        insertBravoWithoutNotification()

        val held = holdFirstPopulation()
        val dying = executor.submit { repo2.populate() }
        held.awaitStreaming()

        member2.lifecycleService.terminate()
        node2 = null
        waitUntil { node1.cluster.members.size == membersBefore }

        held.release()
        runCatching { dying.get(30, TimeUnit.SECONDS) }

        // the first node to notice discards the dead node's work and reports the map unpopulated
        repo1.refreshByIds(emptyList())
        assertThat(control("bravo_hz_blotter").get(POPULATED)).isEqualTo(false)

        // the set node 1 still holds is stale (the third row never reached it): it is rebuilt, not served as current
        assertThat(repo1.findAll()).hasSize(3)
        assertThat(control("bravo_hz_blotter").get(POPULATED)).isEqualTo(true)

    }


    @Test
    fun `a populator that hangs loses its lock at the lease so another node can populate, and its late finish is refused`() {

        createBravos(2)
        val shortLease = MockEnvironment().withProperty("maia.hazelcast-blotter.populate-lock-lease-seconds", "1")
        val hung = repoOn(node1, shortLease)
        val other = repoOn(startSecondMember(), shortLease)
        val held = holdFirstPopulation()

        val hanging = executor.submit { hung.populate() }
        held.awaitStreaming()

        other.populate()
        assertThat(other.findAll()).hasSize(2)

        held.release()
        assertThatThrownBy { hanging.get(30, TimeUnit.SECONDS) }.hasRootCauseInstanceOf(PopulationLockLostException::class.java)
        assertThat(other.findAll()).hasSize(2)

    }


    @Test
    fun `a maxRows failure leaves the map unpopulated for every node`() {

        createBravos(21)
        val capped2 = BravoHzCappedDtoHazelcastRepo(
            dtoRepo = cappedDtoRepo,
            environment = environment,
            hazelcastInstance = startSecondMember(),
            transactionManager = transactionManager
        )

        assertThatThrownBy { capped2.populate() }.isInstanceOf(BlotterTooLargeException::class.java)
        assertThat(node1.getMap<DomainId, Any>("bravo_hz_capped_blotter").size).isZero()

        // node 1 does not serve a partial or stale set: it tries to populate itself and fails the same way
        assertThatThrownBy { springCappedRepo.findAll() }.isInstanceOf(BlotterTooLargeException::class.java)
        assertThatThrownBy { capped2.findAll() }.isInstanceOf(BlotterTooLargeException::class.java)

        jdbcOps.update("delete from ${BravoEntityMeta.SCHEMA_AND_TABLE_NAME} where some_string = 'bravo-0'")

        assertThat(springCappedRepo.findAll()).hasSize(20)
        assertThat(capped2.findAll()).hasSize(20)

    }


    /** A repo for this member over its own spy of the database repo, so each node's reads can be held and counted. */
    private fun repoOn(member: HazelcastInstance, env: Environment = environment): BravoHzDtoHazelcastRepo {

        val spy = delegatingTo(dtoRepo)
        spies.add(spy)

        return BravoHzDtoHazelcastRepo(
            dtoRepo = spy,
            environment = env,
            hazelcastInstance = member,
            transactionManager = transactionManager
        )

    }


    private fun control(mapName: String) = node1.getMap<String, Any>(mapName + "__control")


    /**
     * A second member of the same cluster. It is configured exactly as the application's own member is (the test
     * `hazelcast.yaml` joins members at the local address by TCP-IP rather than by multicast, so the test does not
     * depend on multicast working on the machine running it, and the second member has to use the same joiner).
     */
    private fun startSecondMember(): HazelcastInstance {

        val config: Config = YamlConfigBuilder().build()
        config.instanceName = "hz-cluster-test-node-2"
        config.clusterName = node1.config.clusterName
        hazelcastConfig.serializers.forEach { config.serializationConfig.compactSerializationConfig.addSerializer(it) }

        membersBefore = node1.cluster.members.size

        val member = Hazelcast.newHazelcastInstance(config)
        node2 = member

        // other Spring contexts cached in this JVM are members of the same cluster too, so count relative to before
        assertThat(node1.cluster.members).hasSize(membersBefore + 1)

        return member

    }


    private class HeldPopulation(
        private val streaming: CountDownLatch,
        private val release: CountDownLatch
    ) {

        fun awaitStreaming() {
            assertThat(streaming.await(30, TimeUnit.SECONDS)).isTrue()
        }

        fun release() {
            release.countDown()
        }

    }


    /**
     * The first population to read the database (on whichever node) delivers its snapshot and then stays held until
     * released (and then fails with [failWith], when given), standing in for a slow read; any later population reads
     * normally.
     */
    private fun holdFirstPopulation(failWith: Exception? = null): HeldPopulation {

        val streaming = CountDownLatch(1)
        val release = CountDownLatch(1)
        val calls = AtomicInteger()

        val answer = Answer { _ ->
            val real = dtoRepo.findAllAsSequence()
            if (calls.incrementAndGet() > 1) {
                real
            } else {
                val snapshot = real.toList()
                sequence {
                    yieldAll(snapshot)
                    streaming.countDown()
                    check(release.await(30, TimeUnit.SECONDS)) { "population was never released" }
                    failWith?.let { throw it }
                }
            }
        }

        spies.forEach { spy -> Mockito.doAnswer(answer).`when`(spy).findAllAsSequence() }

        return HeldPopulation(streaming, release)

    }


    /** A mock that passes every call to the Spring-managed (proxied, so not spy-able) [real] repo unless stubbed. */
    private inline fun <reified T : Any> delegatingTo(real: T): T {

        return Mockito.mock(T::class.java, AdditionalAnswers.delegatesTo<T>(real))

    }


    private fun populations(): Int {

        return spies.sumOf { spy -> Mockito.mockingDetails(spy).invocations.count { it.method.name == "findAllAsSequence" } }

    }


    private fun isDoneWithin(future: Future<*>, millis: Long): Boolean {

        return try {
            future.get(millis, TimeUnit.MILLISECONDS)
            true
        } catch (e: TimeoutException) {
            false
        }

    }


    private fun waitUntil(condition: () -> Boolean) {

        val deadline = System.currentTimeMillis() + 30_000
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "condition not reached" }
            Thread.sleep(50)
        }

    }


    private fun setSomeStringWithoutNotification(id: DomainId, value: String) {

        jdbcOps.update("update ${BravoEntityMeta.SCHEMA_AND_TABLE_NAME} set some_string = '$value' where id = '${id.value}'")

    }


    /** Straight through the database, so no CrudListener event refreshes the map: only a population can show the row. */
    private fun insertBravoWithoutNotification() {

        val alpha = AlphaEntityTestBuilder().build().also { alphaDao.insert(it) }
        bravoDao.insert(BravoEntityTestBuilder(alphaId = alpha.id, someString = "bravo-extra").build())

    }


    private fun createBravos(count: Int): List<BravoEntity> {

        val alpha = alphaCrudService.create(AlphaCreateRequestDto(someInt_raw = 1, someString_raw = "alpha"))

        return (0 until count).map {
            bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = it, someString_raw = "bravo-$it"))
        }

    }


    private companion object {

        // the control map entries of the population protocol (see HazelcastClusterPopulator)
        const val POPULATED = "populated"

        const val SHAPE_STAMP = "shapeStamp"

    }


}
