package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.HazelcastSpec
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


/**
 * Renders the repo that owns the Hazelcast map of a Hazelcast-backed Searchable DTO (ADR-0008): one document per row,
 * keyed by the row id. The repo fills the map from the database (eagerly at start or lazily on the first fetch, per the
 * population mode), keeps it in step through the after-commit refresh hooks, and is the target of the rebuild service.
 *
 * The repo is cluster-aware: every piece of coordination state lives in Hazelcast, none in the JVM, so any number of
 * nodes sharing one Hazelcast cluster behave as one. It needs no CP subsystem; it uses IMap key locks, which Hazelcast
 * releases when the owning member leaves the cluster.
 *
 * - `<map>` holds the served rows. `<map>__staging` receives a population while viewers keep reading `<map>`.
 *   `<map>__changed` holds the ids refreshed or deleted while a population was running, whichever node they came from.
 *   `<map>__control` holds the cluster-wide facts: `populated`, `populating`, `epoch` (completed populations) and
 *   `swap` (a sequence number that is odd while the staged set is being copied into `<map>`).
 * - Two key locks on the control map: the populate lock, held for a whole population (one populator cluster-wide),
 *   and the state lock, held briefly around every change to the flags or to `<map>` (refresh, delete, completion).
 *   Lock order: populate lock, then state lock.
 * - A population streams the database into the staging map, so the served map stays the previous complete set. On
 *   completion, under the state lock, the ids changed meanwhile are re-read from the database into the staging map
 *   (a deleted row is absent there, so it cannot be resurrected), then the staged set is copied into the served map
 *   between two bumps of `swap`. Readers retry a read that overlapped a swap, so they see the old or the new complete set.
 * - A node that dies mid-population releases its locks with it; the next node to notice (`populating` set, populate
 *   lock free) discards the half-done state, so the map is not left marked populated.
 */
