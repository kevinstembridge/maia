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
 * Concurrency: [populate] is serialised by `populateLock`; `stateLock` guards the `populated`/`populating` flags and
 * every change to the map made by a refresh or delete. A refresh or delete that arrives while a population is
 * streaming is buffered and replayed (re-read from the database) when the population completes, so a write committed
 * during a population is never lost to the older snapshot the population read. Lock order: `populateLock`, then `stateLock`.
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
            |    private val populateLock = Any()
            |
            |
            |    private val stateLock = Any()
            |
            |
            |    @Volatile
            |    private var populated = false
            |
            |
            |    // guarded by stateLock
            |    private var populating = false
            |
            |
            |    // guarded by stateLock: ids refreshed or deleted while a population was streaming
            |    private val changedWhilePopulating = LinkedHashSet<DomainId>()
            |
            |
            |    private val map: IMap<DomainId, ${searchableDtoDef.uqcn}> = hazelcastInstance.getMap("${searchableDtoDef.hazelcastMapName}")
            |""".trimMargin())

    }


    override fun renderFunctions() {

        `render function findAll`()
        `render function onContextRefreshed`()
        `render function populate`()
        `render function populationMode`()
        `render functions refreshByIds and deleteById`()

    }


    /**
     * In lazy mode the first fetch after start fills the map; every later fetch just reads it.
     */
    private fun `render function findAll`() {

        append("""
            |
            |
            |    fun findAll(): List<${searchableDtoDef.uqcn}> {
            |
            |        if (!this.populated) {
            |            synchronized(this.populateLock) {
            |                if (!this.populated) {
            |                    populate()
            |                }
            |            }
            |        }
            |
            |        return this.map.values.toList()
            |
            |    }
            |""".trimMargin())

    }


    /**
     * Application start: the map is derived data, so whatever a previous run left behind is discarded. Eager mode then
     * refills it from the database; lazy mode leaves it empty until the first fetch.
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
            |        synchronized(this.populateLock) {
            |
            |            when (populationMode()) {
            |                POPULATION_EAGER -> populate()
            |                POPULATION_LAZY -> {
            |                    synchronized(this.stateLock) {
            |                        this.map.clear()
            |                        this.populated = false
            |                        this.changedWhilePopulating.clear()
            |                    }
            |                    logger.info("Hazelcast map ${searchableDtoDef.hazelcastMapName} will be populated by the first fetch")
            |                }
            |                else -> error("Invalid value '${'$'}{populationMode()}' for ${'$'}POPULATION_PROPERTY; valid values are ${'$'}POPULATION_VALUES")
            |            }
            |
            |        }
            |
            |    }
            |""".trimMargin())

    }


    /**
     * The single population entry point, also used by the rebuild service: clears the map and refills it from the
     * database. Runs in a read-only transaction because the rows are streamed from the database. A set larger than
     * `maxRows` is never truncated: it is detected while streaming, before the chunk that would exceed the limit is
     * stored, and population fails with the map left empty and unpopulated so the next fetch tries again.
     */
    private fun `render function populate`() {

        addImportRaw("org.maiaframework.common.BlotterTooLargeException")

        append("""
            |
            |
            |    fun populate() {
            |
            |        synchronized(this.populateLock) {
            |
            |            logger.info("BEGIN: Populating Hazelcast map ${searchableDtoDef.hazelcastMapName}")
            |
            |            beginPopulation()
            |
            |            var count = 0
            |
            |            try {
            |                this.readOnlyTransaction.executeWithoutResult {
            |                    this.dtoRepo.findAllAsSequence().chunked(HazelcastRefreshCollector.CHUNK_SIZE).forEach { chunk ->
            |                        if (count + chunk.size > MAX_ROWS) {
            |                            throw BlotterTooLargeException("${searchableDtoDef.uqcn}", MAX_ROWS)
            |                        }
            |                        this.map.putAll(chunk.associateBy { it.id })
            |                        count += chunk.size
            |                    }
            |                }
            |                completePopulation()
            |            } catch (e: Exception) {
            |                abandonPopulation()
            |                throw e
            |            }
            |
            |            logger.info("END: Populated Hazelcast map ${searchableDtoDef.hazelcastMapName} with ${'$'}count rows")
            |
            |        }
            |
            |    }
            |
            |
            |    private fun beginPopulation() {
            |
            |        synchronized(this.stateLock) {
            |            this.populated = false
            |            this.populating = true
            |            this.changedWhilePopulating.clear()
            |            this.map.clear()
            |        }
            |
            |    }
            |
            |
            |    /** Replays what changed while the population streamed: those rows are re-read, so the map ends up current. */
            |    private fun completePopulation() {
            |
            |        synchronized(this.stateLock) {
            |            reconcile(this.changedWhilePopulating.toList())
            |            this.changedWhilePopulating.clear()
            |            this.populating = false
            |            this.populated = true
            |        }
            |
            |    }
            |
            |
            |    private fun abandonPopulation() {
            |
            |        synchronized(this.stateLock) {
            |            this.map.clear()
            |            this.changedWhilePopulating.clear()
            |            this.populating = false
            |            this.populated = false
            |        }
            |
            |    }
            |""".trimMargin())

    }


    /**
     * The after-commit auto-refresh hooks (ADR-0004): the listeners enlist ids with the collector, which calls
     * [refreshByIds] once the owning transaction has committed. A map that is not populated (lazy mode, before the first
     * fetch) is left alone: the first fetch builds it from the database, so there is nothing to keep in step, and a
     * refresh must not trigger a population of its own.
     */
    private fun `render functions refreshByIds and deleteById`() {

        append("""
            |
            |
            |    override fun refreshByIds(ids: Collection<DomainId>) {
            |
            |        synchronized(this.stateLock) {
            |
            |            if (this.populating) {
            |                this.changedWhilePopulating.addAll(ids)
            |                return
            |            }
            |
            |            if (!this.populated) {
            |                return
            |            }
            |
            |            reconcile(ids)
            |
            |        }
            |
            |    }
            |
            |
            |    override fun deleteById(id: DomainId) {
            |
            |        synchronized(this.stateLock) {
            |
            |            if (this.populating) {
            |                this.changedWhilePopulating.add(id)
            |                return
            |            }
            |
            |            this.map.delete(id)
            |
            |        }
            |
            |    }
            |
            |
            |    /** Brings the map entries of [ids] in line with the database: present rows are stored, absent rows removed. */
            |    private fun reconcile(ids: Collection<DomainId>) {
            |
            |        ids.chunked(HazelcastRefreshCollector.CHUNK_SIZE).forEach { chunk ->
            |
            |            val found = this.dtoRepo.findByIds(chunk).associateBy { it.id }
            |
            |            this.map.putAll(found)
            |
            |            chunk.filterNot { it in found }.forEach { this.map.delete(it) }
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
            |    }
            |""".trimMargin())

    }


}
