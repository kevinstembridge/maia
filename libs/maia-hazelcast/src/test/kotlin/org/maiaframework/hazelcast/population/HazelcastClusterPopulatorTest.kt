package org.maiaframework.hazelcast.population

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.maiaframework.common.BlotterTooLargeException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference


/**
 * Tests of the cluster population protocol on its own (ADR-0008), over real Hazelcast members in this JVM and a fake
 * "database": rows are strings of the form `id=value`. Distinct threads stand in for distinct nodes where the protocol
 * only needs distinct lock owners; a second member is started where a node has to die.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HazelcastClusterPopulatorTest {


    private class FakeDb {

        val rows = ConcurrentHashMap<String, String>()

        val streams = AtomicInteger()

        val afterStreaming = AtomicReference<(() -> Unit)?>(null)

        val onFindByIds = AtomicReference<(() -> Unit)?>(null)

        fun put(id: String, value: String) { rows[id] = "$id=$value" }

        fun stream(): Sequence<String> {
            streams.incrementAndGet()
            val snapshot = rows.values.sorted().toList()
            val hook = afterStreaming.getAndSet(null)
            return sequence {
                yieldAll(snapshot)
                hook?.invoke()
            }
        }

        fun find(ids: Collection<String>): List<String> {
            onFindByIds.getAndSet(null)?.invoke()
            return ids.mapNotNull { rows[it] }
        }

    }


    private val clusterName = "populator-test-" + UUID.randomUUID()

    private lateinit var member1: HazelcastInstance

    private val db = FakeDb()

    private var mapName = ""

    private var executor = newExecutor()

    private val extraMembers = mutableListOf<HazelcastInstance>()


    @BeforeAll
    fun startMember() {
        member1 = newMember(5901)
    }


    @AfterAll
    fun stopMember() {
        member1.shutdown()
    }


    @BeforeEach
    fun freshMap() {
        executor = newExecutor()
        mapName = "rows_" + UUID.randomUUID().toString().replace("-", "")
        db.rows.clear()
        db.streams.set(0)
        db.afterStreaming.set(null)
        db.onFindByIds.set(null)
    }


    @AfterEach
    fun cleanUp() {
        extraMembers.forEach { if (it.lifecycleService.isRunning) it.shutdown() }
        extraMembers.clear()
        executor.shutdownNow()
    }


    private fun newExecutor() = Executors.newCachedThreadPool { r -> Thread(r).apply { isDaemon = true } }


    private fun newMember(port: Int): HazelcastInstance {
        val config = Config()
        config.clusterName = clusterName
        config.instanceName = "populator-test-$port"
        config.networkConfig.port = port
        config.networkConfig.isPortAutoIncrement = false
        config.networkConfig.join.multicastConfig.isEnabled = false
        config.networkConfig.join.autoDetectionConfig.isEnabled = false
        config.networkConfig.join.tcpIpConfig.isEnabled = true
        config.networkConfig.join.tcpIpConfig.members = listOf("127.0.0.1:5901", "127.0.0.1:5902")
        return Hazelcast.newHazelcastInstance(config)
    }


    private fun populator(
        member: HazelcastInstance = member1,
        stamp: String = "shape-1",
        maxRows: Int = 10_000,
        settings: HazelcastClusterPopulator.Settings = HazelcastClusterPopulator.Settings()
    ) = HazelcastClusterPopulator<String, String>(
        hazelcastInstance = member,
        mapName = mapName,
        dtoName = "Row",
        shapeStamp = stamp,
        maxRows = maxRows,
        idOf = { it.substringBefore('=') },
        findAllAsSequence = { db.stream() },
        findByIds = { db.find(it) },
        settings = settings
    )


    private fun seed(count: Int) {
        (0 until count).forEach { db.put("id$it", "v$it") }
    }


    private class Held(val streaming: CountDownLatch = CountDownLatch(1), val release: CountDownLatch = CountDownLatch(1)) {
        fun hook(): () -> Unit = {
            streaming.countDown()
            check(release.await(30, TimeUnit.SECONDS)) { "never released" }
        }
        fun awaitStreaming() = assertThat(streaming.await(30, TimeUnit.SECONDS)).isTrue()
    }


    private fun values(p: HazelcastClusterPopulator<String, String>) = p.findAll().map { it.substringAfter('=') }.sorted()


    @Test
    fun `findAll populates an unpopulated map once and then serves it`() {
        seed(3)
        val p = populator()

        assertThat(values(p)).containsExactly("v0", "v1", "v2")
        assertThat(values(p)).hasSize(3)

        assertThat(db.streams.get()).isEqualTo(1)
        assertThat(p.isPopulated()).isTrue()
    }


    @Test
    fun `a rebuild that waited behind another population still runs its own`() {
        seed(2)
        val first = populator()
        val second = populator()
        val held = Held()
        db.afterStreaming.set(held.hook())

        val running = executor.submit { first.rebuild() }
        held.awaitStreaming()
        val queued = executor.submit { second.rebuild() }
        Thread.sleep(300)
        db.put("late", "v-late")

        held.release.countDown()
        running.get(30, TimeUnit.SECONDS)
        queued.get(30, TimeUnit.SECONDS)

        assertThat(db.streams.get()).isEqualTo(2)
        assertThat(values(second)).contains("v-late")
        assertThat(second.populationCount()).isEqualTo(2)
    }


    @Test
    fun `an ensure that waited behind another population does not run a second one`() {
        seed(2)
        val first = populator()
        val second = populator()
        val held = Held()
        db.afterStreaming.set(held.hook())

        val running = executor.submit { first.ensurePopulated() }
        held.awaitStreaming()
        val queued = executor.submit { second.ensurePopulated() }
        Thread.sleep(300)

        held.release.countDown()
        running.get(30, TimeUnit.SECONDS)
        queued.get(30, TimeUnit.SECONDS)

        assertThat(db.streams.get()).isEqualTo(1)
    }


    @Test
    fun `a failed rebuild keeps the previous complete set, surfaces the failure and a reader during it is undisturbed`() {
        seed(3)
        val p = populator()
        p.rebuild()
        db.put("extra", "v-extra")
        val held = Held()
        db.afterStreaming.set { held.hook()(); throw IllegalStateException("transient database error") }

        val failing = executor.submit { p.rebuild() }
        held.awaitStreaming()
        assertThat(values(p)).containsExactly("v0", "v1", "v2")

        held.release.countDown()
        assertThatThrownBy { failing.get(30, TimeUnit.SECONDS) }.hasRootCauseMessage("transient database error")

        assertThat(p.isPopulated()).isTrue()
        assertThat(values(p)).containsExactly("v0", "v1", "v2")
        assertThat(db.streams.get()).isEqualTo(2)
    }


    @Test
    fun `a refresh that arrived during a failed rebuild is applied to the previous set`() {
        seed(2)
        val p = populator()
        p.rebuild()
        val held = Held()
        db.afterStreaming.set { held.hook()(); throw IllegalStateException("boom") }

        val failing = executor.submit { p.rebuild() }
        held.awaitStreaming()
        db.put("id0", "changed")
        p.refreshByIds(listOf("id0"))
        held.release.countDown()
        runCatching { failing.get(30, TimeUnit.SECONDS) }

        assertThat(values(p)).containsExactly("changed", "v1")
    }


    @Test
    fun `a first population that fails leaves the map unpopulated and the next fetch tries again`() {
        seed(2)
        val p = populator()
        db.afterStreaming.set { throw IllegalStateException("down") }

        assertThatThrownBy { p.findAll() }.hasMessage("down")
        assertThat(p.isPopulated()).isFalse()

        assertThat(values(p)).containsExactly("v0", "v1")
    }


    @Test
    fun `a maxRows failure empties the map and leaves it unpopulated even when it was populated`() {
        seed(3)
        populator().rebuild()
        seed(8)
        val capped = populator(maxRows = 5)

        assertThatThrownBy { capped.rebuild() }.isInstanceOf(BlotterTooLargeException::class.java)

        assertThat(capped.isPopulated()).isFalse()
        assertThat(member1.getMap<String, String>(mapName).size).isZero()
        assertThat(member1.getMap<String, String>(mapName + "__staging").size).isZero()
        assertThatThrownBy { capped.findAll() }.isInstanceOf(BlotterTooLargeException::class.java)
    }


    @Test
    fun `a row refreshed while the replay is re-reading is not lost`() {
        seed(2)
        val p = populator()
        val held = Held()
        db.afterStreaming.set(held.hook())

        val running = executor.submit { p.rebuild() }
        held.awaitStreaming()
        db.put("id0", "first")
        p.refreshByIds(listOf("id0"))

        // the replay of id0 is under way; a refresh now must neither block nor be lost
        val replaying = CountDownLatch(1)
        val proceed = CountDownLatch(1)
        db.onFindByIds.set { replaying.countDown(); proceed.await(30, TimeUnit.SECONDS) }
        held.release.countDown()
        assertThat(replaying.await(30, TimeUnit.SECONDS)).isTrue()

        db.put("id1", "second")
        p.refreshByIds(listOf("id1"))
        db.rows.remove("id0")
        p.deleteById("id0")
        proceed.countDown()
        running.get(30, TimeUnit.SECONDS)

        assertThat(values(p)).containsExactly("second")
    }


    @Test
    fun `rows changing throughout a population are all reflected afterwards`() {
        seed(1500)
        val p = populator()
        val stop = AtomicBoolean(false)
        val writer = executor.submit {
            var n = 0
            while (!stop.get()) {
                val id = "id${n % 50}"
                db.put(id, "w$n")
                p.refreshByIds(listOf(id))
                n++
            }
        }

        repeat(5) { p.rebuild() }
        stop.set(true)
        writer.get(30, TimeUnit.SECONDS)

        assertThat(member1.getMap<String, String>(mapName).values.toSet()).isEqualTo(db.rows.values.toSet())
    }


    @Test
    fun `readers always see a complete set while rebuilds and failing rebuilds run`() {
        seed(1200)
        val p = populator()
        p.rebuild()
        val stop = AtomicBoolean(false)
        val bad = AtomicInteger()
        val readers = (1..3).map {
            executor.submit {
                while (!stop.get()) {
                    if (p.findAll().size != 1200) bad.incrementAndGet()
                }
            }
        }

        repeat(6) { i ->
            if (i % 2 == 1) db.afterStreaming.set { throw IllegalStateException("flaky") }
            runCatching { p.rebuild() }
        }
        stop.set(true)
        readers.forEach { it.get(30, TimeUnit.SECONDS) }

        assertThat(bad.get()).isZero()
    }


    @Test
    fun `a node whose shape stamp equals the stamp in the map leaves a populated map alone`() {
        seed(2)
        populator(stamp = "A").ensurePopulated()

        val sameShape = populator(stamp = "A")
        sameShape.onStart(eager = true)
        sameShape.onStart(eager = false)
        sameShape.onStart(eager = true)

        assertThat(db.streams.get()).isEqualTo(1)
        assertThat(sameShape.isPopulated()).isTrue()
        assertThat(values(sameShape)).hasSize(2)
    }


    @Test
    fun `an eager start of a node with a different shape stamp rebuilds the map`() {
        seed(2)
        populator(stamp = "A").ensurePopulated()
        db.put("new", "v-new")

        populator(stamp = "B").onStart(eager = true)

        assertThat(db.streams.get()).isEqualTo(2)
        assertThat(values(populator(stamp = "B"))).hasSize(3)
        assertThat(db.streams.get()).isEqualTo(2)
    }


    @Test
    fun `a lazy start of a node with a different shape stamp marks the map unpopulated without emptying it`() {
        seed(2)
        populator(stamp = "A").ensurePopulated()
        val lazy = populator(stamp = "B")

        lazy.onStart(eager = false)

        assertThat(lazy.isPopulated()).isFalse()
        assertThat(member1.getMap<String, String>(mapName).size).isEqualTo(2)
        assertThat(db.streams.get()).isEqualTo(1)
        assertThat(values(lazy)).hasSize(2)
        assertThat(db.streams.get()).isEqualTo(2)
    }


    @Test
    fun `a hung populator releases the populate lock at the lease so another caller can populate, and the hung one cannot publish`() {
        seed(2)
        val hung = populator(settings = HazelcastClusterPopulator.Settings(populateLockLeaseMillis = 1_500, populateLockWaitMillis = 20_000))
        val other = populator(settings = HazelcastClusterPopulator.Settings(populateLockLeaseMillis = 1_500, populateLockWaitMillis = 20_000))
        val held = Held()
        db.afterStreaming.set(held.hook())

        val hanging = executor.submit { hung.rebuild() }
        held.awaitStreaming()

        other.rebuild()

        assertThat(values(other)).containsExactly("v0", "v1")
        held.release.countDown()
        assertThatThrownBy { hanging.get(30, TimeUnit.SECONDS) }.hasRootCauseInstanceOf(PopulationLockLostException::class.java)
        assertThat(values(other)).containsExactly("v0", "v1")
        assertThat(other.populationCount()).isEqualTo(1)
    }


    @Test
    fun `a caller that cannot get the populate lock in time fails with a clear error`() {
        seed(2)
        val slow = populator(settings = HazelcastClusterPopulator.Settings(populateLockLeaseMillis = 60_000, populateLockWaitMillis = 60_000))
        val impatient = populator(settings = HazelcastClusterPopulator.Settings(populateLockLeaseMillis = 60_000, populateLockWaitMillis = 500))
        val held = Held()
        db.afterStreaming.set(held.hook())

        val running = executor.submit { slow.rebuild() }
        held.awaitStreaming()

        assertThatThrownBy { impatient.rebuild() }
            .isInstanceOf(PopulationLockTimeoutException::class.java)
            .hasMessageContaining(mapName)

        held.release.countDown()
        running.get(30, TimeUnit.SECONDS)
    }


    @Test
    fun `when the populating member dies the map is reported unpopulated and another node populates it`() {
        seed(2)
        populator().ensurePopulated()
        val member2 = newMember(5902).also { extraMembers.add(it) }
        val dying = populator(member = member2)
        db.put("extra", "v-extra")
        val held = Held()
        db.afterStreaming.set(held.hook())

        val future = executor.submit { dying.rebuild() }
        held.awaitStreaming()
        member2.lifecycleService.terminate()
        held.release.countDown()
        runCatching { future.get(30, TimeUnit.SECONDS) }

        val survivor = populator()
        survivor.refreshByIds(listOf("extra"))
        assertThat(survivor.isPopulated()).isFalse()
        assertThat(values(survivor)).containsExactly("v-extra", "v0", "v1")
        assertThat(survivor.isPopulated()).isTrue()
    }


}
