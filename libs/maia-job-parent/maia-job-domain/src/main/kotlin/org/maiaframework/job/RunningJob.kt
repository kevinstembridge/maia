package org.maiaframework.job

import org.maiaframework.metrics.JobMetrics
import org.maiaframework.domain.DomainId
import java.time.Instant

data class RunningJob(
    val id: DomainId,
    val jobName: JobName,
    val startTimestamp: Instant,
    val invokedBy: String,
    val jobMetrics: JobMetrics
)
