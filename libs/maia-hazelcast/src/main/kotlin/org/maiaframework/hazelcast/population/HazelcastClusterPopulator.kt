package org.maiaframework.hazelcast.population

import com.hazelcast.collection.ISet
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import org.maiaframework.common.BlotterTooLargeException
import org.maiaframework.common.logging.getLogger
import java.util.UUID
import java.util.concurrent.TimeUnit


/**
 * The cluster population protocol of a Hazelcast-backed Blotter map (ADR-0008), written once as ordinary code that the
 * generated `{Dto}HazelcastRepo` calls (it is not a proxy, so it is consistent with ADR-0002). Every piece of
 * coordination state lives in Hazelcast, none in the JVM, so any number of nodes sharing one Hazelcast cluster behave
 * as one. It needs no CP subsystem: it uses IMap key locks, which Hazelcast releases when the owning member leaves.
 *
 * Structures, all named after the served map `<mapName>`:
 * - `<mapName>`: the served rows.
 * - `<mapName>__staging`: receives a population while viewers keep reading the served map.
 * - `<mapName>__changed` (a set): ids refreshed or deleted while a population runs, whichever node they came from.
 * - `<mapName>__control`: the cluster-wide [ControlState]: `populated`, `populating`, `populationCount`,
 *   `swapSequence` and the `shapeStamp` the served set was built with.
 *
 * Locks (key locks on the control map). The populate lock is held for a whole population (one populator cluster-wide)
 * and has a lease, so a populator that hangs cannot block the cluster forever. The state lock is held only briefly,
 * around every change to the flags or to the served map. Lock order: populate lock, then state lock.
 *
 * A population streams the database into the staging map, then repeatedly drains [changed] and re-reads those ids
 * from the database into the staging map (a deleted row is absent there, so it cannot be resurrected). These re-reads
 * do not hold the state lock, so refreshes keep being accepted; only the last step, which finds nothing left to
 * replay and copies the staged set into the served map, holds it (that step does no database reads). After
 * [MAX_REPLAY_PASSES] passes the remainder is replayed under the state lock, so a constant stream of writes cannot
 * keep a population from ever finishing.
 *
 * Readers never see a partial set. The copy happens between two increments of `swapSequence`: odd means "being
 * copied". A read that saw an odd or a changed sequence is discarded and retried.
 *
 * Failure semantics. A population that fails (for example a transient database error) discards its staging and
 * leaves the previous complete set served and still marked populated, after applying the changes recorded meanwhile
 * to it; the failure is rethrown. The one exception is a `maxRows` failure: it leaves the served map empty and
 * unpopulated for every node (the set is never truncated), and the next fetch on any node tries again. A population
 * whose node dies, or whose lock lease expires, is detected by the next node to look (`populating` is set but the
 * populate lock is free): it marks the map unpopulated without emptying it (the old rows keep being served until the
 * replacement is published) and populates it again. A node that lost its lease notices at its next chunk or at
 * publication and fails without touching shared state.
 *
 * Start-up and the shape stamp. The stamp identifies the document shape of the DTO the node was generated with. At
 * population time it is stored in the control map. [onStart] leaves an already populated map alone only if the
 * stored stamp equals the node's own; a different stamp means this node's documents differ from what is stored, so
 * the map (derived data) is rebuilt (eager) or marked unpopulated so the first fetch rebuilds it (lazy). Same-shape
 * restarts and joins never wipe or rebuild a populated map. [onStart] is idempotent: a repeated event on the same
 * node, or the start of the only node of a fresh cluster (whose map is empty, hence unstamped), follow the same rules.
 */
