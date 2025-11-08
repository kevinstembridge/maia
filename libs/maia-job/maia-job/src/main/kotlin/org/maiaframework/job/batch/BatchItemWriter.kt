package org.maiaframework.job.batch

import org.maiaframework.metrics.JobMetrics

interface BatchItemWriter<T> {

    fun writeItems(items: List<T>, jm: JobMetrics)

}
