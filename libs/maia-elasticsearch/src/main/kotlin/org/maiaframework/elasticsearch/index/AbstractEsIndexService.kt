package org.maiaframework.elasticsearch.index

import org.maiaframework.elasticsearch.EsDocHolder
import org.maiaframework.metrics.JobMetrics
import org.maiaframework.props.Props
import org.maiaframework.domain.DomainId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractEsIndexService<ESDOC>(
    private val esIndexOps: org.maiaframework.elasticsearch.index.EsIndexOps,
    private val props: Props
) {


    protected val logger: Logger = LoggerFactory.getLogger(javaClass)


    protected abstract val bulkDeleteChunkSizePropertyName: String


    protected abstract fun indexName(): org.maiaframework.elasticsearch.index.EsIndexName


    fun refreshIndex(jm: JobMetrics) {

        logger.info("Refresh index ${indexName()}")

        val currentIds = upsertAllCurrentRecords(jm)

        val chunkSize = this.props.getIntOrNull(bulkDeleteChunkSizePropertyName) ?: 1000

        this.esIndexOps.removeDeletedRecordsFromIndex(currentIds, indexName(), chunkSize, jm)

    }


    private fun upsertAllCurrentRecords(jm: JobMetrics): Set<String> {

        val upsertJob = jm.getOrCreateChildJob("upsertChunk")

        val esDocBatches = getEsDocBatches(jm)

        return esDocBatches.map { esDocs ->

            upsertJob.timeInstanceOfJob { bulkUpsert(esDocs) }
            jm.getOrCreateCounter("upsertCount").inc(esDocs.size.toLong())

            esDocs.map { it.id }

        }.flatMap { it }.toSet()

    }


    protected fun bulkUpsert(esDocs: List<org.maiaframework.elasticsearch.EsDocHolder<ESDOC>>) {

        this.esIndexOps.bulkUpsert(esDocs)

    }


    protected abstract fun getEsDocBatches(jm: JobMetrics): Sequence<List<org.maiaframework.elasticsearch.EsDocHolder<ESDOC>>>


    open fun deleteById(id: DomainId) {

        this.esIndexOps.deleteById(id.value, indexName())

    }


}
