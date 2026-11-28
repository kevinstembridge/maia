package org.maiaframework.showcase.hazelcast_blotter

import com.hazelcast.config.Config
import com.hazelcast.config.YamlConfigBuilder
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.maiaframework.domain.DomainId
import org.maiaframework.showcase.join.BravoEntityMeta
import org.maiaframework.showcase.join.AlphaEntityTestBuilder
import org.maiaframework.showcase.join.BravoEntityTestBuilder
import org.maiaframework.showcase.join.BravoHzCappedDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoHzCappedDtoRepo
import org.maiaframework.showcase.join.BravoHzLazyDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoHzLazyDtoRepo
import org.maiaframework.showcase.join.BravoUpdate_someStringRequestDto
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.join.AlphaCreateRequestDto
import org.maiaframework.showcase.join.AlphaCrudService
import org.maiaframework.showcase.join.AlphaEntityMeta
import org.maiaframework.showcase.join.BravoCreateRequestDto
import org.maiaframework.showcase.join.BravoCrudService
import org.maiaframework.showcase.join.BravoHzDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoHzDtoRepo
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.PlatformTransactionManager
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread


/**
 * Black-box tests of Hazelcast-backed Blotter population on a cluster of two members (ADR-0008). The application's own
 * embedded member is "node 1"; the test starts a second member in the same JVM, joined to the same cluster (the
 * application's embedded member uses the default multicast joiner, and a joining member must use the same joiner), and gives it its own generated repo ("node 2") over the same database. Both repos share one IMap.
 */
