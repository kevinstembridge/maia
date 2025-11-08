package org.maiaframework.job

import org.maiaframework.metrics.JobMetrics

interface MaiaJob {


    val jobName: JobName


    val description: String?


    fun executeJob(jm: JobMetrics)


}
