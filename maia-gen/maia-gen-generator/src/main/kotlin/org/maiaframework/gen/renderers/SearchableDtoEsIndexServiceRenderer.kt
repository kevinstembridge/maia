package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


// Keep in sync with TypeaheadIndexServiceRenderer; extract a shared base when a third consumer appears.
/**
 * Renders the index service for an EsDoc derived from a searchable DTO. Unlike the typeahead index service it
 * refreshes by id collection and implements [Fqcns.ES_REFRESH_TARGET].
 */
class SearchableDtoEsIndexServiceRenderer(
    private val esDocDef: EsDocDef
) : AbstractKotlinRenderer(
    esDocDef.indexServiceClassDef
) {


    init {

        addConstructorArg(aClassField("elasticClient", Fqcns.ELASTIC_CLIENT).privat().build())
        addConstructorArg(aClassField("esIndexOps", Fqcns.MAIA_ES_INDEX_OPS).privat().build())
        addConstructorArg(aClassField("props", Fqcns.MAIA_PROPS).privat().build())
        addConstructorArg(aClassField("esIndexControl", this.esDocDef.esIndexControlClassDef.fqcn).privat().build())
        addConstructorArg(aClassField("esDocRepo", this.esDocDef.esDocRepoClassDef.fqcn).privat().build())
        addConstructorArg(aClassField("indexNameFactory", Fqcns.ES_INDEX_NAME_FACTORY).constructorOnly().build())
        addConstructorArg(aClassField("jsonMapper", Fqcns.JACKSON_JSON_MAPPER).privat().build())

    }


    override fun renderSource(): String {

        val classKdoc = """
            |/**
            | * Generated. Rebuilds [${this.esDocDef.uqcn}] documents by id from the current database state, and deletes
            | * the documents whose source row no longer exists or no longer matches the searchable DTO's joins and type discriminator.
            | */
            |""".trimMargin()

        val marker = "\n@Component\n"
        val rendered = super.renderSource()

        check(rendered.contains(marker)) { "Could not inject class KDoc into ${this.classDef.fqcn}: expected annotation marker not found" }

        return rendered.replaceFirst(marker, "\n$classKdoc@Component\n")

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.SLF4J_LOGGER_FACTORY)

        append("""
            |
            |
            |    private val logger = LoggerFactory.getLogger(${this.esDocDef.indexServiceClassDef.uqcn}::class.java)
            |
            |
            |    private val indexResolvedName = indexNameFactory.indexNameFrom(${this.esDocDef.esDocMetaClassDef.uqcn}.indexBaseNameAndVersion)
            |""".trimMargin())

    }


    override fun renderFunctions() {

        `render function refreshByIds`()
        `render function ensureIndexExists`()
        `render function deleteById`()
        `render function refreshIndex`()
        `render function upsertAllCurrentRecords`()
        `render function removeDeletedRecordsForIndex`()
        `render function buildEsDocHolder`()

    }


    private fun `render function refreshByIds`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        append($$"""
            |
            |
            |    /**
            |     * Invoked from EsRefreshCollector's afterCommit callback. Read-only: it must never write through JDBC,
            |     * because writes made in afterCommit would not be committed.
            |     */
            |    override fun refreshByIds(ids: Collection<DomainId>) {
            |
            |        logger.debug("BEGIN: Refreshing index ${this.indexResolvedName} for ${ids.size} ids")
            |
            |        val docs = this.esDocRepo.findByIds(ids)
            |        val found = docs.map { it.$${this.esDocDef.idField.classFieldName}.value }.toSet()
            |
            |        if (docs.isNotEmpty()) {
            |            ensureIndexExists()
            |            this.esIndexOps.bulkUpsert(docs.map { buildEsDocHolder(it) })
            |        }
            |
            |        // rows that were deleted, or that no longer match the DTO's joins or type discriminator
            |        val missing = ids.map { it.value }.filter { it !in found }
            |
            |        if (missing.isNotEmpty()) {
            |            this.esIndexOps.deleteByIds(missing, this.indexResolvedName)
            |        }
            |
            |        logger.debug("END: Refreshing index ${this.indexResolvedName} for ${ids.size} ids")
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function ensureIndexExists`() {

        addImportFor(Fqcns.ELASTICSEARCH_EXCEPTION)

        append($$"""
            |
            |
            |    /**
            |     * Elasticsearch auto-creates a missing index on the first write, with dynamic mappings: no `raw` keyword
            |     * sub-field and possibly text-mapped dates. Creating the index from its generated type mapping first keeps
            |     * the mapping correct however the first write arrives.
            |     */
            |    private fun ensureIndexExists() {
            |
            |        if (this.elasticClient.indices().exists { r -> r.index(this.indexResolvedName.value) }.value()) {
            |            return
            |        }
            |
            |        logger.info("Index ${this.indexResolvedName} does not exist; creating it from its type mapping")
            |
            |        try {
            |            this.esIndexControl.createIndex()
            |        } catch (e: ElasticsearchException) {
            |            // created concurrently, e.g. by an admin or another node
            |            if (e.error().type() != "resource_already_exists_exception") {
            |                throw e
            |            }
            |        }
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function deleteById`() {

        append($$"""
            |
            |
            |    override fun deleteById(id: DomainId) {
            |
            |        logger.debug("BEGIN: Deleting from index ${this.indexResolvedName} for id $id")
            |
            |        val deleteResponse = this.esIndexOps.deleteById(id.value, this.indexResolvedName)
            |
            |        logger.debug("END: Deleting from index ${this.indexResolvedName} for id $id with result ${deleteResponse.result()}")
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function refreshIndex`() {

        addImportFor(Fqcns.MAIA_JOB_METRICS)
        addImportFor(Fqcns.SPRING_TRANSACTIONAL)

        append($$"""
            |
            |
            |    /**
            |     * Runs in a read-only transaction because the sequence of documents is streamed from the database
            |     * and Postgres only streams through a cursor inside a transaction. Not suspending, because Spring
            |     * cannot apply a blocking transaction to a suspending function.
            |     */
            |    @Transactional(readOnly = true)
            |    fun refreshIndex(jm: JobMetrics) {
            |
            |        logger.info("BEGIN: Refresh index ${this.indexResolvedName}")
            |
            |        ensureIndexExists()
            |
            |        val currentIds = upsertAllCurrentRecords(jm)
            |        removeDeletedRecordsFromIndex(currentIds, jm)
            |
            |        logger.info("END: Refresh index ${this.indexResolvedName}")
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function upsertAllCurrentRecords`() {

        blankLine()
        blankLine()
        appendLine("    private fun upsertAllCurrentRecords(jm: JobMetrics): Set<String> {")
        blankLine()
        appendLine("        val upsertJob = jm.getOrCreateChildJob(\"upsertChunk\")")
        appendLine("        val chunkSize = this.props.getIntOrNull(\"${this.classDef.uqcn.firstToLower()}.bulkUpsert.chunkSize\") ?: 1000")
        blankLine()
        appendLine("        return this.esDocRepo.findAllAsSequence().chunked(chunkSize).map { chunkOfEsDocs ->")
        blankLine()
        appendLine("            val esDocHolders = chunkOfEsDocs.map { buildEsDocHolder(it) }")
        appendLine("            upsertJob.timeInstanceOfJob { this.esIndexOps.bulkUpsert(esDocHolders) }")
        appendLine("            jm.getOrCreateCounter(\"upsertCount\").inc(esDocHolders.size.toLong())")
        blankLine()
        appendLine("            esDocHolders.map { it.id }")
        blankLine()
        appendLine("        }.flatMap { it }.toSet()")
        blankLine()
        appendLine("    }")

    }


    private fun `render function removeDeletedRecordsForIndex`() {

        blankLine()
        blankLine()
        appendLine("    private fun removeDeletedRecordsFromIndex(currentIds: Set<String>, jm: JobMetrics) {")
        blankLine()
        appendLine("        val chunkSize = this.props.getIntOrNull(\"${this.classDef.uqcn.firstToLower()}.bulkDelete.chunkSize\") ?: 1000")
        appendLine("        this.esIndexOps.removeDeletedRecordsFromIndex(currentIds, this.indexResolvedName, chunkSize, jm)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function buildEsDocHolder`() {

        addImportFor(Fqcns.MAIA_ES_DOC_HOLDER)

        blankLine()
        blankLine()
        appendLine("    private fun buildEsDocHolder(esDoc: ${this.esDocDef.uqcn}): EsDocHolder<${this.esDocDef.uqcn}> {")
        blankLine()
        appendLine("        return EsDocHolder(esDoc.${this.esDocDef.idField.classFieldName}.value, esDoc, this.indexResolvedName)")
        blankLine()
        appendLine("    }")

    }


}
