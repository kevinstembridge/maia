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

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.HAZELCAST_IMAP)
        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportRaw("org.maiaframework.common.logging.getLogger")

        append("""
            |
            |
            |    private val logger = getLogger<${classDef.uqcn}>()
            |
            |
            |    private val map: IMap<DomainId, ${searchableDtoDef.uqcn}> = hazelcastInstance.getMap("${searchableDtoDef.hazelcastMapName}")
            |""".trimMargin())

    }


    override fun renderFunctions() {

        `render function findAll`()
        `render function populate`()

    }


    private fun `render function findAll`() {

        append("""
            |
            |
            |    fun findAll(): List<${searchableDtoDef.uqcn}> {
            |
            |        return this.map.values.toList()
            |
            |    }
            |""".trimMargin())

    }


    /**
     * Eager population: clears the map and refills it from the database, at application start and on demand. Runs in a
     * read-only transaction because the rows are streamed from the database.
     */
    private fun `render function populate`() {

        addImportFor(Fqcns.SPRING_TRANSACTIONAL)
        addImportRaw("org.springframework.context.event.ContextRefreshedEvent")
        addImportRaw("org.springframework.context.event.EventListener")

        append("""
            |
            |
            |    @EventListener(ContextRefreshedEvent::class)
            |    @Transactional(readOnly = true)
            |    fun populate() {
            |
            |        logger.info("BEGIN: Populating Hazelcast map ${searchableDtoDef.hazelcastMapName}")
            |
            |        this.map.clear()
            |
            |        var count = 0
            |
            |        this.dtoRepo.findAllAsSequence().chunked(POPULATE_CHUNK_SIZE).forEach { chunk ->
            |            this.map.putAll(chunk.associateBy { it.id })
            |            count += chunk.size
            |        }
            |
            |        logger.info("END: Populated Hazelcast map ${searchableDtoDef.hazelcastMapName} with ${'$'}count rows")
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
            |    }
            |""".trimMargin())

    }


}
