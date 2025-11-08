package org.maiaframework.job.batch

import org.maiaframework.metrics.JobMetrics

class NoopBatchItemProcessor<IN, OUT> : BatchItemProcessor<IN, OUT> {

    override fun processItem(inItem: IN, jobMetrics: JobMetrics): OUT {
        return inItem as OUT
    }

}