class HazelcastClusterPopulator<K : Any, V : Any>(
    hazelcastInstance: HazelcastInstance,
    private val mapName: String,
    private val dtoName: String,
    private val shapeStamp: String,
    private val maxRows: Int,
    private val idOf: (V) -> K,
    private val findAllAsSequence: () -> Sequence<V>,
    private val findByIds: (Collection<K>) -> List<V>,
    private val inReadTransaction: (() -> Unit) -> Unit = { it() },
    private val settings: Settings = Settings()
) {


    /**
     * Timing. [populateLockLeaseMillis] is how long a populator may hold the populate lock before Hazelcast releases it
     * (a population that takes longer fails). [populateLockWaitMillis] is how long a caller waits for the lock, by
     * default the lease plus a grace period so that it outlasts a hung populator. [readTimeoutMillis] bounds a read
     * that keeps being retried, and defaults to the lock wait so that a fetch never gives up before a hung population
     * is taken over.
     */
    data class Settings(
        val populateLockLeaseMillis: Long = DEFAULT_LEASE_MILLIS,
        val populateLockWaitMillis: Long = populateLockLeaseMillis + LOCK_WAIT_GRACE_MILLIS,
        val readTimeoutMillis: Long = populateLockWaitMillis,
        val readRetryMillis: Long = DEFAULT_READ_RETRY_MILLIS
    ) {

        companion object {

            fun fromLeaseSeconds(leaseSeconds: Long): Settings = Settings(populateLockLeaseMillis = TimeUnit.SECONDS.toMillis(leaseSeconds))

        }

    }


    private val logger = getLogger<HazelcastClusterPopulator<*, *>>()

    private val map: IMap<K, V> = hazelcastInstance.getMap(mapName)

    private val staging: IMap<K, V> = hazelcastInstance.getMap("${mapName}__staging")

    private val changed: ISet<K> = hazelcastInstance.getSet("${mapName}__changed")

    private val control: IMap<String, Any> = hazelcastInstance.getMap("${mapName}__control")

    private val state = ControlState(control)


    /** The cluster-wide facts, kept in the control map so that every node sees the same state. */
    private class ControlState(private val control: IMap<String, Any>) {

        /** The served map holds a complete set (possibly one built with another shape; see `shapeStamp`). */
        var populated: Boolean
            get() = control.get(POPULATED) == true
            set(value) { control.set(POPULATED, value) }

        /** A population is recorded as running; refreshes are recorded for replay instead of applied. */
        var populating: Boolean
            get() = control.get(POPULATING) == true
            set(value) { control.set(POPULATING, value) }

        /** The number of populations completed cluster-wide. */
        var populationCount: Long
            get() = (control.get(POPULATION_COUNT) as Long?) ?: 0L
            set(value) { control.set(POPULATION_COUNT, value) }

        /** Even when the served map is consistent, odd while a set is being copied into (or cleared from) it. */
        var swapSequence: Long
            get() = (control.get(SWAP_SEQUENCE) as Long?) ?: 0L
            set(value) { control.set(SWAP_SEQUENCE, value) }

        /** The shape stamp of the node that built the served set, null while there is none. */
        var shapeStamp: String?
            get() = control.get(SHAPE_STAMP) as String?
            set(value) { if (value == null) control.delete(SHAPE_STAMP) else control.set(SHAPE_STAMP, value) }

        /** Identifies the population that holds the populate lock, so one that lost its lease can tell. */
        var populationToken: String?
            get() = control.get(POPULATION_TOKEN) as String?
            set(value) { if (value == null) control.delete(POPULATION_TOKEN) else control.set(POPULATION_TOKEN, value) }

        val isCopying: Boolean get() = swapSequence % 2L == 1L

    }


    // ---- reads ----------------------------------------------------------------------------------------------


    /**
     * The first fetch of an unpopulated map (on any node) fills it; every later fetch, on every node, just reads it.
     * A rebuild running elsewhere does not block the read: the previous complete set is served.
     */
    fun findAll(): List<V> {

        val deadline = System.currentTimeMillis() + settings.readTimeoutMillis

        while (true) {

            if (!isPopulatedAndHealthy()) {
                ensurePopulated()
            }

            readServedSet()?.let { return it }

            if (System.currentTimeMillis() >= deadline) {
                throw PopulationLockTimeoutException("Timed out reading Hazelcast map $mapName")
            }

            Thread.sleep(settings.readRetryMillis)

        }

    }


    /** The served set, or null when the read has to be retried because it overlapped a copy or the map is not populated. */
    private fun readServedSet(): List<V>? {

        val before = state.swapSequence

        if (before % 2L == 1L || !state.populated) {
            return null
        }

        val rows = this.map.values.toList()

        return if (state.swapSequence == before) rows else null

    }


    fun isPopulated(): Boolean = state.populated


    /** The number of populations completed cluster-wide. */
    fun populationCount(): Long = state.populationCount


    /**
     * Populated, and no population left behind by a node that has since left the cluster. A running population holds
     * the populate lock, so `populating` without the lock means its node died or its lease expired.
     */
    private fun isPopulatedAndHealthy(): Boolean = state.populated && !populatorDied()


    private fun populatorDied(): Boolean = state.populating && !control.isLocked(LOCK_POPULATE)


    // ---- start-up -------------------------------------------------------------------------------------------


    /**
     * Application start (and any repeated context-refresh event). Eager: make sure the map is populated with this
     * node's shape. Lazy: leave it for the first fetch, but if a populated map was built with another shape, mark it
     * unpopulated (its rows stay served until the first fetch replaces them).
     */
    fun onStart(eager: Boolean) {

        if (eager) {
            ensurePopulated()
            return
        }

        withStateLock {
            if (state.populated && !state.populating && state.shapeStamp != shapeStamp) {
                logger.info("Hazelcast map $mapName was built with another document shape; it will be rebuilt by the first fetch")
                state.populated = false
            }
        }

    }


    // ---- population -----------------------------------------------------------------------------------------


    /** Populates the map unless it is populated, healthy and built with this node's shape. Used at start and by fetches. */
    fun ensurePopulated() {

        populate(rebuild = false)

    }


    /**
     * Refills the map from the database, always: a call that had to wait behind another population still runs its own,
     * so the data reflects the database as of the call.
     */
    fun rebuild() {

        populate(rebuild = true)

    }


    private fun populate(rebuild: Boolean) {

        acquirePopulateLock()

        try {

            withStateLock {
                if (populatorDied()) {
                    logger.warn("The node populating Hazelcast map $mapName left or lost its lock before it finished; discarding its partial work")
                    discardAbandonedPopulation()
                }
            }

            if (rebuild || !state.populated || state.shapeStamp != shapeStamp) {
                runPopulation()
            }

        } finally {
            releasePopulateLock()
        }

    }


    private fun acquirePopulateLock() {

        val acquired = control.tryLock(
            LOCK_POPULATE,
            settings.populateLockWaitMillis, TimeUnit.MILLISECONDS,
            settings.populateLockLeaseMillis, TimeUnit.MILLISECONDS
        )

        if (!acquired) {
            throw PopulationLockTimeoutException(
                "Gave up after ${settings.populateLockWaitMillis} ms waiting to populate Hazelcast map $mapName: " +
                        "another node holds the populate lock (lease ${settings.populateLockLeaseMillis} ms)"
            )
        }

    }


    /** Does nothing when the lease already expired, which Hazelcast reports as the lock not being held by this thread. */
    private fun releasePopulateLock() {

        try {
            control.unlock(LOCK_POPULATE)
        } catch (e: IllegalMonitorStateException) {
            // the lease expired, and the lock is free or held by another node
        }

    }


    /** The lock is held and this population's token is the recorded one (a node that took over records its own). */
    private fun stillOwns(token: String): Boolean = control.isLocked(LOCK_POPULATE) && state.populationToken == token


    private fun requireOwnership(token: String) {

        if (!stillOwns(token)) {
            throw PopulationLockLostException(
                "The lease on the populate lock of Hazelcast map $mapName expired before this population finished " +
                        "(lease ${settings.populateLockLeaseMillis} ms); it was abandoned"
            )
        }

    }


    /** Called with the populate lock held. */
    private fun runPopulation() {

        logger.info("BEGIN: Populating Hazelcast map $mapName")

        val token = UUID.randomUUID().toString()

        withStateLock {
            state.populationToken = token
            state.populating = true
            staging.clear()
            changed.clear()
        }

        var count = 0

        try {

            inReadTransaction {
                findAllAsSequence().chunked(CHUNK_SIZE).forEach { chunk ->
                    requireOwnership(token)
                    if (count + chunk.size > maxRows) {
                        throw BlotterTooLargeException(dtoName, maxRows)
                    }
                    staging.putAll(chunk.associateBy(idOf))
                    count += chunk.size
                }
            }

            replayAndPublish(token)

        } catch (e: PopulationLockLostException) {
            logger.error("Population of Hazelcast map $mapName abandoned: ${e.message}")
            throw e
        } catch (e: BlotterTooLargeException) {
            emptyAndMarkUnpopulated(token, e)
            throw e
        } catch (t: Throwable) {
            keepPreviousSet(token, t)
            throw t
        }

        logger.info("END: Populated Hazelcast map $mapName with $count rows")

    }


    /**
     * Replays what changed while the population streamed (those rows are re-read, so the staged set ends up current),
     * then publishes the staged set.
     */
    private fun replayAndPublish(token: String) {

        var pass = 0

        while (true) {

            val toReplay = withStateLock {

                requireOwnership(token)

                val pending = changed.toList()

                if (pending.isEmpty() || pass >= MAX_REPLAY_PASSES) {
                    reconcile(pending, staging)
                    publishStagedSet()
                    null
                } else {
                    changed.removeAll(pending.toSet())
                    pending
                }

            } ?: return

            reconcile(toReplay, staging)
            pass++

        }

    }


    /** Called with the state lock held. */
    private fun publishStagedSet() {

        val sequence = state.swapSequence
        state.swapSequence = sequence + 1
        this.map.clear()
        this.staging.keys.chunked(CHUNK_SIZE).forEach { ids ->
            this.map.putAll(this.staging.getAll(ids.toSet()))
        }
        state.swapSequence = sequence + 2

        staging.clear()
        changed.clear()
        state.populationCount = state.populationCount + 1
        state.shapeStamp = shapeStamp
        state.populating = false
        state.populated = true

    }


    /**
     * A population failed: discard its staging and leave the previous complete set served. The changes recorded while
     * it ran were never applied to that set, so they are applied now; if that fails too, the set can no longer be
     * trusted and is marked unpopulated (its rows stay served until the next population replaces them).
     */
    private fun keepPreviousSet(token: String, cause: Throwable) {

        try {

            withStateLock {

                if (!stillOwns(token)) {
                    return@withStateLock
                }

                logger.error("Population of Hazelcast map $mapName failed; keeping the previous set", cause)

                try {
                    repairIfCopyWasInterrupted()
                    if (state.populated) {
                        reconcile(changed.toList(), this.map)
                    }
                } catch (e: Exception) {
                    logger.error("Could not apply the changes recorded during the failed population of Hazelcast map $mapName; it will be rebuilt", e)
                    state.populated = false
                    cause.addSuppressed(e)
                } finally {
                    discardWork()
                }

            }

        } catch (e: Exception) {
            cause.addSuppressed(e)
        }

    }


    /** The one failure that empties the served map: it exceeds `maxRows`, so no set can be served (ticket 09). */
    private fun emptyAndMarkUnpopulated(token: String, cause: Throwable) {

        try {

            withStateLock {

                if (!stillOwns(token)) {
                    return@withStateLock
                }

                logger.warn("Population of Hazelcast map $mapName exceeded maxRows $maxRows; the map is left empty and unpopulated")
                discardWork()
                clearServedMap()
                state.populated = false
                state.shapeStamp = null

            }

        } catch (e: Exception) {
            cause.addSuppressed(e)
        }

    }


    /**
     * The node that ran a population is gone (or lost its lease). Mark the map unpopulated so a node populates it
     * again, but keep its rows served in the meantime unless the death interrupted a copy. Needs the state lock.
     */
    private fun discardAbandonedPopulation() {

        repairIfCopyWasInterrupted()
        discardWork()
        state.populated = false

    }


    /** A copy that never finished leaves a partial served map; clear it so it is not served. Needs the state lock. */
    private fun repairIfCopyWasInterrupted() {

        if (state.isCopying) {
            logger.warn("A copy into Hazelcast map $mapName was interrupted; clearing the partial set")
            clearServedMap()
            state.populated = false
        }

    }


    /** Clears the served map between two increments of the sequence, so readers never see it half cleared. */
    private fun clearServedMap() {

        val sequence = if (state.isCopying) state.swapSequence else state.swapSequence + 1
        state.swapSequence = sequence
        this.map.clear()
        state.swapSequence = sequence + 1

    }


    private fun discardWork() {

        staging.clear()
        changed.clear()
        state.populating = false

    }


    // ---- refresh and delete ---------------------------------------------------------------------------------


    /**
     * The after-commit auto-refresh (ADR-0004). A map that is not populated is left alone: the first fetch builds it
     * from the database, so a refresh must not trigger a population of its own. While a population is running anywhere
     * in the cluster the ids are recorded for the population to replay.
     */
    fun refreshByIds(ids: Collection<K>) {

        withStateLock {

            recoverFromDeadPopulator()

            if (state.populating) {
                changed.addAll(ids)
            } else if (state.populated) {
                reconcile(ids, this.map)
            }

        }

    }


    fun deleteById(id: K) {

        withStateLock {

            recoverFromDeadPopulator()

            if (state.populating) {
                changed.add(id)
            } else {
                this.map.delete(id)
            }

        }

    }


    private fun recoverFromDeadPopulator() {

        if (populatorDied()) {
            logger.warn("The node populating Hazelcast map $mapName left or lost its lock before it finished; discarding its partial work")
            discardAbandonedPopulation()
        }

    }


    /** Brings the [target] entries of [ids] in line with the database: present rows are stored, absent rows removed. */
    private fun reconcile(ids: Collection<K>, target: IMap<K, V>) {

        ids.chunked(CHUNK_SIZE).forEach { chunk ->

            val found = findByIds(chunk).associateBy(idOf)

            target.putAll(found)

            chunk.filterNot { it in found }.forEach { target.delete(it) }

        }

    }


    private fun <R> withStateLock(block: () -> R): R {

        control.lock(LOCK_STATE, STATE_LOCK_LEASE_MILLIS, TimeUnit.MILLISECONDS)

        try {
            return block()
        } finally {
            try {
                control.unlock(LOCK_STATE)
            } catch (e: IllegalMonitorStateException) {
                // the lease expired while this node hung inside the block
            }
        }

    }


    companion object {

        /** The number of ids refreshed per call, and of rows stored per call when a map is populated. */
        const val CHUNK_SIZE = 1000

        /** The property that sets the populate lock lease, in seconds. */
        const val LOCK_LEASE_PROPERTY = "maia.hazelcast-blotter.populate-lock-lease-seconds"

        const val DEFAULT_LEASE_MILLIS = 600_000L

        const val LOCK_WAIT_GRACE_MILLIS = 10_000L

        const val DEFAULT_READ_RETRY_MILLIS = 5L

        /** Re-reads of changed rows done outside the state lock before the rest is replayed under it. */
        const val MAX_REPLAY_PASSES = 5

        /** The state lock is only held for short, bounded work; the lease frees it if a node hangs inside. */
        private const val STATE_LOCK_LEASE_MILLIS = 120_000L

        private const val LOCK_POPULATE = "populate-lock"

        private const val LOCK_STATE = "state-lock"

        private const val POPULATED = "populated"

        private const val POPULATING = "populating"

        private const val POPULATION_COUNT = "populationCount"

        private const val SWAP_SEQUENCE = "swapSequence"

        private const val SHAPE_STAMP = "shapeStamp"

        private const val POPULATION_TOKEN = "populationToken"

        /** The lease in seconds from the application environment, or the default when the property is not set. */
        fun settingsFrom(leaseSecondsProperty: String?): Settings {

            return leaseSecondsProperty?.trim()?.toLongOrNull()?.let { Settings.fromLeaseSeconds(it) } ?: Settings()

        }

    }


}


/** A caller gave up waiting for the populate lock or for a read to become possible. */
class PopulationLockTimeoutException(message: String) : IllegalStateException(message)


/** The populate lock's lease expired under a population, so another node may have taken over; it was abandoned. */
class PopulationLockLostException(message: String) : IllegalStateException(message)
