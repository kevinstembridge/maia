package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


/**
 * Renders the repo that owns the Hazelcast map of a Hazelcast-backed Searchable DTO (ADR-0008): one document per row,
 * keyed by the row id. Later refresh, population-mode, rebuild and authority work builds on this class.
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
            |    @Volatile
            |    private var populated = false
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
            |                POPULATION_LAZY -> {
            |                    this.map.clear()
            |                    this.populated = false
            |                    logger.info("Hazelcast map ${searchableDtoDef.hazelcastMapName} will be populated by the first fetch")
            |                }
            |                else -> populate()
            |            }
            |
            |        }
            |
            |    }
            |""".trimMargin())

    }


    /**
     * The single population entry point: clears the map and refills it from the database. Runs in a read-only
     * transaction because the rows are streamed from the database.
     */
    private fun `render function populate`() {

        append("""
            |
            |
            |    fun populate() {
            |
            |        logger.info("BEGIN: Populating Hazelcast map ${searchableDtoDef.hazelcastMapName}")
            |
            |        this.map.clear()
            |
            |        var count = 0
            |
            |        this.readOnlyTransaction.executeWithoutResult {
            |            this.dtoRepo.findAllAsSequence().chunked(POPULATE_CHUNK_SIZE).forEach { chunk ->
            |                this.map.putAll(chunk.associateBy { it.id })
            |                count += chunk.size
            |            }
            |        }
            |
            |        this.populated = true
            |
            |        logger.info("END: Populated Hazelcast map ${searchableDtoDef.hazelcastMapName} with ${'$'}count rows")
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

        append("""
            |
            |
            |    private companion object {
            |
            |        const val POPULATE_CHUNK_SIZE = 1000
            |
            |        const val POPULATION_PROPERTY = "maia.hazelcast-blotter.population"
            |
            |        const val POPULATION_EAGER = "eager"
            |
            |        const val POPULATION_LAZY = "lazy"
            |
            |    }
            |""".trimMargin())

    }


}
