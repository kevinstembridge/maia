package org.maiaframework.job.batch

import org.maiaframework.metrics.JobMetrics
import org.slf4j.LoggerFactory


class BatchProcessorInstance<READ_ITEM, WRITE_ITEM>(
    private val displayName: String,
    private val itemReader: BatchItemReader<READ_ITEM>,
    private val itemProcessor: BatchItemProcessor<READ_ITEM, WRITE_ITEM> = NoopBatchItemProcessor(),
    private val itemWriter: BatchItemWriter<WRITE_ITEM>,
    private val batchSize: Int,
    private val batchContext: Map<String, Any> = emptyMap(),
    private val jm: JobMetrics
) {

    private val logger = LoggerFactory.getLogger(BatchProcessorInstance::class.java)

    private val itemsChunk = mutableListOf<WRITE_ITEM>()

    private val ratio = jm.getOrCreateCounterRatio("itemsProcessed_$displayName")

    private val ignoredCount = jm.getOrCreateCounter("ignored_$displayName")


    fun process() {

        initBatchContextListeners()

        if (itemReader is BatchItemStream) {
            itemReader.openItemStream()
        }

        var item: READ_ITEM?

        while (this.itemReader.readItem().also { item = it } != null) {
            pushItem(item!!)
        }

        flush()

        if (itemReader is BatchItemStream) {
            itemReader.closeItemStream()
        }

    }


    private fun initBatchContextListeners() {

        initBatchContextListener(itemReader)
        initBatchContextListener(itemProcessor)
        initBatchContextListener(itemWriter)

    }


    private fun initBatchContextListener(listener: Any) {

        if (listener is BatchContextListener) {
            listener.beforeBatch(this.batchContext)
        }

    }


    private fun pushItem(readItem: READ_ITEM) {

        try {

            this.ratio.count {
                val writeItem = this.itemProcessor.processItem(readItem, this.jm)
                this.itemsChunk.add(writeItem)
            }

        } catch (e: IgnorableBatchItemException) {

            // TODO if max batch item exception count has been exceeded, throw an exception to fail the job
            // TODO record the number of warnings in JobMetrics
            // TODO maybe record the full text of warnings in database

            logger.info("Ignorable batch item exception: $e")
            logger.debug("", e)
            this.ignoredCount.inc()

        } finally {

            if (this.itemsChunk.size >= this.batchSize) {
                flush()
            }

        }

    }


    private fun flush() {

        this.jm.timeChildJob("flush_$displayName") { jm ->
            if (this.itemsChunk.isNotEmpty()) {
                logger.debug("Flushing batch")
                jm.getOrCreateCounter("itemCount").inc(this.itemsChunk.size.toLong())
                this.itemWriter.writeItems(this.itemsChunk, jm)
                this.itemsChunk.clear()
            }
        }

    }


}
