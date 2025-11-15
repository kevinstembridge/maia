package org.maiaframework.job.batch

import org.maiaframework.metrics.JobMetrics
import org.slf4j.LoggerFactory


class Batcher<ITEM>(
        private val batchSize: Int,
        private val displayName: String,
        private val jm: JobMetrics,
        private val itemWriter: (List<ITEM>, JobMetrics) -> Unit
) {

    private val logger = LoggerFactory.getLogger(Batcher::class.java)

    private val items = mutableListOf<ITEM>()


    fun pushItem(writeItem: ITEM) {

        this.items.add(writeItem)

        if (this.items.size >= this.batchSize) {
            flush()
        }

    }


    fun flush() {

        this.jm.timeChildJob("flush_$displayName") { metrics ->
            if (this.items.isNotEmpty()) {
                logger.debug("Flushing batch")
                metrics.getOrCreateCounter("itemCount").inc(this.items.size.toLong())
                this.itemWriter.invoke(this.items, metrics)
                this.items.clear()
            }
        }

    }


}