@WithMockUser(authorities = ["WRITE"])
class HazelcastClusterPopulationIntegrationTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var node1: HazelcastInstance

    @Autowired
    private lateinit var serializers: org.maiaframework.showcase.hazelcast.HazelcastConfig

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
    private lateinit var alphaDao: org.maiaframework.showcase.join.AlphaDao

    @Autowired
    private lateinit var bravoDao: org.maiaframework.showcase.join.BravoDao

    private var node2: HazelcastInstance? = null

    private var executor = Executors.newCachedThreadPool()

    private val spies = mutableListOf<BravoHzDtoRepo>()

    private lateinit var repo1: BravoHzDtoHazelcastRepo

    private var membersBefore = 0


    @BeforeEach
    fun reset() {

        executor = Executors.newCachedThreadPool()
        spies.clear()
        repo1 = repoOn(node1)

        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        listOf("bravo_hz_blotter", "bravo_hz_lazy_blotter", "bravo_hz_capped_blotter").forEach { name ->
            listOf("", "__control", "__staging", "__changed").forEach { suffix ->
                node1.getMap<Any, Any>(name + suffix).clear()
            }
        }

    }


    @AfterEach
    fun stopNode2() {

        node2?.let { if (it.lifecycleService.isRunning) it.shutdown() }
        node2 = null
        executor.shutdownNow()

    }


    @Test
    fun `a node that joins a populated map leaves it intact and both nodes serve the full set throughout`() {

        createBravos(3)
        repo1.populate()

        val member2 = startSecondMember()
        val joiner = repoOn(member2)
        Mockito.clearInvocations(*spies.toTypedArray())

        val failures = AtomicInteger()
        val running = AtomicBoolean(true)
        val watcher = thread {
            while (running.get()) {
                if (repo1.findAll().size != 3 || joiner.findAll().size != 3) failures.incrementAndGet()
            }
        }

        joiner.onContextRefreshed()

        running.set(false)
        watcher.join()

        assertThat(failures.get()).isZero()
        assertThat(populations()).isZero()
        assertThat(repo1.findAll()).hasSize(3)
        assertThat(joiner.findAll()).hasSize(3)

    }


    @Test
    fun `two nodes populating at the same time populate once and both serve the complete set`() {

        createBravos(3)
        val repo2 = repoOn(startSecondMember())
        val held = holdFirstPopulation()

        val first = executor.submit { repo1.populate() }
        held.awaitStreaming()
        val second = executor.submit { repo2.populate() }

        assertThat(isDoneWithin(second, 1_000)).isFalse()

        held.release()
        first.get(30, TimeUnit.SECONDS)
        second.get(30, TimeUnit.SECONDS)

        assertThat(populations()).isEqualTo(1)
        assertThat(repo1.findAll()).hasSize(3)
        assertThat(repo2.findAll()).hasSize(3)

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
    fun `when the populating node leaves the cluster mid-population the map is not populated and another node populates it`() {

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

        // the set node 1 would still hold is stale (the third row never reached it): it is rebuilt, not served
        assertThat(repo1.findAll()).hasSize(3)

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

        assertThatThrownBy { capped2.populate() }.hasStackTraceContaining("maxRows")
        assertThat(node1.getMap<DomainId, Any>("bravo_hz_capped_blotter").size).isZero()

        // node 1 does not serve a partial or stale set: it tries to populate itself and fails the same way
        assertThatThrownBy { springCappedRepo.findAll() }.hasStackTraceContaining("maxRows")
        assertThatThrownBy { capped2.findAll() }.hasStackTraceContaining("maxRows")

        jdbcOps.update("delete from ${BravoEntityMeta.SCHEMA_AND_TABLE_NAME} where some_string = 'bravo-0'")

        assertThat(springCappedRepo.findAll()).hasSize(20)
        assertThat(capped2.findAll()).hasSize(20)

    }


    /** A repo for this member over its own spy of the database repo, so each node's reads can be held and counted. */
    private fun repoOn(member: HazelcastInstance): BravoHzDtoHazelcastRepo {

        val spy = delegatingTo(dtoRepo)
        spies.add(spy)

        return BravoHzDtoHazelcastRepo(
            dtoRepo = spy,
            environment = environment,
            hazelcastInstance = member,
            transactionManager = transactionManager
        )

    }


    private fun startSecondMember(): HazelcastInstance {

        val config: Config = YamlConfigBuilder().build()
        config.instanceName = "hz-cluster-test-node-2"
        config.clusterName = node1.config.clusterName
        serializers.serializers.forEach { config.serializationConfig.compactSerializationConfig.addSerializer(it) }

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
     * released, standing in for a slow read; any later population reads normally.
     */
    private fun holdFirstPopulation(): HeldPopulation {

        val streaming = CountDownLatch(1)
        val release = CountDownLatch(1)
        val calls = AtomicInteger()

        val answer = org.mockito.stubbing.Answer { _ ->
            val real = dtoRepo.findAllAsSequence()
            if (calls.incrementAndGet() > 1) {
                real
            } else {
                val snapshot = real.toList()
                sequence {
                    yieldAll(snapshot)
                    streaming.countDown()
                    check(release.await(30, TimeUnit.SECONDS)) { "population was never released" }
                }
            }
        }

        spies.forEach { spy -> Mockito.doAnswer(answer).`when`(spy).findAllAsSequence() }

        return HeldPopulation(streaming, release)

    }


    /** A mock that passes every call to the Spring-managed (proxied, so not spy-able) [real] repo unless stubbed. */
    private inline fun <reified T : Any> delegatingTo(real: T): T {

        return Mockito.mock(T::class.java, org.mockito.AdditionalAnswers.delegatesTo<T>(real))

    }


    private fun populations(): Int {

        return spies.sumOf { spy -> Mockito.mockingDetails(spy).invocations.count { it.method.name == "findAllAsSequence" } }

    }


    private fun isDoneWithin(future: Future<*>, millis: Long): Boolean {

        return try {
            future.get(millis, TimeUnit.MILLISECONDS)
            true
        } catch (e: java.util.concurrent.TimeoutException) {
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


    private fun createBravos(count: Int): List<org.maiaframework.showcase.join.BravoEntity> {

        val alpha = alphaCrudService.create(AlphaCreateRequestDto(someInt_raw = 1, someString_raw = "alpha"))

        return (0 until count).map {
            bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = it, someString_raw = "bravo-$it"))
        }

    }


}
