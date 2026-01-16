package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class TypeaheadIndexServiceRenderer(
    private val typeaheadDef: TypeaheadDef
) : AbstractKotlinRenderer(
    typeaheadDef.indexServiceClassDef
) {


    init {

        addConstructorArg(aClassField("elasticClient", Fqcns.ELASTIC_CLIENT).privat().build())
        addConstructorArg(aClassField("esIndexOps", Fqcns.MAIA_ES_INDEX_OPS).privat().build())
        addConstructorArg(aClassField("props", Fqcns.MAIA_PROPS).privat().build())
        addConstructorArg(aClassField("esDocRepo", this.typeaheadDef.esDocDef.esDocRepoClassDef.fqcn).privat().build())
        addConstructorArg(aClassField("esIndex", this.typeaheadDef.esIndexClassDef.fqcn).privat().build())
        addConstructorArg(aClassField("jsonMapper", Fqcns.JACKSON_JSON_MAPPER).privat().build())

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.SLF4J_LOGGER_FACTORY)

        blankLine()
        blankLine()
        appendLine("    private val logger = LoggerFactory.getLogger(${this.typeaheadDef.indexServiceClassDef.uqcn}::class.java)")

    }


    override fun renderFunctions() {

        `render function refreshById`()
        `render function deleteById`()
        `render function refreshIndex`()
        `render function upsertAllCurrentRecords`()
        `render function removeDeletedRecordsForIndex`()
        `render function buildEsDocHolder`()

    }


    private fun `render function refreshById`() {

        appendLine("""
            |
            |
            |    fun refreshById(id: DomainId) {
            |
            |        logger.debug("BEGIN: Refreshing typeahead index ${"$"}{this.esIndex.indexName()} for id ${"$"}id")
            |
            |        val esDoc = this.esDocRepo.findByPrimaryKey(id)
            |        val (id, doc, indexName) = buildEsDocHolder(esDoc)
            |
            |        val indexResponse = this.elasticClient.index { i ->
            |            i.index(indexName.asString)
            |                .id(id)
            |                .document(doc)
            |        }
            |
            |        logger.debug("END: Refreshing typeahead index ${"$"}{this.esIndex.indexName()} for id ${"$"}id with result ${"$"}{indexResponse.result()}")
            |
            |    }""".trimMargin())

    }


    private fun `render function deleteById`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        appendLine("""
            |
            |
            |    fun deleteById(id: DomainId) {
            |
            |        logger.debug("BEGIN: Deleting from typeahead index ${"$"}{this.esIndex.indexName()} for id ${"$"}id")
            |
            |        val deleteResponse = this.esIndexOps.deleteById(id.value, this.esIndex.indexName())
            |
            |        logger.debug("END: Deleting from typeahead index ${"$"}{this.esIndex.indexName()} for id ${"$"}id with result ${"$"}{deleteResponse.result()}")
            |
            |    }""".trimMargin())

    }


    private fun `render function refreshIndex`() {

        addImportFor(Fqcns.MAIA_JOB_METRICS)

        appendLine("""
            |
            |
            |    suspend fun refreshIndex(jm: JobMetrics) {
            |
            |        logger.info("BEGIN: Refresh index ${"$"}{this.esIndex.indexName()}")
            |
            |        val currentIds = upsertAllCurrentRecords(jm)
            |        removeDeletedRecordsFromIndex(currentIds, jm)
            |
            |        logger.info("END: Refresh index ${"$"}{this.esIndex.indexName()}")
            |
            |    }
        """.trimMargin())

    }


    private fun `render function upsertAllCurrentRecords`() {

        addImportFor(Fqcns.MAIA_JOB_METRICS)

        blankLine()
        blankLine()
        appendLine("    private suspend fun upsertAllCurrentRecords(jm: JobMetrics): Set<String> {")
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
        appendLine("        this.esIndexOps.removeDeletedRecordsFromIndex(currentIds, this.esIndex.indexName(), chunkSize, jm)")
        blankLine()
        appendLine("    }")

    }


    private fun `render function buildEsDocHolder`() {

        addImportFor(Fqcns.MAIA_ES_DOC_HOLDER)

        blankLine()
        blankLine()
        appendLine("    private fun buildEsDocHolder(esDoc: ${this.typeaheadDef.esDocDef.uqcn}): EsDocHolder<${this.typeaheadDef.esDocDef.uqcn}> {")
        blankLine()
        appendLine("        return EsDocHolder(esDoc.${this.typeaheadDef.idFieldName}.value, esDoc, this.esIndex.indexName())")
        blankLine()
        appendLine("    }")

    }


}
