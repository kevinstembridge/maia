package org.maiaframework.job.batch

import org.maiaframework.metrics.JobMetrics


interface BatchItemProcessor<IN, OUT> {


    fun processItem(inItem: IN, jobMetrics: JobMetrics): OUT


}
