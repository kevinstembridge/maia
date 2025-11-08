package org.maiaframework.job.batch.jdbc

import org.maiaframework.job.batch.BatchItemWriter
import org.maiaframework.metrics.JobMetrics

class DefaultBatchItemWriter<T>(private val bulkInsertFunc: (List<T>) -> Unit): BatchItemWriter<T> {


    override fun writeItems(items: List<T>, jm: JobMetrics) {

        jm.timeChildJob("batchWrite") { jm1 ->

            jm1.getOrCreateCounter("itemCount").inc(items.size.toLong())

            this.bulkInsertFunc.invoke(items)

        }

    }


}