class HazelcastBlotterRepoRenderer(
    private val searchableDtoDef: SearchableDtoDef
) : AbstractKotlinRenderer(
    searchableDtoDef.hazelcastRepoClassDef
) {



    init {

        addConstructorArg(aClassField("dtoRepo", searchableDtoDef.dtoRepoClassDef.fqcn).privat().build())
        addConstructorArg(aClassField("hazelcastInstance", Fqcns.HAZELCAST_INSTANCE).privat().build())
        addConstructorArg(aClassField("environment", Fqcns.SPRING_ENVIRONMENT).privat().build())
        addConstructorArg(aClassField("transactionManager", Fqcns.SPRING_PLATFORM_TRANSACTION_MANAGER).privat().build())

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.HAZELCAST_IMAP)
        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportRaw("org.maiaframework.common.logging.getLogger")
        addImportRaw("org.springframework.transaction.support.TransactionTemplate")
        addImportRaw("org.maiaframework.hazelcast.refresh.HazelcastRefreshCollector")

        append("""
            |
            |
            |    private val logger = getLogger<${classDef.uqcn}>()
            |
            |
            |    private val readOnlyTransaction = TransactionTemplate(transactionManager).apply { isReadOnly = true }
            |
            |
            |    private val map: IMap<DomainId, ${searchableDtoDef.uqcn}> = hazelcastInstance.getMap("${searchableDtoDef.hazelcastMapName}")
            |
            |
            |    private val staging: IMap<DomainId, ${searchableDtoDef.uqcn}> = hazelcastInstance.getMap("${searchableDtoDef.hazelcastMapName}__staging")
            |
            |
            |    private val changed: IMap<DomainId, Boolean> = hazelcastInstance.getMap("${searchableDtoDef.hazelcastMapName}__changed")
            |
            |
            |    @Volatile
            |    private var started = false
            |
            |
            |    private val control: IMap<String, Any> = hazelcastInstance.getMap("${searchableDtoDef.hazelcastMapName}__control")
            |""".trimMargin())

    }


    override fun renderFunctions() {

        `render function findAll`()
        `render function onContextRefreshed`()
        `render function populate`()
        `render function populationMode`()
        `render cluster state helpers`()
        `render functions refreshByIds and deleteById`()

    }


    /**
     * The first fetch of an unpopulated map (in any node of the cluster) fills it; every later fetch, on every node,
     * just reads it. A rebuild running elsewhere does not block the read: the previous complete set is served.
     */
    private fun `render function findAll`() {

        append("""
            |
            |
            |    fun findAll(): List<${searchableDtoDef.uqcn}> {
            |
            |        val deadline = System.currentTimeMillis() + READ_TIMEOUT_MILLIS
            |
            |        while (true) {
            |
            |            if (!isPopulatedAndHealthy()) {
            |                populate(force = false)
            |            }
            |
            |            readServedSet()?.let { return it }
            |
            |            check(System.currentTimeMillis() < deadline) { "Timed out reading Hazelcast map ${searchableDtoDef.hazelcastMapName}" }
            |
            |            Thread.sleep(RETRY_MILLIS)
            |
            |        }
            |
            |    }
            |
            |
            |    /**
            |     * The served map, or null when the read has to be retried because it overlapped the copy of a newly
            |     * populated set into the map (a read that overlapped a copy might hold a partial set and is discarded).
            |     */
            |    private fun readServedSet(): List<${searchableDtoDef.uqcn}>? {
            |
            |        val before = swapCount()
            |
            |        if (before % 2L == 1L) {
            |            return null
            |        }
            |
            |        val rows = this.map.values.toList()
            |
            |        return if (swapCount() == before) rows else null
            |
            |    }
            |""".trimMargin())

    }


    /**
     * Application start. A node that joins a cluster with other members leaves the shared map alone: another node is
     * serving it or populating it, and in eager mode this node only makes sure it ends up populated, which is a no-op
     * when it already is. The only member of its cluster treats the map as derived data and discards whatever a
     * previous run left behind: eager mode then refills it from the database, lazy mode leaves it empty until the
     * first fetch. A later refresh event on a node that has already started (the context refreshed in place) is not a
     * join: it re-derives the map as the sole-member start does, which is safe for viewers on other nodes because
     * they keep reading the previous complete set until the new one is published.
     */
    private fun `render function onContextRefreshed`() {

        addImportRaw("org.springframework.context.event.ContextRefreshedEvent")
        addImportRaw("org.springframework.context.event.EventListener")

        append("""
            |
            |
            |    @EventListener(ContextRefreshedEvent::class)
            |    fun onContextRefreshed() {
            |
            |        val mode = populationMode()
            |
            |        require(mode == POPULATION_EAGER || mode == POPULATION_LAZY) {
            |            "Invalid value '${'$'}mode' for ${'$'}POPULATION_PROPERTY; valid values are ${'$'}POPULATION_VALUES"
            |        }
            |
            |        val firstEvent = !this.started
            |        this.started = true
            |
            |        val joiningCluster = firstEvent && this.hazelcastInstance.cluster.members.size > 1
            |
            |        if (mode == POPULATION_EAGER) {
            |            populate(force = !joiningCluster)
            |            return
            |        }
            |
            |        if (!joiningCluster) {
            |            resetToUnpopulated()
            |        }
            |
            |        logger.info("Hazelcast map ${searchableDtoDef.hazelcastMapName} will be populated by the first fetch")
            |
            |    }
            |
            |
            |    private fun resetToUnpopulated() {
            |
            |        this.control.lock(LOCK_POPULATE)
            |
            |        try {
            |            withStateLock { abandonPopulation() }
            |        } finally {
            |            this.control.unlock(LOCK_POPULATE)
            |        }
            |
            |    }
            |""".trimMargin())

    }


    /**
     * Clears the map and refills it from the database. The single population entry point, also used by the rebuild
     * service. Only one node populates at a time, cluster-wide: a caller that had to wait for another node's population
     * finds the map freshly populated and does not run another. A set larger than `maxRows` is never truncated: it is
     * detected while streaming, before the chunk that would exceed the limit is stored, and population fails with the
     * map left empty and unpopulated for every node, so the next fetch (on any node) tries again.
     */
    private fun `render function populate`() {

        addImportRaw("org.maiaframework.common.BlotterTooLargeException")

        append("""
            |
            |
            |    fun populate() {
            |
            |        populate(force = true)
            |
            |    }
            |
            |
            |    /** [force]: rebuild even a populated map, unless another node's population completed while this call waited. */
            |    private fun populate(force: Boolean) {
            |
            |        val epochOnEntry = epoch()
            |
            |        this.control.lock(LOCK_POPULATE)
            |
            |        try {
            |
            |            withStateLock {
            |                if (isPopulating()) {
            |                    logger.warn("The node populating Hazelcast map ${searchableDtoDef.hazelcastMapName} left before it finished; discarding its partial work")
            |                    abandonPopulation()
            |                }
            |            }
            |
            |            val upToDate = if (force) epoch() != epochOnEntry else isPopulated()
            |
            |            if (!upToDate) {
            |                runPopulation()
            |            }
            |
            |        } finally {
            |            this.control.unlock(LOCK_POPULATE)
            |        }
            |
            |    }
            |
            |
            |    /** Called with the populate lock held. */
            |    private fun runPopulation() {
            |
            |        logger.info("BEGIN: Populating Hazelcast map ${searchableDtoDef.hazelcastMapName}")
            |
            |        withStateLock {
            |            this.control.set(POPULATING, true)
            |            this.staging.clear()
            |            this.changed.clear()
            |        }
            |
            |        var count = 0
            |
            |        try {
            |
            |            this.readOnlyTransaction.executeWithoutResult {
            |                this.dtoRepo.findAllAsSequence().chunked(HazelcastRefreshCollector.CHUNK_SIZE).forEach { chunk ->
            |                    if (count + chunk.size > MAX_ROWS) {
            |                        throw BlotterTooLargeException("${searchableDtoDef.uqcn}", MAX_ROWS)
            |                    }
            |                    this.staging.putAll(chunk.associateBy { it.id })
            |                    count += chunk.size
            |                }
            |            }
            |
            |            withStateLock { completePopulation() }
            |
            |        } catch (e: Exception) {
            |            withStateLock { abandonPopulation() }
            |            throw e
            |        }
            |
            |        logger.info("END: Populated Hazelcast map ${searchableDtoDef.hazelcastMapName} with ${'$'}count rows")
            |
            |    }
            |
            |
            |    /**
            |     * Replays what changed while the population streamed (those rows are re-read, so the staged set ends up
            |     * current), then publishes the staged set. Called with the state lock held.
            |     */
            |    private fun completePopulation() {
            |
            |        reconcile(this.changed.keys.toList(), this.staging)
            |
            |        val swap = swapCount()
            |        this.control.set(SWAP, swap + 1)
            |        this.map.clear()
            |        this.staging.keys.chunked(HazelcastRefreshCollector.CHUNK_SIZE).forEach { ids ->
            |            this.map.putAll(this.staging.getAll(ids.toSet()))
            |        }
            |        this.control.set(SWAP, swap + 2)
            |
            |        this.staging.clear()
            |        this.changed.clear()
            |        this.control.set(EPOCH, epoch() + 1)
            |        this.control.set(POPULATING, false)
            |        this.control.set(POPULATED, true)
            |
            |    }
            |
            |
            |    /** Leaves the map empty and unpopulated for every node. Called with the state lock held. */
            |    private fun abandonPopulation() {
            |
            |        this.map.clear()
            |        this.staging.clear()
            |        this.changed.clear()
            |
            |        val swap = swapCount()
            |        if (swap % 2L == 1L) {
            |            this.control.set(SWAP, swap + 1)
            |        }
            |
            |        this.control.set(POPULATING, false)
            |        this.control.set(POPULATED, false)
            |
            |    }
            |""".trimMargin())

    }


    /** The cluster-wide facts, kept in the control map so that every node sees the same state. */
    private fun `render cluster state helpers`() {

        append("""
            |
            |
            |    private fun isPopulated(): Boolean = this.control.get(POPULATED) == true
            |
            |
            |    private fun isPopulating(): Boolean = this.control.get(POPULATING) == true
            |
            |
            |    private fun epoch(): Long = (this.control.get(EPOCH) as Long?) ?: 0L
            |
            |
            |    private fun swapCount(): Long = (this.control.get(SWAP) as Long?) ?: 0L
            |
            |
            |    /**
            |     * Populated, and no population left behind by a node that has since left the cluster. A running
            |     * population holds the populate lock, so `populating` without the lock means its node died.
            |     */
            |    private fun isPopulatedAndHealthy(): Boolean {
            |
            |        return isPopulated() && (!isPopulating() || this.control.isLocked(LOCK_POPULATE))
            |
            |    }
            |
            |
            |    private fun <T> withStateLock(block: () -> T): T {
            |
            |        this.control.lock(LOCK_STATE)
            |
            |        try {
            |            return block()
            |        } finally {
            |            this.control.unlock(LOCK_STATE)
            |        }
            |
            |    }
            |
            |
            |    /** True when a population is recorded as running but its node no longer holds the populate lock. Needs the state lock. */
            |    private fun populatorDied(): Boolean {
            |
            |        return isPopulating() && !this.control.isLocked(LOCK_POPULATE)
            |
            |    }
            |""".trimMargin())

    }


    /**
     * The after-commit auto-refresh hooks (ADR-0004): the listeners enlist ids with the collector, which calls
     * [refreshByIds] once the owning transaction has committed. A map that is not populated (lazy mode, before the first
     * fetch, on any node) is left alone: the first fetch builds it from the database, so there is nothing to keep in
     * step, and a refresh must not trigger a population of its own. While a population is running anywhere in the
     * cluster the ids are recorded for the population to replay.
     */
    private fun `render functions refreshByIds and deleteById`() {

        append("""
            |
            |
            |    override fun refreshByIds(ids: Collection<DomainId>) {
            |
            |        withStateLock {
            |
            |            if (populatorDied()) {
            |                abandonPopulation()
            |            }
            |
            |            if (isPopulating()) {
            |                recordChanged(ids)
            |            } else if (isPopulated()) {
            |                reconcile(ids, this.map)
            |            }
            |
            |        }
            |
            |    }
            |
            |
            |    override fun deleteById(id: DomainId) {
            |
            |        withStateLock {
            |
            |            if (populatorDied()) {
            |                abandonPopulation()
            |            }
            |
            |            if (isPopulating()) {
            |                recordChanged(listOf(id))
            |            } else {
            |                this.map.delete(id)
            |            }
            |
            |        }
            |
            |    }
            |
            |
            |    private fun recordChanged(ids: Collection<DomainId>) {
            |
            |        this.changed.putAll(ids.associateWith { true })
            |
            |    }
            |
            |
            |    /** Brings the [target] entries of [ids] in line with the database: present rows are stored, absent rows removed. */
            |    private fun reconcile(ids: Collection<DomainId>, target: IMap<DomainId, ${searchableDtoDef.uqcn}>) {
            |
            |        ids.chunked(HazelcastRefreshCollector.CHUNK_SIZE).forEach { chunk ->
            |
            |            val found = this.dtoRepo.findByIds(chunk).associateBy { it.id }
            |
            |            target.putAll(found)
            |
            |            chunk.filterNot { it in found }.forEach { target.delete(it) }
            |
            |        }
            |
            |    }
            |""".trimMargin())

    }


    /**
     * The mode comes from the Searchable DTO's own declaration when it has one, otherwise from the global property.
     */
    private fun `render function populationMode`() {

        val override = searchableDtoDef.hazelcastSpec?.population

        val body = if (override != null) {
            "\"${override.propertyValue}\""
        } else {
            "this.environment.getProperty(POPULATION_PROPERTY, POPULATION_EAGER)"
        }

        append("""
            |
            |
            |    private fun populationMode(): String {
            |
            |        return $body
            |
            |    }
            |""".trimMargin())

    }


    override fun renderCompanionObject() {

        val maxRows = searchableDtoDef.hazelcastSpec?.maxRows ?: HazelcastSpec.DEFAULT_MAX_ROWS

        append("""
            |
            |
            |    private companion object {
            |
            |        const val MAX_ROWS = $maxRows
            |
            |        const val POPULATION_PROPERTY = "maia.hazelcast-blotter.population"
            |
            |        const val POPULATION_EAGER = "eager"
            |
            |        const val POPULATION_LAZY = "lazy"
            |
            |        const val POPULATION_VALUES = "eager|lazy"
            |
            |        const val LOCK_POPULATE = "populate-lock"
            |
            |        const val LOCK_STATE = "state-lock"
            |
            |        const val POPULATED = "populated"
            |
            |        const val POPULATING = "populating"
            |
            |        const val EPOCH = "epoch"
            |
            |        const val SWAP = "swap"
            |
            |        const val READ_TIMEOUT_MILLIS = 60_000L
            |
            |        const val RETRY_MILLIS = 5L
            |
            |    }
            |""".trimMargin())

    }


}
