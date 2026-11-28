package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.HazelcastSpec
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.EntityRefFieldType
import java.security.MessageDigest


/**
 * Renders the repo that owns the Hazelcast map of a Hazelcast-backed Searchable DTO (ADR-0008): one document per row,
 * keyed by the row id. The repo is wiring: it hands the database reads (`findAllAsSequence`, `findByIds`), the map name,
 * `maxRows` and the document-shape stamp to a `HazelcastClusterPopulator` (libs/maia-hazelcast), which owns the whole
 * cluster population protocol (locks, staging, replay, publication, failure and take-over rules) and is tested once, in
 * the library. The repo fills the map from the database (eagerly at start or lazily on the first fetch, per the
 * population mode), keeps it in step through the after-commit refresh hooks, and is the target of the rebuild service.
 *
 * The shape stamp is a hash of the field names and types of the DTO the map holds, generated per DTO. A node leaves
 * an already populated map alone at start only if the stamp stored with it equals its own.
 */
class HazelcastBlotterRepoRenderer(
    private val searchableDtoDef: SearchableDtoDef
) : AbstractKotlinRenderer(
    searchableDtoDef.hazelcastRepoClassDef
) {


    private val mapName = searchableDtoDef.hazelcastMapName


    init {

        addConstructorArg(aClassField("dtoRepo", searchableDtoDef.dtoRepoClassDef.fqcn).privat().build())
        addConstructorArg(aClassField("hazelcastInstance", Fqcns.HAZELCAST_INSTANCE).privat().build())
        addConstructorArg(aClassField("environment", Fqcns.SPRING_ENVIRONMENT).privat().build())
        addConstructorArg(aClassField("transactionManager", Fqcns.SPRING_PLATFORM_TRANSACTION_MANAGER).privat().build())

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportRaw("org.maiaframework.common.logging.getLogger")
        addImportRaw("org.maiaframework.hazelcast.population.HazelcastClusterPopulator")
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
            |    private val populator = HazelcastClusterPopulator<DomainId, ${searchableDtoDef.uqcn}>(
            |        hazelcastInstance = hazelcastInstance,
            |        mapName = "$mapName",
            |        dtoName = "${searchableDtoDef.uqcn}",
            |        shapeStamp = SHAPE_STAMP,
            |        maxRows = MAX_ROWS,
            |        idOf = { it.id },
            |        findAllAsSequence = { dtoRepo.findAllAsSequence() },
            |        findByIds = { ids -> dtoRepo.findByIds(ids) },
            |        inReadTransaction = { block -> readOnlyTransaction.executeWithoutResult { block() } },
            |        settings = HazelcastClusterPopulator.settingsFrom(environment.getProperty(HazelcastClusterPopulator.LOCK_LEASE_PROPERTY))
            |    )
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
     * The first fetch of an unpopulated map (in any node of the cluster) fills it; every later fetch, on every node,
     * just reads it. A rebuild running elsewhere does not block the read: the previous complete set is served.
     */
    private fun `render function findAll`() {

        append("""
            |
            |
            |    fun findAll(): List<${searchableDtoDef.uqcn}> {
            |
            |        return this.populator.findAll()
            |
            |    }
            |""".trimMargin())

    }


    /**
     * Application start, and any repeated context-refresh event on the same node: idempotent. Eager mode makes sure the
     * map is populated, which is a no-op when it already is, by this node's document shape (a different shape stamp in
     * the map means it is rebuilt); lazy mode leaves it for the first fetch (a different shape stamp marks the map
     * unpopulated so that the first fetch rebuilds it). A same-shape join, restart or repeated event never wipes or
     * rebuilds a populated map. The only node of a fresh cluster finds an empty, unstamped map and so populates it
     * (eager) or leaves it empty (lazy), exactly as a single node always did.
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
            |        this.populator.onStart(eager = mode == POPULATION_EAGER)
            |
            |        if (mode == POPULATION_LAZY) {
            |            logger.info("Hazelcast map $mapName will be populated by the first fetch")
            |        }
            |
            |    }
            |""".trimMargin())

    }


    /**
     * Refills the map from the database. The single rebuild entry point, also used by the rebuild service. It always
     * runs its own population, even when it had to wait behind another node's: the data then reflects the database as
     * of the call. A failure leaves the previous complete set served and is rethrown; a set larger than `maxRows`
     * is never truncated, and leaves the map empty and unpopulated for every node.
     */
    private fun `render function populate`() {

        append("""
            |
            |
            |    fun populate() {
            |
            |        this.populator.rebuild()
            |
            |    }
            |""".trimMargin())

    }


    /**
     * The after-commit auto-refresh hooks (ADR-0004): the listeners enlist ids with the collector, which calls
     * [refreshByIds] once the owning transaction has committed. A map that is not populated is left alone, and while a
     * population is running anywhere in the cluster the ids are recorded for it to replay.
     */
    private fun `render functions refreshByIds and deleteById`() {

        append("""
            |
            |
            |    override fun refreshByIds(ids: Collection<DomainId>) {
            |
            |        this.populator.refreshByIds(ids)
            |
            |    }
            |
            |
            |    override fun deleteById(id: DomainId) {
            |
            |        this.populator.deleteById(id)
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
            |        /** Identifies the document shape of ${searchableDtoDef.uqcn}: a hash of its field names and types. */
            |        const val SHAPE_STAMP = "${shapeStamp()}"
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


    /** A hash of the names, types and nullability of the fields of the stored DTO, including those of embedded entity refs. */
    private fun shapeStamp(): String {

        val description = describe(searchableDtoDef.dtoDef.allFieldsSorted)

        val digest = MessageDigest.getInstance("SHA-256").digest(description.toByteArray())

        return digest.take(8).joinToString("") { "%02x".format(it) }

    }


    private fun describe(fields: List<ClassFieldDef>): String {

        return fields.joinToString(";") { field ->

            val type = field.fieldType

            val nested = if (type is EntityRefFieldType) "{" + describe(type.entityRefDef.dtoDef.allFieldsSorted) + "}" else ""

            "${field.classFieldName}:${field.fqcn}:${field.nullable}$nested"

        }

    }


}
